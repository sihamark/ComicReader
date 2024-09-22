package de.hamark.comicreader.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hamark.comicreader.model.ComicRepository
import de.hamark.comicreader.model.PageParseController.Page
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
            is State.Loaded -> loadPage(state.page)
            else -> loadPage()
        }
    }

    fun loadPage(page: Page? = null) {
        viewModelScope.launch {
            try {
                state = State.Loading
                state = try {
                    repository.loadComic()
//                    val nextPage = if (page == null) {
//                        repository.loadPage()
//                    } else {
//                        repository.loadPage(page.nextPageUrl)
//                    }
//                    State.Loaded(nextPage)
                    TODO("not yet implemented")
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
        data class Loaded(val page: Page) : State
        data class Error(val error: Throwable) : State
    }
}