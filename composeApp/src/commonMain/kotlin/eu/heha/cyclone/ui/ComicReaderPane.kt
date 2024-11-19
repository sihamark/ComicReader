package eu.heha.cyclone.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ManageSearch
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
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
import kotlinx.coroutines.launch

@Composable
fun ComicReaderPane(
    comicAndChapters: ComicAndChapters,
    state: ComicReaderViewModel.State,
    pageState: Map<Long, ReaderController.PageResult>,
    onLoadPage: (Long) -> Unit,
    onProgress: (Long) -> Unit,
    onClickBack: () -> Unit,
    onClickPreviousChapter: () -> Unit,
    onClickNextChapter: () -> Unit,
    onClickChapter: (Chapter) -> Unit
) {
    val (comic, chapters) = comicAndChapters
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopBar(
                state = state,
                comic = comic,
                onClickBack = onClickBack
            )
        }
    ) { innerPadding ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            var isChapterSelectionDialogVisible by remember { mutableStateOf(false) }
            when (state) {
                ComicReaderViewModel.State.Loading -> CircularProgressIndicator()
                is Loaded -> if (state.pages != null) {
                    ComicReaderContent(
                        comic = comic,
                        chapter = state.chapter,
                        chapters = chapters,
                        pages = state.pages,
                        jumpToPage = state.jumpToPage,
                        pageState = pageState,
                        onLoadPage = onLoadPage,
                        onProgress = onProgress,
                        onClickPreviousChapter = onClickPreviousChapter,
                        onClickNextChapter = onClickNextChapter
                    )
                    ChapterHandle(
                        chapter = state.chapter,
                        chapters = chapters,
                        onClickPreviousChapter = onClickPreviousChapter,
                        onClickNextChapter = onClickNextChapter,
                        onClickShowChapters = { isChapterSelectionDialogVisible = true },
                        modifier = Modifier.align(Alignment.BottomCenter)
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

            if (isChapterSelectionDialogVisible && state is Loaded) {
                ChapterOverview(
                    chapter = state.chapter,
                    chapters = chapters,
                    onDismissRequest = { isChapterSelectionDialogVisible = false },
                    onClickChapter = { chapter ->
                        isChapterSelectionDialogVisible = false
                        onClickChapter(chapter)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    state: ComicReaderViewModel.State,
    comic: Comic,
    onClickBack: () -> Unit
) {
    val title = (state as? Loaded)?.chapter?.title ?: comic.title
    CenterAlignedTopAppBar(
        title = { Text(text = title, textAlign = TextAlign.Center) },
        navigationIcon = {
            IconButton(onClick = onClickBack) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Back")
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
    chapters: List<Chapter>,
    pages: LongRange,
    jumpToPage: Long?,
    pageState: Map<Long, ReaderController.PageResult>,
    onLoadPage: (Long) -> Unit,
    onProgress: (Long) -> Unit,
    onClickPreviousChapter: () -> Unit,
    onClickNextChapter: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        var isScrollingEnabled by remember { mutableStateOf(true) }
        val listState = rememberLazyListState()

        // +1 for the previous chapter button
        fun toListIndex(page: Long) = (page - pages.first).toInt() + 1

        // -1 for the previous chapter button
        fun toPageIndex(index: Int) = ((pages.first + index) - 1).coerceAtMost(pages.last)

        LaunchedEffect(chapter, jumpToPage) {
            listState.scrollToItem(toListIndex(jumpToPage ?: pages.first))
        }
        LaunchedEffect(chapter, listState) {
            snapshotFlow { listState.firstVisibleItemIndex }.collect { index ->
                onProgress(toPageIndex(index))
            }
        }
        LazyColumn(
            userScrollEnabled = isScrollingEnabled,
            horizontalAlignment = Alignment.CenterHorizontally,
            state = listState
        ) {
            item {
                TextButton(
                    onClick = onClickPreviousChapter,
                    enabled = !chapter.isFirst(chapters)
                ) {
                    val text = if (chapter.isFirst(chapters)) {
                        "This is the beginning"
                    } else {
                        "Previous Chapter"
                    }
                    Text(text)
                }
            }
            items(pages.toList()) { pageIndex ->
                val result = pageState[pageIndex]!!
                ComicPage(
                    comic = comic,
                    chapter = chapter,
                    pageIndex = pageIndex,
                    pageResult = result,
                    onLoadPage = onLoadPage,
                    onGestureChanged = { isInGesture -> isScrollingEnabled = !isInGesture }
                )
            }
            item {
                TextButton(
                    onClick = onClickNextChapter,
                    enabled = !chapter.isLast(chapters)
                ) {
                    val text = if (chapter.isLast(chapters)) {
                        "This is the last chapter"
                    } else {
                        "Next Chapter"
                    }
                    Text(text)
                }
            }
            item {
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }
    }
}

@Composable
private fun ComicPage(
    comic: Comic,
    chapter: Chapter,
    pageIndex: Long,
    pageResult: ReaderController.PageResult,
    onLoadPage: (Long) -> Unit,
    onGestureChanged: (isInGesture: Boolean) -> Unit
) {
    val scope = rememberCoroutineScope()
    //the scale from the zoom gesture
    val scale = remember { Animatable(1f) }
    //the offset from the pan gesture
    val offset = remember {
        Animatable(
            Offset(0f, 0f),
            typeConverter = Offset.VectorConverter
        )
    }
    val isInGesture by remember {
        //this is only true if there the user zooms or pans
        derivedStateOf {
            scale.value != 1f || offset.value != Offset(0f, 0f)
        }
    }
    //once a gesture starts this page will be lifted up to be over all other pages
    val zIndex by remember { derivedStateOf { if (isInGesture) 1f else 0f } }

    LaunchedEffect(chapter, pageIndex) {
        //reset the scale and offset when the page/chapter changes
        scale.snapTo(1f)
        offset.snapTo(Offset(0f, 0f))
        onLoadPage(pageIndex)
    }

    LaunchedEffect(isInGesture) {
        onGestureChanged(isInGesture)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(zIndex)
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .height(400.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.weight(1f)
                .zIndex(1f)
        ) {
            when (pageResult) {
                ReaderController.PageResult.Loading -> {
                    CircularProgressIndicator()
                }

                is ReaderController.PageResult.Loaded -> {
                    DefaultAsyncImagePreviewHandler {
                        val platformContext = LocalPlatformContext.current
                        AsyncImage(
                            model = ImageRequest.Builder(platformContext)
                                .data(pageResult.page.imageUrl)
                                .addComicHeader(comic.homeUrl)
                                .build(),
                            contentDescription = "Page $pageIndex",
                            imageLoader = SingletonImageLoader.get(platformContext),
                            modifier = Modifier.fillMaxSize()
                                .pointerInput("page$pageIndex") {
                                    awaitEachGesture {
                                        awaitFirstDown()
                                        do {
                                            val event = awaitPointerEvent()
                                            //scale calculates from the zoom gesture and is capped between 1 and 3
                                            val newScale = (scale.value * event.calculateZoom())
                                                .coerceIn(1f, 3f)
                                            val pan = event.calculatePan()
                                            //offset calculates from the pan gesture and is only used if there is zoom
                                            val newOffset = if (newScale == 1f) {
                                                Offset(0f, 0f)
                                            } else {
                                                offset.value + pan
                                            }
                                            scope.launch {
                                                scale.snapTo(newScale)
                                                offset.snapTo(newOffset)
                                            }
                                        } while (event.changes.any { it.pressed })

                                        //once the gesture is done animate back to the original scale and offset
                                        scope.launch {
                                            launch { scale.animateTo(1f) }
                                            launch { offset.animateTo(Offset(0f, 0f)) }
                                        }
                                    }
                                }
                                .graphicsLayer(
                                    scaleX = scale.value, scaleY = scale.value,
                                    translationX = offset.value.x, translationY = offset.value.y
                                )
                        )
                    }
                }

                is ReaderController.PageResult.Error -> {
                    Text(text = "Error: ${pageResult.error}")
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

@Composable
private fun ChapterHandle(
    chapter: Chapter,
    chapters: List<Chapter>,
    onClickPreviousChapter: () -> Unit,
    onClickNextChapter: () -> Unit,
    onClickShowChapters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.66f),
        modifier = modifier.padding(32.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            IconButton(
                onClick = onClickPreviousChapter,
                enabled = !chapter.isFirst(chapters)
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Chapter")
            }
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = onClickShowChapters
            ) {
                Icon(Icons.AutoMirrored.Default.ManageSearch, contentDescription = "Show Chapters")
            }
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = onClickNextChapter,
                enabled = !chapter.isLast(chapters)
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next Chapter")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChapterOverview(
    chapter: Chapter,
    chapters: List<Chapter>,
    onDismissRequest: () -> Unit,
    onClickChapter: (Chapter) -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest
    ) {
        Surface(
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Chapter Overview",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(4.dp))
                LazyColumn(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxHeight(0.8f)
                ) {
                    items(chapters) { chapterIndex ->
                        TextButton(
                            onClick = { onClickChapter(chapterIndex) },
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(
                                chapterIndex.title,
                                fontWeight = if (chapterIndex == chapter) FontWeight.Bold else null
                            )
                        }
                    }
                    item {
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

private fun Chapter.isFirst(chapters: List<Chapter>) = this == chapters.first()
private fun Chapter.isLast(chapters: List<Chapter>) = this == chapters.last()
