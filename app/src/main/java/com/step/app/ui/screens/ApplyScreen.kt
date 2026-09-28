package com.step.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun ApplyScreen(
    onNavigateToWizard: () -> Unit,
    onNavigateToSchemeDetail: (Scheme) -> Unit
) {
    val schemes = MoTaRepository.schemes

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
        item {
            Column {
                Text(
                    text = "Apply for Scholarships",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark
                )
                Text(
                    text = "Ministry of Tribal Affairs Sovereign Schemes",
                    fontSize = 12.sp,
                    color = TextSubtle
                )
            }
        }

        // Top Half: Eligibility Wizard Entry Point Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PrimarySurfaceLight),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PrimaryDeepOrange.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PrimaryDeepOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.WandMagicSparkles,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Eligibility Wizard",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryDeepOrangeDark
                            )
                            Text(
                                text = "AI-Powered Scheme Recommendation",
                                fontSize = 11.sp,
                                color = TextBody
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Unsure which scholarship yields the highest financial grant? Answer 5 conversational questions and get ranked scheme recommendations.",
                        fontSize = 12.sp,
                        color = TextBody,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onNavigateToWizard,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDeepOrange),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Find Schemes for You",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.ArrowRight,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section Title: All 5 Schemes Listed as Cards
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "All MoTA Schemes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Text(
                    text = "${schemes.size} Schemes Available",
                    fontSize = 12.sp,
                    color = TextSubtle,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // List of 5 Scheme Cards
        items(schemes) { scheme ->
            SchemeListItemCard(
                scheme = scheme,
                onClick = { onNavigateToSchemeDetail(scheme) }
            )
        }
    }
}

@Composable
private fun SchemeListItemCard(
    scheme: Scheme,
    onClick: () -> Unit
) {
    val (tagColor, tagBg) = when (scheme.eligibilityTag) {
        "Eligible" -> Pair(StatusDisbursed, StatusDisbursedBg)
        "Applied" -> Pair(StatusInProgress, StatusInProgressBg)
        "Check eligibility" -> Pair(StatusPending, StatusPendingBg)
        else -> Pair(TextSubtle, SurfaceCard)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = scheme.code,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDeepOrange
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = scheme.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }

                Surface(
                    color = tagBg,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = scheme.eligibilityTag,
                        color = tagColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Benefit Amount (Big Font)
            Text(
                text = "Maximum Financial Benefit",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSubtle
            )
            Text(
                text = scheme.benefitAmountFormatted,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryDeepOrangeDark
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Portal & Deadline row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.Clock,
                        contentDescription = null,
                        tint = TextSubtle,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Deadline: ${scheme.deadlineFormatted}",
                        fontSize = 11.sp,
                        color = TextBody,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Details",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDeepOrange
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
