package eu.heha.cyclone.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ComicReaderRoute(navController: NavController, comicId: String) {
    val model = koinViewModel<ComicReaderViewModel>()
    LaunchedEffect(model) {
        model.loadComic(comicId)
    }
    val comicAndChapters = model.comicAndChapters
    if (comicAndChapters != null) {
        ComicReaderPane(
            comicAndChapters = comicAndChapters,
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