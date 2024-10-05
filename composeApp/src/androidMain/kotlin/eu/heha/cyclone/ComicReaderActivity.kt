package eu.heha.cyclone

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import eu.heha.cyclone.ui.Root

class ComicReaderActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.auto(
                Color.Transparent.toArgb(),
                Color.Transparent.toArgb()
            )
        )
        setContent {
            Root(
                colorSchemeOverride = { isDarkTheme ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (isDarkTheme) {
                            dynamicDarkColorScheme(this)
                        } else {
                            dynamicLightColorScheme(this)
                        }
                    } else {
                        null
                    }
                }
            )
        }
    }
}