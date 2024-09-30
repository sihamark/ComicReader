package eu.heha.cyclone.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.heha.cyclone.model.ComicRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddComicViewModel @Inject constructor(
    private val comicRepository: ComicRepository
) : ViewModel() {
    var state by mutableStateOf(State())
        private set

    fun onComicUrlChange(newComicUrl: String) {
        state = state.copy(comicUrl = newComicUrl)
    }

    fun checkComic() {
        viewModelScope.launch {
            state = state.copy(isCheckingComic = true)
            state = state.copy(
                previewComicResult = try {
                    Result.success(comicRepository.loadComic(state.comicUrl).also {
                        Napier.d("loaded comic: $it")
                    })
                } catch (e: Exception) {
                    Result.failure(e)
                }
            )
            state = state.copy(isCheckingComic = false)
        }
    }

    fun addComic() {
        val previewComic = state.previewComicResult?.getOrNull()
        checkNotNull(previewComic) { "previewComic must not be null, was ${state.previewComicResult}" }
        comicRepository.addComic(previewComic)
    }

    data class State(
        val comicUrl: String = "",
        val previewComicResult: Result<ComicRepository.Comic>? = null,
        val isCheckingComic: Boolean = false
    )
}
