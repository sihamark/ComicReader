package eu.heha.cyclone

import androidx.compose.ui.window.application
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import coil3.PlatformContext
import eu.heha.cyclone.database.Database
import java.io.File

fun main() {
    CycloneApp.initialize(
        Requirements(
            platformContext = PlatformContext.INSTANCE,
            antilog = Logging.antilog(),
            sqlDriverFactory = { databaseName ->
                val databaseFile = File("data").resolve(databaseName).also {
                    it.parentFile.mkdirs()
                }
                JdbcSqliteDriver(
                    url = "jdbc:sqlite:$databaseFile",
                    schema = Database.Schema
                )
            }
        )
    )
    application {
        CycloneWindow(onClose = ::exitApplication)
    }
}