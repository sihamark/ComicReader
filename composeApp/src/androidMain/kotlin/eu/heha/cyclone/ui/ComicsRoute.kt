package eu.heha.cyclone.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import eu.heha.cyclone.ui.SimpleNavigationDestination.ADD_COMIC

@Composable
fun ComicsRoute(navController: NavController) {
    val model = hiltViewModel<ComicsViewModel>()
    val comics by model.comics.collectAsStateWithLifecycle()

    ComicsPane(
        comics = comics,
        onClickAddComic = { navController.navigate(ADD_COMIC.route) },
        onClickComic = { navController.navigate(ComicDestination.withId(it.id)) }
    )
}