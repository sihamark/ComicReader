package eu.heha.cyclone.ui

import eu.heha.cyclone.database.Chapter
import eu.heha.cyclone.database.Comic
import eu.heha.cyclone.model.ComicAndChapters
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate

object Dummy {
    fun comic() = Comic(
        title = "Comic",
        description = "Description",
        id = -1L,
        homeUrl = "https://example.com",
        coverImageUrl = "https://example.com/cover.jpg",
        addedAt = Clock.System.now(),
        readLastAt = Clock.System.now(),
    )

    fun chapter() = Chapter(
        title = "Chapter",
        comicId = -1L,
        id = -1L,
        url = "https://example.com/chapter",
        numberOfPages = 20,
        releaseDate = LocalDate.fromEpochDays(50),
        orderIndex = 1
    )

    fun comicAndChapters(): ComicAndChapters = comic() to listOf(chapter())
}