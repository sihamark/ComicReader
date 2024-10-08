package eu.heha.cyclone.model

import eu.heha.cyclone.database.Chapter
import eu.heha.cyclone.database.Comic
import eu.heha.cyclone.database.Page
import eu.heha.cyclone.model.database.DatabaseSource
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

typealias ComicAndChapters = Pair<Comic, List<Chapter>>

val ComicAndChapters.comic get() = first
val ComicAndChapters.chapters get() = second

class ComicRepository(
    private val databaseSource: DatabaseSource,
    private val remoteSource: RemoteSource
) {

    val comics
        get() = databaseSource.getAllComicsAsFlow()
            .map { comics ->
                comics.map { comic ->
                    comic to databaseSource.getChaptersOfComic(comic.id)
                }
            }

    suspend fun addComic(previewComic: ComicAndChapters): AddComicResult {
        val (comic, _) = previewComic
        if (databaseSource.isComicInDatabase(comic)) {
            return AddComicResult.ComicAlreadyExists(previewComic)
        }
        return try {
            val comicId = databaseSource.addComic(previewComic)
            AddComicResult.Success(previewComic, comicId)
        } catch (e: Exception) {
            AddComicResult.Failure(previewComic, e)
        }
    }

    sealed class AddComicResult {
        abstract val comic: ComicAndChapters

        data class Success(
            override val comic: ComicAndChapters,
            val comicId: Long
        ) : AddComicResult()

        data class ComicAlreadyExists(override val comic: ComicAndChapters) : AddComicResult()
        data class Failure(
            override val comic: ComicAndChapters,
            val exception: Exception
        ) : AddComicResult()
    }

    suspend fun getComicAndChapters(comicId: Long): ComicAndChapters =
        databaseSource.getComicAndChapters(comicId)

    suspend fun loadComic(comicUrl: String): ComicAndChapters {
        val remoteComic = remoteSource.loadComic(comicUrl)
        val comic = remoteComic.toComic()
        val chapters =
            remoteComic.chapters.mapIndexed { index, chapter -> chapter.toChapter(order = index) }
        return comic to chapters
    }

    private fun RemoteSource.Comic.toComic() = Comic(
        title = title,
        description = description,
        homeUrl = homeUrl,
        coverImageUrl = coverImageUrl,
        id = -1,
        readLastAt = null,
        addedAt = Clock.System.now()
    )

    private fun RemoteSource.Chapter.toChapter(order: Int) = Chapter(
        title = title,
        releaseDate = releaseDate,
        url = url,
        orderIndex = order.toLong(),
        id = -1,
        comicId = -1,
        numberOfPages = 0
    )

    suspend fun loadPage(chapter: Chapter, pageNumber: Long): Page {
        val existingPage = databaseSource.getPage(chapter, pageNumber)
        if (chapter.numberOfPages != 0L && existingPage != null) return existingPage

        Napier.e { "started loading remote page $pageNumber for ${chapter.title} " }
        val remotePage = remoteSource.loadPage(chapter.url, pageNumber)
        databaseSource.updateNumberOfPages(chapter, remotePage.listOfPagesInChapter.size)
        if (existingPage != null) return existingPage
        val newPage = databaseSource.addPage(
            chapter = chapter,
            page = remotePage.toPage(chapter)
        )
        return newPage
    }

    private fun RemoteSource.Page.toPage(chapter: Chapter) = Page(
        pageNumber = pageNumber,
        imageUrl = imageUrl,
        chapterId = chapter.id,
        id = -1
    )

    suspend fun getChapter(id: Long): Chapter = databaseSource.getChapter(id)

    suspend fun wipeData() {
        databaseSource.wipeData()
    }

    companion object {
        fun dummyComics() = listOf(
            "Kaiju No. 8" to "https://www.mangatown.com/manga/kaiju_no_8/",
            "Fairy Tail" to "https://www.mangatown.com/manga/fairy_tail/",
            "Naruto" to "https://www.mangatown.com/manga/naruto/"
        )
    }
}
