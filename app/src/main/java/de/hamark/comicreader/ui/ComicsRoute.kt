package de.hamark.comicreader.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import de.hamark.comicreader.ui.NavigationDestination.ADD_COMIC

@Composable
fun ComicsRoute(navController: NavController) {
    val model = hiltViewModel<ComicsViewModel>()
    val comics by model.comics.collectAsStateWithLifecycle()

    ComicsPane(
        comics = comics,
        onClickAddComic = { navController.navigate(ADD_COMIC.route) },
        onClickComic = {}
    )
}