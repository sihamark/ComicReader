package de.hamark.comicreader.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.hamark.comicreader.ui.theme.ComicReaderTheme

@Composable
fun Root() {
    ComicReaderTheme {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "comicPage") {
            composable("comicPage") {
                PagePane()
            }
        }
    }
}

