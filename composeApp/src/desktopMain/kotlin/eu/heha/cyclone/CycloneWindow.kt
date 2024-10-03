package eu.heha.cyclone

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import coil3.PlatformContext
import eu.heha.cyclone.di.koinModule
import eu.heha.cyclone.ui.Root

@Composable
fun CycloneWindow(onClose: () -> Unit) {
    Window(
        title = "Cyclone",
        onCloseRequest = onClose,
        resizable = true,
        undecorated = false,
        state = rememberWindowState()
    ) {
        Root(koinModule(PlatformContext.INSTANCE))
    }
}