package com.step.admin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.step.admin.data.AdminRepository
import com.step.admin.data.AdminScheme
import com.step.admin.firebase.AdminFirebaseManager
import com.step.admin.ui.theme.*

@Composable
fun SchemesMasterScreen(
    onBack: () -> Unit,
    openCreateModalInitially: Boolean = false
) {
    var showCreateDialog by remember { mutableStateOf(openCreateModalInitially) }

    var codeInput by remember { mutableStateOf("SCH-06") }
    var titleInput by remember { mutableStateOf("") }
    var portalInput by remember { mutableStateOf("National Scholarship Portal (NSP)") }
    var targetClassInput by remember { mutableStateOf("") }
    var incomeCeilingInput by remember { mutableStateOf("250000") }
    var benefitAmountInput by remember { mutableStateOf("50000") }

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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                            Text("←", fontSize = 20.sp, color = TextDark, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Scholarships Master (${AdminRepository.schemes.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDeepOrange),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("+ New", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        },
        containerColor = NavyBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(AdminRepository.schemes) { scheme ->
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
                            Surface(
                                color = PrimarySurfaceLight,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = scheme.code,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryDeepOrange,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }

                            Surface(
                                color = StatusDisbursedBg,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Max: ${scheme.benefitAmountFormatted}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusDisbursed,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = scheme.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Target: ${scheme.targetClass}", fontSize = 12.sp, color = TextBody)
                        Text(text = "Portal: ${scheme.portal}", fontSize = 11.sp, color = StatusInProgress, fontWeight = FontWeight.Medium)
                        Text(text = "Benefits: ${scheme.benefitSummary}", fontSize = 12.sp, color = TextBody)

                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = SurfaceCardAlt,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (scheme.incomeCeiling != null) "Ceiling: ≤ ₹${scheme.incomeCeiling / 100000.0}L" else "Income: No Ceiling",
                                    fontSize = 11.sp,
                                    color = TextBody,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Deadline: ${scheme.deadlineFormatted}",
                                    fontSize = 11.sp,
                                    color = StatusPending,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create New Scholarship Scheme", fontWeight = FontWeight.Bold, color = TextDark) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = { codeInput = it },
                        label = { Text("Scheme Code (e.g. SCH-06)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Scheme Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = targetClassInput,
                        onValueChange = { targetClassInput = it },
                        label = { Text("Target Class (e.g. Higher Secondary)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = incomeCeilingInput,
                        onValueChange = { incomeCeilingInput = it },
                        label = { Text("Income Ceiling (₹/annum)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = benefitAmountInput,
                        onValueChange = { benefitAmountInput = it },
                        label = { Text("Annual Benefit Amount (₹)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newScheme = AdminScheme(
                            id = codeInput.lowercase().replace("-", "_"),
                            code = codeInput,
                            title = titleInput.ifBlank { "Special Tribal Scholar Scheme" },
                            hindiTitle = "विशेष जनजातीय छात्रवृत्ति",
                            portal = portalInput,
                            targetClass = targetClassInput.ifBlank { "Class IX to Post-Graduation" },
                            incomeCeiling = incomeCeilingInput.toLongOrNull() ?: 250000L,
                            benefitSummary = "Annual grant ₹${benefitAmountInput} DBT direct credit",
                            maxBenefitAmount = benefitAmountInput.toLongOrNull() ?: 50000L,
                            benefitAmountFormatted = "₹${benefitAmountInput}/yr",
                            deadlineFormatted = "31 Dec 2026",
                            isActive = true
                        )
                        AdminFirebaseManager.saveNewScheme(newScheme)
                        showCreateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDeepOrange)
                ) {
                    Text("Save to Cloud Firestore", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel", color = TextBody)
                }
            },
            containerColor = BackgroundWhite,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
