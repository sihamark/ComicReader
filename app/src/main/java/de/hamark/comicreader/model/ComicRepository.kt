package de.hamark.comicreader.model

import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.jsoup.Jsoup
import javax.inject.Inject

class ComicRepository @Inject constructor(
    private val httpClient: HttpClient
) {

    suspend fun loadComic(url: String = DEFAULT_COMIC_URL): Comic {
        val content = httpClient.get(url).body<String>()
        val document = Jsoup.parse(content)
        val chapterListElements = document.select("ul.chapter_list > li")
        Napier.e { "got ${chapterListElements.count()} chapters" }
        val chapters = chapterListElements.reversed().mapIndexed { chapterIndex, chapter ->
            val link = chapter.select("a")
            val chapterName = link.text()
            val chapterUrl = URLBuilder(url).apply {
                path()
                appendPathSegments(link.attr("href"))
            }.buildString()
            val pages = loadPages(chapterUrl)
            Napier.e { "chapter $chapterIndex $chapterName, url: $chapterUrl, pages: ${pages.size}" }
            Chapter(chapterName, chapterUrl, pages)
        }
        Napier.e { "chapters: $chapters" }
        TODO("not yet implemented")
    }

    private suspend fun loadPages(
        chapterUrl: String,
    ): List<Page> {
        suspend fun loadPage(page: Int): Page? {
            val pageUrl = URLBuilder(chapterUrl).apply {
                appendPathSegments("${page}.html")
            }.buildString()
            val response = httpClient.get(pageUrl)
            if (response.status == HttpStatusCode.NotFound) {
                return null
            }
            val pageDocument = Jsoup.parse(response.body<String>())
            val imageUrl = pageDocument.select("div#viewer img").attr("src")
            return Page(imageUrl)
        }

        var currentPage = 1
        val pages = mutableListOf<Page>()
        var page: Page? = loadPage(currentPage)

        while (page != null) {
            pages.add(page)
            page = loadPage(++currentPage)
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
        val url: String,
        val pages: List<Page>
    )

    data class Page(
        val imageUrl: String
    )

    companion object {
        private const val DEFAULT_COMIC_URL = "https://www.mangatown.com/manga/kaiju_no_8/"
    }
}