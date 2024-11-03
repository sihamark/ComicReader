package eu.heha.cyclone

import androidx.compose.ui.window.ComposeUIViewController
import eu.heha.cyclone.ui.Root

/**
 * View controller for the iOS app.
 * This is a simple wrapper around the [Root] composable.
 */
@Suppress("unused")
object RootView {
    fun viewController() = ComposeUIViewController {
        Root()
    }
}
