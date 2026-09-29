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
import com.step.app.data.MoTaRepository
import com.step.app.data.ScannedDocument
import com.step.app.firebase.FirebaseManager
import com.step.app.firebase.SharedHostingManager
import com.step.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ScannerScreen(
    onAutoFillClicked: () -> Unit
) {
    var selectedDoc by remember { mutableStateOf<ScannedDocument?>(MoTaRepository.sampleCasteDoc) }
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
                        text = "AI SMART VISION • SHARED HOSTING PIPELINE",
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
                    text = "Extracts fields via AI Vision, uploads image to Shared Hosting CDN, and stores the permanent link in Firebase Firestore.",
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

                    Text(
                        text = "1-Click Sandbox Test Documents*:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                selectedDoc = MoTaRepository.sampleCasteDoc
                                uploadStatusMessage = null
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedDoc?.documentType?.contains("Caste") == true) SaffronPrimary.copy(alpha = 0.2f) else Color.Transparent
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("ST Caste*", fontSize = 11.sp, color = TextMain)
                        }

                        OutlinedButton(
                            onClick = {
                                selectedDoc = MoTaRepository.sampleIncomeDoc
                                uploadStatusMessage = null
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedDoc?.documentType?.contains("Income") == true) SaffronPrimary.copy(alpha = 0.2f) else Color.Transparent
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Income*", fontSize = 11.sp, color = TextMain)
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

                        // Shared Hosting & Firebase Sync Box
                        Surface(
                            color = Color.Black.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Link, contentDescription = null, tint = SaffronLight, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Shared Hosting CDN URL (Stored in Firebase):",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SaffronLight
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = doc.sharedHostingUrl,
                                    fontSize = 11.sp,
                                    color = TextMain,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1
                                )
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

                        // Upload to Shared Hosting + Save Link in Firebase Button
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    isUploadingToSharedHost = true
                                    val dummyBytes = ByteArray(1024 * 64)
                                    val uploadResult = SharedHostingManager.uploadToSharedHosting(
                                        dummyBytes,
                                        doc.documentType,
                                        doc.candidateName
                                    )
                                    FirebaseManager.saveDocumentToFirestore(doc, uploadResult.publicUrl)
                                    isUploadingToSharedHost = false
                                    uploadStatusMessage = "✓ Image uploaded to Shared Hosting (${uploadResult.storageServer}) • Link saved to Firebase Firestore!"
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SaffronLight),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isUploadingToSharedHost) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = SaffronLight, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Uploading to Shared Hosting...", fontSize = 12.sp)
                            } else {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Upload Image to Shared Hosting & Link to Firebase", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
