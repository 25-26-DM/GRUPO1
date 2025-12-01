package ec.edu.uce.cupcake.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable



private val LightColorScheme = lightColorScheme(
    primary = BlueUCE,
    secondary = RedUCE,
    background = White,
    surface = White,
    onPrimary = White,
    onSecondary = White,
    onBackground = DarkGray,
    onSurface = DarkGray,
    error = RedUCE,
    onError = White,
    errorContainer = PastelRed,
    surfaceVariant = LightGray,
    outline = DarkGray
)

@Composable
fun CupcakeTheme(
    darkTheme: Boolean = false, // Dark theme is disabled
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
