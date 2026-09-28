package com.step.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.step.app.data.MoTaRepository
import com.step.app.ui.theme.*

@Composable
fun EligibilityScreen(
    onApplyClicked: (String) -> Unit
) {
    var selectedEdu by remember { mutableStateOf("premier_institute") }
    var income by remember { mutableFloatStateOf(145000f) }
    var isPvtg by remember { mutableStateOf(false) }

    val schemes = MoTaRepository.schemes

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
                    color = Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "MULTI-SCHEME REASONING ENGINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SaffronLight,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "5-Scheme Eligibility Wizard",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMain
                )
                Text(
                    text = "Evaluates Pre-Matric, Post-Matric, Top Class, NFST, and NOS guidelines simultaneously to find your maximum financial entitlement.",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }

        // Education Level Selector Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NavyBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Current Education Stage",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextMain
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val eduOptions = listOf(
                        "class9_10" to "Class 9 - 10 (Pre-Matric)",
                        "class11_12" to "Class 11 - 12 / ITI",
                        "premier_institute" to "Premier Institutes (IIT, IIM, NIT)",
                        "mphil_phd" to "M.Phil / Ph.D (India)",
                        "study_abroad" to "Study Abroad (QS Top 500)"
                    )

                    eduOptions.forEach { (key, label) ->
                        val isSelected = selectedEdu == key
                        Surface(
                            color = if (isSelected) SaffronPrimary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.04f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) SaffronPrimary else Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedEdu = key }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) SaffronLight else TextMain
                                )
                                if (isSelected) {
                                    Text("✓ Selected", fontSize = 11.sp, color = SaffronLight, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Income Slider
                    Text(
                        text = "2. Certified Family Annual Income",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextMain
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹ ${income.toLong() / 1000}k / year",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldSuccess
                    )
                    Slider(
                        value = income,
                        onValueChange = { income = it },
                        valueRange = 50000f..1000000f,
                        steps = 19,
                        colors = SliderDefaults.colors(
                            thumbColor = SaffronPrimary,
                            activeTrackColor = SaffronPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // PVTG Affirmative Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Particularly Vulnerable Tribal Group (PVTG)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMain)
                            Text("Priority reservation under NOS & PM-JANMAN", fontSize = 10.sp, color = TextMuted)
                        }
                        Switch(
                            checked = isPvtg,
                            onCheckedChange = { isPvtg = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = AmberWarning)
                        )
                    }
                }
            }
        }

        // Top Pick Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A2B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, EmeraldSuccess, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "★ HIGHEST BENEFIT ENTITLEMENT FOR YOU",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFA7F3D0)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when (selectedEdu) {
                            "study_abroad" -> "National Overseas Scholarship (NOS)"
                            "mphil_phd" -> "National Fellowship for ST (NFST)"
                            "class9_10" -> "Pre-Matric Scholarship for ST"
                            "class11_12" -> "Post-Matric Scholarship (PMS-ST)"
                            else -> "Top Class Education (IIT Bhubaneswar)"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    Text(
                        text = when (selectedEdu) {
                            "study_abroad" -> "Full Tuition + £9,900/yr Living + Airfare (Up to ₹22,00,000)"
                            "mphil_phd" -> "₹35,000/mo SRF + HRA + ₹25,000 Contingency (No income limit!)"
                            "class9_10" -> "₹7,000/yr Hosteller + ₹1,000 Book grant"
                            "class11_12" -> "100% Tuition Waiver + Maintenance up to ₹13,500/yr"
                            else -> "Full Tuition + ₹3,000/mo Living + ₹45,000 Computer Grant (₹2,86,000/yr)"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = EmeraldSuccess
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onApplyClicked("TOP_CLASS") },
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Apply with 1-Tap DigiLocker", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // All 5 Schemes Comparative Evaluation
        items(schemes) { scheme ->
            val isEligible = when (scheme.id) {
                "TOP_CLASS" -> selectedEdu == "premier_institute" && income <= 600000
                "NOS" -> selectedEdu == "study_abroad" && income <= 800000
                "NFST" -> selectedEdu == "mphil_phd"
                "PRE_MATRIC" -> selectedEdu == "class9_10" && income <= 250000
                "POST_MATRIC" -> (selectedEdu == "class11_12" || selectedEdu == "premier_institute") && income <= 250000
                else -> false
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (isEligible) EmeraldSuccess.copy(alpha = 0.5f) else NavyBorder,
                        RoundedCornerShape(14.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                color = if (isEligible) EmeraldSuccess.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (isEligible) "✓ Fully Eligible" else "✕ Not Applicable",
                                    color = if (isEligible) EmeraldSuccess else TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = scheme.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextMain
                            )
                            Text(
                                text = scheme.portal,
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Max Benefit", fontSize = 9.sp, color = TextMuted)
                            Text(
                                text = "₹${scheme.maxBenefitAmount / 1000}k",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (isEligible) EmeraldSuccess else TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = scheme.benefitSummary, fontSize = 11.sp, color = TextMain)
                }
            }
        }
    }
}
