package eu.heha.cyclone.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import eu.heha.cyclone.ui.SimpleNavigationDestination.ADD_COMIC
import org.koin.androidx.compose.koinViewModel

@Composable
fun ComicsRoute(navController: NavController) {
    val model = koinViewModel<ComicsViewModel>()
    val comics by model.comics.collectAsStateWithLifecycle()

    ComicsPane(
        comics = comics,
        onClickAddComic = { navController.navigate(ADD_COMIC.route) },
        onClickComic = { navController.navigate(ComicDestination.withId(it.id)) }
    )
}