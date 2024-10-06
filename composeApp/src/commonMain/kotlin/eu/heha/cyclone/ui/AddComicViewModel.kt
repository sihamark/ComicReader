package eu.heha.cyclone.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.heha.cyclone.model.ComicAndChapters
import eu.heha.cyclone.model.ComicRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AddComicViewModel(
    private val comicRepository: ComicRepository
) : ViewModel() {
    var state by mutableStateOf(State())
        private set

    private var progressJob: Job? = null

    fun onComicUrlChange(newComicUrl: String) {
        state = state.copy(comicUrl = newComicUrl)
    }

    fun checkComic() {
        progressJob = viewModelScope.launch {
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

    fun cancelProgress() {
        progressJob?.cancel()
        state = state.copy(progress = null)
    }

    fun addComic() {
        progressJob = viewModelScope.launch {
            val previewComic = state.previewComicResult?.getOrNull()
            checkNotNull(previewComic) { "previewComic must not be null, was ${state.previewComicResult}" }
            state = state.copy(progress = Progress.AddingComic)
            val result = comicRepository.addComic(previewComic)
            Napier.d { "add comic result: $result" }
            state = state.copy(addComicResult = result, progress = null)
        }
    }

    data class State(
        val comicUrl: String = "",
        val previewComicResult: Result<ComicAndChapters>? = null,
        val addComicResult: ComicRepository.AddComicResult? = null,
        val progress: Progress? = null
    )

    enum class Progress(val isCancellable: Boolean) {
        CheckingComic(isCancellable = true),
        AddingComic(isCancellable = false)
    }
}
