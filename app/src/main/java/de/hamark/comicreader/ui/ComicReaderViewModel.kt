package de.hamark.comicreader.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
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
    private val readerController: ReaderController,
    private val repository: ComicRepository
) : ViewModel() {

    var state: State by mutableStateOf(State.Loading)
        private set

    fun reloadCurrentPage() {
        when (val state = state) {
            is State.Loaded -> loadComic(state.chapter, state.pageIndex)
            else -> loadComic()
        }
    }

    fun loadComic(comicId: String) {
        viewModelScope.launch {
            readerController.loadComic(comicId)
        }
    }

    fun loadComic(
        chapter: ComicRepository.Chapter? = null,
        index: Int = ComicRepository.INITIAL_PAGE
    ) {
        viewModelScope.launch {
            try {
                state = State.Loading
                state = try {
                    val comic = repository.loadComic()
                    Napier.e { "got comic: $comic" }

                    val actualChapter = chapter ?: comic.chapters.first()
                    val pageUrl = repository.getPageUrl(actualChapter.url, index)
                    val imageUrl = repository.loadPage(actualChapter.url, index)
                        ?: error("page not found")

                    val image = repository.loadImage(comic.homeUrl, imageUrl.imageUrl)

                    State.Loaded(actualChapter, index, pageUrl, "https:" + imageUrl.imageUrl, image)
                } catch (e: Exception) {
                    Napier.e("error loading initial page", e)
                    State.Error(e)
                }
            } catch (e: Exception) {
                Napier.e("error loading page", e)
                state = State.Error(e)
            }
        }
    }

    sealed interface State {
        data object Loading : State
        data class Loaded(
            val chapter: ComicRepository.Chapter,
            val pageIndex: Int = 0,
            val pageUrl: String,
            val imageUrl: String,
            val image: ImageBitmap
        ) : State

        data class Error(val error: Throwable) : State
    }
}