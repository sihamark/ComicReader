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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import eu.heha.cyclone.model.ComicRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComicsPane(
    comics: List<ComicRepository.Comic>,
    onClickAddComic: () -> Unit,
    onClickComic: (ComicRepository.Comic) -> Unit
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
                            onClick = { onClickComic(comic) },
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
fun ComicItem(comic: ComicRepository.Comic) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        val context = LocalContext.current
        val (name, value) = ComicRepository.imageHeader(comic.homeUrl)
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(comic.coverImageUrl)
                .addHeader(name, value)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = comic.title,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "${comic.chapters.size} Chapters",
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


