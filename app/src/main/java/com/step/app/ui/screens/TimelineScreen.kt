package com.step.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.step.app.data.ApplicationRecord
import com.step.app.data.MoTaRepository
import com.step.app.firebase.FirebaseManager
import com.step.app.ui.theme.*

@Composable
fun TimelineScreen(
    onExplainDeficiency: () -> Unit,
    onOneTapRenew: () -> Unit,
    onLogout: () -> Unit
) {
    val student = MoTaRepository.currentStudent
    val apps = MoTaRepository.applications
    val googleUser = FirebaseManager.currentUser

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Authenticated Google Profile Card & Expiry Alert
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NavyBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(SaffronPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = googleUser?.displayName?.take(1) ?: "S",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = googleUser?.displayName ?: student.fullName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = TextMain
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = EmeraldSuccess.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = "Google Verified",
                                            color = EmeraldSuccess,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = googleUser?.email ?: student.email,
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                                Text(
                                    text = "APAAR ID: ${student.apaarId}",
                                    fontSize = 11.sp,
                                    color = SaffronLight,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        IconButton(onClick = onLogout) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 28-Day Expiry Alert & One-Tap Renewal
                    Surface(
                        color = AmberWarning.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AmberWarning.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = AmberWarning,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Income Cert Expires in 28 Days",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = AmberWarning
                                    )
                                    Text(
                                        text = "Tap to auto-renew via e-District",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }

                            Button(
                                onClick = onOneTapRenew,
                                colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("1-Tap Renew", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Section Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CONSOLIDATED SCHOLARSHIP TIMELINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaffronLight
                    )
                    Text(
                        text = "Active MoTA Applications",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMain
                    )
                }
                Surface(
                    color = Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "3 Portals Unified",
                        fontSize = 11.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // List of Application Timeline Cards
        items(apps) { app ->
            ApplicationTimelineCard(app = app, onExplainDeficiency = onExplainDeficiency)
        }
    }
}

@Composable
fun ApplicationTimelineCard(
    app: ApplicationRecord,
    onExplainDeficiency: () -> Unit
) {
    val isDeficient = app.stage == "DEFICIENCY_FLAGGED"

    Card(
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isDeficient) AmberWarning.copy(alpha = 0.6f) else NavyBorder,
                RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Topbar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        color = Color(0xFF0284C7).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = app.sourcePortal,
                            color = Color(0xFF38BDF8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = app.schemeTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextMain
                    )
                    Text(
                        text = "App ID: ${app.applicationId} • ${app.academicYear}",
                        fontSize = 11.sp,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Sanctioned", fontSize = 10.sp, color = TextMuted)
                    Text(
                        text = "₹${app.sanctionAmount / 1000}k",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = EmeraldSuccess
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5-Step Stepper Line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                app.steps.forEachIndexed { index, step ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    when {
                                        step.completed -> EmeraldSuccess
                                        isDeficient && index == 1 -> AmberWarning
                                        else -> NavyCard
                                    },
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (step.completed) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            } else if (isDeficient && index == 1) {
                                Text("!", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Text("${index + 1}", color = TextMuted, fontSize = 10.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = step.label,
                            fontSize = 8.sp,
                            color = if (step.completed) TextMain else TextMuted,
                            maxLines = 1
                        )
                    }
                }
            }

            // Deficiency Banner if present
            if (isDeficient && app.deficiency != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    color = AmberWarning.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberWarning.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Defect Code: ${app.deficiency.code} (Action Needed)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = AmberWarning
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = app.deficiency.bureaucraticReason.take(110) + "...",
                            fontSize = 11.sp,
                            color = TextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = onExplainDeficiency,
                                colors = ButtonDefaults.buttonColors(containerColor = AmberWarning),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Explain in Simple Words", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = "${app.deficiency.daysRemaining} days left",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmberWarning
                            )
                        }
                    }
                }
            }

            // DBT Strip
            if (app.dbtDetails != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color.White.copy(alpha = 0.04f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "UTR: ${app.dbtDetails.utr}",
                            fontSize = 11.sp,
                            color = SaffronLight,
                            fontFamily = FontFamily.Monospace
                        )
                        Surface(
                            color = EmeraldSuccess.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "DBT Credited",
                                color = EmeraldSuccess,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
