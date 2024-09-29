package de.hamark.comicreader.ui

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
    ComicReaderPane(
        state = model.state,
        pageState = model.pageState,
        onLoadPage = { pageIndex -> model.getPageState(pageIndex) }
    )
}