package de.hamark.comicreader.model

class ComicRepository {

    suspend fun loadPage(url: String = INITIAL_PAGE): PageParseController.Page {
        val controller = PageParseController()
        return controller.parseWebPage(url)
    }

    companion object {
        private const val INITIAL_PAGE = "https://comiconlinefree.net/the-walking-dead/issue-1/1"
    }
}