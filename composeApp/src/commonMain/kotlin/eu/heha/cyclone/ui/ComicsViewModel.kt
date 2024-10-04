package eu.heha.cyclone.ui

import androidx.lifecycle.ViewModel
import eu.heha.cyclone.model.ComicRepository

class ComicsViewModel(
    comicRepository: ComicRepository
) : ViewModel() {
    val comics = comicRepository.comics
}
