package com.step.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val STePLightColorScheme = lightColorScheme(
    primary = PrimaryDeepOrange,
    onPrimary = Color.White,
    primaryContainer = PrimarySurfaceLight,
    onPrimaryContainer = PrimaryDeepOrangeDark,
    secondary = StatusInProgress,
    onSecondary = Color.White,
    tertiary = StatusPending,
    background = BackgroundWhite,
    onBackground = TextDark,
    surface = SurfaceCard,
    onSurface = TextDark,
    surfaceVariant = SurfaceCardAlt,
    onSurfaceVariant = TextBody,
    outline = BorderLight
)

@Composable
fun STePTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = STePLightColorScheme,
        typography = Typography,
        content = content
    )
}
