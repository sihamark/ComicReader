package de.hamark.comicreader.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.hamark.comicreader.ui.SimpleNavigationDestination.ADD_COMIC
import de.hamark.comicreader.ui.SimpleNavigationDestination.COMICS
import de.hamark.comicreader.ui.theme.ComicReaderTheme

@Composable
fun Root() {
    ComicReaderTheme {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = COMICS.route
        ) {
            composable(COMICS.route) {
                ComicsRoute(navController)
            }
            composable(ADD_COMIC.route) {
                AddComicRoute(navController)
            }
            composable(ComicDestination.route) { backStackEntry ->
                val comicId = ComicDestination.getId(backStackEntry)
                PagePane()
            }
        }
    }
}

