package de.hamark.comicreader.model

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class PageParseController {
    suspend fun parseWebPage(url: String): Page = withContext(IO) {
        val response = client.get(url)
        val rawResponse: String = response.body()

        val document = Jsoup.parse(rawResponse)
        val element = document.select("a[href] > img[id=main_img]").first()!!
        val imageUrl = element.attr("src")
        val nextPageUrl = element.parent()!!.attr("href")
        Page(imageUrl, nextPageUrl)
    }

    data class Page(val imageUrl: String, val nextPageUrl: String)

    companion object {
        private val client = HttpClient(CIO)
    }
}