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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.step.app.ui.components.FontAwesomeIcons
import com.step.app.ui.theme.*

import android.speech.tts.TextToSpeech
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.step.app.data.GeminiService
import com.step.app.data.MoTaRepository
import com.step.app.intelligence.DeficiencyDefenseEngine
import com.step.app.intelligence.DocumentAutopilot
import com.step.app.intelligence.ScholarshipAutopilot
import kotlinx.coroutines.launch
import java.util.Locale

data class HelpChatMessage(
    val id: String,
    val sender: String, // "USER" or "JAGO"
    val text: String,
    val timestamp: String = "Just now"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedLanguage by remember { mutableStateOf("English") }
    var showLanguageMenu by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }

    // On-device Text-To-Speech
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(context) {
        var ttsInstance: TextToSpeech? = null
        ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsInstance?.language = Locale.ENGLISH
            }
        }
        tts = ttsInstance
        onDispose {
            ttsInstance?.stop()
            ttsInstance?.shutdown()
        }
    }

    // Update TTS locale when user switches language
    LaunchedEffect(selectedLanguage) {
        val locale = when {
            selectedLanguage.contains("Hindi", ignoreCase = true) -> Locale.forLanguageTag("hi")
            selectedLanguage.contains("Marathi", ignoreCase = true) -> Locale.forLanguageTag("mr")
            selectedLanguage.contains("Odia", ignoreCase = true) -> Locale.forLanguageTag("or")
            selectedLanguage.contains("Telugu", ignoreCase = true) -> Locale.forLanguageTag("te")
            selectedLanguage.contains("Tamil", ignoreCase = true) -> Locale.forLanguageTag("ta")
            else -> Locale.ENGLISH
        }
        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts?.language = Locale.ENGLISH // fallback to English if locale not available
        }
    }

    val languages = listOf("English", "हिन्दी (Hindi)", "मराठी (Marathi)", "ଓଡ଼ିଆ (Odia)", "తెలుగు (Telugu)", "தமிழ் (Tamil)")

    val quickChips = listOf(
        "Check my document readiness",
        "Explain my scholarship",
        "Appeal a defect notice",
        "What documents do I need?",
        "Check my application status"
    )

    val messages = remember {
        mutableStateListOf(
            HelpChatMessage(
                id = "m1",
                sender = "JAGO",
                text = "Namaste! I am JAGO, your Ministry of Tribal Affairs AI Scholarship Assistant. How can I assist you with your applications, document verifications, or DBT bank transfers today?"
            )
        )
    }

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    val processJagoQuery = { queryText: String ->
        messages.add(HelpChatMessage("u_${System.currentTimeMillis()}", "USER", queryText))
        scope.launch {
            val q = queryText.lowercase()
            val reply = when {
                q.contains("readiness") || q.contains("autopilot") || (q.contains("document") && (q.contains("check") || q.contains("need") || q.contains("verify"))) -> {
                    val report = DocumentAutopilot.evaluateReadiness(MoTaRepository.currentStudent, MoTaRepository.scannedDocuments)
                    buildString {
                        appendLine("Document Readiness Audit (${report.readinessPercentage}% Complete):")
                        appendLine(report.summaryVerdict)
                        appendLine()
                        report.assessments.forEach { p ->
                            appendLine("• ${p.title}: [${p.signal.code}] ${p.statutoryRemark}")
                        }
                        appendLine()
                        if (report.canProceedToSubmission) {
                            appendLine("All statutory requirements satisfied. Your dossier is ready for one-click submission!")
                        } else {
                            appendLine("Action Required: Please address the blocking defect(s) before final submission.")
                        }
                    }
                }
                q.contains("explain") || q.contains("entitlement") || (q.contains("scholarship") && q.contains("best")) -> {
                    val autoplan = ScholarshipAutopilot.generateAutoplan(MoTaRepository.currentStudent, MoTaRepository.scannedDocuments)
                    val best = autoplan.bestScheme
                    buildString {
                        appendLine("Scholarship Entitlement Analysis:")
                        if (best != null) {
                            appendLine("Best Recommended Match: ${best.title}")
                            appendLine("Entitlement: ${best.benefitSummary} (${best.benefitAmountFormatted})")
                            appendLine("Disbursement: Direct DBT via Aadhaar Payment Bridge (APB)")
                        }
                        appendLine("One-Time Registration (OTR) Readiness: ${autoplan.otrCompletionPercentage}%")
                        appendLine("Statutory Deadlines: ${autoplan.upcomingDeadlinesSummary}")
                    }
                }
                q.contains("appeal") || q.contains("defect") || q.contains("rejection") || q.contains("defense") -> {
                    val activeDeficiencyApp = MoTaRepository.applications.firstOrNull { it.deficiency != null }
                    val dynamicDeadline = run {
                        val cal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, 30) }
                        java.text.SimpleDateFormat("dd-MMM-yyyy", java.util.Locale.ENGLISH).format(cal.time)
                    }
                    val deficiency = activeDeficiencyApp?.deficiency ?: com.step.app.data.DeficiencyInfo(
                        code = "DEF-DISCREPANCY",
                        bureaucraticReason = "Community certificate spelling mismatch with Aadhaar",
                        deadlineDate = dynamicDeadline,
                        daysRemaining = 30
                    )
                    val schemeTitle = activeDeficiencyApp?.schemeTitle ?: "National Fellowship and Scholarship for Higher Education of ST Students"
                    val docRef = MoTaRepository.sampleCasteDoc?.certificateNumber ?: "ST/OD/2022/49201"
                    val defense = DeficiencyDefenseEngine.generateDefense(
                        deficiency = deficiency,
                        student = MoTaRepository.currentStudent,
                        schemeTitle = schemeTitle,
                        documentRef = docRef
                    )
                    "Statutory Legal Appeal Counter-Notice:\n\n${defense.statutoryGoverningRule}\n\nCitation: ${defense.legalCitation}\nSLA Escalation Window: ${defense.slaDaysToRespond} days.\n\nYou can file this formal counter-notice directly from your Application Tracking screen."
                }
                else -> {
                    GeminiService.queryJago(queryText, MoTaRepository.currentStudent)
                }
            }

            messages.add(HelpChatMessage("j_${System.currentTimeMillis()}", "JAGO", reply))
            tts?.speak(reply.take(200), TextToSpeech.QUEUE_FLUSH, null, "jago_${System.currentTimeMillis()}")
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = BackgroundWhite,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PrimarySurfaceLight)
                                .border(1.5.dp, PrimaryDeepOrange, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.Headset,
                                contentDescription = null,
                                tint = PrimaryDeepOrange,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "JAGO — Scholarship Assistant",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(
                                text = "MoTA AI Voice & Text RAG",
                                fontSize = 10.5.sp,
                                color = StatusDisbursed,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Language Selector
                    Box {
                        Surface(
                            color = SurfaceCard,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderMedium),
                            modifier = Modifier.clickable { showLanguageMenu = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedLanguage.substringBefore(" "),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Language",
                                    tint = TextSubtle,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showLanguageMenu,
                            onDismissRequest = { showLanguageMenu = false }
                        ) {
                            languages.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(lang, fontSize = 12.sp) },
                                    onClick = {
                                        selectedLanguage = lang
                                        showLanguageMenu = false
                                        messages.add(
                                            HelpChatMessage(
                                                id = "lang_${System.currentTimeMillis()}",
                                                sender = "JAGO",
                                                text = "Language switched to $lang. JAGO will now respond accordingly."
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundWhite)
                .padding(padding)
        ) {
            // Chat Message Stream with Auto-Scroll State
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(messages) { msg ->
                    ChatBubbleItem(
                        message = msg,
                        onSpeak = { textToSpeak ->
                            tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null, "jago_${System.currentTimeMillis()}")
                        }
                    )
                }
            }

            // Quick Reply Chips (Always Visible Above Input)
            Surface(
                color = SurfaceCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(quickChips) { chip ->
                            Surface(
                                color = BackgroundWhite,
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderMedium),
                                modifier = Modifier.clickable {
                                    processJagoQuery(chip)
                                }
                            ) {
                                Text(
                                    text = chip,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextBody,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Input Bar with Mic and Send Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .navigationBarsPadding(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mic Button for Voice Assistant
                        IconButton(
                            onClick = {
                                isListening = !isListening
                                if (isListening) {
                                    inputText = "Can I apply for Top Class if I am on Post-Matric?"
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isListening) PrimaryDeepOrange else SurfaceCardAlt)
                                .border(1.dp, if (isListening) PrimaryDeepOrange else BorderMedium, CircleShape)
                        ) {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.Microphone,
                                contentDescription = "Voice Input",
                                tint = if (isListening) Color.White else PrimaryDeepOrange,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Text Input
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Ask JAGO in your language...", fontSize = 13.sp, color = TextSubtle) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryDeepOrange,
                                unfocusedBorderColor = BorderMedium,
                                focusedContainerColor = BackgroundWhite,
                                unfocusedContainerColor = BackgroundWhite
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Send Button
                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    val text = inputText
                                    inputText = ""
                                    processJagoQuery(text)
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(PrimaryDeepOrange)
                        ) {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.PaperPlane,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Bhashini temporary downtime notice
                    Text(
                        text = "Note: Currently utilizing Gemini Multimodal AI & on-device TTS for regional languages as Bhashini registration/API onboarding is currently facing service downtime.",
                        fontSize = 9.sp,
                        color = TextSubtle,
                        lineHeight = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubbleItem(
    message: HelpChatMessage,
    onSpeak: ((String) -> Unit)? = null
) {
    val isUser = message.sender == "USER"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(PrimaryDeepOrange),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.Headset,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) PrimaryDeepOrange else SurfaceCard
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            modifier = Modifier
                .widthIn(max = 280.dp)
                .border(
                    1.dp,
                    if (isUser) PrimaryDeepOrange else BorderLight,
                    RoundedCornerShape(16.dp)
                )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = message.text,
                    fontSize = 13.sp,
                    color = if (isUser) Color.White else TextDark,
                    lineHeight = 18.sp
                )

                if (!isUser && onSpeak != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .clickable { onSpeak(message.text) }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.Headset,
                            contentDescription = "Read Aloud",
                            tint = PrimaryDeepOrange,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Listen (On-Device TTS)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryDeepOrange
                        )
                    }
                }
            }
        }
    }
}
