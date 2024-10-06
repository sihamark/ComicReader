package eu.heha.cyclone.model

import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import eu.heha.cyclone.database.Chapter
import eu.heha.cyclone.database.Page
import eu.heha.cyclone.model.RemoteSource.Companion.INITIAL_PAGE
import eu.heha.cyclone.model.RemoteSource.Companion.addComicHeader
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ReaderController(
    private val platformContext: PlatformContext,
    private val comicRepository: ComicRepository
) {

    lateinit var comicAndChapters: ComicAndChapters

    private val writeMutex = Mutex()
    private val pagesAsyncCache = mutableMapOf<PageKey, Deferred<Result<Page>>>()

    private fun getPageFromCache(
        chapter: Chapter,
        pageIndex: Int
    ): Deferred<Result<Page>>? {
        return pagesAsyncCache[PageKey(chapter, pageIndex)]
    }

    private suspend fun putPageInCache(
        chapter: Chapter,
        pageIndex: Int,
        deferred: Deferred<Result<Page>>
    ) {
        writeMutex.withLock {
            pagesAsyncCache[PageKey(chapter, pageIndex)] = deferred
        }
    }

    private suspend fun removeFromCache(chapter: Chapter, pageIndex: Int) {
        writeMutex.withLock {
            pagesAsyncCache.remove(PageKey(chapter, pageIndex))?.cancel()
        }
    }

    fun setComic(comicId: String) {
        TODO()
//        comicAndChapters = comicRepository.getComicAndChapters(comicId)
    }

    suspend fun loadComic(): ChapterContentResult {
        TODO()
//        Napier.d { "got comic '${comic.title}'" }
//        val chapter = comic.chapters.first()
//        return loadChapter(chapter)
    }

    suspend fun loadChapter(chapter: Chapter): ChapterContentResult {
        setProgress(chapter)
        return ChapterContentResult(
            chapter = chapter,
            pagesInChapter = loadFirstPageInChapter(chapter)
        )
    }

    /**
     * Loads a page from the given chapter as a flow of [PageResult]
     */
    fun getPage(chapter: Chapter, pageIndex: Int) = flow {
        try {
            emit(PageResult.Loading)
            val page = loadPage(chapter, pageIndex).await()
            emit(PageResult.Loaded(page.getOrThrow()))
        } catch (e: Exception) {
            emit(PageResult.Error(e))
        }
    }

    suspend fun setProgress(
        chapter: Chapter,
        pageIndex: Int = INITIAL_PAGE
    ) {
        val pagesInChapter = loadFirstPageInChapter(chapter)
        Napier.d { "loaded chapter '${chapter.title}' with ${pagesInChapter.size} pages" }
        loadAroundCurrentProgress(chapter, pagesInChapter, pageIndex)
    }

    private suspend fun loadAroundCurrentProgress(
        chapter: Chapter,
        pagesInChapter: List<Int>,
        pageIndex: Int
    ) {
        Napier.d { "loading around $pageIndex" }
        coroutineScope {
            //load 2 previous pages and 5 next pages
            ((-2)..5).filterNot { it == 0 }.forEach { delta ->
                launch {
                    loadPageWithDelta(chapter, pagesInChapter, pageIndex, delta)
                }
            }
        }
    }

    private suspend fun loadPageWithDelta(
        chapter: Chapter,
        pagesInChapter: List<Int>,
        pageIndex: Int,
        delta: Int
    ) {
        val newPage = pageIndex + delta
        if (newPage < pagesInChapter.min()) {
            //loadPreviousChapter()
            if (chapter.isFirst()) {
                //it is the first chapter, you can't go further back
                return
            }
//            val previousChapter = comic.chapters[comic.chapters.indexOf(chapter) - 1]
//            val pagesInPreviousChapter = loadChapter(chapter)
//            val pageInPreviousChapter = pagesInPreviousChapter.indices.last + newPage
//            loadPageWithDelta(previousChapter, )
        } else if (newPage > pagesInChapter.max()) {
            //loadNextChapter()
            if (chapter.isLast()) {
                //it is the last chapter, you can't go further
                return
            }
        } else {
            Napier.d { "loading page $newPage in current chapter" }
            loadPage(chapter, newPage).await()
        }
    }

    private fun loadImageAsync(imageUrl: String) {
        val imageRequest = ImageRequest.Builder(platformContext)
            .data(imageUrl)
            .addComicHeader(comicAndChapters.first.homeUrl)
            .build()
        SingletonImageLoader.get(platformContext)
            .enqueue(imageRequest)
    }

    private suspend fun loadFirstPageInChapter(chapter: Chapter): List<Int> {
        val pageAsync = getPageFromCache(chapter, INITIAL_PAGE)
            ?: loadPage(chapter, INITIAL_PAGE)
        TODO()
//        return pageAsync.await().getOrThrow().listOfPagesInChapter
    }

    private suspend fun loadPage(
        chapter: Chapter,
        pageIndex: Int
    ): Deferred<Result<Page>> = coroutineScope {
        val pageAsync = getPageFromCache(chapter, pageIndex)
        val actualDeferred = if (pageAsync == null) {
            Napier.d("no cached page found, loading page $pageIndex")
            val deferred = async(start = CoroutineStart.LAZY, context = Dispatchers.IO) {
                try {
                    Napier.d { "loading page $pageIndex" }
                    val page = comicRepository.loadPage(chapter.url, pageIndex)
                        ?: error("error getting page $pageIndex for chapter ${chapter.title}")
                    Napier.d { "got page $pageIndex preheat image ${page.imageUrl}" }
                    loadImageAsync(page.imageUrl)
                    Result.success(page)
                } catch (e: Exception) {
                    Napier.e(e) { "error loading page $pageIndex" }
                    Result.failure(e)
                }
            }

            putPageInCache(chapter, pageIndex, deferred)
            deferred.start()
            deferred
        } else {
            pageAsync
        }

        val pageResult = actualDeferred.await()
        if (pageResult.isFailure) {
            Napier.e { "page loading was unsuccessful, empty cache and try again" }
            removeFromCache(chapter, pageIndex)
            return@coroutineScope loadPage(chapter, pageIndex)
        }
        return@coroutineScope actualDeferred
    }

    private fun Chapter.isFirst(): Boolean = TODO()
    //    comic.chapters.first() == this

    private fun Chapter.isLast(): Boolean = TODO()
    //    comic.chapters.last() == this

    data class PageKey(val chapter: Chapter, val pageIndex: Int)

    data class ChapterContentResult(
        val chapter: Chapter,
        val pagesInChapter: List<Int>
    )

    sealed interface PageResult {
        data object Loading : PageResult
        data class Loaded(val page: Page) : PageResult
        data class Error(val error: Throwable) : PageResult
    }
}
