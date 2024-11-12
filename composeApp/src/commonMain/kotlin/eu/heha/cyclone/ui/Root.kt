package eu.heha.cyclone.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import eu.heha.cyclone.CycloneApp
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
                startDestination = Comics
            ) {
                composable<Comics> {
                    ComicsRoute(navController)
                }
                composable<ComicAdd> {
                    AddComicRoute(navController)
                }
                composable<Comic> { backStackEntry ->
                    val comicId = backStackEntry.toRoute<Comic>().id
                    ComicReaderRoute(navController, comicId)
                }
            }
        }
    }
}

