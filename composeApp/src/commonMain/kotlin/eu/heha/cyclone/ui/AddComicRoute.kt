package eu.heha.cyclone.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AddComicRoute(navController: NavController) {
    val model = koinViewModel<AddComicViewModel>()
    val scope = rememberCoroutineScope()
    AddComicPane(
        state = model.state,
        onComicUrlChange = model::onComicUrlChange,
        onClickAddComic = {
            scope.launch {
                model.addComic()
                navController.popBackStack()
            }
        },
        onClickCheckComic = model::checkComic,
        onClickBack = { navController.popBackStack() }
    )
}

