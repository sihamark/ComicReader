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
    private fun routeFormat(id: Any) = "comic/$id"
    private const val ID = "id"

    override val route: String = routeFormat("{id}")

    fun withId(id: Long) = routeFormat(id)
    fun getId(entry: NavBackStackEntry): Long = entry.arguments?.getString(ID)?.toLong()
        ?: throw IllegalArgumentException("no id found in $entry")
}