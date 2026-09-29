package com.step.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.step.app.data.MoTaRepository
import com.step.app.data.Scheme
import com.step.app.ui.components.FontAwesomeIcons
import com.step.app.ui.theme.*

@Composable
fun SchemeDetailScreen(
    scheme: Scheme,
    onBack: () -> Unit,
    onStartApply: (Scheme) -> Unit
) {
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Scheme Details",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Ministry of Tribal Affairs • Govt. of India",
                            fontSize = 11.sp,
                            color = TextSubtle,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Surface(
                        color = PrimarySurfaceLight,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryDeepOrange.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = scheme.code,
                            color = PrimaryDeepOrangeDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = BackgroundWhite,
                shadowElevation = 10.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = { onStartApply(scheme) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDeepOrange),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Apply for ${scheme.code}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.ArrowRight,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
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
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // 1. HERO CARD: Scheme Title, Financial Entitlement & Breakdown
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PrimarySurfaceLight),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, PrimaryDeepOrange.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        // Badges Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Eligibility Status Pill
                            val (pillBg, pillTextColor, pillText) = when (scheme.eligibilityTag) {
                                "Applied" -> Triple(StatusInProgressBg, StatusInProgress, "Applied")
                                "Eligible" -> Triple(StatusDisbursedBg, StatusDisbursed, "Eligible")
                                else -> Triple(PrimarySurfaceLight, PrimaryDeepOrange, "Open for 2026")
                            }
                            Surface(
                                color = pillBg,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, pillTextColor.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = pillText,
                                    color = pillTextColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            // Deadline Pill
                            Surface(
                                color = Color.White.copy(alpha = 0.85f),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderMedium)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = FontAwesomeIcons.Solid.Clock,
                                        contentDescription = null,
                                        tint = PrimaryDeepOrange,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = scheme.deadlineFormatted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Scheme Titles
                        Text(
                            text = scheme.title,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDark,
                            lineHeight = 24.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = scheme.hindiTitle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSubtle
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Elevated Financial Entitlement Container
                        Surface(
                            color = BackgroundWhite,
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderMedium),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = FontAwesomeIcons.Solid.Wallet,
                                            contentDescription = null,
                                            tint = PrimaryDeepOrange,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "MAXIMUM ENTITLEMENT",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextSubtle,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                    Surface(
                                        color = StatusDisbursedBg,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "100% DBT",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StatusDisbursed,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = scheme.benefitAmountFormatted,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryDeepOrange,
                                    letterSpacing = (-0.5).sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Breakdown Pills
                                val breakdownItems = scheme.benefitSummary.split("|")
                                    .map { it.trim() }
                                    .filter { it.isNotEmpty() }

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    breakdownItems.forEach { item ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .clip(CircleShape)
                                                    .background(PrimaryDeepOrange)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = item,
                                                fontSize = 11.sp,
                                                color = TextBody,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. QUICK METRICS AT A GLANCE (2x2 Grid)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Key Scheme Parameters",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )

                    // Row 1 of parameters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Metric 1: Target Course / Class
                        MetricChipCard(
                            modifier = Modifier.weight(1f),
                            icon = FontAwesomeIcons.Solid.GraduationCap,
                            label = "Target Course",
                            value = scheme.targetClass
                        )

                        // Metric 2: Income Ceiling
                        MetricChipCard(
                            modifier = Modifier.weight(1f),
                            icon = FontAwesomeIcons.Solid.Wallet,
                            label = "Income Ceiling",
                            value = if (scheme.incomeCeiling == null) "No Income Limit" else "≤ ₹${scheme.incomeCeiling / 100000} Lakh/yr"
                        )
                    }

                    // Row 2 of parameters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Metric 3: Disbursement Agency
                        MetricChipCard(
                            modifier = Modifier.weight(1f),
                            icon = FontAwesomeIcons.Solid.BuildingColumns,
                            label = "Nodal Agency",
                            value = scheme.portal
                        )

                        // Metric 4: Disbursal Mode
                        MetricChipCard(
                            modifier = Modifier.weight(1f),
                            icon = FontAwesomeIcons.Solid.ShieldCheck,
                            label = "Disbursal Channel",
                            value = "Direct DBT via PFMS"
                        )
                    }
                }
            }

            // 3. ABOUT THIS SCHEME (Structured Feature Card)
            item {
                Column {
                    Text(
                        text = "About This Scheme",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderMedium, RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                        ) {
                            // Deep Orange Accent Left Strip
                            Box(
                                modifier = Modifier
                                    .width(5.dp)
                                    .fillMaxHeight()
                                    .background(PrimaryDeepOrange)
                            )
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = scheme.description,
                                    fontSize = 13.sp,
                                    color = TextDark,
                                    lineHeight = 20.sp,
                                    fontWeight = FontWeight.Normal
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Surface(
                                    color = PrimarySurfaceLight,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Centrally Funded • Ministry of Tribal Affairs (MoTA)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryDeepOrangeDark,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. ELIGIBILITY CRITERIA CHECKLIST
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Eligibility Criteria",
                            fontSize = 15.sp,
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
                                text = "${scheme.rules.size} Conditions",
                                color = StatusDisbursed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        scheme.rules.forEach { rule ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(StatusDisbursedBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = FontAwesomeIcons.Solid.CircleCheck,
                                            contentDescription = null,
                                            tint = StatusDisbursed,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = rule,
                                        fontSize = 12.sp,
                                        color = TextDark,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 17.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. REQUIRED DOCUMENTS CHECKLIST
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Required Documents Checklist",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = PrimarySurfaceLight,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "${scheme.documentsNeeded.size} Documents",
                                color = PrimaryDeepOrangeDark,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Scanned documents or DigiLocker verification accepted",
                        fontSize = 11.sp,
                        color = TextSubtle
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderMedium, RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            scheme.documentsNeeded.forEachIndexed { index, doc ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(PrimarySurfaceLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = FontAwesomeIcons.Solid.FileLines,
                                            contentDescription = null,
                                            tint = PrimaryDeepOrange,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = doc,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Surface(
                                        color = Color.White,
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(0.8.dp, BorderMedium)
                                    ) {
                                        Text(
                                            text = "Required",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextSubtle,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                if (index < scheme.documentsNeeded.size - 1) {
                                    HorizontalDivider(
                                        thickness = 0.8.dp,
                                        color = BorderLight
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (scheme.id == "TOP_CLASS") {
                item {
                    TopClassInstitutesSection()
                }
            } else if (scheme.id in listOf("PRE_MATRIC", "POST_MATRIC")) {
                item {
                    StateScholarshipStatsSection()
                }
            }

            // 6. OFFICIAL NODAL AUTHORITY & ASSURANCE
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PrimarySurfaceLight),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, PrimaryDeepOrange.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(BackgroundWhite)
                                .border(1.dp, BorderMedium, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.BuildingColumns,
                                contentDescription = null,
                                tint = PrimaryDeepOrange,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Administering Ministry & Portal",
                                fontSize = 10.sp,
                                color = TextSubtle,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = scheme.portal,
                                fontSize = 12.sp,
                                color = TextDark,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Direct Benefit Transfer under National Tribal Welfare",
                                fontSize = 10.sp,
                                color = TextBody
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricChipCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.border(1.dp, BorderLight, RoundedCornerShape(14.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(PrimarySurfaceLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = PrimaryDeepOrange,
                        modifier = Modifier.size(12.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = TextSubtle,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                maxLines = 2,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun TopClassInstitutesSection() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf("All") }
    val categories = listOf("All", "IIT", "NIT", "IIM", "AIIMS", "NLU", "IIIT", "NIFT")

    val institutes = remember(searchQuery, selectedCat, MoTaRepository.topInstitutes.size) {
        MoTaRepository.searchTopInstitutes(searchQuery, if (selectedCat == "All") null else selectedCat)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderMedium, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "265 Notified Premier Institutes",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = "Official Ministry dataset • 100% Fee Waiver",
                        fontSize = 11.sp,
                        color = TextSubtle
                    )
                }
                Surface(
                    color = PrimarySurfaceLight,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${MoTaRepository.topInstitutes.size} Institutes",
                        color = PrimaryDeepOrangeDark,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search your college or state...", fontSize = 12.sp, color = TextSubtle) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryDeepOrange,
                    unfocusedBorderColor = BorderMedium,
                    focusedContainerColor = BackgroundWhite,
                    unfocusedContainerColor = BackgroundWhite
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCat == cat
                    Surface(
                        color = if (isSelected) PrimaryDeepOrange else BackgroundWhite,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) PrimaryDeepOrange else BorderMedium),
                        modifier = Modifier.clickable { selectedCat = cat }
                    ) {
                        Text(
                            text = cat,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else TextDark,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Showing ${institutes.take(5).size} of ${institutes.size} matching institutes",
                fontSize = 11.sp,
                color = TextSubtle,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(6.dp))

            institutes.take(5).forEach { inst ->
                Surface(
                    color = BackgroundWhite,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, BorderLight),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = PrimarySurfaceLight,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = inst.category,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryDeepOrangeDark,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = inst.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = "${inst.state} • ${inst.course}",
                                fontSize = 10.sp,
                                color = TextSubtle
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StateScholarshipStatsSection() {
    val studentState = MoTaRepository.currentStudent.state
    val initialSelected = if (studentState != "NFS" && studentState.isNotBlank()) studentState else "Odisha"
    var selectedState by remember { mutableStateOf(initialSelected) }
    val stat = remember(selectedState, MoTaRepository.stateStats.size) {
        MoTaRepository.getStateStat(selectedState) ?: MoTaRepository.stateStats.firstOrNull()
    }

    if (stat != null) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderMedium, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "State Scholarship Reach",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Official MoTA Dataset (Annexure I)",
                            fontSize = 11.sp,
                            color = TextSubtle
                        )
                    }
                    Surface(
                        color = StatusDisbursedBg,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = stat.state,
                            color = StatusDisbursed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = BackgroundWhite,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("PRE-MATRIC", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSubtle)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("${java.text.NumberFormat.getIntegerInstance().format(stat.preBen)} students", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            Text("₹ ${stat.preFund} Cr released", fontSize = 10.sp, color = PrimaryDeepOrange, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Surface(
                        color = BackgroundWhite,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("POST-MATRIC", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSubtle)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("${java.text.NumberFormat.getIntegerInstance().format(stat.postBen)} scholars", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            Text("₹ ${stat.postFund} Cr released", fontSize = 10.sp, color = PrimaryDeepOrange, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
