package eu.heha.cyclone.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.SingletonImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import eu.heha.cyclone.model.ComicAndChapters
import eu.heha.cyclone.model.RemoteSource.Companion.addComicHeader

@Composable
fun ComicDisplay(
    comicAndChapters: ComicAndChapters,
    modifier: Modifier = Modifier
) {
    val (comic, chapters) = comicAndChapters
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
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
fun ComicCard(
    comicAndChapters: ComicAndChapters,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onClickDelete: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = modifier
    ) {
        Box {
            var isMoreMenuVisible by remember { mutableStateOf(false) }

            ComicDisplay(comicAndChapters)

            Box(Modifier.align(Alignment.TopEnd)) {
                IconButton(onClick = { isMoreMenuVisible = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Additional actions")
                }

                DropdownMenu(
                    expanded = isMoreMenuVisible,
                    onDismissRequest = { isMoreMenuVisible = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            onClickDelete()
                            isMoreMenuVisible = false
                        }
                    )
                }
            }
        }
    }
}