package eu.heha.cyclone.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import eu.heha.cyclone.CycloneApp
import eu.heha.cyclone.ui.SimpleNavigationDestination.ADD_COMIC
import eu.heha.cyclone.ui.SimpleNavigationDestination.COMICS
import eu.heha.cyclone.ui.theme.CycloneTheme
import org.koin.compose.KoinContext

@Composable
fun Root(
    colorSchemeOverride: (isDarkTheme: Boolean) -> ColorScheme? = { null }
) {
    KoinContext(CycloneApp.koin) {
        CycloneTheme(colorSchemeOverride = colorSchemeOverride) {
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
                    ComicReaderRoute(navController, comicId)
                }
            }
        }
    }
}

