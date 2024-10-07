package eu.heha.cyclone.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.heha.cyclone.model.ComicRepository

@Composable
fun AddComicPane(
    state: AddComicViewModel.State,
    onClickCancelProgress: () -> Unit,
    onComicUrlChange: (String) -> Unit,
    onClickBack: () -> Unit,
    onClickCheckComic: () -> Unit,
    onClickAddComic: () -> Unit
) {
    Scaffold(
        topBar = { TopBar(onClickBack, onComicUrlChange) }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .padding(innerPadding)
        ) {
            AnimatedVisibility(state.progress != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    val text = when (state.progress) {
                        AddComicViewModel.Progress.CheckingComic -> "Checking comic..."
                        AddComicViewModel.Progress.AddingComic -> "Adding comic..."
                        else -> ""
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text)
                        if (state.progress?.isCancellable == true) {
                            Spacer(Modifier.width(8.dp))
                            TextButton(onClickCancelProgress) {
                                Text("Cancel")
                            }
                        }
                    }
                }
            }

            Content(
                state = state,
                onComicUrlChange = onComicUrlChange,
                onClickCheckComic = onClickCheckComic,
                onClickAddComic = onClickAddComic
            )
        }
    }
}

@Composable
private fun Content(
    state: AddComicViewModel.State,
    onComicUrlChange: (String) -> Unit,
    onClickCheckComic: () -> Unit,
    onClickAddComic: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 32.dp)
    ) {
        OutlinedTextField(
            value = state.comicUrl,
            onValueChange = onComicUrlChange,
            enabled = state.progress == null,
            label = { Text("Comic URL") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            enabled = state.progress == null && state.comicUrl.isNotBlank(),
            onClick = onClickCheckComic
        ) {
            Text("Check Comic")
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
                        enabled = state.progress == null,
                        onClick = onClickAddComic
                    ) {
                        Text("Add Comic")
                    }
                }

                previewComicResult?.exceptionOrNull()?.also { exception ->
                    Spacer(Modifier.height(16.dp))
                    Text("Failed to load comic: ${exception.message}")
                    OutlinedButton(
                        enabled = state.progress == null,
                        onClick = onClickCheckComic
                    ) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    onClickBack: () -> Unit,
    onComicUrlChange: (String) -> Unit
) {
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
}
