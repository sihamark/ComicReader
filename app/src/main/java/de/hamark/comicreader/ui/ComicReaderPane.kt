package de.hamark.comicreader.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import de.hamark.comicreader.model.ComicRepository
import de.hamark.comicreader.model.ComicRepository.Companion.imageHeader
import de.hamark.comicreader.model.ReaderController
import io.github.aakira.napier.Napier

@Composable
fun ComicReaderPane(
    state: ComicReaderViewModel.State,
    pageState: Map<Int, ReaderController.PageResult>,
    onLoadPage: (Int) -> Unit
) {
    Scaffold { innerPadding ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (state) {
                ComicReaderViewModel.State.Loading -> CircularProgressIndicator()
                is ComicReaderViewModel.State.Loaded -> ComicReaderContent(
                    comic = state.comic,
                    chapter = state.chapter,
                    pages = state.pages,
                    pageState = pageState,
                    onLoadPage = onLoadPage
                )

                is ComicReaderViewModel.State.Error -> ComicReaderError(state.message)
            }
        }
    }
}

@Composable
private fun ComicReaderError(message: String) {
    Text(text = "Error: $message")
}

@Composable
private fun ComicReaderContent(
    comic: ComicRepository.Comic,
    chapter: ComicRepository.Chapter,
    pages: List<Int>,
    pageState: Map<Int, ReaderController.PageResult>,
    onLoadPage: (Int) -> Unit
) {
    val context = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        Text(text = "Comic: ${comic.title}")
        Text(text = "Chapter: ${chapter.title}")
        LazyColumn {
            items(pages) { pageIndex ->
                val result = pageState[pageIndex]!!
                LaunchedEffect(pageIndex) {
                    onLoadPage(pageIndex)
                }
                LaunchedEffect(result) {
                    Napier.e { "$pageIndex: result: $result" }
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                ) {
                    Text(text = "Page $pageIndex")
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.weight(1f)
                    ) {
                        when (result) {
                            ReaderController.PageResult.Loading -> CircularProgressIndicator()
                            is ReaderController.PageResult.Loaded -> {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(result.page.imageUrl)
                                        .apply {
                                            val (name, value) = imageHeader(comic.homeUrl)
                                            addHeader(name, value)
                                        }
                                        .listener { request, result ->
                                            Napier.e { "image request: $request, result: $result" }
                                        }
                                        .build(),
                                    contentDescription = "Page $pageIndex"
                                )
                            }

                            is ReaderController.PageResult.Error -> Text(text = "Error: ${result.error}")
                        }
                    }
                }
            }
        }
    }
}
