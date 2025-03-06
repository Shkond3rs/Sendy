package shkonda.sendy.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Основные цвета бренда Sendy
private val SendyBlue = Color(0xFF0052CC) // Основной синий цвет
private val SendyLightBlue = Color(0xFF4C9AFF) // Светло-синий цвет
private val SendyDarkBlue = Color(0xFF0747A6) // Темно-синий цвет
private val SendyWhite = Color.White

// Темная тема
private val DarkColorScheme = darkColorScheme(
    primary = SendyLightBlue,
    onPrimary = Color.Black,
    primaryContainer = SendyBlue,
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFB8C8EA),
    onSecondary = Color(0xFF253048),
    secondaryContainer = Color(0xFF3B475F),
    onSecondaryContainer = Color(0xFFD6E3FF),
    tertiary = Color(0xFFC3C5D0),
    onTertiary = Color(0xFF2E3041),
    tertiaryContainer = Color(0xFF444658),
    onTertiaryContainer = Color(0xFFDFE1EC),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1A1C22),
    onBackground = Color(0xFFE3E2E6),
    surface = Color(0xFF121318),
    onSurface = Color(0xFFE3E2E6),
    surfaceVariant = Color(0xFF44474F),
    onSurfaceVariant = Color(0xFFC5C6D0),
    outline = Color(0xFF8E9099)
)

// Светлая тема
private val LightColorScheme = lightColorScheme(
    primary = SendyBlue,
    onPrimary = SendyWhite,
    primaryContainer = SendyLightBlue,
    onPrimaryContainer = Color.White,
    secondary = SendyDarkBlue,
    onSecondary = SendyWhite,
    secondaryContainer = Color(0xFFDCE8FF),
    onSecondaryContainer = SendyDarkBlue,
    tertiary = Color(0xFF5C616F),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE1E5F9),
    onTertiaryContainer = Color(0xFF1A1C22),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onError = Color.White,
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF8F9FC),
    onBackground = Color(0xFF1A1C22),
    surface = Color.White,
    onSurface = Color(0xFF1A1C22),
    surfaceVariant = Color(0xFFE7E7EC),
    onSurfaceVariant = Color(0xFF44474F),
    outline = Color(0xFF74777F)
)

@Composable
fun SendyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}