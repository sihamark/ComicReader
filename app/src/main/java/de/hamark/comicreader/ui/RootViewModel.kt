package de.hamark.comicreader.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hamark.comicreader.model.ComicRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RootViewModel @Inject constructor(
    private val repository: ComicRepository
) : ViewModel() {

    var state: State by mutableStateOf(State.Loading)
        private set

    fun reloadCurrentPage() {
        when (val state = state) {
            is State.Loaded -> loadPage(state.chapter, state.pageIndex)
            else -> loadPage()
        }
    }

    fun loadPage(chapter: ComicRepository.Chapter? = null, index: Int = 0) {
        viewModelScope.launch {
            try {
                state = State.Loading
                state = try {
                    val comic = repository.loadComic()

                    val actualChapter = chapter ?: comic.chapters.first()
//                    val page = repository.loadPage(actualChapter.url, index)
//                        ?: error("page not found")
//
//                    val bitmap = BitmapFactory
//                        .decodeByteArray(page.imageBytes, 0, page.imageBytes.size)
//                        .asImageBitmap()
                    val pageUrl = repository.getPageUrl(actualChapter.url, index)
                    val imageUrl = repository.loadPage(actualChapter.url, index)
                        ?: error("page not found")
                    State.Loaded(actualChapter, index, pageUrl, "https:" + imageUrl.imageUrl)
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
            val imageUrl: String
        ) : State

        data class Error(val error: Throwable) : State
    }
}