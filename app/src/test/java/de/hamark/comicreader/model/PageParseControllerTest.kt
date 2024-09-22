package de.hamark.comicreader.model

import io.github.aakira.napier.Napier
import kotlinx.coroutines.runBlocking
import org.junit.BeforeClass
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PageParseControllerTest {

    @Test
    fun testParseWebPage() = runBlocking {
        val pageParseController = PageParseController()
        val result =
            pageParseController.parseWebPage("https://comiconlinefree.net/the-walking-dead/issue-1/1")
        assertEquals("https://comiconlinefree.net/the-walking-dead/issue-1/2", result.nextPageUrl)
        assertTrue(result.imageUrl.endsWith("jpg") || result.imageUrl.endsWith("png"))
    }

    companion object {
        @JvmStatic
        @BeforeClass
        fun setup() {
            Napier.base(DebugAntilog())
        }
    }
}