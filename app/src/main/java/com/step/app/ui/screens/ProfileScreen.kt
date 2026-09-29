package com.step.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.step.app.data.MoTaRepository
import com.step.app.firebase.FirebaseManager
import com.step.app.ui.components.FontAwesomeIcons
import com.step.app.ui.theme.*

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onNavigateToDocumentWallet: () -> Unit,
    onLogout: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val student = MoTaRepository.currentStudent
    var notificationsEnabled by remember { mutableStateOf(true) }
    var smsAlertsEnabled by remember { mutableStateOf(true) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val updated = student.copy(photoUrl = uri.toString())
            MoTaRepository.currentStudent = updated
            FirebaseManager.saveStudentProfileToFirestore(updated, updated.uid, context)
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
                    verticalAlignment = Alignment.CenterVertically
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
                    Text(
                        text = "Scholar Profile",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundWhite)
                .padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Profile Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderLight, RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Centered avatar with camera badge
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .wrapContentSize()
                                .padding(4.dp)
                        ) {
                            // Circular Avatar
                            Box(
                                modifier = Modifier
                                    .size(86.dp)
                                    .clip(CircleShape)
                                    .background(PrimarySurfaceLight)
                                    .border(2.5.dp, PrimaryDeepOrange, CircleShape)
                                    .clickable { photoPickerLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (student.photoUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = student.photoUrl,
                                        contentDescription = "Profile Photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = FontAwesomeIcons.Solid.User,
                                        contentDescription = null,
                                        tint = PrimaryDeepOrange,
                                        modifier = Modifier.size(38.dp)
                                    )
                                }
                            }

                            // Camera badge overlay anchored at the bottom right rim of the avatar
                            Surface(
                                modifier = Modifier
                                    .size(28.dp)
                                    .align(Alignment.BottomEnd)
                                    .offset(x = 2.dp, y = 2.dp)
                                    .clickable { photoPickerLauncher.launch("image/*") },
                                shape = CircleShape,
                                color = PrimaryDeepOrange,
                                shadowElevation = 3.dp,
                                border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = FontAwesomeIcons.Solid.Camera,
                                        contentDescription = "Upload custom photo",
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = PrimarySurfaceLight,
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryDeepOrange.copy(alpha = 0.35f)),
                            modifier = Modifier.clickable { photoPickerLauncher.launch("image/*") }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = FontAwesomeIcons.Solid.Camera,
                                    contentDescription = null,
                                    tint = PrimaryDeepOrange,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Change Photo",
                                    fontSize = 11.sp,
                                    color = PrimaryDeepOrange,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = student.fullName,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = student.email,
                            fontSize = 12.sp,
                            color = TextSubtle,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Centered ID option badges
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                color = StatusDisbursedBg,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, StatusDisbursed.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "APAAR ID: ${student.apaarId}",
                                    color = StatusDisbursed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                            if (student.digilockerId.isNotBlank() && student.digilockerId != "NFS") {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = PrimarySurfaceLight,
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(0.8.dp, PrimaryDeepOrange.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = "DigiLocker: ${student.digilockerId}",
                                        color = PrimaryDeepOrangeDark,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Student Demographics & Bank Info Card
            item {
                Text(
                    text = "Identification & Bank Account",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderLight, RoundedCornerShape(14.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ProfileInfoRow(
                            label = "Category / Community",
                            value = if (student.subTribe == "NFS" && student.community == "NFS") "NFS" else if (student.subTribe == "NFS") student.community else "${student.subTribe} (${student.community})"
                        )
                        HorizontalDivider(color = BorderLight.copy(alpha = 0.6f), thickness = 0.8.dp)
                        ProfileInfoRow(
                            label = "Aadhaar Number",
                            value = if (student.aadhaarLast4 != "NFS" && student.aadhaarLast4.isNotBlank()) "•••• •••• ${student.aadhaarLast4}" else "NFS"
                        )
                        HorizontalDivider(color = BorderLight.copy(alpha = 0.6f), thickness = 0.8.dp)
                        ProfileInfoRow(label = "Bank Name", value = student.bankName)
                        HorizontalDivider(color = BorderLight.copy(alpha = 0.6f), thickness = 0.8.dp)
                        ProfileInfoRow(label = "Masked Account", value = student.maskedAccount)
                        HorizontalDivider(color = BorderLight.copy(alpha = 0.6f), thickness = 0.8.dp)
                        ProfileInfoRow(label = "Home State", value = student.state)
                        HorizontalDivider(color = BorderLight.copy(alpha = 0.6f), thickness = 0.8.dp)
                        ProfileInfoRow(label = "Enrolled School", value = student.institution)
                        HorizontalDivider(color = BorderLight.copy(alpha = 0.6f), thickness = 0.8.dp)
                        ProfileInfoRow(
                            label = "NPCI Aadhaar Bridge",
                            value = if (student.npciAadhaarSeeded) "Active & Seeded" else "NFS"
                        )
                    }
                }
            }

            // Document Wallet Shortcut Button
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PrimarySurfaceLight),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, PrimaryDeepOrange.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .clickable { onNavigateToDocumentWallet() }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
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
                                text = "Open Document Wallet",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryDeepOrangeDark
                            )
                            Text(
                                text = "View uploaded certificates and CDN links",
                                fontSize = 11.sp,
                                color = TextBody
                            )
                        }
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.ArrowRight,
                            contentDescription = null,
                            tint = PrimaryDeepOrange,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Notification Preferences
            item {
                Text(
                    text = "Notification Preferences",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
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
                                Text(
                                    text = "Scholarship Status Alerts",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextDark
                                )
                                Text(
                                    text = "Receive notifications on verification, sanction, and DBT credit",
                                    fontSize = 11.sp,
                                    color = TextSubtle
                                )
                            }
                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { notificationsEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = PrimaryDeepOrange, checkedTrackColor = PrimarySurfaceLight)
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BorderLight)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Document Expiry SMS",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextDark
                                )
                                Text(
                                    text = "Reminders 30 days before income or caste cert expires",
                                    fontSize = 11.sp,
                                    color = TextSubtle
                                )
                            }
                            Switch(
                                checked = smsAlertsEnabled,
                                onCheckedChange = { smsAlertsEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = PrimaryDeepOrange, checkedTrackColor = PrimarySurfaceLight)
                            )
                        }
                    }
                }
            }

            // Sovereign AES-256 Encryption Security Badge
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PrimarySurfaceLight),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, PrimaryDeepOrange.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PrimaryDeepOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.ShieldCheck,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AES-256-GCM End-to-End Encrypted",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryDeepOrangeDark
                            )
                            Text(
                                text = "Aadhaar, Bank Account, and Financial data are encrypted client-side before cloud synchronization.",
                                fontSize = 10.sp,
                                color = TextBody,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }

            // Data Provenance & Sandbox Notice
            item {
                Surface(
                    color = SurfaceCard,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.ShieldCheck,
                                contentDescription = null,
                                tint = StatusDisbursed,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Data Provenance & Environment",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Account authenticated via Google SSO. Caste, Income & Academic credentials verified via National DigiLocker Sandbox (stage1.digitallocker.gov.in). Simulated data for MoTa testing environment.",
                            fontSize = 10.sp,
                            color = TextSubtle,
                            textAlign = TextAlign.Center,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            // Logout Button
            item {
                OutlinedButton(
                    onClick = {
                        FirebaseManager.logout(context)
                        onLogout()
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRejected),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Text("Sign Out of STeP Account", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSubtle,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.42f)
        )
        Spacer(modifier = Modifier.width(10.dp))
        if (value == "Active & Seeded") {
            Surface(
                color = StatusDisbursedBg,
                shape = RoundedCornerShape(8.dp)
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
                        text = value,
                        fontSize = 11.sp,
                        color = StatusDisbursed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            Text(
                text = value,
                fontSize = 12.sp,
                color = TextDark,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(0.58f)
            )
        }
    }
}
