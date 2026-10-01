package com.step.admin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.step.admin.ui.components.FontAwesomeIcons
import com.step.admin.ui.theme.*

@Composable
fun AdminDashboardScreen(
    onNavigateToScrutiny: () -> Unit,
    onNavigateToSchemes: () -> Unit,
    onNavigateToHeatmap: () -> Unit,
    onNavigateToDossiers: () -> Unit,
    onCreateSchemeClicked: () -> Unit
) {
    val kpis = AdminRepository.kpis

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .statusBarsPadding()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Sleek Compact Sovereign Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(PrimarySurfaceLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.ShieldCheck,
                                contentDescription = "MoTA Emblem",
                                tint = PrimaryDeepOrange,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "STeP Admin Suite",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark,
                                maxLines = 1
                            )
                            Text(
                                text = "MoTA & NeGD Live Server",
                                fontSize = 10.sp,
                                color = TextSubtle,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        color = StatusDisbursedBg,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(0.8.dp, StatusDisbursed.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(StatusDisbursed)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "CENTRAL",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusDisbursed
                            )
                        }
                    }
                }
            }
        }

        // Executive KPI Ribbon
        item {
            Text(
                text = "EXECUTIVE KPI OVERVIEW",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCard(
                        title = "REGISTERED SCHOLARS",
                        value = if (kpis.totalScholars > 0) "${kpis.totalScholars}" else "${AdminRepository.students.size}",
                        subtitle = "MoTA Registry",
                        accentColor = SaffronPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToDossiers
                    )
                    KpiCard(
                        title = "ACTIVE SCHEMES",
                        value = if (kpis.activeSchemes > 0) "${kpis.activeSchemes}" else "${AdminRepository.schemes.size}",
                        subtitle = "NSP, SFMP & NOS",
                        accentColor = BlueAccent,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToSchemes
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCard(
                        title = "DBT SANCTIONED",
                        value = if (kpis.totalDbtDisbursedCr > 0.0) "₹ ${String.format("%.3f", kpis.totalDbtDisbursedCr)} Cr" else "₹ 0.043 Cr",
                        subtitle = "Aadhaar APBS Bridge",
                        accentColor = EmeraldSuccess,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "SCRUTINY QUEUE",
                        value = if (kpis.pendingScrutinyCases > 0) "${kpis.pendingScrutinyCases} Cases" else "${AdminRepository.applications.count { it.status == "PENDING" }} Cases",
                        subtitle = "Auto-Clear Ready",
                        accentColor = AmberWarning,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToScrutiny
                    )
                }
            }
        }

        // Priority Scrutiny Pipeline
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PRIORITY SCRUTINY QUEUE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "View All (${AdminRepository.applications.size}) →",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryDeepOrange,
                    modifier = Modifier.clickable { onNavigateToScrutiny() }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            val pendingApp = AdminRepository.applications.firstOrNull { it.status == "PENDING" }
                ?: AdminRepository.applications.firstOrNull()

            if (pendingApp != null) {
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
                                    text = pendingApp.studentName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Text(
                                    text = "${pendingApp.scheme} • ₹${pendingApp.sanctionAmount}",
                                    fontSize = 12.sp,
                                    color = PrimaryDeepOrangeDark,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Surface(
                                color = StatusDisbursedBg,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${pendingApp.confidenceScore}% Auto-Match",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusDisbursed,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Cert: ${pendingApp.certificateNumber} • ${pendingApp.district}, ${pendingApp.state} • ${pendingApp.source}",
                            fontSize = 10.5.sp,
                            color = TextSubtle,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { AdminRepository.sanctionApplication(pendingApp.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusDisbursed),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Text("✓ 1-Click Sanction", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            OutlinedButton(
                                onClick = onNavigateToScrutiny,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDark),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Text("Inspect Dossier", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        // Registered Tribal Scholars Preview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "REGISTERED SCHOLARS (${AdminRepository.students.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "All Profiles →",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryDeepOrange,
                    modifier = Modifier.clickable { onNavigateToDossiers() }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AdminRepository.students.forEach { scholar ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToDossiers() }
                            .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(PrimarySurfaceLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = scholar.fullName.take(1).uppercase(),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryDeepOrangeDark
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = scholar.fullName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Text(
                                    text = "${scholar.subTribe} ST • ${scholar.apaarId}",
                                    fontSize = 10.5.sp,
                                    color = TextSubtle,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "${scholar.institution} • ${scholar.bankName}",
                                    fontSize = 10.sp,
                                    color = TextBody,
                                    maxLines = 1
                                )
                            }
                            Surface(
                                color = StatusDisbursedBg,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "NPCI Active",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusDisbursed,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Administrative Actions
        item {
            Text(
                text = "ADMINISTRATIVE CONTROL SUITE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCreateSchemeClicked,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDeepOrange),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("+ Create Scheme", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedButton(
                    onClick = onNavigateToHeatmap,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryDeepOrange),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryDeepOrange),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Outreach Heatmap", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryDeepOrange)
                }
            }
        }

        // Action Navigation Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminNavCard(
                    title = "Application Scrutiny & Approval Queue",
                    desc = "Inspect student documents with confidence scores >= 85% auto-approval and 1-click sanction.",
                    badge = "${AdminRepository.applications.size} Cases",
                    onClick = onNavigateToScrutiny
                )
                AdminNavCard(
                    title = "Scholarships Master Catalog",
                    desc = "Manage Pre-Matric, Post-Matric, Top Class, NFST, and NOS eligibility criteria and funding ceilings.",
                    badge = "${AdminRepository.schemes.size} Schemes",
                    onClick = onNavigateToSchemes
                )
                AdminNavCard(
                    title = "UDISE+ vs MoTA Saturation Heatmap",
                    desc = "Identify tribal districts with high ST enrollment but low scholarship uptake. Dispatch mobile vans.",
                    badge = "Coverage Gaps",
                    onClick = onNavigateToHeatmap
                )
                AdminNavCard(
                    title = "Student Dossiers & Digital Vault",
                    desc = "Inspect verified NeGD DigiLocker cryptographic XML, Tehsildar DSC signatures, and student records.",
                    badge = "${AdminRepository.students.size} Scholars",
                    onClick = onNavigateToDossiers
                )
            }
        }

        // Bhashini temporary downtime notice
        item {
            Spacer(modifier = Modifier.height(10.dp))
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
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .defaultMinSize(minHeight = 84.dp)
            .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = TextSubtle,
                letterSpacing = 0.4.sp,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text = value,
                fontSize = 17.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                fontSize = 9.5.sp,
                color = TextBody,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AdminNavCard(
    title: String,
    desc: String,
    badge: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, BorderLight, RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = PrimarySurfaceLight,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDeepOrange,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = desc, fontSize = 12.sp, color = TextBody, lineHeight = 16.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = FontAwesomeIcons.Solid.ArrowRight,
                contentDescription = null,
                tint = PrimaryDeepOrange,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
