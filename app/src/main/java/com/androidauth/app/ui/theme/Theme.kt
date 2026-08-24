package com.androidauth.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.androidauth.app.data.storage.ThemeMode

private fun createDarkM3ExpressiveColorScheme(primary: Color, secondary: Color): ColorScheme {
    return darkColorScheme(
        primary = primary,
        onPrimary = Color(0xFF003258),
        primaryContainer = primary.copy(alpha = 0.22f),
        onPrimaryContainer = Color(0xFFD1E4FF),
        secondary = secondary,
        onSecondary = Color(0xFF223244),
        secondaryContainer = secondary.copy(alpha = 0.22f),
        onSecondaryContainer = Color(0xFFD6E3FF),
        tertiary = Color(0xFFD0BCFF),
        onTertiary = Color(0xFF381E72),
        tertiaryContainer = Color(0xFF4F378B),
        onTertiaryContainer = Color(0xFFEADDFF),
        background = DarkBackground,
        onBackground = DarkOnSurface,
        surface = DarkSurface,
        onSurface = DarkOnSurface,
        surfaceVariant = DarkSurfaceContainerHigh,
        onSurfaceVariant = DarkOnSurfaceVariant,
        surfaceContainerLowest = DarkSurfaceContainerLowest,
        surfaceContainerLow = DarkSurfaceContainerLow,
        surfaceContainer = DarkSurfaceContainer,
        surfaceContainerHigh = DarkSurfaceContainerHigh,
        surfaceContainerHighest = DarkSurfaceContainerHighest,
        outline = DarkOutline,
        outlineVariant = Color(0xFF424A58)
    )
}

private fun createLightM3ExpressiveColorScheme(primary: Color, secondary: Color): ColorScheme {
    return lightColorScheme(
        primary = primary,
        onPrimary = Color.White,
        primaryContainer = primary.copy(alpha = 0.14f),
        onPrimaryContainer = Color(0xFF001E36),
        secondary = secondary,
        onSecondary = Color.White,
        secondaryContainer = secondary.copy(alpha = 0.14f),
        onSecondaryContainer = Color(0xFF001E36),
        tertiary = Color(0xFF7D5260),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFFFFD8E4),
        onTertiaryContainer = Color(0xFF31111D),
        background = LightBackground,
        onBackground = LightOnSurface,
        surface = LightSurface,
        onSurface = LightOnSurface,
        surfaceVariant = LightSurfaceContainerHigh,
        onSurfaceVariant = LightOnSurfaceVariant,
        surfaceContainerLowest = LightSurfaceContainerLowest,
        surfaceContainerLow = LightSurfaceContainerLow,
        surfaceContainer = LightSurfaceContainer,
        surfaceContainerHigh = LightSurfaceContainerHigh,
        surfaceContainerHighest = LightSurfaceContainerHighest,
        outline = LightOutline,
        outlineVariant = Color(0xFFC0C7D5)
    )
}

@Composable
fun AndroidAuthTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    accentColor: AccentColor = AccentColor.MATERIAL_YOU,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val context = LocalContext.current

    // Material 3 Expressive Dynamic Monet Palette
    val colorScheme = when {
        accentColor == AccentColor.MATERIAL_YOU && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> {
            when (accentColor) {
                AccentColor.MATERIAL_YOU,
                AccentColor.DEFAULT -> createDarkM3ExpressiveColorScheme(Color(0xFF82B1FF), Color(0xFF90CAF9))
                AccentColor.OCEAN -> createDarkM3ExpressiveColorScheme(Color(0xFF4DD0E1), Color(0xFF80DEEA))
                AccentColor.EMERALD -> createDarkM3ExpressiveColorScheme(Color(0xFF81C784), Color(0xFFA5D6A7))
                AccentColor.PURPLE -> createDarkM3ExpressiveColorScheme(Color(0xFFBA68C8), Color(0xFFCE93D8))
                AccentColor.AMBER -> createDarkM3ExpressiveColorScheme(Color(0xFFFFB74D), Color(0xFFFFCC80))
                AccentColor.ROSE -> createDarkM3ExpressiveColorScheme(Color(0xFFF06292), Color(0xFFF48FB1))
            }
        }
        else -> {
            when (accentColor) {
                AccentColor.MATERIAL_YOU,
                AccentColor.DEFAULT -> createLightM3ExpressiveColorScheme(Color(0xFF1E88E5), Color(0xFF0288D1))
                AccentColor.OCEAN -> createLightM3ExpressiveColorScheme(Color(0xFF00ACC1), Color(0xFF0097A7))
                AccentColor.EMERALD -> createLightM3ExpressiveColorScheme(Color(0xFF43A047), Color(0xFF388E3C))
                AccentColor.PURPLE -> createLightM3ExpressiveColorScheme(Color(0xFF8E24AA), Color(0xFF7B1FA2))
                AccentColor.AMBER -> createLightM3ExpressiveColorScheme(Color(0xFFFB8C00), Color(0xFFEF6C00))
                AccentColor.ROSE -> createLightM3ExpressiveColorScheme(Color(0xFFD81B60), Color(0xFFC2185B))
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
