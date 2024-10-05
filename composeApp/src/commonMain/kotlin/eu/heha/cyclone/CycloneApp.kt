package eu.heha.cyclone

import coil3.PlatformContext
import eu.heha.cyclone.di.appModule
import io.github.aakira.napier.Antilog
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication

object CycloneApp {

    private lateinit var koinApp: KoinApplication
    val koin by lazy { koinApp.koin }

    fun initialize(requirements: Requirements) {
        Napier.base(requirements.antilog)
        koinApp = koinApplication {
            modules(
                appModule(
                    requirements.platformContext,
                    requirements.httpClientFactory
                )
            )
        }
    }
}

fun defaultHttpFactory() = HttpClient(CIO)

data class Requirements(
    val platformContext: PlatformContext,
    val antilog: Antilog = DebugAntilog(),
    val httpClientFactory: () -> HttpClient = { defaultHttpFactory() },
)