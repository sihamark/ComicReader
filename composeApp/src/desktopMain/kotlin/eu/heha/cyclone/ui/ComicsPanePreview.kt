package eu.heha.cyclone.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import eu.heha.cyclone.ui.theme.CycloneTheme

@Preview
@Composable
fun ComicsPanePreview() {
    CycloneTheme {
        ComicsPane(
            comics = emptyList(),
            onClickAddComic = {},
            onClickComic = {},
            onClickWipeData = {}
        )
    }
}