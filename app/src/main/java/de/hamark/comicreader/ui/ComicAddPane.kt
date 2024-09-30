package de.hamark.comicreader.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import de.hamark.comicreader.model.ComicRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComicAddPane(
    state: AddComicViewModel.State,
    onComicUrlChange: (String) -> Unit,
    onClickBack: () -> Unit,
    onClickCheckComic: () -> Unit,
    onClickAddComic: () -> Unit
) {
    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            title = { Text("Add Comic") },
            navigationIcon = {
                IconButton(onClickBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                var isMoreMenuVisible by remember { mutableStateOf(false) }
                IconButton(onClick = { isMoreMenuVisible = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Actions"
                    )
                }
                DropdownMenu(
                    isMoreMenuVisible,
                    onDismissRequest = { isMoreMenuVisible = false }
                ) {
                    ComicRepository.dummyComics().forEach { (name, url) ->
                        DropdownMenuItem(
                            text = { Text("Fill in \"$name\"") },
                            onClick = {
                                onComicUrlChange(url)
                                isMoreMenuVisible = false
                            }
                        )
                    }
                }
            }
        )
    }) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 32.dp)
        ) {
            OutlinedTextField(
                value = state.comicUrl,
                onValueChange = onComicUrlChange,
                enabled = !state.isCheckingComic,
                label = { Text("Comic URL") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Button(
                enabled = !state.isCheckingComic && state.comicUrl.isNotBlank(),
                onClick = onClickCheckComic
            ) {
                Text("Check Comic")
            }

            AnimatedVisibility(state.isCheckingComic) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(16.dp))
                    Text("Checking comic...")
                    Spacer(Modifier.height(8.dp))
                    CircularProgressIndicator()
                }
            }

            AnimatedContent(
                targetState = state.previewComicResult,
                contentAlignment = Alignment.Center
            ) { previewComicResult ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    previewComicResult?.getOrNull()?.also { previewComic ->
                        Spacer(Modifier.height(16.dp))
                        Text("Comic Preview:")
                        Spacer(Modifier.height(8.dp))
                        ComicItem(previewComic)
                        Spacer(Modifier.height(8.dp))
                        Button(
                            enabled = !state.isCheckingComic,
                            onClick = onClickAddComic
                        ) {
                            Text("Add Comic")
                        }
                    }

                    previewComicResult?.exceptionOrNull()?.also { exception ->
                        Spacer(Modifier.height(16.dp))
                        Text("Failed to load comic: ${exception.message}")
                    }
                }
            }
        }
    }
}
