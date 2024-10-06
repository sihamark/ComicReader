package eu.heha.cyclone.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import eu.heha.cyclone.model.ComicRepository.AddComicResult.Success
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AddComicRoute(navController: NavController) {
    val model = koinViewModel<AddComicViewModel>()
    val state = model.state
    LaunchedEffect(state) {
        if (state.addComicResult is Success) {
            navController.popBackStack()
        }
    }
    AddComicPane(
        state = state,
        onComicUrlChange = model::onComicUrlChange,
        onClickAddComic = model::addComic,
        onClickCheckComic = model::checkComic,
        onClickBack = { navController.popBackStack() },
        onClickCancelProgress = model::cancelProgress
    )
}

