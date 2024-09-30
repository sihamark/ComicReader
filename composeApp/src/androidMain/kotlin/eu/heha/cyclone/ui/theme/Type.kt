package eu.heha.cyclone.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import eu.heha.cyclone.R


@OptIn(ExperimentalTextApi::class)
val bodyFontFamily = FontFamily(
    Font(
        resId = R.font.fredoka,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(400),
            FontVariation.width(100f),
        )
    )
)

val displayFontFamily = FontFamily(
    Font(
        resId = R.font.kanit_black,
        weight = FontWeight.Black
    ),
    Font(
        resId = R.font.kanit_blackitalic,
        weight = FontWeight.Black,
        style = FontStyle.Italic
    ),
    Font(
        resId = R.font.kanit_bold,
        weight = FontWeight.Bold
    ),
    Font(
        resId = R.font.kanit_bolditalic,
        weight = FontWeight.Bold,
        style = FontStyle.Italic
    ),
    Font(
        resId = R.font.kanit_extrabold,
        weight = FontWeight.ExtraBold
    ),
    Font(
        resId = R.font.kanit_extrabolditalic,
        weight = FontWeight.ExtraBold,
        style = FontStyle.Italic
    ),
    Font(
        resId = R.font.kanit_extralight,
        weight = FontWeight.Light
    ),
    Font(
        resId = R.font.kanit_extralightitalic,
        weight = FontWeight.Light,
        style = FontStyle.Italic
    ),
    Font(
        resId = R.font.kanit_italic,
        style = FontStyle.Italic
    ),
    Font(
        resId = R.font.kanit_light,
        weight = FontWeight.Light
    ),
    Font(
        resId = R.font.kanit_lightitalic,
        weight = FontWeight.Light,
        style = FontStyle.Italic
    ),
    Font(
        resId = R.font.kanit_medium,
        weight = FontWeight.Medium
    ),
    Font(
        resId = R.font.kanit_mediumitalic,
        weight = FontWeight.Medium,
        style = FontStyle.Italic
    ),
    Font(
        resId = R.font.kanit_regular
    ),
    Font(
        resId = R.font.kanit_semibold,
        weight = FontWeight.SemiBold
    ),
    Font(
        resId = R.font.kanit_semibolditalic,
        weight = FontWeight.SemiBold,
        style = FontStyle.Italic
    ),
    Font(
        resId = R.font.kanit_thin,
        weight = FontWeight.Thin
    ),
    Font(
        resId = R.font.kanit_thinitalic,
        weight = FontWeight.Thin,
        style = FontStyle.Italic
    )
)

// Default Material 3 typography values
val baseline = Typography()

val AppTypography = Typography(
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

