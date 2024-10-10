package eu.heha.cyclone.model.database

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.db.SqlDriver
import eu.heha.cyclone.database.Chapter
import eu.heha.cyclone.database.Comic
import eu.heha.cyclone.database.Database
import eu.heha.cyclone.database.Page
import eu.heha.cyclone.model.ComicAndChapters
import eu.heha.cyclone.model.ComicPosition
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
                addedAtAdapter = InstantAdapter,
                latestPositionAdapter = ComicPositionAdapter
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

    suspend fun getComicAndChapters(comicId: Long): ComicAndChapters = withContext(Dispatchers.IO) {
        val comic = database.comicQueries.getById(comicId).executeAsOne()
        val chapters = database.chapterQueries.getAllForComic(comicId).executeAsList()
        comic to chapters
    }

    suspend fun getPage(chapter: Chapter, pageNumber: Long): Page? = withContext(Dispatchers.IO) {
        database.pageQueries.getByChapterAndPageNumber(
            chapterId = chapter.id,
            pageNumber = pageNumber
        ).executeAsOneOrNull()
    }

    suspend fun updateNumberOfPages(chapter: Chapter, numberOfPages: Int) =
        withContext(Dispatchers.IO) {
            database.chapterQueries.updateNumberOfPages(
                numberOfPages = numberOfPages.toLong(),
                id = chapter.id
            )
        }

    suspend fun addPage(
        chapter: Chapter, page: Page
    ): Page = withContext(Dispatchers.IO) {
        database.transactionWithResult {
            database.pageQueries.insert(
                chapterId = chapter.id,
                pageNumber = page.pageNumber,
                imageUrl = page.imageUrl
            )
            val pageId = database.pageQueries.getLatestRowId().executeAsOne()
            database.pageQueries.getById(pageId).executeAsOne()
        }
    }

    suspend fun getChapter(id: Long): Chapter = withContext(Dispatchers.IO) {
        database.chapterQueries.getById(id).executeAsOne()
    }

    suspend fun wipeData() = withContext(Dispatchers.IO) {
        database.transaction {
            database.comicQueries.deleteAll()
            database.chapterQueries.deleteAll()
            database.pageQueries.deleteAll()
        }
    }

    suspend fun saveProgress(comic: Comic, chapter: Chapter, pageIndex: Long) =
        withContext(Dispatchers.IO) {
            database.transaction {
                database.comicQueries.updateReadLastAt(
                    id = comic.id,
                    readLastAt = Clock.System.now()
                )
                database.comicQueries.updateLatestPosition(
                    id = comic.id,
                    latestPosition = ComicPosition(chapter.id, pageIndex)
                )
            }
        }
}
