package de.hamark.comicreader.model

import android.content.Context
import androidx.collection.ArrayMap
import coil.Coil
import coil.request.ImageRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.coroutineScope
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

    private val chapterLock = ReentrantReadWriteLock()
    private val chapterCache = ArrayMap<ComicRepository.Chapter, List<ComicRepository.Page>>()

    suspend fun loadComic(comicId: String) {
        comic = comicRepository.getComic(comicId)
        setProgress(comic.chapters.first())
    }

    private suspend fun setProgress(
        chapter: ComicRepository.Chapter,
        pageIndex: Int = ComicRepository.INITIAL_PAGE
    ) {
        loadChapter(chapter, pageIndex)
        loadAroundCurrentProgress(chapter, pageIndex)
    }

    private fun loadAroundCurrentProgress(chapter: ComicRepository.Chapter, pageIndex: Int) {
        //load 2 previous pages and 5 next pages
        ((-2)..5).filterNot { it == 0 }.forEach { delta ->
            loadPage(chapter, pageIndex, delta)
        }
    }

    private fun loadPage(chapter: ComicRepository.Chapter, pageIndex: Int, delta: Int) {
        val pages = chapterLock.read { chapterCache[chapter] }?.takeUnless { it.isEmpty() }
            ?: error("chapter ${chapter.title} was not prepared correctly")

        val pagesInChapter = pages.first().listOfPagesInChapter

        if (chapter.isNotFirst() && pageIndex < pagesInChapter.min()) {
            //loadPreviousChapter()
        } else if (pageIndex > pagesInChapter.max()) {
            //loadNextChapter()
        } else {

        }

        TODO("Not yet implemented")
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

    private suspend fun loadChapter(chapter: ComicRepository.Chapter, pageIndex: Int) {
        val cachedPages = chapterLock.read { chapterCache[chapter] }
        if (cachedPages.isNullOrEmpty()) {
            val page = comicRepository.loadPage(chapter.url, pageIndex)
                ?: error("error getting initial page")
            chapterLock.write {
                chapterCache[chapter] = (chapterCache[chapter] ?: listOf()) + page
            }
        }
    }

    private fun ComicRepository.Chapter.isNotFirst() = comic.chapters.first() != this
}
