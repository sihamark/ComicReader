package de.hamark.comicreader.model

import com.fleeksoft.ksoup.Ksoup
import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import javax.inject.Inject

class ComicRepository @Inject constructor(
    private val httpClient: HttpClient
) {

    suspend fun loadComic(url: String = DEFAULT_COMIC_URL): Comic {
        val content = httpClient.get(url).body<String>()
        val document = Ksoup.parse(content)
        val comicTitle = document.select("head meta[property=og:title]").attr("content")
        val chapterListElements = document.select("ul.chapter_list > li")
        Napier.e { "got ${chapterListElements.count()} chapters" }
        val chapters = chapterListElements.reversed()
            .map { chapter ->
                val link = chapter.select("a")
                val chapterName = link.text()
                val chapterUrl = URLBuilder(url).apply {
                    path()
                    appendPathSegments(link.attr("href"))
                }.buildString()
                Chapter(chapterName, chapterUrl)
            }
        Napier.e { "chapters: ${chapters.size}" }
        return Comic(
            title = comicTitle,
            homeUrl = url,
            chapters = chapters
        )
    }

    suspend fun loadPage(chapterUrl: String, page: Int): Page? {
        val pageUrl = getPageUrl(chapterUrl, page)
        val response = httpClient.get(pageUrl)
        if (response.status == HttpStatusCode.NotFound) {
            return null
        }
        val pageDocument = Ksoup.parse(response.body<String>())
        val imageUrl = pageDocument.select("div#viewer img").attr("src")

        Napier.e { "page url: $pageUrl image url: $imageUrl" }

        return Page(imageUrl)
    }

    fun getPageUrl(chapterUrl: String, page: Int) = URLBuilder(chapterUrl).apply {
        appendPathSegments("${page + 1}.html")
    }.buildString()

    private suspend fun loadPages(
        chapterUrl: String,
    ): List<Page> {

        var currentPage = 1
        val pages = mutableListOf<Page>()
        var page: Page? = loadPage(chapterUrl, currentPage)

        while (page != null) {
            pages.add(page)
            page = loadPage(chapterUrl, ++currentPage)
        }

        return pages
    }

    data class Comic(
        val title: String,
        val homeUrl: String,
        val chapters: List<Chapter>
    )

    data class Chapter(
        val title: String,
        val url: String
    )

    data class Page(
        val imageUrl: String,
    )

    companion object {
        private const val DEFAULT_COMIC_URL = "https://www.mangatown.com/manga/kaiju_no_8/"
    }
}