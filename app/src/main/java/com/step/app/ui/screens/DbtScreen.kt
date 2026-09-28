package com.step.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.step.app.data.MoTaRepository
import com.step.app.ui.theme.*

@Composable
fun DbtScreen(
    onEscalateClicked: () -> Unit
) {
    val student = MoTaRepository.currentStudent
    val apps = MoTaRepository.applications.filter { it.dbtDetails != null }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Surface(
                    color = EmeraldSuccess.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "NPCI AADHAAR PAYMENT BRIDGE (APB)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldSuccess,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "DBT Payment Tracker & SLA Desk",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )
                Text(
                    text = "Direct sovereign bank-level disbursements with UTR confirmation and 30-day RTI-backed grievance countdown.",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }

        // NPCI Seeding Status Card
        item {
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
                        Column {
                            Text("Aadhaar Seeded DBT Bank Account", fontSize = 11.sp, color = TextMuted)
                            Text(student.bankName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextMain)
                            Text("Account: ${student.maskedAccount} • IFSC: ${student.ifsc}", fontSize = 12.sp, color = TextMuted, fontFamily = FontFamily.Monospace)
                        }
                        Surface(
                            color = EmeraldSuccess.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "● Active for DBT",
                                color = EmeraldSuccess,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // DBT Ledger Entries
        item {
            apps.forEach { app ->
                val dbt = app.dbtDetails!!
                Card(
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .border(1.dp, NavyBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(app.schemeTitle, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextMain)
                                Text("Mode: ${dbt.paymentMode}", fontSize = 11.sp, color = TextMuted)
                            }
                            Text(
                                text = "₹${app.sanctionAmount.toLocaleString()}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = EmeraldSuccess
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            color = Color.White.copy(alpha = 0.04f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("PFMS UTR Number:", fontSize = 11.sp, color = TextMuted)
                                    Text(dbt.utr, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SaffronLight, fontFamily = FontFamily.Monospace)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Credit Date:", fontSize = 11.sp, color = TextMuted)
                                    Text(dbt.disbursedDate, fontSize = 11.sp, color = TextMain)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Share Ratio:", fontSize = 11.sp, color = TextMuted)
                                    Text("${dbt.centralShare} Central : ${dbt.stateShare} State", fontSize = 11.sp, color = TextMain)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = onEscalateClicked,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberWarning),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("⚠️ Payment Not Received? Escalate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Active Citizen Charter SLA Countdown Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AmberWarning.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = AmberWarning.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "TICKET: GRV-2026-MOTA-9104",
                                color = AmberWarning,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "20 Days Left",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = AmberWarning
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "PFMS UTR Status Confirmation (Post-Matric Q4)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextMain
                    )
                    Text(
                        text = "Assigned: Shri R. K. Soren, Deputy Secretary, MoTA New Delhi",
                        fontSize = 11.sp,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { 0.66f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = AmberWarning,
                        trackColor = Color.White.copy(alpha = 0.1f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Under MoTA Citizen Charter, all DBT grievances must be resolved within 30 days.",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

fun Long.toLocaleString(): String {
    return this.toString().reversed().chunked(3).joinToString(",").reversed()
}
