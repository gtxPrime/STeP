package com.step.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.step.app.data.MoTaRepository
import com.step.app.data.NotificationItem
import com.step.app.ui.components.FontAwesomeIcons
import com.step.app.ui.theme.*

@Composable
fun NotificationCenterScreen(
    onBack: () -> Unit
) {
    val notifications = MoTaRepository.notifications

    Scaffold(
        topBar = {
            Surface(
                color = BackgroundWhite,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.ArrowLeft,
                            contentDescription = "Back",
                            tint = TextDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Notifications & Alerts",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundWhite)
                .padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        if (notifications.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                        .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(PrimarySurfaceLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.Bell,
                                contentDescription = null,
                                tint = PrimaryDeepOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Notifications",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "You have no unread alerts. You will receive updates here when your applications are verified or funds are credited.",
                            fontSize = 12.sp,
                            color = TextSubtle,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        } else {
            items(notifications) { notif ->
                NotificationCardItem(item = notif)
            }
        }
    }
}
}


@Composable
private fun NotificationCardItem(item: NotificationItem) {
    val (icon, iconTint, bgTint) = when (item.type) {
        "PAYMENT" -> Triple(FontAwesomeIcons.Solid.CircleCheck, StatusDisbursed, StatusDisbursedBg)
        "EXPIRY" -> Triple(FontAwesomeIcons.Solid.Clock, StatusRejected, StatusRejectedBg)
        "DEFICIENCY" -> Triple(FontAwesomeIcons.Solid.TriangleExclamation, StatusPending, StatusPendingBg)
        else -> Triple(FontAwesomeIcons.Solid.ShieldCheck, StatusInProgress, StatusInProgressBg)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (item.isUnread) PrimaryDeepOrange.copy(alpha = 0.4f) else BorderLight,
                RoundedCornerShape(14.dp)
            )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(bgTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = item.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.time,
                        fontSize = 11.sp,
                        color = TextSubtle,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.body,
                    fontSize = 12.sp,
                    color = TextBody,
                    lineHeight = 17.sp
                )
            }
        }
    }
}
