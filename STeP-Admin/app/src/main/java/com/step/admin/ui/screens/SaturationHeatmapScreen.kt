package com.step.admin.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.step.admin.data.AdminRepository
import com.step.admin.data.HeatmapDistrict
import com.step.admin.ui.components.FontAwesomeIcons
import com.step.admin.ui.theme.*

@Composable
fun SaturationHeatmapScreen(
    onBack: () -> Unit
) {
    var actionToastMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            Surface(
                color = BackgroundWhite,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(androidx.compose.foundation.BorderStroke(1.dp, BorderLight))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(52.dp)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                        Text("←", fontSize = 20.sp, color = TextDark, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "UDISE+ Deficit Heatmap",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
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
            item {
                Surface(
                    color = PrimarySurfaceLight,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryDeepOrange.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "SMART OUTREACH & SATURATION ENGINE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDeepOrange
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Identifies unreached ST students in remote pockets by evaluating enrollment deficits. Directly dispatch Mobile Enrollment Vans or geo-targeted SMS directives.",
                            fontSize = 12.sp,
                            color = TextBody,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            if (actionToastMessage != null) {
                item {
                    Surface(
                        color = StatusDisbursedBg,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = actionToastMessage!!,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusDisbursed,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            items(AdminRepository.heatmapDistricts) { district ->
                DistrictHeatmapCard(
                    item = district,
                    onDispatchVan = {
                        actionToastMessage = "Mobile Enrollment Van dispatched to ${district.district}, ${district.state}! Camp scheduled at Ashram Schools."
                    },
                    onBroadcastSms = {
                        actionToastMessage = "Geo-targeted SMS broadcast dispatched to ${district.gapCount} unreached ST families in ${district.district}."
                    }
                )
            }
        }
    }
}

@Composable
fun DistrictHeatmapCard(
    item: HeatmapDistrict,
    onDispatchVan: () -> Unit,
    onBroadcastSms: () -> Unit
) {
    val riskColor = when (item.riskLevel) {
        "CRITICAL" -> StatusRejected
        "MODERATE" -> StatusPending
        else -> StatusDisbursed
    }
    val riskBg = when (item.riskLevel) {
        "CRITICAL" -> StatusRejectedBg
        "MODERATE" -> StatusPendingBg
        else -> StatusDisbursedBg
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = BackgroundWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                    Text(text = "${item.district}, ${item.state}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "Census: ${item.enrolledSt} ST Students • Active: ${item.activeScholarships}", fontSize = 11.sp, color = TextBody)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = riskBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${item.gapPercent}% GAP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = riskColor,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = SurfaceCardAlt,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(text = "Unreached Beneficiary Gap: ${item.gapCount} Scholars", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryDeepOrange)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "Directive: ${item.recommendedAction}", fontSize = 11.sp, color = TextBody)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDispatchVan,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDeepOrange),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(FontAwesomeIcons.Solid.VanShuttle, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Dispatch Van", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                OutlinedButton(
                    onClick = onBroadcastSms,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusInProgress),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusInProgress),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(FontAwesomeIcons.Solid.CommentSms, contentDescription = null, tint = StatusInProgress, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Broadcast SMS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
