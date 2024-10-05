package eu.heha.cyclone

import androidx.compose.ui.window.application
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import coil3.PlatformContext

fun main() {
    CycloneApp.initialize(
        Requirements(
            platformContext = PlatformContext.INSTANCE,
            antilog = Logging.antilog(),
            sqlDriverFactory = { databaseName -> JdbcSqliteDriver("jdbc:sqlite:$databaseName") }
        )
    )
    application {
        CycloneWindow(onClose = ::exitApplication)
    }
}