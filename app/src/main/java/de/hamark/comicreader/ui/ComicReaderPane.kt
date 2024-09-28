package de.hamark.comicreader.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ComicReaderPane() {
    val model: ComicReaderViewModel = hiltViewModel()
    LaunchedEffect(model) {
        model.loadComic()
    }
    Scaffold { innerPadding ->
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (val state = model.state) {
                is ComicReaderViewModel.State.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Error loading page")
                        Button(onClick = { model.reloadCurrentPage() }) {
                            Text(text = "Retry")
                        }
                    }
                }

                is ComicReaderViewModel.State.Loaded ->
                    Surface(onClick = { model.loadComic(state.chapter, state.pageIndex + 1) }) {
                        Image(
                            bitmap = state.image,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                ComicReaderViewModel.State.Loading -> CircularProgressIndicator()
            }
        }
    }
}