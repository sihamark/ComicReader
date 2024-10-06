package eu.heha.cyclone.model

import eu.heha.cyclone.database.Chapter
import eu.heha.cyclone.database.Comic
import eu.heha.cyclone.model.database.DatabaseSource
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

typealias ComicAndChapters = Pair<Comic, List<Chapter>>

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
        try {
            val comicId = databaseSource.addComic(previewComic)
            return AddComicResult.Success(previewComic, comicId)
        } catch (e: Exception) {
            return AddComicResult.Failure(previewComic, e)
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

    suspend fun getComic(comicId: String): ComicAndChapters = TODO()
    //_comics.value.find { it.id == comicId } ?: error("no comic with id '$comicId' found")

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

    companion object {
        fun dummyComics() = listOf(
            "Kaiju No. 8" to "https://www.mangatown.com/manga/kaiju_no_8/",
            "Fairy Tail" to "https://www.mangatown.com/manga/fairy_tail/",
            "Naruto" to "https://www.mangatown.com/manga/naruto/"
        )
    }

    data class ComicWithChapterCount(
        val value: Comic,
        val chapterCount: Long
    )
}