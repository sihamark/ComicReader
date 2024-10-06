package eu.heha.cyclone.model.database

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.db.SqlDriver
import eu.heha.cyclone.database.Chapter
import eu.heha.cyclone.database.Comic
import eu.heha.cyclone.database.Database
import eu.heha.cyclone.model.ComicAndChapters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class DatabaseSource(
    private val sqlDriver: SqlDriver
) {
    private val database by lazy {
        Database(
            sqlDriver,
            ComicAdapter = Comic.Adapter(
                readLastAtAdapter = InstantAdapter,
                addedAtAdapter = InstantAdapter
            ),
            ChapterAdapter = Chapter.Adapter(
                releaseDateAdapter = LocalDateAdapter
            )
        )
    }

    fun getAllComicsAsFlow(): Flow<List<Comic>> = database.comicQueries
        .getAll()
        .asFlow()
        .mapToList(Dispatchers.IO)

    suspend fun countChaptersForComic(comicId: Long): Long = withContext(Dispatchers.IO) {
        database.chapterQueries
            .countForComic(comicId)
            .executeAsOne()
    }

    suspend fun isComicInDatabase(comic: Comic): Boolean = withContext(Dispatchers.IO) {
        database.comicQueries.doesExist(comic.homeUrl).executeAsOne()
    }

    suspend fun addComic(comicAndChapters: ComicAndChapters): Long = withContext(Dispatchers.IO) {
        database.transactionWithResult {
            val (comic, chapters) = comicAndChapters
            database.comicQueries.insert(
                title = comic.title,
                description = comic.description,
                homeUrl = comic.homeUrl,
                coverImageUrl = comic.coverImageUrl,
                addedAt = Clock.System.now(),
            )
            val comicId = database.comicQueries.getLatestRowId().executeAsOne()
            chapters.forEach { chapter ->
                database.chapterQueries.insert(
                    comicId = comicId,
                    title = chapter.title,
                    url = chapter.url,
                    orderIndex = chapter.orderIndex,
                    releaseDate = chapter.releaseDate
                )
            }
            comicId
        }
    }

    suspend fun getChaptersOfComic(comicId: Long): List<Chapter> = withContext(Dispatchers.IO) {
        database.chapterQueries.getAllForComic(comicId).executeAsList()
    }
}
