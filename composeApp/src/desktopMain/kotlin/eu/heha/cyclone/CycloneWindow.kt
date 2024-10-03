package eu.heha.cyclone

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import coil3.PlatformContext
import cyclone.composeapp.generated.resources.Res
import cyclone.composeapp.generated.resources.ic_cyclone
import eu.heha.cyclone.di.koinModule
import eu.heha.cyclone.ui.Root
import org.jetbrains.compose.resources.painterResource
import java.awt.Dimension

@Composable
fun CycloneWindow(onClose: () -> Unit) {
    Window(
        title = "Cyclone",
        onCloseRequest = onClose,
        resizable = true,
        undecorated = false,
        icon = painterResource(Res.drawable.ic_cyclone),
        state = rememberWindowState()
    ) {
        LaunchedEffect(window) {
            window.minimumSize = Dimension(400, 600)
        }
        Root(koinModule(PlatformContext.INSTANCE))
    }
}