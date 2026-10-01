package com.step.admin.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val STePAdminLightColorScheme = lightColorScheme(
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
    outline = BorderLight,
    error = StatusRejected,
    onError = Color.White
)

@Composable
fun STePAdminTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = STePAdminLightColorScheme,
        content = content
    )
}
