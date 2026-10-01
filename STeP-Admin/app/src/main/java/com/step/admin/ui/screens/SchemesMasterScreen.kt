package com.step.admin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.step.admin.data.AdminRepository
import com.step.admin.data.AdminScheme
import com.step.admin.firebase.AdminFirebaseManager
import com.step.admin.ui.components.FontAwesomeIcons
import com.step.admin.ui.theme.*

@Composable
fun SchemesMasterScreen(
    onBack: () -> Unit,
    openCreateModalInitially: Boolean = false,
    onDismissCreateModal: () -> Unit = {}
) {
    var showCreateBottomSheet by remember(openCreateModalInitially) { mutableStateOf(openCreateModalInitially) }

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
                        onClick = { showCreateBottomSheet = true },
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
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Master Schemes List
            items(AdminRepository.schemes) { scheme ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderLight, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
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
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Surface(
                                color = if (scheme.isActive) StatusDisbursedBg else StatusRejectedBg,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (scheme.isActive) "ACTIVE" else "ARCHIVED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (scheme.isActive) StatusDisbursed else StatusRejected,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
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

    // Modern Material 3 Modal Bottom Sheet for Creating Schemes
    if (showCreateBottomSheet) {
        CreateSchemeBottomSheet(
            onDismiss = {
                showCreateBottomSheet = false
                onDismissCreateModal()
            },
            onSchemeCreated = { newScheme ->
                AdminFirebaseManager.saveNewScheme(newScheme)
                showCreateBottomSheet = false
                onDismissCreateModal()
            }
        )
    }
}

/**
 * Dedicated Sovereign Modal Bottom Sheet for provisioning new scholarship schemes.
 * Eliminates legacy Dialog boxes and delivers a state-of-the-art administrative form with
 * proper typography, validation, portal chips, and instant synchronization with Cloud Firestore.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSchemeBottomSheet(
    onDismiss: () -> Unit,
    onSchemeCreated: (AdminScheme) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var codeInput by remember { mutableStateOf("SCH-0${AdminRepository.schemes.size + 1}") }
    var titleInput by remember { mutableStateOf("") }
    var portalInput by remember { mutableStateOf("National Scholarship Portal (NSP)") }
    var targetClassInput by remember { mutableStateOf("") }
    var incomeCeilingInput by remember { mutableStateOf("250000") }
    var benefitAmountInput by remember { mutableStateOf("50000") }
    var deadlineInput by remember { mutableStateOf("31 Dec 2026") }
    var rulesInput by remember { mutableStateOf("Family income <= 2.50L, Enrolled in recognized institute") }
    var hasNoIncomeLimit by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BackgroundWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(PrimarySurfaceLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.BuildingColumns,
                            contentDescription = null,
                            tint = PrimaryDeepOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Create Scholarship Scheme",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Ministry of Tribal Affairs • National Master Catalog",
                            fontSize = 10.5.sp,
                            color = TextSubtle
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(FontAwesomeIcons.Solid.CircleXmark, contentDescription = "Close", tint = TextSubtle, modifier = Modifier.size(16.dp))
                }
            }

            HorizontalDivider(color = BorderLight, thickness = 0.8.dp)

            // Form Inputs with proper labels and styling
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = codeInput,
                    onValueChange = { codeInput = it },
                    label = { Text("Code", fontSize = 11.5.sp) },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp),
                    modifier = Modifier.weight(0.38f),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryDeepOrange,
                        unfocusedBorderColor = BorderLight
                    )
                )
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("Scheme Title", fontSize = 11.5.sp) },
                    placeholder = { Text("e.g. Special Tribal Higher Education", fontSize = 12.sp, color = TextSubtle) },
                    singleLine = true,
                    modifier = Modifier.weight(0.62f),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryDeepOrange,
                        unfocusedBorderColor = BorderLight
                    )
                )
            }

            // Target Class
            OutlinedTextField(
                value = targetClassInput,
                onValueChange = { targetClassInput = it },
                label = { Text("Target Class / Eligibility Group", fontSize = 11.5.sp) },
                placeholder = { Text("e.g. Class XI, XII, UG, PG & PhD", fontSize = 12.sp, color = TextSubtle) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryDeepOrange,
                    unfocusedBorderColor = BorderLight
                )
            )

            // Portal Selection Chips
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "DISBURSEMENT / NODAL PORTAL",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSubtle,
                    letterSpacing = 0.8.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val portals = listOf(
                        "NSP" to "National Scholarship Portal (NSP)",
                        "SFMP" to "SFMP / Canara Bank Portal",
                        "NOS" to "Standalone NOS Portal (overseas.tribal.gov.in)"
                    )
                    portals.forEach { (shortName, fullName) ->
                        val isSelected = portalInput == fullName
                        Surface(
                            color = if (isSelected) PrimarySurfaceLight else BackgroundWhite,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) PrimaryDeepOrange else BorderLight),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { portalInput = fullName }
                        ) {
                            Text(
                                text = shortName,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) PrimaryDeepOrange else TextBody,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Financial Ceilings (Income & Benefit)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = if (hasNoIncomeLimit) "No Ceiling" else incomeCeilingInput,
                    onValueChange = { if (!hasNoIncomeLimit) incomeCeilingInput = it },
                    label = { Text("Income Ceiling (₹/yr)", fontSize = 11.5.sp) },
                    enabled = !hasNoIncomeLimit,
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryDeepOrange,
                        unfocusedBorderColor = BorderLight
                    )
                )
                OutlinedTextField(
                    value = benefitAmountInput,
                    onValueChange = { benefitAmountInput = it },
                    label = { Text("Annual Grant (₹/yr)", fontSize = 11.5.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryDeepOrange,
                        unfocusedBorderColor = BorderLight
                    )
                )
            }

            // Deadline & Rules
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = deadlineInput,
                    onValueChange = { deadlineInput = it },
                    label = { Text("Application Deadline", fontSize = 11.5.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryDeepOrange,
                        unfocusedBorderColor = BorderLight
                    )
                )
                OutlinedTextField(
                    value = rulesInput,
                    onValueChange = { rulesInput = it },
                    label = { Text("Rules / Criteria", fontSize = 11.5.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryDeepOrange,
                        unfocusedBorderColor = BorderLight
                    )
                )
            }

            // Publish and Cancel action buttons
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = {
                    val parsedAmount = benefitAmountInput.filter { it.isDigit() }.toLongOrNull() ?: 50000L
                    val parsedCeiling = if (hasNoIncomeLimit) null else incomeCeilingInput.filter { it.isDigit() }.toLongOrNull() ?: 250000L
                    val newScheme = AdminScheme(
                        id = codeInput.lowercase().replace("-", "_").trim(),
                        code = codeInput.trim(),
                        title = titleInput.ifBlank { "National Tribal Scholar Scheme" }.trim(),
                        hindiTitle = "राष्ट्रीय जनजातीय छात्रवृत्ति",
                        portal = portalInput,
                        targetClass = targetClassInput.ifBlank { "Class IX to Post-Graduation" }.trim(),
                        incomeCeiling = parsedCeiling,
                        benefitSummary = "Annual grant ₹${parsedAmount} DBT direct credit",
                        maxBenefitAmount = parsedAmount,
                        benefitAmountFormatted = "₹${parsedAmount}/yr",
                        deadlineFormatted = deadlineInput.ifBlank { "31 Dec 2026" }.trim(),
                        rules = rulesInput.split(",").map { it.trim() }.filter { it.isNotBlank() },
                        isActive = true
                    )
                    onSchemeCreated(newScheme)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryDeepOrange),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.ShieldCheck,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Publish & Sync to Cloud",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Text(
                    text = "Cancel",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextBody
                )
            }
        }
    }
}
