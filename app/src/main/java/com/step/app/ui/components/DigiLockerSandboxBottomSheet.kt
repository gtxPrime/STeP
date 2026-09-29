package com.step.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.step.app.digilocker.DigiLockerSandboxManager
import com.step.app.digilocker.DigiLockerSandboxResult
import com.step.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DigiLockerSandboxBottomSheet(
    onDismiss: () -> Unit,
    onSuccess: (DigiLockerSandboxResult) -> Unit
) {
    val scope = rememberCoroutineScope()
    val testCerts = remember { DigiLockerSandboxManager.getSandboxTestCertificates() }
    var selectedDocType by remember { mutableStateOf("CASTC") }
    var certNumber by remember { mutableStateOf("OD/ST/2022/49201") }
    val currentStudent = com.step.app.data.MoTaRepository.currentStudent
    var candidateName by remember { mutableStateOf(if (currentStudent.fullName.isNotBlank() && currentStudent.fullName != "NFS") currentStudent.fullName else "Scholar") }
    var fatherName by remember { mutableStateOf(if (currentStudent.subTribe.isNotBlank() && currentStudent.subTribe != "NFS") "Parent (${currentStudent.subTribe})" else "Parent / Guardian") }
    var isPulling by remember { mutableStateOf(false) }
    var pullResult by remember { mutableStateOf<DigiLockerSandboxResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BackgroundWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = BorderMedium,
                height = 4.dp,
                width = 36.dp
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = StatusDisbursedBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "NEGD • DIGILOCKER REAL SANDBOX GATEWAY",
                        color = StatusDisbursed,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Surface(
                    color = PrimarySurfaceLight,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "STAGE1 ENV",
                        color = PrimaryDeepOrangeDark,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Pull Authentic Sovereign Certificate",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Text(
                text = "Connects to stage1.digitallocker.gov.in using MoTA Pull URI API. Verified certificates are saved directly to the National MoTA Repository.",
                fontSize = 11.sp,
                color = TextSubtle,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Certificate Type Selector
            Text(
                text = "Select Document Type:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                testCerts.forEach { cert ->
                    val isSelected = selectedDocType == cert.docType
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) PrimarySurfaceLight else SurfaceCard
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (isSelected) PrimaryDeepOrange else BorderLight,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                selectedDocType = cert.docType
                                certNumber = when (cert.docType) {
                                    "CASTC" -> "OD/ST/2022/49201"
                                    "INCMC" -> "OD/INC/2025/11093"
                                    else -> "CHSE-2025-881924"
                                }
                                pullResult = null
                                errorMessage = null
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) PrimaryDeepOrange else BorderMedium),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = cert.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Text(
                                    text = cert.issuerName,
                                    fontSize = 10.sp,
                                    color = TextSubtle
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Certificate Number Field
            Text(
                text = "Certificate / Registration Number:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = certNumber,
                onValueChange = { certNumber = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryDeepOrange,
                    unfocusedBorderColor = BorderMedium
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Candidate Name Field (User Entered or Pre-filled)
            Text(
                text = "Scholar / Candidate Name (As on Certificate):",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = candidateName,
                onValueChange = { candidateName = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryDeepOrange,
                    unfocusedBorderColor = BorderMedium
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Father Name Field
            Text(
                text = "Father / Guardian Name:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = fatherName,
                onValueChange = { fatherName = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryDeepOrange,
                    unfocusedBorderColor = BorderMedium
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Success Card if pulled
            if (pullResult != null) {
                var showXml by remember { mutableStateOf(false) }
                Card(
                    colors = CardDefaults.cardColors(containerColor = StatusDisbursedBg),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.ShieldCheck,
                                contentDescription = null,
                                tint = StatusDisbursed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "100% Cryptographic DSC Verified via DigiLocker XML!",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusDisbursed
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "DigiLocker URN: ${pullResult!!.uri}",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextDark
                        )
                        Text(
                            text = "Signer: ${pullResult!!.signerCn}",
                            fontSize = 9.sp,
                            color = TextSubtle
                        )
                        Text(
                            text = "Saved to National MoTA Repository (DigiLocker Verified)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = StatusDisbursed
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { showXml = !showXml },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (showXml) "Hide DigiLocker XML" else "Inspect Sovereign DigiLocker XML", fontSize = 11.sp)
                        }
                        if (showXml && pullResult!!.xmlPayload.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                color = NavySurface,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = pullResult!!.xmlPayload,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = EmeraldSuccess,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Pull Action Button
            Button(
                onClick = {
                    if (pullResult != null) {
                        onSuccess(pullResult!!)
                        onDismiss()
                    } else {
                        isPulling = true
                        errorMessage = null
                        scope.launch {
                            val res = DigiLockerSandboxManager.pullCertificateFromSandbox(
                                docType = selectedDocType,
                                certificateNumber = certNumber,
                                candidateName = candidateName,
                                fatherName = fatherName
                            )
                            isPulling = false
                            if (res.success) {
                                pullResult = res
                                onSuccess(res)
                            } else {
                                errorMessage = res.message
                            }
                        }
                    }
                },
                enabled = !isPulling && certNumber.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryDeepOrange),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isPulling) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Connecting to DigiLocker Sandbox...", color = Color.White, fontSize = 13.sp)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.ShieldCheck,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (pullResult != null) "Done & Close" else "Pull from DigiLocker Sandbox",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
