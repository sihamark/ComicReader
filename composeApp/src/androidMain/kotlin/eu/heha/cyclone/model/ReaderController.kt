package eu.heha.cyclone.model

import androidx.collection.ArrayMap
import coil3.PlatformContext
import coil3.imageLoader
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import eu.heha.cyclone.model.ComicRepository.Companion.toHeader
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class ReaderController(
    private val platformContext: PlatformContext,
    private val comicRepository: ComicRepository
) {

    lateinit var comic: ComicRepository.Comic

    private val pagesAsyncLock = ReentrantReadWriteLock()
    private val pagesAsyncCache = ArrayMap<PageKey, Deferred<Result<ComicRepository.Page>>>()

    fun setComic(comicId: String) {
        comic = comicRepository.getComic(comicId)
    }

    suspend fun loadComic(): ChapterContentResult {
        Napier.d { "got comic '${comic.title}'" }
        val chapter = comic.chapters.first()
        return loadChapter(chapter)
    }

    suspend fun loadChapter(chapter: ComicRepository.Chapter): ChapterContentResult {
        setProgress(chapter)
        return ChapterContentResult(
            chapter = chapter,
            pagesInChapter = loadFirstPageInChapter(chapter)
        )
    }

    /**
     * Loads a page from the given chapter as a flow of [PageResult]
     */
    fun getPage(chapter: ComicRepository.Chapter, pageIndex: Int) = flow {
        try {
            emit(PageResult.Loading)
            val page = loadPage(chapter, pageIndex).await()
            emit(PageResult.Loaded(page.getOrThrow()))
        } catch (e: Exception) {
            emit(PageResult.Error(e))
        }
    }

    suspend fun setProgress(
        chapter: ComicRepository.Chapter,
        pageIndex: Int = ComicRepository.INITIAL_PAGE
    ) {
        val pagesInChapter = loadFirstPageInChapter(chapter)
        Napier.d { "loaded chapter '${chapter.title}' with ${pagesInChapter.size} pages" }
        loadAroundCurrentProgress(chapter, pagesInChapter, pageIndex)
    }

    private suspend fun loadAroundCurrentProgress(
        chapter: ComicRepository.Chapter,
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
        chapter: ComicRepository.Chapter,
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
            .httpHeaders(ComicRepository.imageHeader(comic.homeUrl).toHeader())
            .build()
        platformContext.imageLoader.enqueue(imageRequest)
    }

    private suspend fun loadFirstPageInChapter(chapter: ComicRepository.Chapter): List<Int> {
        val pageAsync = pagesAsyncLock
            .read { pagesAsyncCache[PageKey(chapter, ComicRepository.INITIAL_PAGE)] }
            ?: loadPage(chapter, ComicRepository.INITIAL_PAGE)
        return pageAsync.await().getOrThrow().listOfPagesInChapter
    }

    private suspend fun loadPage(
        chapter: ComicRepository.Chapter,
        pageIndex: Int
    ): Deferred<Result<ComicRepository.Page>> = coroutineScope {
        val pageAsync = pagesAsyncLock.read {
            pagesAsyncCache[PageKey(chapter, pageIndex)]
        }
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
            pagesAsyncLock.write {
                pagesAsyncCache[PageKey(chapter, pageIndex)] = deferred
            }
            deferred.start()
            deferred
        } else {
            pageAsync
        }

        val pageResult = actualDeferred.await()
        if (pageResult.isFailure) {
            Napier.e { "page loading was unsuccessful, empty cache and try again" }
            pagesAsyncLock.write {
                pagesAsyncCache.remove(PageKey(chapter, pageIndex))?.cancel()
            }
            return@coroutineScope loadPage(chapter, pageIndex)
        }
        return@coroutineScope actualDeferred
    }

    private fun ComicRepository.Chapter.isFirst() = comic.chapters.first() == this
    private fun ComicRepository.Chapter.isLast() = comic.chapters.last() == this

    data class PageKey(val chapter: ComicRepository.Chapter, val pageIndex: Int)

    data class ChapterContentResult(
        val chapter: ComicRepository.Chapter,
        val pagesInChapter: List<Int>
    )

    sealed interface PageResult {
        data object Loading : PageResult
        data class Loaded(val page: ComicRepository.Page) : PageResult
        data class Error(val error: Throwable) : PageResult
    }
}
