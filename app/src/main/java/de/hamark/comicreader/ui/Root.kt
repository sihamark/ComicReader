package de.hamark.comicreader.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.hamark.comicreader.ui.NavigationDestination.ADD_COMIC
import de.hamark.comicreader.ui.NavigationDestination.COMICS
import de.hamark.comicreader.ui.NavigationDestination.COMIC_PAGE
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
            composable(COMIC_PAGE.route) {
                PagePane()
            }
        }
    }
}

enum class NavigationDestination(val route: String) {
    COMICS("comics"),
    ADD_COMIC("comic_add"),
    COMIC_PAGE("comic_page")
}

