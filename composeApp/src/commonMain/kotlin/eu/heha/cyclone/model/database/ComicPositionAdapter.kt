package eu.heha.cyclone.model.database

import app.cash.sqldelight.ColumnAdapter
import eu.heha.cyclone.model.ComicPosition

object ComicPositionAdapter : ColumnAdapter<ComicPosition, String> {
    override fun decode(databaseValue: String): ComicPosition {
        val (chapterId, pageNumber) = databaseValue.split(":")
        return ComicPosition(
            chapterId = chapterId.toLong(),
            pageNumber = pageNumber.toLong()
        )
    }

    override fun encode(value: ComicPosition): String =
        "${value.chapterId}:${value.pageNumber}"
}