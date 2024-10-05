package eu.heha.cyclone

import android.app.Application
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

class ComicReaderApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CycloneApp.initialize(
            Requirements(
                platformContext = this,
                sqlDriverFactory = { databaseName ->
                    AndroidSqliteDriver(
                        context = this,
                        schema = Database.Schema,
                        name = databaseName
                    )
                }
            )
        )
    }
}