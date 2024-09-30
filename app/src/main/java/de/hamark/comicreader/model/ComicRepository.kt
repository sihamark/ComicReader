package de.hamark.comicreader.model

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.fleeksoft.ksoup.Ksoup
import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ComicRepository @Inject constructor(
    private val httpClient: HttpClient
) {

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

    suspend fun loadImage(comicUrl: String, imageUrl: String): ImageBitmap {
        val response = httpClient.get {
            url(imageUrl)
            headers {
                val (name, value) = imageHeader(comicUrl)
                append(name, value)
            }
        }
        if (!response.status.isSuccess()) {
            error("failed to load image: $imageUrl, status: ${response.status}")
        }
        val bytes = response.bodyAsChannel().toByteArray()
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap()
    }

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
    }
}