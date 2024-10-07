package eu.heha.cyclone

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import coil3.PlatformContext
import eu.heha.cyclone.database.Database
import io.ktor.client.*
import io.ktor.client.engine.darwin.*

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
                httpClientFactory = { HttpClient(Darwin) },
                sqlDriverFactory = { databaseName ->
                    NativeSqliteDriver(schema = Database.Schema, name = databaseName)
                }
            )
        )
    }
}