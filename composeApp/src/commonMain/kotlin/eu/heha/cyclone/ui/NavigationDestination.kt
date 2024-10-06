package eu.heha.cyclone.ui

import androidx.navigation.NavBackStackEntry

interface NavigationDestination {
    val route: String
}

enum class SimpleNavigationDestination(override val route: String) : NavigationDestination {
    COMICS("comics"),
    ADD_COMIC("comic_add")
}

data object ComicDestination : NavigationDestination {
    private const val ID = "id"

    override val route: String = routeFormat("{id}")

    private fun routeFormat(id: Any) = "comic/$id"
    fun withId(id: Long) = routeFormat(id)
    fun getId(entry: NavBackStackEntry): String =
        entry.arguments?.getString(ID) ?: throw IllegalArgumentException("No ID found in $entry")
}