package eu.heha.cyclone

import androidx.compose.ui.window.ComposeUIViewController
import coil3.PlatformContext
import eu.heha.cyclone.di.koinModule
import eu.heha.cyclone.ui.Root

fun MainViewController() = ComposeUIViewController {
    Root(koinModule(PlatformContext.INSTANCE))
}