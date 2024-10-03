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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ComicRepository(private val httpClient: HttpClient) {

    private val _comics = MutableStateFlow(emptyList<Comic>())
    val comics: StateFlow<List<Comic>> = _comics.asStateFlow()

    suspend fun loadComic(url: String): Comic {
        val content = httpClient.get(url).body<String>()
        val document = Ksoup.parse(content)
        val comicTitle = document.select("head meta[property=og:title]").attr("content")
        val chapterListElements = document.select("ul.chapter_list > li")
        val coverImageUrl = document.select("div.detail_info.clearfix img").first()?.attr("src")
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
        if (chapters.isEmpty()) {
            error("no chapters found")
        }
        Napier.e { "chapters: ${chapters.size}" }
        return Comic(
            title = comicTitle,
            coverImageUrl = coverImageUrl ?: error("cover image not found"),
            homeUrl = url,
            chapters = chapters
        )
    }

    suspend fun loadPage(chapterUrl: String, page: Int = INITIAL_PAGE): Page? {
        check(page >= INITIAL_PAGE) { "page must be >= $INITIAL_PAGE" }
        val pageUrl = getPageUrl(chapterUrl, page)
        val response = httpClient.get(pageUrl)
        if (response.status == HttpStatusCode.NotFound) {
            return null
        }
        val pageDocument = Ksoup.parse(response.body<String>())
        val imageUrl = pageDocument.select("div#viewer img").attr("src")

        val pageIndices = pageDocument.select("div.page_select option")
            .mapNotNull { it.text().toIntOrNull() }
            .distinct()

        Napier.d { "page url: $pageUrl image url: $imageUrl, indices(${pageIndices.size}): $pageIndices" }

        return Page(
            pageIndex = page,
            imageUrl = URLBuilder(imageUrl)
                .apply { protocol = URLProtocol.HTTPS }
                .buildString(),
            listOfPagesInChapter = pageIndices
        )
    }

    private fun getPageUrl(chapterUrl: String, page: Int) = URLBuilder(chapterUrl).apply {
        appendPathSegments("$page.html")
    }.buildString()

    fun addComic(previewComic: Comic) {
        _comics.value = (_comics.value + previewComic).distinct()
    }

    fun getComic(comicId: String): Comic =
        _comics.value.find { it.id == comicId } ?: error("no comic with id '$comicId' found")

    data class Comic(
        val title: String,
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
        val url: String
    )

    data class Page(
        val pageIndex: Int,
        val imageUrl: String,
        val listOfPagesInChapter: List<Int>
    )

    companion object {
        fun dummyComics() = listOf(
            "Kaiju No. 8" to "https://www.mangatown.com/manga/kaiju_no_8/",
            "Fairy Tail" to "https://www.mangatown.com/manga/fairy_tail/",
            "Naruto" to "https://www.mangatown.com/manga/naruto/"
        )

        const val INITIAL_PAGE = 1

        fun imageHeader(comicUrl: String): Pair<String, String> =
            HttpHeaders.Referrer to URLBuilder(comicUrl).apply { path() }.buildString()

        fun ImageRequest.Builder.addComicHeader(comicUrl: String) = apply {
            val (name, value) = imageHeader(comicUrl)
            httpHeaders(NetworkHeaders.Builder().set(name, value).build())
        }
    }
}