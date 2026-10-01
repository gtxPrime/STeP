package com.step.admin.ui.theme

import androidx.compose.ui.graphics.Color

// Primary MoTA Tribal Identity (Deep orange-red matching Student App)
val PrimaryDeepOrange = Color(0xFFD9480F)
val PrimaryDeepOrangeDark = Color(0xFFBF3A0A)
val PrimaryDeepOrangeLight = Color(0xFFFF6B35)
val PrimarySurfaceLight = Color(0xFFFFF4E6)

// Clean Sovereign Light Palette (Identical to Student App)
val BackgroundWhite = Color(0xFFFFFFFF)
val SurfaceCard = Color(0xFFF8F9FA)
val SurfaceCardAlt = Color(0xFFFFFFFF)
val BorderLight = Color(0xFFE9ECEF)
val BorderMedium = Color(0xFFDEE2E6)

// Semantic Status Colors
val StatusDisbursed = Color(0xFF2B8A3E)
val StatusDisbursedBg = Color(0xFFEBFBEE)
val StatusInProgress = Color(0xFF1971C2)
val StatusInProgressBg = Color(0xFFE7F5FF)
val StatusPending = Color(0xFFE67700)
val StatusPendingBg = Color(0xFFFFF9DB)
val StatusRejected = Color(0xFFC92A2A)
val StatusRejectedBg = Color(0xFFFFE3E3)

// Typography & Contrast Colors (Identical to Student App)
val TextDark = Color(0xFF1A1D20)
val TextBody = Color(0xFF495057)
val TextSubtle = Color(0xFF868E96)
val TextLight = Color(0xFFFFFFFF)

// Saffron / Brand Identifiers
val SaffronPrimary = PrimaryDeepOrange
val SaffronDark = PrimaryDeepOrangeDark
val SaffronLight = PrimaryDeepOrangeLight
val SaffronSurface = PrimarySurfaceLight

val EmeraldSuccess = StatusDisbursed
val EmeraldLight = StatusDisbursedBg
val AmberWarning = StatusPending
val AmberLight = StatusPendingBg
val CrimsonError = StatusRejected
val CrimsonLight = StatusRejectedBg
val BlueAccent = StatusInProgress
val BlueLight = StatusInProgressBg

// Clean Sovereign Light Theme Tokens (Remapping old dark tokens to clean light style)
val NavyBackground = Color(0xFFF8F9FA)  // Crisp light page background like student app
val NavySurface = Color(0xFFFFFFFF)     // Clean white surface cards
val NavyCard = Color(0xFFFFFFFF)        // Pure white cards with borders
val NavyDark = Color(0xFFF1F3F5)        // Soft light container for tags / status
val NavyBorder = Color(0xFFE9ECEF)      // Subtle light border

val TextMain = TextDark                 // #1A1D20 (deep high-contrast dark text)
val TextMuted = TextBody                // #495057 (readable body text)
val TextDim = TextSubtle                // #868E96 (subtle secondary text)
