package de.hamark.comicreader.model

import android.content.Context
import androidx.collection.ArrayMap
import coil.Coil
import coil.request.ImageRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.inject.Inject
import kotlin.concurrent.read
import kotlin.concurrent.write

class ReaderController @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val comicRepository: ComicRepository
) {

    private lateinit var comic: ComicRepository.Comic

    private val pagesAsyncLock = ReentrantReadWriteLock()
    private val pagesAsyncCache = ArrayMap<PageKey, Deferred<Result<ComicRepository.Page>>>()

    private val chapterLock = ReentrantReadWriteLock()
    private val chapterCache = ArrayMap<ComicRepository.Chapter, List<ComicRepository.Page>>()

    suspend fun loadComic(comicId: String): InitialResult {
        comic = comicRepository.getComic(comicId)
        val chapter = comic.chapters.first()
        setProgress(chapter)
        return InitialResult(
            comic = comic,
            chapter = chapter,
            pagesInChapter = loadChapter(chapter)
        )
    }

    fun getPage(chapter: ComicRepository.Chapter, pageIndex: Int) = flow {
        try {
            emit(PageResult.Loading)
            val page = loadPage(chapter, pageIndex).await()
            emit(PageResult.Loaded(page.getOrThrow()))
        } catch (e: Exception) {
            emit(PageResult.Error(e))
        }
    }

    private suspend fun setProgress(
        chapter: ComicRepository.Chapter,
        pageIndex: Int = ComicRepository.INITIAL_PAGE
    ) {
        val pagesInChapter = loadChapter(chapter)
        loadAroundCurrentProgress(chapter, pagesInChapter, pageIndex)
    }

    private suspend fun loadAroundCurrentProgress(
        chapter: ComicRepository.Chapter,
        pagesInChapter: List<Int>,
        pageIndex: Int
    ) {
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
            loadPage(chapter, newPage).await()
        }

//        pagesAsyncCache[PageKey(chapter, pageIndex)]?.await()

//        TODO("Not yet implemented")
    }

    private suspend fun loadImageAsync(imageUrl: String) {
        coroutineScope {
            launch {
                val (name, value) = ComicRepository.imageHeader(comic.homeUrl)
                val imageRequest = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .addHeader(name, value)
                    .build()
                Coil.imageLoader(context).execute(imageRequest)
            }
        }
    }

    private suspend fun loadChapter(chapter: ComicRepository.Chapter): List<Int> {
        val pageAsync = pagesAsyncLock
            .read { pagesAsyncCache[PageKey(chapter, ComicRepository.INITIAL_PAGE)] }
            ?: loadPage(chapter, ComicRepository.INITIAL_PAGE)
        return pageAsync.await().getOrThrow().listOfPagesInChapter
    }

    private suspend fun loadPage(
        chapter: ComicRepository.Chapter,
        pageIndex: Int
    ): Deferred<Result<ComicRepository.Page>> {
        val pageAsync = pagesAsyncLock.read {
            pagesAsyncCache[PageKey(chapter, pageIndex)]
        }
        val actualDeferred = if (pageAsync == null) {
            val deferred = coroutineScope {
                async {
                    try {
                        val page = comicRepository.loadPage(chapter.url, pageIndex)
                            ?: error("error getting page $pageIndex for chapter ${chapter.title}")
                        loadImageAsync(page.imageUrl)
                        Result.success(page)
                    } catch (e: Exception) {
                        Result.failure(e)
                    }
                }
            }
            pagesAsyncLock.write {
                pagesAsyncCache[PageKey(chapter, pageIndex)] = deferred
            }
            deferred
        } else {
            pageAsync
        }

        val pageResult = actualDeferred.await()
        if (pageResult.isFailure) {
            pagesAsyncLock.write {
                pagesAsyncCache.remove(PageKey(chapter, pageIndex))?.cancel()
            }
            return loadPage(chapter, pageIndex)
        }
        return actualDeferred
    }

    fun loadedPages(chapter: ComicRepository.Chapter) {

    }

    private fun ComicRepository.Chapter.isFirst() = comic.chapters.first() == this
    private fun ComicRepository.Chapter.isLast() = comic.chapters.last() == this

    data class PageKey(val chapter: ComicRepository.Chapter, val pageIndex: Int)

    data class InitialResult(
        val comic: ComicRepository.Comic,
        val chapter: ComicRepository.Chapter,
        val pagesInChapter: List<Int>
    )

    sealed interface PageResult {
        data object Loading : PageResult
        data class Loaded(val page: ComicRepository.Page) : PageResult
        data class Error(val error: Throwable) : PageResult
    }
}
