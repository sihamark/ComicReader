package de.hamark.comicreader.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hamark.comicreader.model.ComicRepository
import de.hamark.comicreader.model.ReaderController
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ComicReaderViewModel @Inject constructor(
    private val readerController: ReaderController
) : ViewModel() {

    var comic by mutableStateOf<ComicRepository.Comic?>(null)
        private set

    var state: State by mutableStateOf(State.Loading)
        private set

    var pageState = mutableStateMapOf<Int, ReaderController.PageResult>()
        private set

    private fun requireComic() = comic ?: error("comic not loaded")
    private fun requireChapter() = (state as? State.Loaded)?.chapter ?: error("chapter not loaded")

    private val pageJobCache = mutableMapOf<Int, Job>()

    fun loadComic(comicId: String) {
        readerController.setComic(comicId)
        comic = readerController.comic
        viewModelScope.launch {
            val result = readerController.loadComic()
            setState(result)
        }
    }

    fun loadPageState(pageIndex: Int) {
        pageJobCache[pageIndex]?.cancel()
        pageJobCache[pageIndex] = viewModelScope.launch {
            val chapter = requireChapter()
            readerController.getPage(chapter, pageIndex).collect { pageResult ->
                pageState[pageIndex] = pageResult
            }
        }
    }

    fun setProgress(pageIndex: Int) {
        viewModelScope.launch {
            val chapter = requireChapter()
            readerController.setProgress(chapter, pageIndex)
        }
    }

    fun loadPreviousChapter() = loadNewChapter(-1)
    fun loadNextChapter() = loadNewChapter(1)

    private fun loadNewChapter(indexDelta: Int) {
        viewModelScope.launch {
            val comic = requireComic()
            val chapter = requireChapter()
            val newIndex = comic.chapters.indexOf(chapter) + indexDelta
            if (newIndex in comic.chapters.indices) {
                val newChapter = comic.chapters[newIndex]
                state = State.Loading
                val result = readerController.loadChapter(newChapter)
                setState(result)
            }
        }
    }

    private fun setState(result: ReaderController.ChapterContentResult) {
        result.pagesInChapter.forEach {
            pageState += it to ReaderController.PageResult.Loading
        }
        state = State.Loaded(
            chapter = result.chapter,
            pages = result.pagesInChapter
        )
    }

    sealed interface State {
        data object Loading : State
        data class Loaded(
            val chapter: ComicRepository.Chapter,
            val pages: List<Int>,
        ) : State

        data class Error(val message: String) : State
    }
}