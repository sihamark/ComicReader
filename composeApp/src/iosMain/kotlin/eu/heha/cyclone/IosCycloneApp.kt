package eu.heha.cyclone

import coil3.PlatformContext
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

/**
 * This object is used to initialize the CycloneApp on iOS.
 *
 * Must be called exactly once before any other usage of CycloneApp.
 */
@Suppress("unused")
object IosCycloneApp {
    fun initialize() {
        CycloneApp.initialize(
            Requirements(
                platformContext = PlatformContext.INSTANCE,
                httpClientFactory = { HttpClient(Darwin) }
            )
        )
    }
}