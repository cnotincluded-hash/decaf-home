package com.caffeinenotincluded.decafhome.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.caffeinenotincluded.decafhome.R
import com.caffeinenotincluded.decafhome.data.HomeStyle
import com.caffeinenotincluded.decafhome.data.ThemeMode

/** Caffeine Not Included colours. */
object Brand {
    val Ink = Color(0xFF1F2233)
    val InkSoft = Color(0xFF2B2F45)
    val Cream = Color(0xFFFBF3DB)
    val Paper = Color(0xFFFFFDF5)
    val Coral = Color(0xFFF26B6B)
    val Sunshine = Color(0xFFFFD23F)
    val Mint = Color(0xFF5BC07E)
    val Sky = Color(0xFF3D8BFD)
    val Lilac = Color(0xFFC792EA)

    /** Project colours, picked by index. */
    val Accents = listOf(Coral, Sunshine, Mint, Sky, Lilac)
}

@Immutable
data class DecafColors(
    val dark: Boolean,
    val background: Color,
    val dots: Color,
    val onBackground: Color,
    val mutedOnBackground: Color,
    val card: Color,
    val onCard: Color,
    val mutedOnCard: Color,
    val outline: Color,
    /** Wallpaper style: home text sits on the phone's wallpaper, not on brand paper. */
    val minimal: Boolean = false,
    val homeText: Color = onBackground,
    val homeMuted: Color = mutedOnBackground,
)

private val LightDecaf = DecafColors(
    dark = false,
    background = Brand.Cream,
    dots = Brand.Ink.copy(alpha = 0.10f),
    onBackground = Brand.Ink,
    mutedOnBackground = Brand.Ink.copy(alpha = 0.62f),
    card = Brand.Paper,
    onCard = Brand.Ink,
    mutedOnCard = Brand.Ink.copy(alpha = 0.62f),
    outline = Brand.Ink,
)

private val DarkDecaf = DecafColors(
    dark = true,
    background = Brand.Ink,
    dots = Brand.Cream.copy(alpha = 0.08f),
    onBackground = Brand.Cream,
    mutedOnBackground = Brand.Cream.copy(alpha = 0.66f),
    card = Brand.Cream,
    onCard = Brand.Ink,
    mutedOnCard = Brand.Ink.copy(alpha = 0.62f),
    outline = Brand.Ink,
)

val LocalDecafColors = staticCompositionLocalOf { LightDecaf }

@OptIn(ExperimentalTextApi::class)
private fun variable(resId: Int, weight: Int): Font =
    Font(resId, FontWeight(weight), variationSettings = FontVariation.Settings(FontVariation.weight(weight)))

/** Display font: titles, numbers, buttons. */
val Fredoka = FontFamily(
    variable(R.font.fredoka, 400),
    variable(R.font.fredoka, 500),
    variable(R.font.fredoka, 600),
    variable(R.font.fredoka, 700),
)

/** Body font. */
val Nunito = FontFamily(
    variable(R.font.nunito, 400),
    variable(R.font.nunito, 600),
    variable(R.font.nunito, 700),
    variable(R.font.nunito, 800),
)

private fun TextStyle.nunito() = copy(fontFamily = Nunito)
private fun TextStyle.fredoka() = copy(fontFamily = Fredoka)

private val BaseType = Typography()
private val DecafType = Typography(
    displayLarge = BaseType.displayLarge.fredoka(),
    displayMedium = BaseType.displayMedium.fredoka(),
    displaySmall = BaseType.displaySmall.fredoka(),
    headlineLarge = BaseType.headlineLarge.fredoka(),
    headlineMedium = BaseType.headlineMedium.fredoka(),
    headlineSmall = BaseType.headlineSmall.fredoka(),
    titleLarge = BaseType.titleLarge.fredoka(),
    titleMedium = BaseType.titleMedium.fredoka(),
    titleSmall = BaseType.titleSmall.fredoka(),
    bodyLarge = BaseType.bodyLarge.nunito(),
    bodyMedium = BaseType.bodyMedium.nunito(),
    bodySmall = BaseType.bodySmall.nunito(),
    labelLarge = BaseType.labelLarge.nunito(),
    labelMedium = BaseType.labelMedium.nunito(),
    labelSmall = BaseType.labelSmall.nunito(),
)

@Composable
fun DecafTheme(mode: ThemeMode, style: HomeStyle = HomeStyle.CNI, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val base = if (dark) DarkDecaf else LightDecaf
    val decaf = if (style == HomeStyle.MINIMAL) {
        // Light text over a darkened wallpaper, whatever the theme.
        base.copy(minimal = true, homeText = Brand.Cream, homeMuted = Brand.Cream.copy(alpha = 0.78f))
    } else {
        base
    }
    val scheme = if (dark) {
        darkColorScheme(
            primary = Brand.Sunshine,
            onPrimary = Brand.Ink,
            secondary = Brand.Coral,
            onSecondary = Brand.Ink,
            background = Brand.Ink,
            onBackground = Brand.Cream,
            surface = Brand.InkSoft,
            onSurface = Brand.Cream,
            surfaceContainerHigh = Brand.InkSoft,
            onSurfaceVariant = Brand.Cream.copy(alpha = 0.7f),
            outline = Brand.Cream.copy(alpha = 0.5f),
        )
    } else {
        lightColorScheme(
            primary = Brand.Ink,
            onPrimary = Brand.Cream,
            secondary = Brand.Coral,
            onSecondary = Brand.Ink,
            background = Brand.Cream,
            onBackground = Brand.Ink,
            surface = Brand.Paper,
            onSurface = Brand.Ink,
            surfaceContainerHigh = Brand.Paper,
            onSurfaceVariant = Brand.Ink.copy(alpha = 0.7f),
            outline = Brand.Ink.copy(alpha = 0.5f),
        )
    }
    CompositionLocalProvider(LocalDecafColors provides decaf) {
        MaterialTheme(colorScheme = scheme, typography = DecafType, content = content)
    }
}
