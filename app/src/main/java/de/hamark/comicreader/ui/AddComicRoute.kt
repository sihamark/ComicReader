package de.hamark.comicreader.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun AddComicRoute(navController: NavController) {
    val model = hiltViewModel<AddComicViewModel>()
    val scope = rememberCoroutineScope()
    ComicAddPane(
        comicUrl = model.comicUrl,
        onComicUrlChange = model::onComicUrlChange,
        isCheckingComic = model.isCheckingComic,
        onClickAddComic = {
            scope.launch {
                model.addComic()
                navController.popBackStack()
            }
        },
        onClickCheckComic = model::checkComic,
        previewComic = model.previewComic,
        onClickBack = { navController.popBackStack() }
    )
}

