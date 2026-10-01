package com.step.admin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.step.admin.data.AdminApplication
import com.step.admin.data.AdminGeminiService
import com.step.admin.data.AdminRepository
import com.step.admin.firebase.AdminFirebaseManager
import com.step.admin.ui.components.FontAwesomeIcons
import com.step.admin.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ScrutinyQueueScreen(
    onBack: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    var selectedAppForDetail by remember { mutableStateOf<AdminApplication?>(null) }
    var showDefectDialog by remember { mutableStateOf(false) }
    var defectReasonInput by remember { mutableStateOf("") }
    var aiDraftedNotice by remember { mutableStateOf<String?>(null) }
    var isDraftingWithAi by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val filteredApps = remember(selectedFilter, AdminRepository.applications.size) {
        when (selectedFilter) {
            "PENDING" -> AdminRepository.applications.filter { it.stage != "SANCTIONED" && it.stage != "DISBURSED" }
            "SANCTIONED" -> AdminRepository.applications.filter { it.stage == "SANCTIONED" }
            "DISBURSED" -> AdminRepository.applications.filter { it.stage == "DISBURSED" }
            "DEFECTS" -> AdminRepository.applications.filter { it.stage == "DEFICIENCY_FLAGGED" }
            else -> AdminRepository.applications
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = BackgroundWhite,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(androidx.compose.foundation.BorderStroke(1.dp, BorderLight))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(52.dp)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                        Text("←", fontSize = 20.sp, color = TextDark, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Scrutiny & Approval Queue",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        },
        containerColor = NavyBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("ALL", "PENDING", "SANCTIONED", "DISBURSED", "DEFECTS")
                items(filters) { filter ->
                    val isSelected = selectedFilter == filter
                    Surface(
                        color = if (isSelected) PrimaryDeepOrange else BackgroundWhite,
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) PrimaryDeepOrange else BorderLight),
                        modifier = Modifier.clickable { selectedFilter = filter }
                    ) {
                        Text(
                            text = filter,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else TextBody,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredApps.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp)
                                .border(1.dp, BorderLight, RoundedCornerShape(14.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "NAS",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryDeepOrange
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "No Applications on Server",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "No fallback or mock applications are loaded. Applications will appear here in real time as scholars submit them through the unified platform.",
                                    fontSize = 11.sp,
                                    color = TextBody,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }

                items(filteredApps) { app ->
                    ApplicationScrutinyCard(
                        app = app,
                        onSanction = {
                            AdminRepository.sanctionApplication(app.id)
                            AdminFirebaseManager.syncSanctionToFirestore(app.id, app.docPath)
                        },
                        onDisburse = {
                            AdminRepository.disburseApplication(app.id)
                            AdminFirebaseManager.syncDisbursementToFirestore(
                                app.id,
                                app.docPath,
                                "RBI${System.currentTimeMillis().toString().takeLast(9)}"
                            )
                        },
                        onFlagDefect = {
                            selectedAppForDetail = app
                            defectReasonInput = "Clause 4.1: Certificate validity expired or unreadable seal."
                            showDefectDialog = true
                        }
                    )
                }
            }
        }
    }

    // Flag Defect with Gemini AI Draft Dialog
    if (showDefectDialog && selectedAppForDetail != null) {
        val app = selectedAppForDetail!!
        AlertDialog(
            onDismissRequest = { showDefectDialog = false },
            title = {
                Text("Flag Application Defect", fontWeight = FontWeight.Bold, color = TextDark)
            },
            text = {
                Column {
                    Text(
                        text = "Candidate: ${app.studentName} (${app.scheme})",
                        fontSize = 12.sp,
                        color = PrimaryDeepOrange,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = defectReasonInput,
                        onValueChange = { defectReasonInput = it },
                        label = { Text("Bureaucratic Defect Reason") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                isDraftingWithAi = true
                                aiDraftedNotice = AdminGeminiService.draftDefectNotice(defectReasonInput, app.studentName)
                                isDraftingWithAi = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDeepOrange),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isDraftingWithAi) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(FontAwesomeIcons.Solid.WandMagicSparkles, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Draft Student Notice with Gemini AI", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    if (aiDraftedNotice != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = SurfaceCardAlt,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                        ) {
                            Text(
                                text = aiDraftedNotice!!,
                                fontSize = 11.sp,
                                color = TextDark,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        AdminRepository.flagDeficiency(app.id, defectReasonInput)
                        AdminFirebaseManager.syncDeficiencyToFirestore(app.id, app.docPath, defectReasonInput)
                        showDefectDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRejected)
                ) {
                    Text("Issue Formal Deficiency Order", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDefectDialog = false }) {
                    Text("Cancel", color = TextBody)
                }
            },
            containerColor = BackgroundWhite,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun ApplicationScrutinyCard(
    app: AdminApplication,
    onSanction: () -> Unit,
    onDisburse: () -> Unit,
    onFlagDefect: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderLight, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = app.studentName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Text(text = app.district, fontSize = 11.sp, color = TextBody)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = if (app.confidenceScore >= 85) StatusDisbursedBg else StatusPendingBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${app.confidenceScore}% Confidence",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (app.confidenceScore >= 85) StatusDisbursed else StatusPending,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = SurfaceCardAlt,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(text = app.scheme, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryDeepOrange)
                    Text(text = "Portal: ${app.portal} • Sanction: ₹${app.sanctionAmount}", fontSize = 11.sp, color = TextBody)
                    Text(text = "Doc Verification: ${app.source}", fontSize = 10.sp, color = StatusDisbursed, fontFamily = FontFamily.Monospace)
                }
            }

            if (app.anomaly != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = StatusRejectedBg,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Defect: ${app.anomaly}",
                        fontSize = 11.sp,
                        color = StatusRejected,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (app.stage == "SUBMITTED") {
                    Button(
                        onClick = onSanction,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDeepOrange),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(FontAwesomeIcons.Solid.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Issue Sanction", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    OutlinedButton(
                        onClick = onFlagDefect,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusPending),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusPending),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(FontAwesomeIcons.Solid.Flag, contentDescription = null, tint = StatusPending, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Flag Defect", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (app.stage == "SANCTIONED") {
                    Button(
                        onClick = onDisburse,
                        colors = ButtonDefaults.buttonColors(containerColor = StatusDisbursed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(FontAwesomeIcons.Solid.CreditCard, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Execute PFMS APB Disbursement", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                } else {
                    Surface(
                        color = SurfaceCardAlt,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Stage: ${app.stageText}",
                            fontSize = 11.sp,
                            color = TextBody,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}
