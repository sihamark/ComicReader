package eu.heha.cyclone.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.heha.cyclone.model.ComicAndChapters
import eu.heha.cyclone.model.ComicRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch

class AddComicViewModel(
    private val comicRepository: ComicRepository
) : ViewModel() {
    var state by mutableStateOf(State())
        private set

    fun onComicUrlChange(newComicUrl: String) {
        state = state.copy(comicUrl = newComicUrl)
    }

    fun checkComic() {
        viewModelScope.launch {
            state = state.copy(progress = Progress.CheckingComic)
            state = state.copy(
                previewComicResult = try {
                    val (comic, chapters) = comicRepository.loadComic(state.comicUrl)
                    Napier.d { "loaded comic '${comic.title}' with ${chapters.size} chapters" }
                    Result.success(comic to chapters)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            )
            state = state.copy(progress = null)
        }
    }

    suspend fun addComic(): Long? {
        val previewComic = state.previewComicResult?.getOrNull()
        checkNotNull(previewComic) { "previewComic must not be null, was ${state.previewComicResult}" }
        state = state.copy(progress = Progress.AddingComic)
        when (val result = comicRepository.addComic(previewComic)) {
            is ComicRepository.AddComicResult.ComicAlreadyExists -> {
                Napier.d { "comic '${result.comic.first.title}' already exists" }
                state = state.copy(addComicFailure = Failure.ComicAlreadyExists)
            }

            is ComicRepository.AddComicResult.Failure -> {
                Napier.e(result.exception) { "failed to add comic '${result.comic.first.title}'" }
                state = state.copy(addComicFailure = Failure.Generic(result.exception))
            }

            is ComicRepository.AddComicResult.Success -> return result.comicId
        }
        state = state.copy(progress = null)
        return null
    }

    data class State(
        val comicUrl: String = "",
        val previewComicResult: Result<ComicAndChapters>? = null,
        val addComicFailure: Failure? = null,
        val progress: Progress? = null
    )

    enum class Progress {
        CheckingComic, AddingComic
    }

    sealed interface Failure {
        data object ComicAlreadyExists : Failure
        data class Generic(val exception: Exception) : Failure
    }
}
