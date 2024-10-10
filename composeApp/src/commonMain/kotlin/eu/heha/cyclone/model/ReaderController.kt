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
    private val comic get() = comicAndChapters.comic
    private val chapters get() = comicAndChapters.chapters

    private val writeMutex = Mutex()
    private val pagesAsyncCache = mutableMapOf<PageKey, Deferred<Result<Page>>>()

    private fun getPageFromCache(
        chapter: Chapter,
        pageIndex: Long
    ): Deferred<Result<Page>>? {
        return pagesAsyncCache[PageKey(chapter, pageIndex)]
    }

    private suspend fun putPageInCache(
        chapter: Chapter,
        pageIndex: Long,
        deferred: Deferred<Result<Page>>
    ) {
        writeMutex.withLock {
            pagesAsyncCache[PageKey(chapter, pageIndex)] = deferred
        }
    }

    private suspend fun removeFromCache(chapter: Chapter, pageIndex: Long) {
        writeMutex.withLock {
            pagesAsyncCache.remove(PageKey(chapter, pageIndex))?.cancel()
        }
    }

    suspend fun setComic(comicId: Long): ComicAndChapters {
        comicAndChapters = comicRepository.getComicAndChapters(comicId)
        return comicAndChapters
    }

    suspend fun loadComic(chapter: Chapter): Chapter {
        Napier.d { "got comic '${comic.title}'" }
        return loadChapter(chapter)
    }

    suspend fun loadChapter(chapter: Chapter): Chapter {
        loadFirstPageInChapter(chapter)
        val chapterFromDatabase = comicRepository.getChapter(chapter.id)
        val numberOfPages = chapterFromDatabase.numberOfPages
        Napier.d { "loaded chapter '${chapterFromDatabase.title}' with $numberOfPages pages" }
        //loadAroundCurrentProgress(chapterFromDatabase, numberOfPages, INITIAL_PAGE)
        return chapterFromDatabase
    }

    /**
     * Loads a page from the given chapter as a flow of [PageResult]
     */
    fun getPage(chapter: Chapter, pageIndex: Long) = flow {
        try {
            emit(PageResult.Loading)
            val page = loadPage(chapter, pageIndex).await()
            emit(PageResult.Loaded(page.getOrThrow()))
        } catch (e: Exception) {
            emit(PageResult.Error(e))
        }
    }

    suspend fun saveProgress(chapter: Chapter, pageIndex: Long) {
        Napier.e { "saving progress in chapter ${chapter.title} at page $pageIndex" }
        comicRepository.saveProgress(comic, chapter, pageIndex)
    }

    private suspend fun loadAroundCurrentProgress(
        chapter: Chapter,
        numberOfPages: Long,
        pageIndex: Long
    ) {
        Napier.d { "loading around $pageIndex" }
        coroutineScope {
            //load 2 previous pages and 5 next pages
            ((-2L)..5L).filterNot { it == 0L }.forEach { delta ->
                launch {
                    loadPageWithDelta(chapter, numberOfPages, pageIndex, delta)
                }
            }
        }
    }

    private suspend fun loadPageWithDelta(
        chapter: Chapter,
        numberOfPages: Long,
        pageIndex: Long,
        delta: Long
    ) {
        val newPage = pageIndex + delta
        if (newPage < INITIAL_PAGE || newPage > numberOfPages) {
            return
        }

        Napier.d { "loading page $newPage in current chapter" }
        loadPage(chapter, newPage).await()
    }

    private fun loadImageAsync(imageUrl: String) {
        val imageRequest = ImageRequest.Builder(platformContext)
            .data(imageUrl)
            .addComicHeader(comicAndChapters.comic.homeUrl)
            .build()
        SingletonImageLoader.get(platformContext)
            .enqueue(imageRequest)
    }

    private suspend fun loadFirstPageInChapter(chapter: Chapter) {
        loadPage(chapter, INITIAL_PAGE).join()
    }

    private suspend fun loadPage(
        chapter: Chapter,
        pageIndex: Long
    ): Deferred<Result<Page>> = coroutineScope {
        val pageAsync = getPageFromCache(chapter, pageIndex)
        val actualDeferred = if (pageAsync == null) {
            Napier.d("no cached page found, loading page $pageIndex")
            val deferred = async(start = CoroutineStart.LAZY, context = Dispatchers.IO) {
                try {
                    Napier.d { "loading page $pageIndex" }
                    val page = comicRepository.loadPage(chapter, pageIndex)
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

    data class PageKey(val chapter: Chapter, val pageIndex: Long)

    sealed interface PageResult {
        data object Loading : PageResult
        data class Loaded(val page: Page) : PageResult
        data class Error(val error: Throwable) : PageResult
    }
}
