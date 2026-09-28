package com.step.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.step.app.data.ApplicationRecord
import com.step.app.data.MoTaRepository
import com.step.app.data.PendingAction
import com.step.app.ui.components.FontAwesomeIcons
import com.step.app.ui.theme.*

@Composable
fun HomeScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToWizard: () -> Unit,
    onNavigateToApplicationDetail: (ApplicationRecord) -> Unit,
    onResolvePendingAction: (PendingAction) -> Unit
) {
    val student = MoTaRepository.currentStudent
    val applications = MoTaRepository.applications
    val pendingActions = MoTaRepository.pendingActions
    var isOffline by remember { mutableStateOf(false) }

    val totalDisbursed = applications
        .filter { it.stage.equals("DISBURSED", ignoreCase = true) || it.currentStepIndex >= 3 }
        .sumOf { it.sanctionAmount }

    val activeEnrolledCount = applications.count { 
        it.stage.equals("DISBURSED", ignoreCase = true) || it.stage.equals("SANCTIONED", ignoreCase = true)
    }

    val pendingCount = applications.count { 
        !it.stage.equals("DISBURSED", ignoreCase = true) && !it.stage.equals("SANCTIONED", ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // 1. Offline Banner (if connection is lost or offline test mode)
        if (isOffline) {
            item {
                Surface(
                    color = OfflineBannerBackground,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.TriangleExclamation,
                            contentDescription = null,
                            tint = OfflineBannerText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "You are currently offline. Showing cached scholarship data.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = OfflineBannerText,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2. Top Header: Greeting + Student Avatar + Notification Bell
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { onNavigateToProfile() }
                        .weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(PrimarySurfaceLight)
                            .border(1.5.dp, PrimaryDeepOrange, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (student.photoUrl.isNotBlank()) {
                            AsyncImage(
                                model = student.photoUrl,
                                contentDescription = "Profile Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.User,
                                contentDescription = "Profile Avatar",
                                tint = PrimaryDeepOrange,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Namaste,",
                            fontSize = 12.sp,
                            color = TextSubtle,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = student.fullName,
                            fontSize = 18.sp,
                            color = TextDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Notification Bell with Unread Badge
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SurfaceCard)
                        .border(1.dp, BorderLight, CircleShape)
                        .clickable { onNavigateToNotifications() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.Bell,
                        contentDescription = "Notifications",
                        tint = TextBody,
                        modifier = Modifier.size(18.dp)
                    )
                    // Unread Red Dot
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-8).dp, y = 8.dp)
                            .clip(CircleShape)
                            .background(StatusRejected)
                    )
                }
            }
        }

        // 3. "Scholarship Wallet" Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PrimaryDeepOrange),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.Wallet,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Scholarship Wallet",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "AY 2026-27",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Total Received This Year",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (totalDisbursed > 0) "₹ %,d".format(totalDisbursed) else "₹ 0",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Two sub-labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF8CE99A))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "$activeEnrolledCount ${if (activeEnrolledCount == 1) "Scheme" else "Schemes"}",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Active & Enrolled",
                                        color = Color.White.copy(alpha = 0.75f),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Surface(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFD43B))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "$pendingCount Pending",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Action Needed",
                                        color = Color.White.copy(alpha = 0.75f),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. "Your Schemes" Section (Horizontal Scroll)
        item {
            Spacer(modifier = Modifier.height(26.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Schemes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = "${applications.size} Applied",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSubtle
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (applications.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.GraduationCap,
                            contentDescription = null,
                            tint = PrimaryDeepOrange,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Scholarship Applications Yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "You haven't submitted any scholarship applications yet. Apply for centrally-funded Pre-Matric, Post-Matric, Top Class, NFST, or NOS schemes directly through STeP.",
                            fontSize = 12.sp,
                            color = TextSubtle,
                            textAlign = TextAlign.Center,
                            lineHeight = 17.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onNavigateToWizard,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryDeepOrange),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Explore & Apply for Schemes", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(applications) { app ->
                        SchemeCardItem(
                            application = app,
                            onClick = { onNavigateToApplicationDetail(app) }
                        )
                    }
                }
            }
        }

        // 5. "Pending Actions" Section (Only if pending actions exist)
        if (pendingActions.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(28.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(StatusRejected)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pending Actions",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = StatusRejectedBg,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "${pendingActions.size}",
                            color = StatusRejected,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pendingActions.forEach { action ->
                        PendingActionCard(
                            action = action,
                            onClick = { onResolvePendingAction(action) }
                        )
                    }
                }
            }
        }

        // 6. "Discover Schemes" Banner at bottom
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = PrimarySurfaceLight),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .border(1.dp, PrimaryDeepOrange.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .clickable { onNavigateToWizard() }
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PrimaryDeepOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.WandMagicSparkles,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Discover More Schemes",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDeepOrangeDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Check eligibility across all 5 sovereign MoTA schemes.",
                            fontSize = 12.sp,
                            color = TextBody
                        )
                    }

                    Icon(
                        imageVector = FontAwesomeIcons.Solid.ArrowRight,
                        contentDescription = "Check Now",
                        tint = PrimaryDeepOrange,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // 7. Data Provenance Footnote at bottom
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Surface(
                color = SurfaceCard,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "Profile Data: Authenticated via Google SSO • Verified via DigiLocker Sandbox • MoTa Test Environment",
                    fontSize = 10.sp,
                    color = TextSubtle,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
private fun SchemeCardItem(
    application: ApplicationRecord,
    onClick: () -> Unit
) {
    val (statusColor, statusBg, statusLabel) = when (application.stage) {
        "DISBURSED" -> Triple(StatusDisbursed, StatusDisbursedBg, "Disbursed")
        "SANCTIONED" -> Triple(StatusInProgress, StatusInProgressBg, "Sanctioned")
        else -> Triple(StatusPending, StatusPendingBg, "Action Needed")
    }

    val formattedSanction = java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("en-IN")).format(application.sanctionAmount)

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .width(280.dp)
            .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = application.academicYear,
                    fontSize = 11.sp,
                    color = TextSubtle,
                    fontWeight = FontWeight.Medium
                )

                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = application.schemeTitle,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                maxLines = 2,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Next Action:",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSubtle
            )
            Text(
                text = application.nextActionText,
                fontSize = 11.sp,
                color = TextBody,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₹ $formattedSanction",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryDeepOrange
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Details",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDeepOrange
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.ArrowRight,
                        contentDescription = null,
                        tint = PrimaryDeepOrange,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PendingActionCard(
    action: PendingAction,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderMedium, RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row: Warning Icon + Scheme Pill + Urgent Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(StatusRejectedBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.TriangleExclamation,
                            contentDescription = null,
                            tint = StatusRejected,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = PrimarySurfaceLight,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = action.scheme,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDeepOrangeDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    color = StatusRejectedBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "ACTION REQUIRED",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = StatusRejected,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Title - Full Width! No wrapping into single words
            Text(
                text = action.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Reason Text
            Text(
                text = action.reason,
                fontSize = 12.sp,
                color = TextBody,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Footer Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDeepOrange),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = action.actionText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.ArrowRight,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}
