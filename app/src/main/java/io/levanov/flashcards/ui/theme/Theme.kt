package io.levanov.flashcards.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Swedish flag colors
private val SwedishBlue = Color(0xFF006AA7)
private val SwedishYellow = Color(0xFFFECC02)

private val LightColors = lightColorScheme(
    primary = SwedishBlue,
    secondary = SwedishYellow,
)

private val DarkColors = darkColorScheme(
    primary = SwedishBlue,
    secondary = SwedishYellow,
)

@Composable
fun FlashcardsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}