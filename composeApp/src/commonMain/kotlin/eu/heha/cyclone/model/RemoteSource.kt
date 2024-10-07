package eu.heha.cyclone.model

import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import com.fleeksoft.ksoup.Ksoup
import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char

class RemoteSource(
    private val httpClient: HttpClient
) {
    suspend fun loadComic(url: String): Comic = withContext(Dispatchers.IO) {
        val content = httpClient.get(url).body<String>()
        val document = Ksoup.parse(content)

        val comicTitle = document.select("head meta[property=og:title]").attr("content")
        val comicDescription = document.select("head meta[property=og:description]").attr("content")
        val coverImageUrl = document.select("div.detail_info.clearfix img").first()?.attr("src")

        val chapterListElements = document.select("ul.chapter_list > li")
        val chapters = chapterListElements.reversed().map { chapter ->
            val link = chapter.select("a")
            val chapterName = link.text()
            val chapterUrl = URLBuilder(url).apply {
                path()
                appendPathSegments(link.attr("href"))
            }.buildString()

            val releaseDateRaw = chapter.select("span.time").text()
            val releaseDate = LocalDate.parse(releaseDateRaw, LocalDate.Format {
                //format for 'Sep 01,2023'
                monthName(MonthNames.ENGLISH_ABBREVIATED)
                char(' ')
                dayOfMonth(padding = Padding.ZERO)
                char(',')
                year()
            })

            Chapter(
                title = chapterName,
                releaseDate = releaseDate,
                url = chapterUrl
            )
        }
        if (chapters.isEmpty()) {
            error("no chapters found")
        }
        Napier.d { "chapters: ${chapters.size}" }
        return@withContext Comic(
            title = comicTitle,
            description = comicDescription,
            coverImageUrl = coverImageUrl ?: error("cover image not found"),
            homeUrl = url,
            chapters = chapters
        )
    }

    suspend fun loadPage(chapterUrl: String, page: Long = INITIAL_PAGE): Page {
        check(page >= INITIAL_PAGE) { "page must be >= $INITIAL_PAGE" }
        val pageUrl = getPageUrl(chapterUrl, page)
        val response = httpClient.get(pageUrl)
        val pageDocument = Ksoup.parse(response.body<String>())
        val imageUrl = pageDocument.select("div#viewer img").attr("src")

        val pageIndices = pageDocument.select("div.page_select option")
            .mapNotNull { it.text().toIntOrNull() }
            .distinct()

        Napier.d { "page url: $pageUrl image url: $imageUrl, indices(${pageIndices.size})" }

        return Page(
            pageNumber = page,
            imageUrl = URLBuilder(imageUrl)
                .apply { protocol = URLProtocol.HTTPS }
                .buildString(),
            listOfPagesInChapter = pageIndices
        )
    }

    private fun getPageUrl(chapterUrl: String, page: Long) = URLBuilder(chapterUrl).apply {
        appendPathSegments("$page.html")
    }.buildString()

    data class Comic(
        val title: String,
        val description: String,
        val coverImageUrl: String,
        val homeUrl: String,
        val chapters: List<Chapter>
    ) {
        val id = homeUrl.replace("/", "")
            .replace(":", "")
            .replace(".", "")
    }

    data class Chapter(
        val title: String,
        val releaseDate: LocalDate,
        val url: String
    )

    data class Page(
        val pageNumber: Long,
        val imageUrl: String,
        val listOfPagesInChapter: List<Int>
    )

    companion object {
        const val INITIAL_PAGE = 1L

        private fun imageHeader(comicUrl: String): Pair<String, String> =
            HttpHeaders.Referrer to URLBuilder(comicUrl).apply { path() }.buildString()

        fun ImageRequest.Builder.addComicHeader(comicUrl: String) = apply {
            val (name, value) = imageHeader(comicUrl)
            httpHeaders(NetworkHeaders.Builder().set(name, value).build())
        }
    }
}