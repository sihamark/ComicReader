package de.hamark.comicreader.ui

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
    comicUrl: String,
    onComicUrlChange: (String) -> Unit,
    isCheckingComic: Boolean,
    previewComic: ComicRepository.Comic?,
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
                    DropdownMenuItem(
                        text = { Text("Fill in Kaiju No. 8") },
                        onClick = {
                            onComicUrlChange(ComicRepository.DEFAULT_COMIC_URL)
                            isMoreMenuVisible = false
                        }
                    )
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
                value = comicUrl,
                onValueChange = onComicUrlChange,
                enabled = !isCheckingComic,
                label = { Text("Comic URL") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Button(
                enabled = !isCheckingComic,
                onClick = onClickCheckComic
            ) {
                Text("Check Comic")
            }

            //todo: use AnimatedVisibility
            if (isCheckingComic) {
                Spacer(Modifier.height(16.dp))
                Text("Checking comic...")
                Spacer(Modifier.height(8.dp))
                CircularProgressIndicator()
            }

            //todo: use AnimatedContent
            if (previewComic != null) {
                Spacer(Modifier.height(16.dp))
                Text("Comic Preview:")
                Spacer(Modifier.height(8.dp))
                ComicItem(previewComic)
                Spacer(Modifier.height(8.dp))
                Button(
                    enabled = !isCheckingComic,
                    onClick = onClickAddComic
                ) {
                    Text("Add Comic")
                }
            }
        }
    }
}