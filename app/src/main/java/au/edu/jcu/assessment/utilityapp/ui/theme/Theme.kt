package au.edu.jcu.assessment.utilityapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/*
 * "Clear sky" Material 3 colour scheme: a calm teal primary with cool neutral surfaces,
 * so the coloured AQI and UV levels are what stand out.
 */

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00687A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFACEDFF),
    onPrimaryContainer = Color(0xFF001F26),
    secondary = Color(0xFF4B6268),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCEE7EE),
    onSecondaryContainer = Color(0xFF061F24),
    tertiary = Color(0xFF565D7E),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDDE1FF),
    onTertiaryContainer = Color(0xFF131937),
    background = Color(0xFFF5FAFC),
    onBackground = Color(0xFF171D1F),
    surface = Color(0xFFF5FAFC),
    onSurface = Color(0xFF171D1F),
    surfaceVariant = Color(0xFFDBE4E7),
    onSurfaceVariant = Color(0xFF3F484B),
    outline = Color(0xFF6F797B),
    outlineVariant = Color(0xFFBFC8CB),
    surfaceDim = Color(0xFFD5DBDD),
    surfaceBright = Color(0xFFF5FAFC),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFEFF4F6),
    surfaceContainer = Color(0xFFE9EFF1),
    surfaceContainerHigh = Color(0xFFE3E9EB),
    surfaceContainerHighest = Color(0xFFDEE3E5)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF84D2E6),
    onPrimary = Color(0xFF003640),
    primaryContainer = Color(0xFF004E5C),
    onPrimaryContainer = Color(0xFFACEDFF),
    secondary = Color(0xFFB2CBD2),
    onSecondary = Color(0xFF1C3439),
    secondaryContainer = Color(0xFF334A50),
    onSecondaryContainer = Color(0xFFCEE7EE),
    tertiary = Color(0xFFBFC4EB),
    onTertiary = Color(0xFF282F4D),
    tertiaryContainer = Color(0xFF3F4565),
    onTertiaryContainer = Color(0xFFDDE1FF),
    background = Color(0xFF0E1416),
    onBackground = Color(0xFFDEE3E5),
    surface = Color(0xFF0E1416),
    onSurface = Color(0xFFDEE3E5),
    surfaceVariant = Color(0xFF3F484B),
    onSurfaceVariant = Color(0xFFBFC8CB),
    outline = Color(0xFF899295),
    outlineVariant = Color(0xFF3F484B),
    surfaceDim = Color(0xFF0E1416),
    surfaceBright = Color(0xFF343A3C),
    surfaceContainerLowest = Color(0xFF090F11),
    surfaceContainerLow = Color(0xFF171D1F),
    surfaceContainer = Color(0xFF1B2123),
    surfaceContainerHigh = Color(0xFF252B2D),
    surfaceContainerHighest = Color(0xFF303638)
)

@Composable
fun MobileTech_UtilityAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Off by default so the app keeps its own calm teal look on every phone
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
