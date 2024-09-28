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
class AddComicViewModel @Inject constructor(
    private val comicRepository: ComicRepository
) : ViewModel() {
    var comicUrl by mutableStateOf("")
        private set
    var previewComic by mutableStateOf<ComicRepository.Comic?>(null)
        private set
    var isCheckingComic by mutableStateOf(false)
        private set

    fun onComicUrlChange(newComicUrl: String) {
        comicUrl = newComicUrl
    }

    fun checkComic() {
        viewModelScope.launch {
            isCheckingComic = true
            previewComic = try {
                comicRepository.loadComic(comicUrl).also {
                    Napier.d("Loaded comic: $it")
                }
            } catch (e: Exception) {
                null
            }
            isCheckingComic = false
        }
    }

    suspend fun addComic() {
        comicRepository.addComic(previewComic!!)
    }
}
