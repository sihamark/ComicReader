package eu.heha.cyclone.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.navOptions
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ComicsRoute(navController: NavController) {
    val model = koinViewModel<ComicsViewModel>()
    val comics by model.comics.collectAsStateWithLifecycle(initialValue = emptyList())

    ComicsPane(
        comics = comics,
        onClickAddComic = {
            navController.navigate(
                ComicAdd,
                navOptions = navOptions { launchSingleTop = true })
        },
        onClickComic = { comic ->
            navController.navigate(Comic(comic.id))
        },
        onClickWipeData = model::wipeData
    )
}