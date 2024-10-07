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

    fun loadComic(comicId: Long) {
        viewModelScope.launch {
            readerController.setComic(comicId)
            comicAndChapters = readerController.comicAndChapters
            val result = readerController.loadComic()
            setState(result)
        }
    }

    fun loadPageState(pageNumber: Long) {
        pageJobCache[pageNumber]?.cancel()
        pageJobCache[pageNumber] = viewModelScope.launch {
            val chapter = requireChapter()
            readerController.getPage(chapter, pageNumber).collect { pageResult ->
                pageState[pageNumber] = pageResult
            }
        }
    }

    fun setProgress(pageIndex: Long) {
        viewModelScope.launch {
            val chapter = requireChapter()
            readerController.setProgress(chapter, pageIndex)
        }
    }

    fun loadPreviousChapter() = loadNewChapter(-1)
    fun loadNextChapter() = loadNewChapter(1)

    private fun loadNewChapter(indexDelta: Int) {
        viewModelScope.launch {
            val chapters = requireComic().chapters
            val chapter = requireChapter()
            val newIndex = chapters.indexOfFirst { it.id == chapter.id } + indexDelta
            Napier.d { "loading chapter $newIndex" }
            if (newIndex in chapters.indices) {
                pageJobCache.forEach { (_, job) -> job.cancel() }
                pageJobCache.clear()
                pageState.clear()
                val newChapter = chapters[newIndex]
                state = State.Loading
                val result = readerController.loadChapter(newChapter)
                setState(result)
            }
        }
    }

    private fun setState(chapter: Chapter) {
        val pagesInChapter = chapter.pagesInChapter
        pagesInChapter.forEach {
            pageState += it to ReaderController.PageResult.Loading
        }
        state = State.Loaded(
            chapter = chapter,
            pages = pagesInChapter
        )
    }

    private val Chapter.pagesInChapter
        get() = (RemoteSource.INITIAL_PAGE..(numberOfPages.coerceAtLeast(RemoteSource.INITIAL_PAGE)))

    sealed interface State {
        data object Loading : State
        data class Loaded(
            val chapter: Chapter,
            val pages: LongRange,
        ) : State

        data class Error(val message: String) : State
    }
}