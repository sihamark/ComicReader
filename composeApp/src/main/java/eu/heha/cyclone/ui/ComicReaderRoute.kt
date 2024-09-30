package eu.heha.cyclone.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun ComicReaderRoute(navController: NavController, comicId: String) {
    val model = hiltViewModel<ComicReaderViewModel>()
    LaunchedEffect(model) {
        model.loadComic(comicId)
    }
    val comic = model.comic
    if (comic != null) {
        ComicReaderPane(
            comic = comic,
            state = model.state,
            pageState = model.pageState,
            onLoadPage = { pageIndex -> model.loadPageState(pageIndex) },
            onProgress = { pageIndex -> model.setProgress(pageIndex) },
            onClickBack = { navController.popBackStack() },
            onClickPreviousChapter = { model.loadPreviousChapter() },
            onClickNextChapter = { model.loadNextChapter() }
        )
    }
}