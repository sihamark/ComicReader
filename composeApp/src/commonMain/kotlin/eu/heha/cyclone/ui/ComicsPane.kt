package eu.heha.cyclone.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.heha.cyclone.database.Comic
import eu.heha.cyclone.model.ComicAndChapters
import eu.heha.cyclone.model.comic
import eu.heha.cyclone.ui.theme.CycloneTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComicsPane(
    comics: List<ComicAndChapters>,
    onClickAddComic: () -> Unit,
    onClickComic: (Comic) -> Unit,
    onClickWipeData: () -> Unit,
    onCLickDeleteComic: (Comic) -> Unit
) {
    var comicDeletionPrompt by remember { mutableStateOf<Comic?>(null) }
    var isWipeDataPromptVisible by remember { mutableStateOf(false) }
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Comics") },
                actions = {
                    var isMoreMenuExpanded by remember { mutableStateOf(false) }
                    IconButton(onClick = onClickAddComic) {
                        Icon(Icons.Default.Add, contentDescription = "Add Comic")
                    }
                    IconButton(onClick = { isMoreMenuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(
                        expanded = isMoreMenuExpanded,
                        onDismissRequest = { isMoreMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Wipe Data") },
                            onClick = {
                                isMoreMenuExpanded = false
                                isWipeDataPromptVisible = true
                            }
                        )
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
                LazyColumn(Modifier.fillMaxSize()) {
                    items(comics, key = { it.comic.id }) { comic ->
                        ComicCard(
                            comicAndChapters = comic,
                            onClick = { onClickComic(comic.comic) },
                            onClickDelete = { comicDeletionPrompt = comic.comic },
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
                                .animateItem()
                        )
                    }
                    item {
                        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                    }
                }
            }
        }

        val comicToDelete = comicDeletionPrompt
        if (comicToDelete != null) {
            DeleteDialog(
                text = "Are you sure you want to delete the comic '${comicToDelete.title}'?",
                onDismissRequest = { comicDeletionPrompt = null },
                onDelete = { onCLickDeleteComic(comicToDelete) }
            )
        }
        if (isWipeDataPromptVisible) {
            DeleteDialog(
                text = "Are you sure you want to wipe all data?",
                onDismissRequest = { isWipeDataPromptVisible = false },
                onDelete = onClickWipeData
            )
        }
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
            onClickWipeData = {},
            onCLickDeleteComic = {}
        )
    }
}