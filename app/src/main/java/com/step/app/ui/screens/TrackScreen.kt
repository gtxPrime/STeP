package com.step.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.step.app.data.ApplicationRecord
import com.step.app.data.MoTaRepository
import com.step.app.ui.components.FontAwesomeIcons
import com.step.app.ui.theme.*

@Composable
fun TrackScreen(
    onNavigateToApplicationDetail: (ApplicationRecord) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Active", "Completed"
    val allApplications = MoTaRepository.applications

    val filteredApplications = remember(selectedFilter, allApplications.size) {
        when (selectedFilter) {
            "Active" -> allApplications.filter { it.stage != "DISBURSED" }
            "Completed" -> allApplications.filter { it.stage == "DISBURSED" }
            else -> allApplications.toList()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column(modifier = Modifier.statusBarsPadding()) {
                Text(
                    text = "Track Applications",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark
                )
                Text(
                    text = "Consolidated Live Status across NSP, SFMP, and NOS",
                    fontSize = 12.sp,
                    color = TextSubtle
                )
            }
        }

        // Filter Row: All / Active / Completed
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf("All", "Active", "Completed").forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Surface(
                        color = if (isSelected) PrimaryDeepOrange else SurfaceCard,
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) PrimaryDeepOrange else BorderMedium
                        ),
                        modifier = Modifier.clickable { selectedFilter = filter }
                    ) {
                        Text(
                            text = filter,
                            color = if (isSelected) Color.White else TextBody,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }

        if (filteredApplications.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(PrimarySurfaceLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.GraduationCap,
                                contentDescription = null,
                                tint = PrimaryDeepOrange,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Applications Yet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "You haven't applied for any scholarships yet. Explore available schemes from the Apply tab and submit your application.",
                            fontSize = 12.sp,
                            color = TextSubtle,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        } else {
            items(filteredApplications) { application ->
                TrackApplicationCard(
                    application = application,
                    onClick = { onNavigateToApplicationDetail(application) }
                )
            }
        }
    }
}

@Composable
private fun TrackApplicationCard(
    application: ApplicationRecord,
    onClick: () -> Unit
) {
    val (statusColor, statusBg, statusText) = when (application.stage) {
        "DISBURSED" -> Triple(StatusDisbursed, StatusDisbursedBg, "Disbursed via DBT")
        "SANCTIONED" -> Triple(StatusInProgress, StatusInProgressBg, "Sanctioned - Clearing")
        else -> Triple(StatusPending, StatusPendingBg, "Pending Action (Defect)")
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 10.dp)
                ) {
                    Text(
                        text = application.schemeTitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "ID: ${application.applicationId} • ${application.academicYear}",
                        fontSize = 11.sp,
                        color = TextSubtle,
                        fontWeight = FontWeight.Medium
                    )
                }

                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Horizontal Step Bar: Submitted -> Verified -> Sanctioned -> Disbursed
            HorizontalStepBar(currentStepIndex = application.currentStepIndex)

            Spacer(modifier = Modifier.height(18.dp))

            // Next Action & Details Link
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
                        imageVector = FontAwesomeIcons.Solid.BuildingColumns,
                        contentDescription = null,
                        tint = TextSubtle,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = application.sourcePortal,
                        fontSize = 11.sp,
                        color = TextBody,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = "View Timeline",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDeepOrange,
                        maxLines = 1,
                        softWrap = false
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.ArrowRight,
                        contentDescription = null,
                        tint = PrimaryDeepOrange,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HorizontalStepBar(currentStepIndex: Int) {
    val steps = listOf("Submitted", "Verified", "Sanctioned", "Disbursed")

    Column(modifier = Modifier.fillMaxWidth()) {
        // Row 1: Circles & Connecting Lines
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, _ ->
                val isCompleted = index < currentStepIndex
                val isCurrent = index == currentStepIndex
                val isFuture = index > currentStepIndex

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCompleted -> StatusDisbursed
                                isCurrent -> PrimaryDeepOrange
                                else -> BorderMedium
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.CircleCheck,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    } else {
                        Text(
                            text = "${index + 1}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFuture) TextSubtle else Color.White
                        )
                    }
                }

                if (index < steps.size - 1) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.5.dp)
                            .background(
                                if (index < currentStepIndex) StatusDisbursed else BorderLight
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Row 2: Labels aligned with circles
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            steps.forEachIndexed { index, stepName ->
                val isCompleted = index < currentStepIndex
                val isCurrent = index == currentStepIndex

                Text(
                    text = stepName,
                    fontSize = 9.5.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                    color = if (isCurrent) PrimaryDeepOrange else if (isCompleted) StatusDisbursed else TextSubtle,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}
