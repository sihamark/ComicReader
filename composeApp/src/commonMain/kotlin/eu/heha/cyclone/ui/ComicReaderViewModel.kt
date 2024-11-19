package eu.heha.cyclone.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.heha.cyclone.database.Chapter
import eu.heha.cyclone.model.ComicAndChapters
import eu.heha.cyclone.model.ReaderController
import eu.heha.cyclone.model.RemoteSource
import eu.heha.cyclone.model.chapters
import eu.heha.cyclone.model.comic
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ComicReaderViewModel(
    private val readerController: ReaderController
) : ViewModel() {

    var comicAndChapters by mutableStateOf<ComicAndChapters?>(null)
        private set

    var state: State by mutableStateOf(State.Loading)
        private set

    var pageState = mutableStateMapOf<Long, ReaderController.PageResult>()
        private set

    private fun requireComic() = comicAndChapters ?: error("comic not loaded")
    private fun requireChapter() = (state as? State.Loaded)?.chapter ?: error("chapter not loaded")

    private val pageJobCache = mutableMapOf<Long, Job>()

    private var chapterChangeJob: Job? = null

    fun loadComic(comicId: Long) {
        viewModelScope.launch {
            val comicAndChapters = readerController.setComic(comicId)
            this@ComicReaderViewModel.comicAndChapters = comicAndChapters


            val latestPosition = comicAndChapters.comic.latestPosition
            val latestChapter = latestPosition?.let { position ->
                //get the chapter of the latest position
                comicAndChapters.chapters.find { it.id == position.chapterId }
            } ?: comicAndChapters.chapters.first()
            setState(latestChapter)

            //get the page of the latest position
            val result = readerController.loadComic(latestChapter)
            val pageNumber = if (result == latestChapter) {
                val pages = result.pagesInChapter
                (latestPosition?.pageNumber ?: pages.first())
                    .takeIf { it in pages }
            } else null
            setState(result, pageNumber)
        }
    }

    fun loadPageState(pageNumber: Long) {
        Napier.d { "page $pageNumber came into view, loading state" }
        pageJobCache[pageNumber]?.cancel()
        pageJobCache[pageNumber] = viewModelScope.launch {
            val chapter = requireChapter()
            readerController.getPage(chapter, pageNumber).collect { pageResult ->
                pageState[pageNumber] = pageResult
            }
        }
    }

    fun setProgress(pageNumber: Long) {
        Napier.d { "page $pageNumber is centered, loading around it" }
        viewModelScope.launch {
            val chapter = requireChapter()
            readerController.saveProgress(chapter, pageNumber)
        }
    }

    fun loadPreviousChapter() = loadNewChapter(-1)
    fun loadNextChapter() = loadNewChapter(1)

    private fun loadNewChapter(indexDelta: Int) {
        chapterChangeJob?.cancel()
        chapterChangeJob = viewModelScope.launch {
            val chapters = requireComic().chapters
            val chapter = requireChapter()
            val newIndex = chapters.indexOfFirst { it.id == chapter.id } + indexDelta
            Napier.d { "loading chapter $newIndex" }
            if (newIndex in chapters.indices) {
                pageJobCache.forEach { (_, job) -> job.cancel() }
                pageJobCache.clear()
                pageState.clear()
                val newChapter = chapters[newIndex]
                setState(newChapter)
                val result = readerController.loadChapter(newChapter)
                setState(result)
            }
        }
    }

    private fun setState(chapter: Chapter, pageNumber: Long? = null) {
        val pagesInChapter = chapter.pagesInChapter
        pagesInChapter.forEach {
            pageState += it to ReaderController.PageResult.Loading
        }
        state = State.Loaded(
            chapter = chapter,
            pages = pagesInChapter.takeIf { chapter.numberOfPages != 0L },
            jumpToPage = pageNumber
        )
    }

    fun setChapter(chapter: Chapter) {
        if (chapter !in requireComic().chapters) {
            Napier.e { "chapter $chapter not in comic" }
            return
        }
        viewModelScope.launch {
            pageJobCache.forEach { (_, job) -> job.cancel() }
            pageJobCache.clear()
            pageState.clear()
            setState(chapter)
            val result = readerController.loadChapter(chapter)
            setState(result)
        }
    }

    private val Chapter.pagesInChapter
        get() = (RemoteSource.INITIAL_PAGE..(numberOfPages.coerceAtLeast(RemoteSource.INITIAL_PAGE)))

    sealed interface State {
        data object Loading : State
        data class Loaded(
            val chapter: Chapter,
            val pages: LongRange?,
            val jumpToPage: Long? = null
        ) : State

        data class Error(val message: String) : State
    }
}