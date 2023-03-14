package de.hamark.comicreader.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import de.hamark.comicreader.ui.theme.ComicReaderTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Root() {
    ComicReaderTheme {
        val model: RootViewModel = viewModel()
        LaunchedEffect(model) {
            model.loadInitialPage()
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
                        Column {
                            Text(text = "Error loading page")
                            Button(onClick = { model.reloadCurrentPage() }) {
                                Text(text = "Retry")
                            }
                        }
                    }

                    is RootViewModel.State.Loaded ->
                        Surface(onClick = { model.loadNextPage(state.page) }) {
                            AsyncImage(
                                model = state.page.imageUrl,
                                contentDescription = null
                            )
                        }

                    RootViewModel.State.Loading -> CircularProgressIndicator()
                }
            }
        }
    }
}

