package ec.edu.uce.appproductosfinal.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AlpineColorScheme = lightColorScheme(
    primary = AzureMist,
    secondary = DeepSpace,
    background = AlpineWhite,
    surface = SurfaceGray,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = DeepSpace,
    onSurface = DeepSpace,
    primaryContainer = SilkGray,
    onPrimaryContainer = AzureMist,
    outline = BorderGray
)

@Composable
fun AppProductosTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AlpineColorScheme,
        typography = Typography,
        content = content
    )
}
