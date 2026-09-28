package com.step.app.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.step.app.data.MoTaRepository
import com.step.app.data.Scheme
import com.step.app.ui.components.FontAwesomeIcons
import com.step.app.ui.theme.*

@Composable
fun EligibilityWizardScreen(
    onBack: () -> Unit,
    onSelectSchemeToApply: (Scheme) -> Unit
) {
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedEducation by remember { mutableStateOf(1) } // 0..4
    var selectedIncomeIndex by remember { mutableStateOf(0) } // 0: <=2.5L, 1: <=6.0L, 2: <=8.0L, 3: >8.0L
    var isSTbyCaste by remember { mutableStateOf(true) }
    var hasPremierAdmission by remember { mutableStateOf(true) }
    var isPVTGbyQuota by remember { mutableStateOf(false) }

    var isEvaluated by remember { mutableStateOf(false) }

    val educationOptions = listOf(
        "Class 9 or 10 (Secondary School)",
        "Class 11, 12, Degree, ITI or PG",
        "Premier National Institute (IIT, IIM, NIT, AIIMS)",
        "Ph.D. / M.Phil Regular Research Scholar",
        "Top 500 QS World Ranking Foreign University"
    )

    val incomeOptions = listOf(
        "Under ₹ 2.50 Lakh / year (Pre/Post-Matric Cap)",
        "₹ 2.50 Lakh to ₹ 6.00 Lakh / year (Top Class Cap)",
        "₹ 6.00 Lakh to ₹ 8.00 Lakh / year (NOS Cap)",
        "Above ₹ 8.00 Lakh / year (NFST Research Fellowship)"
    )

    val matchedSchemes = remember(isEvaluated) {
        if (!isEvaluated) emptyList()
        else {
            val list = mutableListOf<Scheme>()
            if (selectedEducation == 0 && selectedIncomeIndex == 0) list.add(MoTaRepository.schemes[0])
            if (selectedEducation >= 1 && selectedIncomeIndex == 0) list.add(MoTaRepository.schemes[1])
            if (selectedEducation == 2 && selectedIncomeIndex <= 1) list.add(MoTaRepository.schemes[2])
            if (selectedEducation == 3) list.add(MoTaRepository.schemes[3])
            if (selectedEducation == 4 && selectedIncomeIndex <= 2) list.add(MoTaRepository.schemes[4])
            if (list.isEmpty()) {
                list.add(MoTaRepository.schemes[2])
                list.add(MoTaRepository.schemes[1])
            }
            list.sortedByDescending { it.maxBenefitAmount }
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
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (isEvaluated) {
                                isEvaluated = false
                            } else if (currentQuestionIndex > 0) {
                                currentQuestionIndex--
                            } else {
                                onBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.ArrowLeft,
                            contentDescription = "Back",
                            tint = TextDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "5-Scheme Eligibility Wizard",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = if (isEvaluated) "Personalized Recommendations" else "Question ${currentQuestionIndex + 1} of 5",
                            fontSize = 11.sp,
                            color = PrimaryDeepOrange,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (isEvaluated) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundWhite)
                    .padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PrimarySurfaceLight),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, PrimaryDeepOrange.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = FontAwesomeIcons.Solid.WandMagicSparkles,
                                    contentDescription = null,
                                    tint = PrimaryDeepOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Sovereign AI Evaluation Complete",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryDeepOrangeDark
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Based on your income and academic profile, you qualify for ${matchedSchemes.size} MoTA scholarships, ranked below by highest financial benefit.",
                                fontSize = 12.sp,
                                color = TextBody,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "Qualified Schemes (Ranked by Grant Amount)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }

                items(matchedSchemes.size) { index ->
                    val scheme = matchedSchemes[index]
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (index == 0) PrimaryDeepOrange else BorderLight,
                                RoundedCornerShape(16.dp)
                            )
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (index == 0) {
                                    Surface(
                                        color = PrimaryDeepOrange,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "Top Financial Pick",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = scheme.code,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSubtle
                                    )
                                }

                                Text(
                                    text = scheme.benefitAmountFormatted,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryDeepOrange
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = scheme.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = scheme.benefitSummary,
                                fontSize = 12.sp,
                                color = TextBody,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = { onSelectSchemeToApply(scheme) },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryDeepOrange),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Apply for ${scheme.code}", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundWhite)
                    .padding(padding)
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // Question Header
                    when (currentQuestionIndex) {
                        0 -> {
                            WizardQuestionView(
                                question = "What is your current level of education?",
                                options = educationOptions,
                                selectedIndex = selectedEducation,
                                onSelect = { selectedEducation = it }
                            )
                        }
                        1 -> {
                            WizardQuestionView(
                                question = "What is your certified annual family income?",
                                options = incomeOptions,
                                selectedIndex = selectedIncomeIndex,
                                onSelect = { selectedIncomeIndex = it }
                            )
                        }
                        2 -> {
                            WizardBooleanView(
                                question = "Do you have a valid Scheduled Tribe (ST) Certificate?",
                                subtitle = "DigiLocker verified or certified by competent state revenue authority.",
                                value = isSTbyCaste,
                                onSelect = { isSTbyCaste = it }
                            )
                        }
                        3 -> {
                            WizardBooleanView(
                                question = "Are you enrolled in an IIT, IIM, NIT, AIIMS or Top 500 QS Global University?",
                                subtitle = "Premier institutes qualify for 100% tuition and living grants under Top Class / NOS.",
                                value = hasPremierAdmission,
                                onSelect = { hasPremierAdmission = it }
                            )
                        }
                        4 -> {
                            WizardBooleanView(
                                question = "Do you belong to a Particularly Vulnerable Tribal Group (PVTG) or have PwD disability?",
                                subtitle = "Special affirmative reservations and income relaxations apply for 75 notified PVTG communities.",
                                value = isPVTGbyQuota,
                                onSelect = { isPVTGbyQuota = it }
                            )
                        }
                    }
                }

                // Next Button
                Button(
                    onClick = {
                        if (currentQuestionIndex < 4) {
                            currentQuestionIndex++
                        } else {
                            isEvaluated = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDeepOrange),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = if (currentQuestionIndex < 4) "Next Question" else "Evaluate Eligible Schemes",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun WizardQuestionView(
    question: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Text(
        text = question,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = TextDark,
        lineHeight = 24.sp
    )

    Spacer(modifier = Modifier.height(18.dp))

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        options.forEachIndexed { index, option ->
            val isSelected = selectedIndex == index
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
                    .clickable { onSelect(index) }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelect(index) },
                        colors = RadioButtonDefaults.colors(selectedColor = PrimaryDeepOrange)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = option,
                        fontSize = 13.sp,
                        color = if (isSelected) PrimaryDeepOrangeDark else TextDark,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun WizardBooleanView(
    question: String,
    subtitle: String,
    value: Boolean,
    onSelect: (Boolean) -> Unit
) {
    Text(
        text = question,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = TextDark,
        lineHeight = 24.sp
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        text = subtitle,
        fontSize = 12.sp,
        color = TextSubtle,
        lineHeight = 18.sp
    )

    Spacer(modifier = Modifier.height(24.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        listOf(true to "Yes, I qualify", false to "No / Not Applicable").forEach { (choice, label) ->
            val isSelected = value == choice
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) PrimarySurfaceLight else SurfaceCard
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(
                        1.dp,
                        if (isSelected) PrimaryDeepOrange else BorderLight,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelect(choice) }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onSelect(choice) },
                        colors = RadioButtonDefaults.colors(selectedColor = PrimaryDeepOrange)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) PrimaryDeepOrangeDark else TextDark
                    )
                }
            }
        }
    }
}
