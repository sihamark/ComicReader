package eu.heha.cyclone.ui

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.SingletonImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import eu.heha.cyclone.database.Chapter
import eu.heha.cyclone.database.Comic
import eu.heha.cyclone.model.ComicAndChapters
import eu.heha.cyclone.model.ReaderController
import eu.heha.cyclone.model.RemoteSource.Companion.addComicHeader
import eu.heha.cyclone.ui.ComicReaderViewModel.State.Loaded
import io.github.aakira.napier.Napier

@Composable
fun ComicReaderPane(
    comicAndChapters: ComicAndChapters,
    state: ComicReaderViewModel.State,
    pageState: Map<Long, ReaderController.PageResult>,
    onLoadPage: (Long) -> Unit,
    onProgress: (Long) -> Unit,
    onClickBack: () -> Unit,
    onClickPreviousChapter: () -> Unit,
    onClickNextChapter: () -> Unit
) {
    val (comic, chapters) = comicAndChapters
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopBar(
                state = state,
                comic = comic,
                chapters = chapters,
                onClickBack = onClickBack,
                onClickPreviousChapter = onClickPreviousChapter,
                onClickNextChapter = onClickNextChapter
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
                is Loaded -> if (state.pages != null) {
                    ComicReaderContent(
                        comic = comic,
                        chapter = state.chapter,
                        pages = state.pages,
                        pageState = pageState,
                        onLoadPage = onLoadPage,
                        onProgress = onProgress
                    )
                } else {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is ComicReaderViewModel.State.Error -> ComicReaderError(state.message)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    state: ComicReaderViewModel.State,
    comic: Comic,
    chapters: List<Chapter>,
    onClickBack: () -> Unit,
    onClickPreviousChapter: () -> Unit,
    onClickNextChapter: () -> Unit
) {
    val title = (state as? Loaded)?.chapter?.title ?: comic.title
    CenterAlignedTopAppBar(
        title = { Text(text = title, textAlign = TextAlign.Center) },
        navigationIcon = {
            IconButton(onClick = onClickBack) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            if (state is Loaded) {
                IconButton(
                    enabled = chapters.first() != state.chapter,
                    onClick = onClickPreviousChapter
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Chapter")
                }
                IconButton(
                    enabled = chapters.last() != state.chapter,
                    onClick = onClickNextChapter
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Chapter")
                }
            }
        }
    )
}

@Composable
private fun ComicReaderError(message: String) {
    Text(text = "Error: $message")
}

@Composable
private fun ComicReaderContent(
    comic: Comic,
    chapter: Chapter,
    pages: LongRange,
    pageState: Map<Long, ReaderController.PageResult>,
    onLoadPage: (Long) -> Unit,
    onProgress: (Long) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        val listState = rememberLazyListState()
        LaunchedEffect(chapter) {
            listState.scrollToItem(0)
        }
        LaunchedEffect(chapter, listState) {
            snapshotFlow { listState.firstVisibleItemIndex }.collect { index ->
                val page = pages.first + index
                Napier.e { "first visible page: $page" }
                onProgress(page)
            }
        }
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            state = listState
        ) {
            items(pages.toList()) { pageIndex ->
                val result = pageState[pageIndex]!!
                LaunchedEffect(chapter, pageIndex) {
                    onLoadPage(pageIndex)
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
                            ReaderController.PageResult.Loading -> {
                                CircularProgressIndicator()
                            }

                            is ReaderController.PageResult.Loaded -> {
                                DefaultAsyncImagePreviewHandler {
                                    val platformContext = LocalPlatformContext.current
                                    AsyncImage(
                                        model = ImageRequest.Builder(platformContext)
                                            .data(result.page.imageUrl)
                                            .addComicHeader(comic.homeUrl)
                                            .build(),
                                        contentDescription = "Page $pageIndex",
                                        imageLoader = SingletonImageLoader.get(platformContext),
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            is ReaderController.PageResult.Error -> {
                                Text(text = "Error: ${result.error}")
                            }
                        }
                    }
                    Text(
                        text = "Page $pageIndex",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}
