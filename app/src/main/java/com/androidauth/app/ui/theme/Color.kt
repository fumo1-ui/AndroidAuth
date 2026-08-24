package com.androidauth.app.ui.theme

import androidx.compose.ui.graphics.Color

// Accent Palettes for Material 3 Expressive
enum class AccentColor(val displayName: String, val previewColor: Color, val isDynamic: Boolean = false) {
    MATERIAL_YOU("Material You", Color(0xFF4285F4), true),
    DEFAULT("Steam Blue", Color(0xFF1E88E5)),
    OCEAN("Океан", Color(0xFF00ACC1)),
    EMERALD("Изумруд", Color(0xFF43A047)),
    PURPLE("Фиолетовый", Color(0xFF8E24AA)),
    AMBER("Янтарь", Color(0xFFFB8C00)),
    ROSE("Рубин", Color(0xFFD81B60))
}

// Favorite Star Colors
val StarActiveColor = Color(0xFFFFB800)
val StarInactiveColor = Color(0xFF8B9198)

// TOTP Alert Status Colors (Expressive vibrant indicators)
val TotpNormalColor = Color(0xFF10B981)
val TotpWarningColor = Color(0xFFF59E0B)
val TotpCriticalColor = Color(0xFFEF4444)

// Material 3 Expressive Tonal Surfaces (Dark Palette - Soft, Deep, High-Contrast - NO AMOLED)
val DarkBackground = Color(0xFF0F141C)
val DarkSurface = Color(0xFF0F141C)
val DarkSurfaceContainerLowest = Color(0xFF0A0E14)
val DarkSurfaceContainerLow = Color(0xFF161B23)
val DarkSurfaceContainer = Color(0xFF1D232D)
val DarkSurfaceContainerHigh = Color(0xFF262D39)
val DarkSurfaceContainerHighest = Color(0xFF313947)
val DarkOnSurface = Color(0xFFE4E8F0)
val DarkOnSurfaceVariant = Color(0xFFB8C0CE)
val DarkOutline = Color(0xFF7F8897)

// Material 3 Expressive Tonal Surfaces (Light Palette - Fresh, Crisp, Layered)
val LightBackground = Color(0xFFF6F8FC)
val LightSurface = Color(0xFFF6F8FC)
val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
val LightSurfaceContainerLow = Color(0xFFEFF2F8)
val LightSurfaceContainer = Color(0xFFE7ECF4)
val LightSurfaceContainerHigh = Color(0xFFDFE4ED)
val LightSurfaceContainerHighest = Color(0xFFD6DCE6)
val LightOnSurface = Color(0xFF141922)
val LightOnSurfaceVariant = Color(0xFF424A57)
val LightOutline = Color(0xFF717986)
