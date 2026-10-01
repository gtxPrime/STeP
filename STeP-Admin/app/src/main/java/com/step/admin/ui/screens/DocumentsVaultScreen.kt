package com.step.admin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.step.admin.data.AdminDocument
import com.step.admin.data.AdminRepository
import com.step.admin.ui.theme.*

@Composable
fun DocumentsVaultScreen(
    onBack: () -> Unit
) {
    var selectedDoc by remember { mutableStateOf<AdminDocument?>(null) }

    Scaffold(
        topBar = {
            Surface(
                color = NavySurface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 20.sp, color = TextMain, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Sovereign Document Vault", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextMain)
                        Text("NeGD DigiLocker Sandbox & PKI DSC Scrutiny", fontSize = 11.sp, color = TextMuted)
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
            if (AdminRepository.documents.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = NavySurface),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                            .border(1.dp, NavyBorder, RoundedCornerShape(14.dp))
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
                                color = SaffronLight
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "No Verified Documents on Server",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMain
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "No fallback or mock document payloads are loaded. Cryptographically signed NeGD DigiLocker XML certificates will be cataloged here once fetched from the real server.",
                                fontSize = 11.sp,
                                color = TextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            items(AdminRepository.documents) { doc ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedDoc = doc }
                        .border(1.dp, NavyBorder, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = doc.documentType, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextMain)
                            Surface(
                                color = EmeraldSuccess.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "DSC Verified (${doc.confidenceScore}%)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldSuccess,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Candidate: ${doc.candidateName} (Father: ${doc.fatherName})", fontSize = 12.sp, color = TextMuted)
                        
                        if (doc.rollNumber != "NAS" && doc.rollNumber.isNotBlank()) {
                            Text(text = "Roll Number: ${doc.rollNumber}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SaffronLight, fontFamily = FontFamily.Monospace)
                        } else {
                            Text(text = "Cert Number: ${doc.certificateNumber}", fontSize = 11.sp, color = SaffronLight, fontFamily = FontFamily.Monospace)
                        }

                        if (doc.boardName != "NAS" && doc.boardName.isNotBlank()) {
                            Text(text = "Board / Council: ${doc.boardName}", fontSize = 11.sp, color = TextMain)
                        } else {
                            Text(text = "Issuing Authority: ${doc.issuingAuthority}", fontSize = 11.sp, color = TextMain)
                        }

                        if (doc.marksPercentage > 0.0 || doc.marksObtained > 0) {
                            Text(
                                text = "Academic Score: ${if (doc.marksObtained > 0) "${doc.marksObtained}/${doc.maxMarks} • " else ""}${doc.marksPercentage}% (PASS)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldSuccess
                            )
                        }

                        if (doc.annualIncome != "NAS" && doc.annualIncome.isNotBlank()) {
                            Text(text = "Verified Annual Income: ${doc.annualIncome}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SaffronLight)
                        }

                        if (doc.casteCommunity != "NAS" && doc.casteCommunity.isNotBlank()) {
                            Text(text = "Verified Tribe: ${doc.casteCommunity}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = BlueAccent)
                        }

                        Text(text = "Issue Date: ${doc.issueDate} • Validity: ${doc.validity}", fontSize = 11.sp, color = TextMuted)

                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = NavyCard,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "DSC Serial: ${doc.dscSerialNumber}", fontSize = 10.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                                Text(text = "Tap to Inspect Sovereign XML", fontSize = 10.sp, color = BlueAccent, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedDoc != null) {
        val d = selectedDoc!!
        AlertDialog(
            onDismissRequest = { selectedDoc = null },
            title = {
                Text(text = "NeGD DigiLocker Sovereign Verification Dossier", fontWeight = FontWeight.Bold, color = TextMain, fontSize = 16.sp)
            },
            text = {
                val scrollState = rememberScrollState()
                Column(modifier = Modifier.verticalScroll(scrollState)) {
                        Text(text = "Document: ${d.documentType}", fontWeight = FontWeight.Bold, color = SaffronLight, fontSize = 14.sp)
                        Text(text = "Candidate: ${d.candidateName}", color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        
                        if (d.rollNumber != "NAS" && d.rollNumber.isNotBlank()) {
                            Text(text = "Roll Number: ${d.rollNumber}", color = SaffronLight, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                        if (d.boardName != "NAS" && d.boardName.isNotBlank()) {
                            Text(text = "Exam Board: ${d.boardName}", color = TextMuted, fontSize = 12.sp)
                        }
                        if (d.passingYear != "NAS" && d.passingYear.isNotBlank()) {
                            Text(text = "Passing Year: ${d.passingYear}", color = TextMuted, fontSize = 12.sp)
                        }
                        if (d.marksPercentage > 0.0) {
                            Text(text = "Academic Marks: ${d.marksObtained}/${d.maxMarks} (${d.marksPercentage}%)", color = EmeraldSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        if (d.annualIncome != "NAS" && d.annualIncome.isNotBlank()) {
                            Text(text = "Extracted Income: ${d.annualIncome}", color = SaffronLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        if (d.casteCommunity != "NAS" && d.casteCommunity.isNotBlank()) {
                            Text(text = "Caste / Tribe: ${d.casteCommunity}", color = BlueAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "DSC Serial: ${d.dscSerialNumber}", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        Text(text = "Signer: ${d.signerCn}", color = TextMuted, fontSize = 10.sp)
                        Text(text = "PKI Timestamp: ${d.pkiTimestamp}", color = TextMuted, fontSize = 10.sp)
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "Raw Sovereign Signed XML / JSON Payload:", color = TextMain, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = NavyDark,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = d.digilockerXml,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = EmeraldSuccess,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
            },
            confirmButton = {
                Button(
                    onClick = { selectedDoc = null },
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary)
                ) {
                    Text("Close Dossier")
                }
            },
            containerColor = NavySurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
