package com.example.plantpal.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val White = Color(0xFFFFFFFF)
val Mist = Color(0xFFEAF7F0)
val Bloom = Color(0xFFFF6F61)
val Moss = Color(0xFF8BC34A)
val Charcoal = Color(0xFF2E2E2E)
val Brown = Color(0xFF6D4C41)
val Sprout = Color(0xFFA5D6A7)
val Plant = Color(0xFF2E7D32)
val Water = Color(0xFF28697C)
val Sunlight = Color(0xFFF4D06F)

val LightColors = lightColorScheme(
    primary = Plant,
    onPrimary = White,
    secondary = Water,
    onSecondary = White,
    tertiary = Sunlight,
    background = Mist,
    onBackground = Charcoal,
    surface = White,
    onSurface = Charcoal,
    error = Bloom,
    onError = White
)

val DarkColors = darkColorScheme(
    primary = Sprout,
    onPrimary = Charcoal,
    secondary = Water,
    onSecondary = White,
    tertiary = Sunlight,
    background = Charcoal,
    onBackground = Mist,
    surface = Brown,
    onSurface = White,
    error = Bloom,
    onError = Charcoal
)

val PlantPalTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
    )
)

@Composable
fun PlantPalTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = PlantPalTypography,
        content = content
    )
}
