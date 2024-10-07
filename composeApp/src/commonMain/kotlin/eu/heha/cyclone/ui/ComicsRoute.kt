package eu.heha.cyclone.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.navOptions
import eu.heha.cyclone.ui.SimpleNavigationDestination.ADD_COMIC
import io.github.aakira.napier.Napier
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ComicsRoute(navController: NavController) {
    val model = koinViewModel<ComicsViewModel>()
    val comics by model.comics.collectAsStateWithLifecycle(initialValue = emptyList())

    ComicsPane(
        comics = comics,
        onClickAddComic = {
            navController.navigate(
                ADD_COMIC.route,
                navOptions = navOptions { launchSingleTop = true })
        },
        onClickComic = { comic ->
            Napier.d { "clicked on comic $comic" }
            navController.navigate(ComicDestination.withId(comic.id))
        }
    )
}