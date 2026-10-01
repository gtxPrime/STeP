package com.step.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.*
import com.step.app.data.ApplicationRecord
import com.step.app.data.MoTaRepository
import com.step.app.intelligence.DeficiencyDefenseEngine
import com.step.app.intelligence.StatutoryDefenseNotice
import com.step.app.ui.components.FontAwesomeIcons
import com.step.app.ui.theme.*

@Composable
fun ApplicationDetailScreen(
    application: ApplicationRecord,
    onBack: () -> Unit,
    onExplainDeficiency: () -> Unit,
    onEscalateGrievance: () -> Unit
) {
    var showDefenseDialog by remember { mutableStateOf(false) }
    var defenseNotice by remember { mutableStateOf<StatutoryDefenseNotice?>(null) }
    var appealSubmittedMessage by remember { mutableStateOf<String?>(null) }
    Scaffold(
        topBar = {
            Surface(
                color = BackgroundWhite,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = "Application Status",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            maxLines = 1
                        )
                        Text(
                            text = application.applicationId,
                            fontSize = 11.sp,
                            color = TextSubtle,
                            maxLines = 1
                        )
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
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderLight, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = application.schemeTitle,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Source: ${application.sourcePortal}",
                                fontSize = 11.sp,
                                color = TextSubtle,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f, fill = false),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "₹ ${application.sanctionAmount}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryDeepOrange,
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Horizontal Step Indicator
                        HorizontalStepBar(currentStepIndex = application.currentStepIndex)
                    }
                }
            }

            // Deficiencies Section (if flagged)
            application.deficiency?.let { def ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StatusRejectedBg),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, StatusRejected.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Icon(
                                        imageVector = FontAwesomeIcons.Solid.TriangleExclamation,
                                        contentDescription = null,
                                        tint = StatusRejected,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Defect Code: ${def.code}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusRejected,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${def.daysRemaining} Days Left to Cure",
                                        color = StatusRejected,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = def.bureaucraticReason,
                                fontSize = 12.sp,
                                color = TextBody,
                                lineHeight = 17.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = onExplainDeficiency,
                                colors = ButtonDefaults.buttonColors(containerColor = StatusRejected),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = FontAwesomeIcons.Solid.WandMagicSparkles,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Explain in Plain Language",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = {
                                    val notice = DeficiencyDefenseEngine.generateDefense(
                                        deficiency = def,
                                        student = MoTaRepository.currentStudent,
                                        schemeTitle = application.schemeTitle
                                    )
                                    defenseNotice = notice
                                    showDefenseDialog = true
                                },
                                border = androidx.compose.foundation.BorderStroke(1.dp, StatusRejected),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = FontAwesomeIcons.Solid.ShieldCheck,
                                        contentDescription = null,
                                        tint = StatusRejected,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "JAGO Statutory Anti-Corruption Defense",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusRejected
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Verification Breakdown Panel
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Verification Breakdown",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = StatusDisbursedBg,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "Confidence: ${application.verificationConfidence}%",
                                    color = StatusDisbursed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { application.verificationConfidence / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = StatusDisbursed,
                            trackColor = BorderLight
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "DigiLocker sovereign API verified matching certificate against State Revenue & Council of Higher Secondary Education registries.",
                            fontSize = 11.sp,
                            color = TextBody,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // DBT Bank Ledger (if Disbursed)
            application.dbtDetails?.let { dbt ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StatusDisbursedBg),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, StatusDisbursed.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = FontAwesomeIcons.Solid.CircleCheck,
                                    contentDescription = null,
                                    tint = StatusDisbursed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Direct Benefit Transfer (DBT) Receipt",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusDisbursed
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            DbtInfoRow(label = "Bank UTR Number", value = dbt.utr, isMono = true)
                            DbtInfoRow(label = "Disbursed Date", value = dbt.disbursedDate)
                            DbtInfoRow(label = "Credited Bank", value = "${dbt.bankName} (${dbt.accountNo})")
                            DbtInfoRow(label = "Central Share (75%)", value = dbt.centralShare)
                            DbtInfoRow(label = "State Share (25%)", value = dbt.stateShare)

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedButton(
                                onClick = onEscalateGrievance,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryDeepOrange),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Payment Not Received? Escalate Grievance",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Full Timeline History with Dates on Each Step
            item {
                Text(
                    text = "Timeline Event History",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            }

            items(application.steps.size) { index ->
                val step = application.steps[index]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (step.completed) StatusDisbursed else BorderMedium),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (step.completed) FontAwesomeIcons.Solid.CircleCheck else FontAwesomeIcons.Solid.Clock,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        if (index < application.steps.size - 1) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(44.dp)
                                    .background(if (step.completed) StatusDisbursed else BorderLight)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = step.label,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark,
                                modifier = Modifier.weight(1f, fill = false),
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = step.date,
                                fontSize = 11.sp,
                                color = TextSubtle,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = step.note,
                            fontSize = 11.sp,
                            color = TextBody,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        if (showDefenseDialog && defenseNotice != null) {
            val notice = defenseNotice!!
            AlertDialog(
                onDismissRequest = { showDefenseDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.ShieldCheck,
                            contentDescription = null,
                            tint = StatusDisbursed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Statutory Anti-Corruption Appeal",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextDark
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = PrimarySurfaceLight,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryDeepOrange.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("GOVERNING STATUTORY NORM:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PrimaryDeepOrangeDark)
                                Text(notice.statutoryGoverningRule, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("LEGAL PRECEDENT / ACT:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PrimaryDeepOrangeDark)
                                Text(notice.legalCitation, fontSize = 11.sp, color = TextBody)
                            }
                        }

                        Text("Auto-Drafted Formal Legal Counter-Notice:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Surface(
                            color = SurfaceCard,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = notice.formalAppealText,
                                fontSize = 9.5.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextSubtle,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        Text("Escalation Target: ${notice.escalationAuthority} (15-Day SLA Guarantee)", fontSize = 10.sp, color = TextSubtle)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDefenseDialog = false
                            appealSubmittedMessage = "Statutory Appeal filed under Ticket ${notice.grievanceTicketId}! 15-Day statutory response SLA active."
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDeepOrange),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("File Statutory Appeal (Zero-Bribe Protection)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDefenseDialog = false }) {
                        Text("Close", color = TextSubtle, fontSize = 12.sp)
                    }
                },
                containerColor = BackgroundWhite,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
private fun DbtInfoRow(label: String, value: String, isMono: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = TextSubtle, fontWeight = FontWeight.Medium)
        Text(
            text = value,
            fontSize = 12.sp,
            color = TextDark,
            fontWeight = FontWeight.Bold,
            fontFamily = if (isMono) FontFamily.Monospace else FontFamily.Default
        )
    }
}
