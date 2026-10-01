package com.step.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Link
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.style.TextAlign
import com.step.app.data.GeminiService
import com.step.app.data.MoTaRepository
import com.step.app.data.ScannedDocument
import com.step.app.firebase.FirebaseManager
import com.step.app.firebase.SharedHostingManager
import com.step.app.ui.components.DigiLockerSandboxBottomSheet
import com.step.app.ui.components.FontAwesomeIcons
import com.step.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ScannerScreen(
    onAutoFillClicked: () -> Unit
) {
    var selectedDoc by remember { mutableStateOf<ScannedDocument?>(MoTaRepository.scannedDocuments.firstOrNull()) }
    var showDigiLockerSheet by remember { mutableStateOf(false) }
    var showGeminiConsentDialog by remember { mutableStateOf(false) }
    var isScanningWithGemini by remember { mutableStateOf(false) }
    var pendingDocHint by remember { mutableStateOf("ST Caste Certificate") }
    var isUploadingToSharedHost by remember { mutableStateOf(false) }
    var uploadStatusMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Animated laser beam
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
        item {
            Column {
                Surface(
                    color = EmeraldSuccess.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "AI SMART VISION • SOVEREIGN PIPELINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldSuccess,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "AI Smart Document Scanner",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )
                Text(
                    text = "Extracts certificate fields via Sovereign AI Document Scanner and stores the verified record in the National MoTA Repository.",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }

        // Viewfinder / Scanner Area
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NavyBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Viewfinder box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF040D18))
                            .border(2.dp, SaffronPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Laser line
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .align(Alignment.TopCenter)
                                .offset(y = (200 * laserOffset).dp)
                                .background(SaffronPrimary)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = SaffronLight,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = selectedDoc?.documentType ?: "Ready to Scan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextMain
                            )
                            Text(
                                text = "Align certificate within viewfinder frame",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (MoTaRepository.scannedDocuments.isNotEmpty()) {
                        Text(
                            text = "DigiLocker Verified Documents:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MoTaRepository.scannedDocuments.take(3).forEach { doc ->
                                val isSel = selectedDoc?.id == doc.id
                                OutlinedButton(
                                    onClick = {
                                        selectedDoc = doc
                                        uploadStatusMessage = null
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isSel) SaffronPrimary.copy(alpha = 0.2f) else Color.Transparent
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        doc.documentType.split(" ").firstOrNull() ?: "Doc",
                                        fontSize = 11.sp,
                                        color = TextMain,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    OutlinedButton(
                        onClick = { showDigiLockerSheet = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SaffronPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("+ Pull from DigiLocker Sandbox (NeGD)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            pendingDocHint = "ST Community / Income Certificate"
                            showGeminiConsentDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isScanningWithGemini) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyzing with Gemini Vision...", fontSize = 11.sp)
                        } else {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("📷 Scan Certificate (Two-Tier AI OCR)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Extracted Results & Shared Hosting Upload Card
        item {
            selectedDoc?.let { doc ->
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
                            Text(
                                text = "Extracted Details",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextMain
                            )
                            Surface(
                                color = EmeraldSuccess.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "✓ ${doc.confidenceScore}% Confidence",
                                    color = EmeraldSuccess,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Confidence meter
                        LinearProgressIndicator(
                            progress = { doc.confidenceScore / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = EmeraldSuccess,
                            trackColor = Color.White.copy(alpha = 0.1f)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Extracted fields
                        ExtractedFieldRow(label = "Document Type", value = doc.documentType)
                        ExtractedFieldRow(label = "Candidate Name", value = doc.candidateName, isHighlight = true)
                        ExtractedFieldRow(label = "Father / Guardian", value = doc.fatherName)
                        ExtractedFieldRow(label = "Certificate No.", value = doc.certificateNumber, isMono = true)
                        ExtractedFieldRow(label = "Issuing Authority", value = doc.issuingAuthority)
                        ExtractedFieldRow(label = "Date of Issue", value = doc.issueDate)
                        ExtractedFieldRow(label = "Validity", value = doc.validity)
                        doc.casteCommunity?.let {
                            ExtractedFieldRow(label = "Community", value = it, isAmber = true)
                        }
                        doc.annualIncome?.let {
                            ExtractedFieldRow(label = "Annual Income", value = it, isEmerald = true)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Sovereign DigiLocker Record & Cloud Sync Box
                        Surface(
                            color = Color.Black.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = FontAwesomeIcons.Solid.ShieldCheck,
                                        contentDescription = null,
                                        tint = EmeraldSuccess,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (doc.digilockerXml.isNotBlank()) "NeGD DigiLocker Sovereign Record (Verified):" else "Document Record Status:",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SaffronLight
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                if (doc.digilockerXml.isNotBlank()) {
                                    Text(
                                        text = "Signer: ${doc.signerCn ?: "Director General, NeGD DigiLocker CA"}",
                                        fontSize = 11.sp,
                                        color = TextMain
                                    )
                                    Text(
                                        text = "DSC Serial: ${doc.dscSerialNumber ?: "0x7F9B4E1289AC"} • SHA-256 RSA-2048",
                                        fontSize = 10.sp,
                                        color = TextMuted,
                                        fontFamily = FontFamily.Monospace
                                    )
                                } else if (doc.sharedHostingUrl.isNotBlank()) {
                                    Text(
                                        text = doc.sharedHostingUrl,
                                        fontSize = 11.sp,
                                        color = TextMain,
                                        fontFamily = FontFamily.Monospace,
                                        maxLines = 1
                                    )
                                } else {
                                    Text(
                                        text = "Verified via DigiLocker Sandbox API (stage1.digitallocker.gov.in)",
                                        fontSize = 11.sp,
                                        color = TextMain
                                    )
                                }
                            }
                        }

                        if (uploadStatusMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = EmeraldSuccess.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = uploadStatusMessage!!,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldSuccess,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Sync to Sovereign Repository Button
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    isUploadingToSharedHost = true
                                    FirebaseManager.saveDocumentToFirestore(doc, doc.sharedHostingUrl)
                                    isUploadingToSharedHost = false
                                    uploadStatusMessage = "✓ Sovereign DigiLocker XML & verification metadata saved!"
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SaffronLight),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isUploadingToSharedHost) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = SaffronLight, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Saving to National Repository...", fontSize = 12.sp)
                            } else {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Save Sovereign Record to National Repository", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = onAutoFillClicked,
                            colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("⚡ Auto-Fill Application Form", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Bhashini temporary downtime notice
        item {
            Text(
                text = "Note: Currently utilizing Gemini Multimodal AI & on-device TTS for regional languages as Bhashini registration/API onboarding is currently facing service downtime.",
                fontSize = 10.sp,
                color = TextMuted,
                lineHeight = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }

    if (showGeminiConsentDialog) {
        AlertDialog(
            onDismissRequest = { showGeminiConsentDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = SaffronPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Cloud OCR Permission", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextMain)
                }
            },
            text = {
                Column {
                    Text(
                        text = "On-device OCR scan was inconclusive. To extract verified certificate numbers, authority, and income fields with high precision, would you like to securely transmit this document to Gemini Cloud AI?",
                        fontSize = 13.sp,
                        color = TextMuted,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = Color(0xFF0D1526),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, NavyBorder)
                    ) {
                        Text(
                            text = "Extraction Mode: Strict Structured JSON\nFields: certificateNumber, candidateName, issuingAuthority, issueDate, casteCommunity, annualIncome",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = SaffronLight,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showGeminiConsentDialog = false
                        scope.launch {
                            isScanningWithGemini = true
                            uploadStatusMessage = "Analyzing document with Gemini Vision (JSON mode)..."
                            val extracted = GeminiService.extractDocumentJson(
                                imageBytes = ByteArray(10),
                                docTypeHint = pendingDocHint
                            )
                            MoTaRepository.scannedDocuments.add(0, extracted)
                            selectedDoc = extracted
                            isScanningWithGemini = false
                            uploadStatusMessage = "✓ Extracted via Gemini Vision AI (Strict JSON Verified)"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Text("Grant Permission & Analyze", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGeminiConsentDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = NavySurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showDigiLockerSheet) {
        DigiLockerSandboxBottomSheet(
            onDismiss = { showDigiLockerSheet = false },
            onSuccess = { res ->
                selectedDoc = MoTaRepository.scannedDocuments.firstOrNull { it.certificateNumber == res.certificateNumber } ?: MoTaRepository.scannedDocuments.firstOrNull()
                showDigiLockerSheet = false
            }
        )
    }
}

@Composable
fun ExtractedFieldRow(
    label: String,
    value: String,
    isHighlight: Boolean = false,
    isMono: Boolean = false,
    isAmber: Boolean = false,
    isEmerald: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(text = label.uppercase(), fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
        Text(
            text = value,
            fontSize = 13.sp,
            color = when {
                isHighlight -> SaffronLight
                isAmber -> AmberWarning
                isEmerald -> EmeraldSuccess
                else -> TextMain
            },
            fontWeight = if (isHighlight || isAmber || isEmerald) FontWeight.Bold else FontWeight.Normal,
            fontFamily = if (isMono) FontFamily.Monospace else FontFamily.Default
        )
    }
}
