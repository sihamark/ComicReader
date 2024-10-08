package eu.heha.cyclone.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.toArgb
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePainter.State.Loading
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import coil3.compose.LocalPlatformContext
import coil3.compose.asPainter
import coil3.test.FakeImage

@OptIn(ExperimentalCoilApi::class)
@Composable
fun DefaultAsyncImagePreviewHandler(content: @Composable () -> Unit) {
    val platformContext = LocalPlatformContext.current
    val primaryColor = MaterialTheme.colorScheme.primary
    CompositionLocalProvider(LocalAsyncImagePreviewHandler provides AsyncImagePreviewHandler { _, _ ->
        Loading(FakeImage(color = primaryColor.toArgb()).asPainter(platformContext))
    }, content = content)
}