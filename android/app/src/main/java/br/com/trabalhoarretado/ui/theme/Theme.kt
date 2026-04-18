package br.com.trabalhoarretado.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = NavyDark,
    onPrimary = OnNavy,
    primaryContainer = NavyDarkVariant,
    secondary = OrangeRed,
    onSecondary = OnOrangeRed,
    secondaryContainer = OrangeRedDark,
    background = SurfaceLight,
    surface = SurfaceLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = NavyMedium,
    onPrimary = OnNavy,
    primaryContainer = NavyDark,
    secondary = OrangeRed,
    onSecondary = OnOrangeRed,
    secondaryContainer = OrangeRedDark,
)

@Composable
fun TrabalhoArretadoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
