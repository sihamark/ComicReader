package eu.heha.cyclone.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.heha.cyclone.database.Comic
import eu.heha.cyclone.model.ComicRepository
import kotlinx.coroutines.launch

class ComicsViewModel(
    private val comicRepository: ComicRepository
) : ViewModel() {
    val comics = comicRepository.comics

    fun wipeData() {
        viewModelScope.launch {
            comicRepository.wipeData()
        }
    }

    fun deleteComic(comic: Comic) {
        viewModelScope.launch {
            comicRepository.deleteComic(comic)
        }
    }
}
