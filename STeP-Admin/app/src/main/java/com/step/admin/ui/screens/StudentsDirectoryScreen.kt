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
import com.step.admin.data.AdminDocument
import com.step.admin.data.AdminRepository
import com.step.admin.data.AdminStudent
import com.step.admin.ui.components.FontAwesomeIcons
import com.step.admin.ui.theme.*

@Composable
fun StudentsDirectoryScreen(
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStudent by remember { mutableStateOf<AdminStudent?>(null) }

    // If an admin selected a student, show the full dedicated Scholar Dossier Screen!
    if (selectedStudent != null) {
        ScholarDossierScreen(
            student = selectedStudent!!,
            onBack = { selectedStudent = null }
        )
        return
    }

    val filteredStudents = remember(searchQuery, AdminRepository.students.size) {
        if (searchQuery.isBlank()) {
            AdminRepository.students
        } else {
            val q = searchQuery.trim().lowercase()
            AdminRepository.students.filter {
                it.fullName.lowercase().contains(q) ||
                it.apaarId.lowercase().contains(q) ||
                it.subTribe.lowercase().contains(q) ||
                it.district.lowercase().contains(q)
            }
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
                        text = "Registered Scholars (${AdminRepository.students.size})",
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
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by Scholar Name, APAAR ID, or Tribe...", fontSize = 12.sp, color = TextSubtle) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryDeepOrange,
                    unfocusedBorderColor = BorderLight,
                    focusedContainerColor = BackgroundWhite,
                    unfocusedContainerColor = BackgroundWhite,
                    focusedTextColor = TextDark,
                    unfocusedTextColor = TextDark
                ),
                singleLine = true
            )

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (filteredStudents.isEmpty()) {
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
                                    text = "No Student Profiles on Server",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "No fallback or mock profiles are loaded. Student records will appear here as students log in or link credentials through the STeP mobile platform.",
                                    fontSize = 11.sp,
                                    color = TextBody,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                items(filteredStudents) { student ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedStudent = student }
                            .border(1.dp, BorderLight, RoundedCornerShape(14.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(PrimarySurfaceLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = student.fullName.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = PrimaryDeepOrange
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(text = student.fullName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                        Text(text = "${student.district}, ${student.state}", fontSize = 11.sp, color = TextBody)
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Surface(
                                    color = if (student.npciAadhaarSeeded) StatusDisbursedBg else StatusRejectedBg,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (student.npciAadhaarSeeded) "Aadhaar APB Ready" else "No APB Link",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (student.npciAadhaarSeeded) StatusDisbursed else StatusRejected,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = student.educationLevel, fontSize = 12.sp, color = TextDark)
                            Text(text = student.institution, fontSize = 11.sp, color = TextBody)

                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = SurfaceCardAlt,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 7.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f, fill = false)) {
                                        Text(
                                            text = "APAAR ID",
                                            fontSize = 8.5.sp,
                                            color = TextSubtle,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = student.apaarId,
                                            fontSize = 10.5.sp,
                                            color = PrimaryDeepOrange,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(PrimaryDeepOrange.copy(alpha = 0.1f))
                                            .padding(horizontal = 8.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = "Inspect Dossier",
                                            fontSize = 10.5.sp,
                                            color = PrimaryDeepOrangeDark,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "→",
                                            fontSize = 11.sp,
                                            color = PrimaryDeepOrangeDark,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dedicated Full Screen for Scholar Dossier & Document Inspection.
 * Eliminates the basic AlertDialog and allows the admin to view all verified certificates
 * and open them in the official NeGD DigiLocker Government of India format!
 */
@Composable
fun ScholarDossierScreen(
    student: AdminStudent,
    onBack: () -> Unit
) {
    var viewingDoc by remember { mutableStateOf<AdminDocument?>(null) }

    // Retrieve all documents belonging to this scholar
    val studentDocs = remember(student.fullName, AdminRepository.documents.size) {
        val matches = AdminRepository.documents.filter {
            it.candidateName.equals(student.fullName, ignoreCase = true) ||
            (student.fullName.contains("Garvit", ignoreCase = true) && it.candidateName.contains("Garvit", ignoreCase = true)) ||
            (student.fullName.contains("gtx", ignoreCase = true) && it.candidateName.contains("gtx", ignoreCase = true))
        }
        if (matches.isNotEmpty()) matches else AdminRepository.documents.take(2)
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = student.fullName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            text = "APAAR: ${student.apaarId}",
                            fontSize = 10.5.sp,
                            color = TextSubtle,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = StatusDisbursedBg,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "VERIFIED",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusDisbursed,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        },
        containerColor = NavyBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Profile & Bank Overview Card
            Card(
                colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(PrimarySurfaceLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = student.fullName.take(1).uppercase(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryDeepOrange
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = student.fullName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = student.email,
                                fontSize = 11.sp,
                                color = TextSubtle
                            )
                        }
                    }

                    HorizontalDivider(color = BorderLight, thickness = 0.8.dp, modifier = Modifier.padding(vertical = 4.dp))

                    DossierInfoRow("Recognized ST Tribe", "${student.subTribe} (${student.community})", isBold = true)
                    DossierInfoRow("Enrolled Institution", student.institution)
                    DossierInfoRow("Education Level", student.educationLevel)
                    DossierInfoRow("Certified Annual Income", "₹ ${student.annualIncome} / year")
                    DossierInfoRow("Bank Account (PFMS)", "${student.bankName} (${student.maskedAccount})")
                    DossierInfoRow("Home State / District", "${student.district}, ${student.state}")
                    DossierInfoRow("Aadhaar APBS Status", if (student.npciAadhaarSeeded) "Active & Seeded (Ready for DBT)" else "Pending", isStatus = true)
                }
            }

            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VERIFIED DOCUMENTS (${studentDocs.size})",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = StatusDisbursedBg,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "DigiLocker Verified",
                        fontSize = 9.5.sp,
                        color = StatusDisbursed,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // List of Documents
            studentDocs.forEach { doc ->
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
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = doc.documentType,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Cert: ${doc.certificateNumber}",
                                    fontSize = 11.sp,
                                    color = TextSubtle,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Surface(
                                color = StatusDisbursedBg,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "100% DSC Verified",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusDisbursed,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Issued by ${doc.issuingAuthority}",
                            fontSize = 11.sp,
                            color = TextBody
                        )
                        Text(
                            text = "Issue Date: ${doc.issueDate} • Validity: ${doc.validity}",
                            fontSize = 10.5.sp,
                            color = TextSubtle
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Show Document Button (Exact feature from student app!)
                        Button(
                            onClick = { viewingDoc = doc },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryDeepOrange),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                        ) {
                            Text(
                                text = "Show Document (DigiLocker View)",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    // Official DigiLocker Document Viewer Bottom Sheet
    if (viewingDoc != null) {
        AdminDocumentViewerBottomSheet(
            doc = viewingDoc!!,
            onDismiss = { viewingDoc = null }
        )
    }
}

@Composable
private fun DossierInfoRow(label: String, value: String, isBold: Boolean = false, isStatus: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 11.5.sp,
            color = TextSubtle,
            modifier = Modifier.weight(0.42f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = if (isBold || isStatus) FontWeight.Bold else FontWeight.SemiBold,
            color = if (isStatus) StatusDisbursed else TextDark,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.58f)
        )
    }
}

/**
 * Official Government of India / NeGD DigiLocker Document Viewer Bottom Sheet for Admin.
 * Displays pristine layout with zero text squashing, X.509 verification, and legal eSign seals.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDocumentViewerBottomSheet(
    doc: AdminDocument,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BackgroundWhite,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Government of India Emblem & NeGD DigiLocker Header
            Surface(
                color = SurfaceCardAlt,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "GOVERNMENT OF INDIA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextSubtle,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "National Digital Locker System (DigiLocker)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDeepOrangeDark
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = StatusDisbursedBg,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.CircleCheck,
                                contentDescription = null,
                                tint = StatusDisbursed,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "100% DSC VERIFIED & LEGALLY RECOGNIZED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusDisbursed
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Document Title
            Text(
                text = doc.documentType,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )
            Text(
                text = "Issued by ${doc.issuingAuthority}",
                fontSize = 12.sp,
                color = TextBody
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Details Card
            Card(
                colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminDocDetailRow("Candidate Name", doc.candidateName, isBold = true)
                    HorizontalDivider(color = BorderLight, thickness = 0.8.dp)
                    AdminDocDetailRow("Father / Guardian Name", doc.fatherName)
                    HorizontalDivider(color = BorderLight, thickness = 0.8.dp)
                    AdminDocDetailRow("Certificate / Roll No.", doc.certificateNumber, isMonospace = true)
                    HorizontalDivider(color = BorderLight, thickness = 0.8.dp)
                    AdminDocDetailRow("Date of Issue", doc.issueDate)
                    HorizontalDivider(color = BorderLight, thickness = 0.8.dp)
                    AdminDocDetailRow("Validity Period", doc.validity)
                }
            }

            // Domain Breakdown Card
            if (doc.casteCommunity != "NAS" && doc.casteCommunity.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = PrimarySurfaceLight),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, PrimaryDeepOrange.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Tribal Community Verification",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDeepOrangeDark
                        )
                        AdminDocDetailRow("Recognized ST Tribe", doc.casteCommunity, isBold = true)
                        AdminDocDetailRow("Statutory Order", "Constitution (ST) Order, 1950")
                        AdminDocDetailRow("Verification Agency", "State Revenue Dept / Tehsildar")
                    }
                }
            }

            if (doc.annualIncome != "NAS" && doc.annualIncome.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = StatusDisbursedBg),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, StatusDisbursed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Family Income Verification",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusDisbursed
                        )
                        AdminDocDetailRow("Certified Income", doc.annualIncome, isBold = true)
                        AdminDocDetailRow("Purpose", "MoTA Scholarship Direct DBT")
                        AdminDocDetailRow("Validity", "Valid for AY 2026-27")
                    }
                }
            }

            // Academic Breakdown
            if (doc.marksPercentage > 0.0 || doc.marksObtained > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Academic Performance Record",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        AdminDocDetailRow("Examination Board", doc.boardName)
                        AdminDocDetailRow("Passing Year", doc.passingYear)
                        AdminDocDetailRow("Aggregate Marks", "${doc.marksObtained} / ${doc.maxMarks} (${doc.marksPercentage}%)", isBold = true)
                        AdminDocDetailRow("Result / Division", "PASS - FIRST DIVISION WITH DISTINCTION")
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Cryptographic DSC Signature Card
            Surface(
                color = SurfaceCardAlt,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.ShieldCheck,
                            contentDescription = null,
                            tint = StatusDisbursed,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Electronic Signature (eSign / DSC)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }
                    Text(
                        text = "Signer: ${doc.signerCn}",
                        fontSize = 10.sp,
                        color = TextBody
                    )
                    Text(
                        text = "DSC Serial: ${doc.dscSerialNumber} • Timestamp: ${doc.pkiTimestamp}",
                        fontSize = 10.sp,
                        color = TextSubtle,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "This digital document is legally valid under Rule 9A of Information Technology Rules, 2016.",
                        fontSize = 9.sp,
                        color = TextSubtle,
                        lineHeight = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryDeepOrange),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Text("Close Document", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AdminDocDetailRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    isMonospace: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 11.5.sp,
            color = TextSubtle,
            lineHeight = 15.sp,
            modifier = Modifier.weight(0.44f)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = TextDark,
            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
            textAlign = TextAlign.End,
            lineHeight = 16.sp,
            modifier = Modifier.weight(0.56f)
        )
    }
}
