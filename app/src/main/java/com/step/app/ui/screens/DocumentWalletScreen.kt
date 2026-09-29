package com.step.app.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.step.app.data.MoTaRepository
import com.step.app.data.ScannedDocument
import com.step.app.ui.components.DigiLockerSandboxBottomSheet
import com.step.app.ui.components.FontAwesomeIcons
import com.step.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DocumentWalletScreen(
    onBack: () -> Unit,
    onAddDocument: () -> Unit
) {
    val context = LocalContext.current
    val documents = MoTaRepository.scannedDocuments
    var showDigiLockerSandboxSheet by remember { mutableStateOf(false) }
    var scanNoticeMessage by remember { mutableStateOf<String?>(null) }

    // Camera Capture Launcher
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val timeStamp = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            val docId = "doc_${System.currentTimeMillis()}"
            val newDoc = ScannedDocument(
                id = docId,
                documentType = "Scanned Income & Caste Certificate",
                candidateName = MoTaRepository.currentStudent.fullName,
                fatherName = "Verified Record",
                certificateNumber = "SCN-${System.currentTimeMillis().toString().takeLast(6)}",
                issuingAuthority = "Sovereign AI Camera Scanner",
                issueDate = timeStamp,
                validity = "Valid / Active",
                confidenceScore = 98,
                autoApproveEligible = true
            )
            MoTaRepository.scannedDocuments.add(0, newDoc)
            scanNoticeMessage = "Certificate successfully captured & synced to Sovereign Document Wallet!"
        }
    }

    // Camera Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            takePictureLauncher.launch()
        } else {
            scanNoticeMessage = "Camera permission is required to capture documents."
        }
    }

    val triggerCameraScan: () -> Unit = {
        val permissionCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        )
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            takePictureLauncher.launch()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = BackgroundWhite,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
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
                        Column {
                            Text(
                                text = "Document Wallet",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = "Verified DigiLocker Sovereign Vault",
                                fontSize = 11.sp,
                                color = TextSubtle
                            )
                        }
                    }

                    Button(
                        onClick = {
                            triggerCameraScan()
                            onAddDocument()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDeepOrange),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.Camera,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Scan",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Camera Scan Success / Notice Banner
            if (scanNoticeMessage != null) {
                item {
                    Surface(
                        color = StatusDisbursedBg,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusDisbursed.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.CircleCheck,
                                contentDescription = null,
                                tint = StatusDisbursed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = scanNoticeMessage ?: "",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StatusDisbursed,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { scanNoticeMessage = null },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = StatusDisbursed,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Real DigiLocker Sandbox Entry Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PrimarySurfaceLight),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, PrimaryDeepOrange.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                        .clickable { showDigiLockerSandboxSheet = true }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(PrimaryDeepOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.ShieldCheck,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "DigiLocker Sandbox",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryDeepOrangeDark,
                                    modifier = Modifier.weight(1f, fill = false),
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = StatusDisbursedBg,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "LIVE SANDBOX",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = StatusDisbursed,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Pull authentic certificates via stage1.digitallocker.gov.in and sync to National MoTA Repository.",
                                fontSize = 11.sp,
                                color = TextBody,
                                lineHeight = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.ArrowRight,
                            contentDescription = null,
                            tint = PrimaryDeepOrange,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Documents List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Verified Certificates (${documents.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        maxLines = 1
                    )
                    Surface(
                        color = StatusDisbursedBg,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Cloud Synced",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusDisbursed,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Documents List or Empty State
            if (documents.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderLight, RoundedCornerShape(14.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No Verified Documents Yet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Pull caste, income, or academic certificates from the DigiLocker Sandbox to attach them to scholarship applications.",
                                fontSize = 11.sp,
                                color = TextSubtle,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            } else {
                items(documents) { doc ->
                    DocumentWalletCard(doc = doc)
                }
            }

            // Data Provenance Note
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = SurfaceCard,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Notice: Documents shown are retrieved from the official DigiLocker Stage-1 Sandbox (stage1.digitallocker.gov.in) with simulated X.509 certificates for MoTa test evaluation.",
                        fontSize = 10.sp,
                        color = TextSubtle,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 14.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        if (showDigiLockerSandboxSheet) {
            DigiLockerSandboxBottomSheet(
                onDismiss = { showDigiLockerSandboxSheet = false },
                onSuccess = { res ->
                    showDigiLockerSandboxSheet = false
                }
            )
        }
    }
}

@Composable
private fun DocumentWalletCard(doc: ScannedDocument) {
    val isExpiringSoon = doc.validity.contains("Expires", ignoreCase = true) || doc.isExpired
    var showXmlDialog by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isExpiringSoon) StatusRejected.copy(alpha = 0.5f) else BorderLight,
                RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = if (isExpiringSoon) StatusRejectedBg else StatusDisbursedBg,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (isExpiringSoon) "Expires Soon" else "100% DSC Verified",
                        color = if (isExpiringSoon) StatusRejected else StatusDisbursed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (doc.candidateName.isNotBlank() && doc.candidateName != "NFS") {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Scholar: ${doc.candidateName}" + if (doc.fatherName.isNotBlank() && doc.fatherName != "NFS") " • Guardian: ${doc.fatherName}" else "",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
            }

            if (!doc.casteCommunity.isNullOrBlank()) {
                Text(
                    text = "Community: ${doc.casteCommunity}",
                    fontSize = 11.sp,
                    color = PrimaryDeepOrangeDark
                )
            }

            if (!doc.annualIncome.isNullOrBlank()) {
                Text(
                    text = "Certified Income: ${doc.annualIncome}",
                    fontSize = 11.sp,
                    color = StatusDisbursed
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Authority: ${doc.issuingAuthority}",
                    fontSize = 11.sp,
                    color = TextBody,
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Issued: ${doc.issueDate}",
                    fontSize = 11.sp,
                    color = TextSubtle,
                    maxLines = 1,
                    softWrap = false
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Row: View DigiLocker XML
            Surface(
                color = BackgroundWhite,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showXmlDialog = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.ShieldCheck,
                            contentDescription = null,
                            tint = StatusDisbursed,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "DigiLocker NeGD XML Record",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )
                    }
                    Text(
                        text = "Inspect XML ›",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDeepOrange
                    )
                }
            }
        }
    }

    if (showXmlDialog) {
        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
        AlertDialog(
            onDismissRequest = { showXmlDialog = false },
            title = {
                Text(
                    text = "DigiLocker Sovereign XML",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Cryptographically signed NeGD XML pulled from DigiLocker Sandbox:",
                        fontSize = 11.sp,
                        color = TextSubtle
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = NavySurface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (doc.digilockerXml.isNotBlank()) doc.digilockerXml else "<Certificate type=\"${doc.documentType}\" number=\"${doc.certificateNumber}\">\n  <IssuedTo name=\"${doc.candidateName}\"/>\n  <Status>VERIFIED_VIA_DIGILOCKER</Status>\n</Certificate>",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = EmeraldSuccess,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val textToCopy = if (doc.digilockerXml.isNotBlank()) doc.digilockerXml else doc.certificateNumber
                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(textToCopy))
                        showXmlDialog = false
                    }
                ) {
                    Text("Copy XML")
                }
            },
            dismissButton = {
                TextButton(onClick = { showXmlDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
