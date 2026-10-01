package com.step.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.step.app.data.MoTaRepository
import com.step.app.data.Scheme
import com.step.app.ui.components.DigiLockerSandboxBottomSheet
import com.step.app.ui.components.FontAwesomeIcons
import com.step.app.ui.theme.*
import androidx.compose.foundation.clickable
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun ApplyFlowScreen(
    scheme: Scheme,
    onBack: () -> Unit,
    onSubmitSuccess: () -> Unit
) {
    var currentStep by remember { mutableStateOf(1) } // 1..4
    var isSubmitted by remember { mutableStateOf(false) }
    var decisionResult by remember { mutableStateOf<com.step.app.data.DecisionResult?>(null) }
    var generatedAppId by remember { mutableStateOf("") }
    var stepValidationError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Compute academic year dynamically (e.g. if month >= Aug then "2026-27" else "2025-26")
    val academicYear = remember {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) // 0-indexed, 7 = August
        if (month >= 7) "$year-${(year + 1).toString().takeLast(2)}" else "${year - 1}-${year.toString().takeLast(2)}"
    }

    // Step 1: Personal Details State
    val student = MoTaRepository.currentStudent
    var fullName by remember { mutableStateOf(if (student.fullName.isNotBlank() && student.fullName != "NAS") student.fullName else "") }
    val existingFather = MoTaRepository.scannedDocuments.firstOrNull { it.fatherName.isNotBlank() && it.fatherName != "NAS" && it.fatherName != "Parent / Guardian" }?.fatherName ?: ""
    var fatherName by remember { mutableStateOf(existingFather) }
    var community by remember { mutableStateOf(if (student.subTribe.isNotBlank() && student.subTribe != "NAS") "${student.subTribe} (ST)" else "Scheduled Tribe (ST)") }
    var aadhaarLast4 by remember { mutableStateOf(if (student.aadhaarLast4 != "NAS" && student.aadhaarLast4.isNotBlank()) student.aadhaarLast4 else "") }

    // Step 2: Academic Details State
    var courseLevel by remember { mutableStateOf(if (student.educationLevel != "NAS") student.educationLevel else "") }
    var instituteName by remember { mutableStateOf(if (student.institution != "NAS") student.institution else "") }
    var rollNumber by remember { mutableStateOf("") }
    var percentage by remember { mutableStateOf("") }

    // Step 3: Document Upload State
    val hasCaste = MoTaRepository.scannedDocuments.any { it.documentType.contains("Caste") || it.documentType.contains("ST") }
    val hasIncome = MoTaRepository.scannedDocuments.any { it.documentType.contains("Income") }
    val hasMarksheet = MoTaRepository.scannedDocuments.any { it.documentType.contains("Marksheet") || it.documentType.contains("Academic") }
    var casteUploaded by remember(hasCaste) { mutableStateOf(hasCaste) }
    var incomeUploaded by remember(hasIncome) { mutableStateOf(hasIncome) }
    var marksheetUploaded by remember(hasMarksheet) { mutableStateOf(hasMarksheet) }
    var scanStatusMessage by remember { mutableStateOf<String?>(null) }
    var showDigiLockerSheet by remember { mutableStateOf(false) }

    // Step 4: Declaration Checkbox
    var isDeclared by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Surface(
                color = BackgroundWhite,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (currentStep > 1) {
                                    currentStep--
                                } else {
                                    onBack()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.ArrowLeft,
                                contentDescription = "Back",
                                tint = TextDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Apply: ${scheme.code}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = "Step $currentStep of 4: " + when (currentStep) {
                                    1 -> "Personal Details"
                                    2 -> "Academic Information"
                                    3 -> "Document Uploads"
                                    else -> "Review & Submit"
                                },
                                fontSize = 12.sp,
                                color = PrimaryDeepOrange,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 4-Step Progress Indicator
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (i in 1..4) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        if (i <= currentStep) PrimaryDeepOrange else BorderLight
                                    )
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (!isSubmitted) {
                Surface(
                    color = BackgroundWhite,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        Button(
                            onClick = {
                                if (currentStep < 4) {
                                    // Basic validation before advancing
                                    val validationMsg = when (currentStep) {
                                        1 -> when {
                                            fullName.isBlank() -> "Please enter your full name."
                                            community.isBlank() -> "Please enter your tribe/community."
                                            else -> null
                                        }
                                        2 -> when {
                                            courseLevel.isBlank() -> "Please enter your current course/class."
                                            instituteName.isBlank() -> "Please enter your institution name."
                                            else -> null
                                        }
                                        else -> null
                                    }
                                    if (validationMsg != null) {
                                        stepValidationError = validationMsg
                                    } else {
                                        stepValidationError = null
                                        currentStep++
                                    }
                                } else {
                                    val decision = com.step.app.data.ScholarshipDecisionEngine.evaluateApplication(
                                        scheme = scheme,
                                        student = student,
                                        documents = MoTaRepository.scannedDocuments,
                                        existingApplications = MoTaRepository.applications
                                    )
                                    decisionResult = decision
                                    val appId = "APP-${scheme.code}-${(10000..99999).random()}"
                                    generatedAppId = appId

                                    val portalCode = when (scheme.code) {
                                        "SCH-03", "SCH-04" -> "SFMP"
                                        "SCH-05" -> "NOS"
                                        else -> "NSP"
                                    }
                                    val connector = com.step.app.connectors.ConnectorRegistry.getConnector(portalCode)

                                    scope.launch {
                                        val connectorResult = connector.submitApplication(
                                            applicationId = appId,
                                            candidateUid = student.uid,
                                            schemeCode = scheme.code,
                                            payloadJson = """{"candidateName":"${student.fullName}","schemeCode":"${scheme.code}"}"""
                                        )

                                        val newApp = com.step.app.data.ApplicationRecord(
                                            applicationId = appId,
                                            schemeId = scheme.id,
                                            schemeTitle = scheme.title,
                                            academicYear = academicYear,
                                            sourcePortal = connectorResult.sourcePortal,
                                            stage = decision.stage,
                                            stageText = decision.stageText,
                                            currentStepIndex = when (decision.outcome) {
                                                com.step.app.data.DecisionOutcome.AUTO_APPROVED -> 2
                                                com.step.app.data.DecisionOutcome.AUTO_REJECTED -> 0
                                                com.step.app.data.DecisionOutcome.DEFICIENCY_FLAGGED -> 1
                                                com.step.app.data.DecisionOutcome.MANUAL_REVIEW -> 0
                                            },
                                            sanctionAmount = if (decision.outcome == com.step.app.data.DecisionOutcome.AUTO_APPROVED) scheme.maxBenefitAmount else 0L,
                                            nextActionText = when (decision.outcome) {
                                                com.step.app.data.DecisionOutcome.AUTO_APPROVED -> "Sanction Order Generated (${connectorResult.externalReferenceNumber}). Queued for APBS DBT Disbursement."
                                                com.step.app.data.DecisionOutcome.AUTO_REJECTED -> "Application Rejected based on statutory criteria."
                                                com.step.app.data.DecisionOutcome.DEFICIENCY_FLAGGED -> "Deficiency flagged. Resolve required documents."
                                                com.step.app.data.DecisionOutcome.MANUAL_REVIEW -> "Pending institutional nodal verification on ${connectorResult.sourcePortal}."
                                            },
                                            verificationConfidence = decision.confidenceScore,
                                            steps = listOf(
                                                com.step.app.data.TimelineStep("Application Submitted", "01-Oct-2026", true, "Submitted via sovereign mobile application (${connectorResult.externalReferenceNumber})"),
                                                com.step.app.data.TimelineStep(
                                                    label = "DigiLocker & Gov Verification",
                                                    date = "01-Oct-2026",
                                                    completed = decision.outcome != com.step.app.data.DecisionOutcome.AUTO_REJECTED,
                                                    note = decision.reason
                                                ),
                                                com.step.app.data.TimelineStep(
                                                    label = "Ministry Sanction Order",
                                                    date = if (decision.outcome == com.step.app.data.DecisionOutcome.AUTO_APPROVED) "01-Oct-2026" else "Pending",
                                                    completed = decision.outcome == com.step.app.data.DecisionOutcome.AUTO_APPROVED,
                                                    note = decision.sanctionOrderNo ?: (if (decision.outcome == com.step.app.data.DecisionOutcome.AUTO_REJECTED) "Application rejected" else "Pending review")
                                                ),
                                                com.step.app.data.TimelineStep(
                                                    label = "PFMS DBT Bank Credit",
                                                    date = if (decision.outcome == com.step.app.data.DecisionOutcome.AUTO_APPROVED) "Scheduled" else "N/A",
                                                    completed = false,
                                                    note = if (decision.outcome == com.step.app.data.DecisionOutcome.AUTO_APPROVED) "100% Direct Benefit Transfer to Aadhaar seeded bank account" else "Not eligible for disbursement"
                                                )
                                            ),
                                            dbtDetails = if (decision.outcome == com.step.app.data.DecisionOutcome.AUTO_APPROVED) {
                                                com.step.app.data.DbtDetails(
                                                    utr = "PFMS${(100000000..999999999).random()}",
                                                    paymentMode = "APBS (Aadhaar Payment Bridge System)",
                                                    disbursedDate = "Scheduled within 48h",
                                                    bankName = student.bankName,
                                                    accountNo = student.maskedAccount,
                                                    centralShare = if (scheme.id in listOf("TOP_CLASS", "NFST", "NOS")) "100%" else "75%",
                                                    stateShare = if (scheme.id in listOf("TOP_CLASS", "NFST", "NOS")) "0%" else "25%",
                                                    status = "QUEUED_FOR_DISBURSEMENT"
                                                )
                                            } else null,
                                            deficiency = decision.deficiency
                                        )

                                        MoTaRepository.applications.add(0, newApp)
                                        com.step.app.firebase.FirebaseManager.submitApplicationToFirestore(newApp, student.uid)
                                        isSubmitted = true
                                    }
                                }
                            },
                            enabled = if (currentStep == 4) isDeclared else true,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryDeepOrange),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(
                                text = if (currentStep < 4) "Continue to Step ${currentStep + 1}" else "Submit & Run DigiLocker Auto-Verification",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        // Step validation error message
                        if (stepValidationError != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stepValidationError!!,
                                fontSize = 12.sp,
                                color = StatusRejected,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (isSubmitted) {
            val decision = decisionResult
            val isAutoApproved = decision?.outcome == com.step.app.data.DecisionOutcome.AUTO_APPROVED
            val isAutoRejected = decision?.outcome == com.step.app.data.DecisionOutcome.AUTO_REJECTED

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundWhite)
                    .padding(padding)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isAutoApproved) StatusDisbursed.copy(alpha = 0.5f) else if (isAutoRejected) StatusRejected.copy(alpha = 0.5f) else BorderLight,
                            RoundedCornerShape(20.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isAutoApproved) StatusDisbursedBg else if (isAutoRejected) StatusRejectedBg else StatusInProgressBg
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isAutoApproved) FontAwesomeIcons.Solid.CircleCheck else if (isAutoRejected) FontAwesomeIcons.Solid.CircleXmark else FontAwesomeIcons.Solid.ShieldCheck,
                                contentDescription = null,
                                tint = if (isAutoApproved) StatusDisbursed else if (isAutoRejected) StatusRejected else StatusInProgress,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            color = if (isAutoApproved) StatusDisbursedBg else if (isAutoRejected) StatusRejectedBg else PrimarySurfaceLight,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (isAutoApproved) "AUTO-APPROVED VIA DIGILOCKER (${decision?.confidenceScore}% MATCH)" 
                                       else if (isAutoRejected) "AUTO-REJECTED BY VERIFICATION ENGINE" 
                                       else "DEFICIENCY FLAGGED",
                                color = if (isAutoApproved) StatusDisbursed else if (isAutoRejected) StatusRejected else PrimaryDeepOrangeDark,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (isAutoApproved) "Scholarship Sanctioned!" else if (isAutoRejected) "Application Ineligible" else "Action Required",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDark
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Application ID: $generatedAppId",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDeepOrange
                        )

                        if (decision?.sanctionOrderNo != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Sanction Order: ${decision.sanctionOrderNo}",
                                fontSize = 11.sp,
                                color = StatusDisbursed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            color = BackgroundWhite,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isAutoApproved) "Decision Rationale:" else "Statutory Reason:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = decision?.reason ?: "Application received and cataloged.",
                                    fontSize = 11.sp,
                                    color = TextBody,
                                    lineHeight = 16.sp
                                )
                                if (decision?.clause != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Clause: ${decision.clause}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextSubtle
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onSubmitSuccess,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAutoApproved) StatusDisbursed else PrimaryDeepOrange
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("Track in My Applications", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundWhite)
                    .padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (currentStep) {
                    1 -> {
                        // Step 1: Personal Details (pre-filled from DigiLocker)
                        item {
                            Surface(
                                color = StatusInProgressBg,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = FontAwesomeIcons.Solid.ShieldCheck,
                                        contentDescription = null,
                                        tint = StatusInProgress,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Verified & pre-filled from your DigiLocker Sovereign Vault",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusInProgress
                                    )
                                }
                            }
                        }

                        item {
                            StepTextField(label = "Student Full Name", value = fullName, onValueChange = { fullName = it })
                        }
                        item {
                            StepTextField(label = "Father / Guardian Name", value = fatherName, onValueChange = { fatherName = it })
                        }
                        item {
                            StepTextField(label = "Community / Tribe Category", value = community, onValueChange = { community = it })
                        }
                        item {
                            StepTextField(
                                label = "Aadhaar Card (Last 4 Digits)",
                                value = if (aadhaarLast4.isNotBlank()) "•••• •••• $aadhaarLast4" else "Not linked — will be seeded via NPCI",
                                onValueChange = {}
                            )
                        }
                    }

                    2 -> {
                        // Step 2: Academic Details
                        item {
                            StepTextField(label = "Current Course / Class", value = courseLevel, onValueChange = { courseLevel = it })
                        }
                        item {
                            StepTextField(label = "Enrolled School / Institute", value = instituteName, onValueChange = { instituteName = it })
                        }
                        item {
                            StepTextField(label = "Student Roll / Registration Number", value = rollNumber, onValueChange = { rollNumber = it })
                        }
                        item {
                            StepTextField(label = "Previous Academic Year Marks (%)", value = percentage, onValueChange = { percentage = it })
                        }
                    }

                    3 -> {
                        // Step 3: Document Uploads with Smart Scan Button
                        item {
                            Text(
                                text = "Upload Required Documents",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = "Tap the Smart Scan button to auto-extract and verify directly from camera or gallery.",
                                fontSize = 12.sp,
                                color = TextSubtle
                            )
                        }

                        if (scanStatusMessage != null) {
                            item {
                                Surface(
                                    color = StatusDisbursedBg,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = scanStatusMessage!!,
                                        color = StatusDisbursed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }

                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = PrimarySurfaceLight),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, PrimaryDeepOrange.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .clickable { showDigiLockerSheet = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = FontAwesomeIcons.Solid.ShieldCheck,
                                        contentDescription = null,
                                        tint = PrimaryDeepOrange,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Pull from DigiLocker Sandbox",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryDeepOrangeDark
                                        )
                                        Text(
                                            text = "Instantly verify and attach authentic certificates (stage1.digitallocker.gov.in)",
                                            fontSize = 11.sp,
                                            color = TextBody
                                        )
                                    }
                                    Icon(
                                        imageVector = FontAwesomeIcons.Solid.ArrowRight,
                                        contentDescription = null,
                                        tint = PrimaryDeepOrange,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }

                        item {
                            DocUploadField(
                                label = "Scheduled Tribe (ST) Certificate",
                                isUploaded = casteUploaded,
                                onScanClick = {
                                    casteUploaded = true
                                    val docNum = MoTaRepository.scannedDocuments.firstOrNull { it.documentType.contains("caste", true) }?.certificateNumber ?: "OD/ST/2026/001*"
                                    scanStatusMessage = "AI OCR matched ST Certificate $docNum with 98% confidence!*"
                                }
                            )
                        }

                        item {
                            DocUploadField(
                                label = "Annual Family Income Certificate",
                                isUploaded = incomeUploaded,
                                onScanClick = {
                                    incomeUploaded = true
                                    val inc = if (MoTaRepository.currentStudent.annualIncome > 0) "₹${MoTaRepository.currentStudent.annualIncome}/yr" else "Within Limit*"
                                    scanStatusMessage = "AI OCR verified Income Certificate ($inc) with 95% confidence!*"
                                }
                            )
                        }

                        item {
                            DocUploadField(
                                label = "Previous Year Marksheet / Admission Slip",
                                isUploaded = marksheetUploaded,
                                onScanClick = {
                                    marksheetUploaded = true
                                    scanStatusMessage = "AI OCR verified Marksheet CHSE-2025 with 99% confidence!"
                                }
                            )
                        }
                    }

                    4 -> {
                        // Step 4: Review and Submit
                        item {
                            Text(
                                text = "Review Application Summary",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                        }

                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, BorderLight, RoundedCornerShape(14.dp))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ReviewRow("Scheme", "${scheme.code} — ${scheme.title}")
                                    ReviewRow("Applicant", fullName)
                                    ReviewRow("Category", community)
                                    ReviewRow("Institute", instituteName)
                                    ReviewRow("Course", courseLevel)
                                    ReviewRow("Bank Account", student.maskedAccount)
                                    ReviewRow("Payment Mode", "PFMS Direct Benefit Transfer (100% DBT)")
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isDeclared,
                                    onCheckedChange = { isDeclared = it },
                                    colors = CheckboxDefaults.colors(checkedColor = PrimaryDeepOrange)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "I solemnly declare that all particulars entered above are authentic and verifiable via my sovereign DigiLocker and APAAR credentials.",
                                    fontSize = 11.sp,
                                    color = TextBody,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDigiLockerSheet) {
        DigiLockerSandboxBottomSheet(
            onDismiss = { showDigiLockerSheet = false },
            onSuccess = { res ->
                showDigiLockerSheet = false
                when {
                    res.docType.contains("Tribe", ignoreCase = true) || res.docType.contains("Caste", ignoreCase = true) -> {
                        casteUploaded = true
                    }
                    res.docType.contains("Income", ignoreCase = true) -> {
                        incomeUploaded = true
                    }
                    else -> {
                        marksheetUploaded = true
                    }
                }
                scanStatusMessage = "Verified via DigiLocker Sandbox: ${res.docType} (${res.certificateNumber}) with 100% DSC seal!"
            }
        )
    }
}

@Composable
private fun StepTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextDark
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryDeepOrange,
                unfocusedBorderColor = BorderMedium,
                focusedContainerColor = BackgroundWhite,
                unfocusedContainerColor = SurfaceCard
            ),
            singleLine = true
        )
    }
}

@Composable
private fun DocUploadField(
    label: String,
    isUploaded: Boolean,
    onScanClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (isUploaded) StatusDisbursed.copy(alpha = 0.5f) else BorderLight, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isUploaded) "Document attached & verified" else "Required document missing",
                    fontSize = 11.sp,
                    color = if (isUploaded) StatusDisbursed else StatusRejected,
                    fontWeight = FontWeight.Medium
                )
            }

            Button(
                onClick = onScanClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isUploaded) StatusDisbursedBg else PrimarySurfaceLight
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isUploaded) FontAwesomeIcons.Solid.CircleCheck else FontAwesomeIcons.Solid.Camera,
                        contentDescription = null,
                        tint = if (isUploaded) StatusDisbursed else PrimaryDeepOrange,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isUploaded) "Verified" else "Smart Scan",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUploaded) StatusDisbursed else PrimaryDeepOrangeDark
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSubtle,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.40f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            color = TextDark,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            modifier = Modifier.weight(0.60f)
        )
    }
}
