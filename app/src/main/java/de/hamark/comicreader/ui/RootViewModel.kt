package de.hamark.comicreader.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.hamark.comicreader.model.ComicRepository
import de.hamark.comicreader.model.PageParseController.Page
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch

class RootViewModel : ViewModel() {
    private val repository: ComicRepository = ComicRepository()

    var state: State by mutableStateOf(State.Loading)
        private set

    fun loadInitialPage() {
        viewModelScope.launch {
            state = try {
                val page = repository.loadPage()
                State.Loaded(page)
            } catch (e: Exception) {
                Napier.e("error loading initial page", e)
                State.Error(e)

            }
        }
    }

    fun reloadCurrentPage() {
        TODO("Not yet implemented")
    }

    fun loadNextPage(page: Page) {
        viewModelScope.launch {
            state = State.Loading
            state = try {
                val nextPage = repository.loadPage(page.nextPageUrl)
                State.Loaded(nextPage)
            } catch (e: Exception) {
                Napier.e("error loading initial page", e)
                State.Error(e)
            }
        }
    }

    sealed interface State {
        object Loading : State
        data class Loaded(val page: Page) : State
        data class Error(val error: Throwable) : State
    }
}