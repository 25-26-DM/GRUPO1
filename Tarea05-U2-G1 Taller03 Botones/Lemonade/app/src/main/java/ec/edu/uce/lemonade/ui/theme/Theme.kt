package ec.edu.uce.lemonade.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import ec.edu.uce.lemonade.ui.theme.Shapes
import ec.edu.uce.lemonade.ui.theme.Typography

private val DarkColorScheme = darkColorScheme(
    primary = Green,
    onPrimary = DarkGreen,
    primaryContainer = Brown,
    onPrimaryContainer = White,
    secondary = Green,
    onSecondary = DarkGreen,
    secondaryContainer = Brown,
    onSecondaryContainer = White,
    tertiary = Yellow,
    onTertiary = DarkGreen,
    tertiaryContainer = Brown,
    onTertiaryContainer = White,
    background = Black,
    onBackground = White,
    surface = Black,
    onSurface = White,
    surfaceVariant = DarkGreen,
    onSurfaceVariant = LightGreen,
    outline = Green
)

private val LightColorScheme = lightColorScheme(
    primary = Yellow,
    onPrimary = DarkGreen,
    primaryContainer = Green,
    onPrimaryContainer = DarkGreen,
    secondary = Yellow,
    onSecondary = DarkGreen,
    secondaryContainer = Green,
    onSecondaryContainer = DarkGreen,
    tertiary = Yellow,
    onTertiary = DarkGreen,
    tertiaryContainer = Green,
    onTertiaryContainer = DarkGreen,
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    surfaceVariant = LightGreen,
    onSurfaceVariant = DarkGreen,
    outline = Green
)

@Composable
fun LemonadeTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
