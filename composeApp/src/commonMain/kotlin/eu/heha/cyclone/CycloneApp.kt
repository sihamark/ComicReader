package eu.heha.cyclone

import app.cash.sqldelight.db.SqlDriver
import coil3.PlatformContext
import eu.heha.cyclone.di.appModule
import io.github.aakira.napier.Antilog
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.engine.cio.*
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
                    requirements.httpClientFactory,
                    requirements.sqlDriverFactory
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
    val sqlDriverFactory: (databaseName: String) -> SqlDriver
)