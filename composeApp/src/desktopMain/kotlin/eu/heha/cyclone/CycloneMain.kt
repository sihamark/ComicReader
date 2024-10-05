package eu.heha.cyclone

import androidx.compose.ui.window.application
import coil3.PlatformContext

fun main() {
    CycloneApp.initialize(
        Requirements(platformContext = PlatformContext.INSTANCE)
    )
    application {
        CycloneWindow(onClose = ::exitApplication)
    }
}