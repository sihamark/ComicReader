package eu.heha.cyclone.model

import app.cash.sqldelight.db.SqlDriver
import eu.heha.cyclone.Database

class DatabaseSource(
    private val sqlDriver: SqlDriver
) {
    private val database = Database(sqlDriver)

    suspend fun foo() {

    }
}