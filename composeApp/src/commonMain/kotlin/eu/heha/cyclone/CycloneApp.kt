package eu.heha.cyclone

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

object CycloneApp {
    fun initialize() {
        Napier.base(DebugAntilog())
    }
}