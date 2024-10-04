package eu.heha.cyclone

import androidx.compose.ui.window.application

fun main() {
    CycloneApp.initialize()
    application {
        CycloneWindow(onClose = ::exitApplication)
    }
}