package eu.heha.cyclone.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import eu.heha.cyclone.ui.SimpleNavigationDestination.ADD_COMIC
import eu.heha.cyclone.ui.SimpleNavigationDestination.COMICS
import eu.heha.cyclone.ui.theme.CycloneTheme
import org.koin.compose.KoinApplication
import org.koin.core.module.Module

@Composable
fun Root(
    koinModule: Module,
    colorSchemeOverride: (isDarkTheme: Boolean) -> ColorScheme? = { null }
) {
    KoinApplication(
        application = { modules(koinModule) }
    ) {
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

