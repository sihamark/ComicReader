package eu.heha.cyclone.model.database

import app.cash.sqldelight.ColumnAdapter
import kotlinx.datetime.LocalDate

object LocalDateAdapter : ColumnAdapter<LocalDate, Long> {
    override fun decode(databaseValue: Long): LocalDate =
        LocalDate.fromEpochDays(databaseValue.toInt())

    override fun encode(value: LocalDate): Long = value.toEpochDays().toLong()
}