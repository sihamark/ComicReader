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
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ComicReaderViewModel @Inject constructor(
    private val readerController: ReaderController
) : ViewModel() {

    var state: State by mutableStateOf(State.Loading)
        private set

    var pageState = mutableStateMapOf<Int, ReaderController.PageResult>()
        private set

    fun loadComic(comicId: String) {
        viewModelScope.launch {
            val result = readerController.loadComic(comicId)
            state = State.Loaded(
                comic = result.comic,
                chapter = result.chapter,
                pages = result.pagesInChapter
            )
        }
    }

    fun getPageState(pageIndex: Int) {
        viewModelScope.launch {
            val chapter = (state as? State.Loaded)?.chapter ?: error("chapter not loaded")
            Napier.e { "get page state for page: $pageIndex" }
            readerController.getPage(chapter, pageIndex).collect { pageResult ->
                pageState[pageIndex] = pageResult
            }
        }
    }

    sealed interface State {
        data object Loading : State
        data class Loaded(
            val comic: ComicRepository.Comic,
            val chapter: ComicRepository.Chapter,
            val pages: List<Int>,
        ) : State

        data class Error(val message: String) : State
    }
}