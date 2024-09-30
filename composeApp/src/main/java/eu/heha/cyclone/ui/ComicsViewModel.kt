package eu.heha.cyclone.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.heha.cyclone.model.ComicRepository
import javax.inject.Inject

@HiltViewModel
class ComicsViewModel @Inject constructor(
    private val comicRepository: ComicRepository
) : ViewModel() {
    val comics = comicRepository.comics
}
