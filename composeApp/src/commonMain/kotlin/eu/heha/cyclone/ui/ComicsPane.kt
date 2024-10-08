package eu.heha.cyclone.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.SingletonImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import eu.heha.cyclone.database.Comic
import eu.heha.cyclone.model.ComicAndChapters
import eu.heha.cyclone.model.RemoteSource.Companion.addComicHeader
import eu.heha.cyclone.model.comic
import eu.heha.cyclone.ui.theme.CycloneTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComicsPane(
    comics: List<ComicAndChapters>,
    onClickAddComic: () -> Unit,
    onClickComic: (Comic) -> Unit,
    onClickWipeData: () -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Comics") },
                actions = {
                    IconButton(onClick = onClickAddComic) {
                        Icon(Icons.Default.Add, contentDescription = "Add Comic")
                    }
                    IconButton(onClick = onClickWipeData) {
                        Icon(Icons.Default.Delete, contentDescription = "Wipe Data")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding)
        ) {
            if (comics.isEmpty()) {
                EmptyContent(onClickAddComic)
            } else {
                LazyColumn {
                    items(comics) { comic ->
                        Surface(
                            onClick = { onClickComic(comic.comic) },
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            ComicItem(comic)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ComicItem(comicAndChapters: ComicAndChapters) {
    val (comic, chapters) = comicAndChapters
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        DefaultAsyncImagePreviewHandler {
            val platformContext = LocalPlatformContext.current
            AsyncImage(
                model = ImageRequest.Builder(platformContext)
                    .data(comic.coverImageUrl)
                    .addComicHeader(comic.homeUrl)
                    .build(),
                contentDescription = null,
                imageLoader = SingletonImageLoader.get(platformContext),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = comic.title,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "${chapters.size} Chapters",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun EmptyContent(
    onClickAddComic: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "No comics found")
        Spacer(Modifier.height(8.dp))
        Button(onClickAddComic) {
            Text(text = "Add Comic")
        }
    }
}

@Composable
fun ComicsPreviewEmptyCommon() {
    CycloneTheme {
        BasePreview()
    }
}

@Composable
fun ComicsPreviewLoadedCommon() {
    CycloneTheme {
        BasePreview(listOf(Dummy.comicAndChapters(), Dummy.comicAndChapters()))
    }
}

@Composable
private fun BasePreview(comics: List<ComicAndChapters> = emptyList()) {
    CycloneTheme {
        ComicsPane(
            comics = comics,
            onClickAddComic = {},
            onClickComic = {},
            onClickWipeData = {}
        )
    }
}