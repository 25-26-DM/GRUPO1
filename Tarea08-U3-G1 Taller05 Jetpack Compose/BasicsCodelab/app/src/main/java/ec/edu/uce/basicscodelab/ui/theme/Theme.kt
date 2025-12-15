package ec.edu.uce.basicscodelab.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = LightBlue,
    onPrimary = Navy,
    secondary = Blue,
    onSecondary = White,
    background = Navy,
    onBackground = OffWhite,
    surface = Navy,
    onSurface = OffWhite,
    surfaceVariant = Navy, // Color for Cards
    onSurfaceVariant = White
)

private val LightColorScheme = lightColorScheme(
    primary = Navy,
    onPrimary = White,
    secondary = Blue,
    onSecondary = White,
    background = OffWhite,
    onBackground = Navy,
    surface = OffWhite, 
    onSurface = Navy,
    surfaceVariant = White, // Color for Cards
    onSurfaceVariant = Navy
)

@Composable
fun BasicsCodelabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
