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
import androidx.lifecycle.viewmodel.compose.viewModel
import de.hamark.comicreader.ui.theme.ComicReaderTheme

@Composable
fun Root() {
    ComicReaderTheme {
        val model: RootViewModel = viewModel()
        LaunchedEffect(model) {
            model.loadPage()
        }
        Scaffold { innerPadding ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                when (val state = model.state) {
                    is RootViewModel.State.Error -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Error loading page")
                            Button(onClick = { model.reloadCurrentPage() }) {
                                Text(text = "Retry")
                            }
                        }
                    }

                    is RootViewModel.State.Loaded ->
                        Surface(onClick = { model.loadPage(state.chapter, state.pageIndex + 1) }) {
                            Image(
                                bitmap = state.image,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                    RootViewModel.State.Loading -> CircularProgressIndicator()
                }
            }
        }
    }
}

