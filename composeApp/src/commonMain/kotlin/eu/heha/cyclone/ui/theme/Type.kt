package eu.heha.cyclone.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import cyclone.composeapp.generated.resources.Res
import cyclone.composeapp.generated.resources.fredoka_bold
import cyclone.composeapp.generated.resources.fredoka_light
import cyclone.composeapp.generated.resources.fredoka_medium
import cyclone.composeapp.generated.resources.fredoka_regular
import cyclone.composeapp.generated.resources.fredoka_semibold
import cyclone.composeapp.generated.resources.kanit_black
import cyclone.composeapp.generated.resources.kanit_blackitalic
import cyclone.composeapp.generated.resources.kanit_bold
import cyclone.composeapp.generated.resources.kanit_bolditalic
import cyclone.composeapp.generated.resources.kanit_extrabold
import cyclone.composeapp.generated.resources.kanit_extrabolditalic
import cyclone.composeapp.generated.resources.kanit_extralight
import cyclone.composeapp.generated.resources.kanit_extralightitalic
import cyclone.composeapp.generated.resources.kanit_italic
import cyclone.composeapp.generated.resources.kanit_light
import cyclone.composeapp.generated.resources.kanit_lightitalic
import cyclone.composeapp.generated.resources.kanit_medium
import cyclone.composeapp.generated.resources.kanit_mediumitalic
import cyclone.composeapp.generated.resources.kanit_regular
import cyclone.composeapp.generated.resources.kanit_semibold
import cyclone.composeapp.generated.resources.kanit_semibolditalic
import cyclone.composeapp.generated.resources.kanit_thin
import cyclone.composeapp.generated.resources.kanit_thinitalic
import org.jetbrains.compose.resources.Font


@Composable
fun bodyFontFamily() = FontFamily(
    Font(
        resource = Res.font.fredoka_regular,
        weight = FontWeight.Normal
    ),
    Font(
        resource = Res.font.fredoka_light,
        weight = FontWeight.Light
    ),
    Font(
        resource = Res.font.fredoka_bold,
        weight = FontWeight.Bold
    ),
    Font(
        resource = Res.font.fredoka_medium,
        weight = FontWeight.Medium
    ),
    Font(
        resource = Res.font.fredoka_semibold,
        weight = FontWeight.SemiBold
    ),
    Font(
        resource = Res.font.fredoka_semibold,
        weight = FontWeight.SemiBold
    ),
)

@Composable
fun displayFontFamily() = FontFamily(
    Font(
        resource = Res.font.kanit_black,
        weight = FontWeight.Black
    ),
    Font(
        resource = Res.font.kanit_blackitalic,
        weight = FontWeight.Black,
        style = FontStyle.Italic
    ),
    Font(
        resource = Res.font.kanit_bold,
        weight = FontWeight.Bold
    ),
    Font(
        resource = Res.font.kanit_bolditalic,
        weight = FontWeight.Bold,
        style = FontStyle.Italic
    ),
    Font(
        resource = Res.font.kanit_extrabold,
        weight = FontWeight.ExtraBold
    ),
    Font(
        resource = Res.font.kanit_extrabolditalic,
        weight = FontWeight.ExtraBold,
        style = FontStyle.Italic
    ),
    Font(
        resource = Res.font.kanit_extralight,
        weight = FontWeight.Light
    ),
    Font(
        resource = Res.font.kanit_extralightitalic,
        weight = FontWeight.Light,
        style = FontStyle.Italic
    ),
    Font(
        resource = Res.font.kanit_italic,
        style = FontStyle.Italic
    ),
    Font(
        resource = Res.font.kanit_light,
        weight = FontWeight.Light
    ),
    Font(
        resource = Res.font.kanit_lightitalic,
        weight = FontWeight.Light,
        style = FontStyle.Italic
    ),
    Font(
        resource = Res.font.kanit_medium,
        weight = FontWeight.Medium
    ),
    Font(
        resource = Res.font.kanit_mediumitalic,
        weight = FontWeight.Medium,
        style = FontStyle.Italic
    ),
    Font(
        resource = Res.font.kanit_regular
    ),
    Font(
        resource = Res.font.kanit_semibold,
        weight = FontWeight.SemiBold
    ),
    Font(
        resource = Res.font.kanit_semibolditalic,
        weight = FontWeight.SemiBold,
        style = FontStyle.Italic
    ),
    Font(
        resource = Res.font.kanit_thin,
        weight = FontWeight.Thin
    ),
    Font(
        resource = Res.font.kanit_thinitalic,
        weight = FontWeight.Thin,
        style = FontStyle.Italic
    )
)

@Composable
fun appTypography(): Typography {
    // Default Material 3 typography values
    val baseline = Typography()
    val displayFontFamily = displayFontFamily()
    val bodyFontFamily = bodyFontFamily()
    return Typography(
        displayLarge = baseline.displayLarge.copy(fontFamily = displayFontFamily),
        displayMedium = baseline.displayMedium.copy(fontFamily = displayFontFamily),
        displaySmall = baseline.displaySmall.copy(fontFamily = displayFontFamily),
        headlineLarge = baseline.headlineLarge.copy(fontFamily = displayFontFamily),
        headlineMedium = baseline.headlineMedium.copy(fontFamily = displayFontFamily),
        headlineSmall = baseline.headlineSmall.copy(fontFamily = displayFontFamily),
        titleLarge = baseline.titleLarge.copy(fontFamily = displayFontFamily),
        titleMedium = baseline.titleMedium.copy(fontFamily = displayFontFamily),
        titleSmall = baseline.titleSmall.copy(fontFamily = displayFontFamily),
        bodyLarge = baseline.bodyLarge.copy(fontFamily = bodyFontFamily),
        bodyMedium = baseline.bodyMedium.copy(fontFamily = bodyFontFamily),
        bodySmall = baseline.bodySmall.copy(fontFamily = bodyFontFamily),
        labelLarge = baseline.labelLarge.copy(fontFamily = bodyFontFamily),
        labelMedium = baseline.labelMedium.copy(fontFamily = bodyFontFamily),
        labelSmall = baseline.labelSmall.copy(fontFamily = bodyFontFamily),
    )
}

