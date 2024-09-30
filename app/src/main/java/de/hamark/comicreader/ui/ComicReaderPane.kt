package de.hamark.comicreader.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import de.hamark.comicreader.model.ComicRepository
import de.hamark.comicreader.model.ComicRepository.Companion.imageHeader
import de.hamark.comicreader.model.ReaderController
import de.hamark.comicreader.ui.ComicReaderViewModel.State.Loaded
import io.github.aakira.napier.Napier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComicReaderPane(
    comic: ComicRepository.Comic,
    state: ComicReaderViewModel.State,
    pageState: Map<Int, ReaderController.PageResult>,
    onLoadPage: (Int) -> Unit,
    onProgress: (Int) -> Unit,
    onClickBack: () -> Unit,
    onClickPreviousChapter: () -> Unit,
    onClickNextChapter: () -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            val chapter = (state as? Loaded)?.chapter?.title?.let { "\n$it" } ?: ""
            CenterAlignedTopAppBar(
                title = { Text(text = comic.title + chapter, textAlign = TextAlign.Center) },
                navigationIcon = {
                    IconButton(onClick = onClickBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state is Loaded) {
                        IconButton(
                            enabled = comic.chapters.first() != state.chapter,
                            onClick = onClickPreviousChapter
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Chapter")
                        }
                        IconButton(
                            enabled = comic.chapters.last() != state.chapter,
                            onClick = onClickNextChapter
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Chapter")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (state) {
                ComicReaderViewModel.State.Loading -> CircularProgressIndicator()
                is Loaded -> ComicReaderContent(
                    comic = comic,
                    chapter = state.chapter,
                    pages = state.pages,
                    pageState = pageState,
                    onLoadPage = onLoadPage,
                    onProgress = onProgress
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
    onLoadPage: (Int) -> Unit,
    onProgress: (Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        val listState = rememberLazyListState()
        LaunchedEffect(chapter) {
            listState.scrollToItem(0)
        }
        LaunchedEffect(listState) {
            snapshotFlow { listState.firstVisibleItemIndex }.collect { index ->
                val page = pages[index]
                Napier.e { "first visible page: $page" }
                onProgress(page)
            }
        }
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            state = listState
        ) {
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
                        .padding(vertical = 8.dp, horizontal = 16.dp)
                        .height(400.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.weight(1f)
                    ) {
                        when (result) {
                            ReaderController.PageResult.Loading -> CircularProgressIndicator()
                            is ReaderController.PageResult.Loaded -> {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(result.page.imageUrl)
                                        .apply {
                                            val (name, value) = imageHeader(comic.homeUrl)
                                            addHeader(name, value)
                                        }
                                        .build(),
                                    contentDescription = "Page $pageIndex",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            is ReaderController.PageResult.Error -> Text(text = "Error: ${result.error}")
                        }
                    }
                    Text(
                        text = "Page $pageIndex",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}
