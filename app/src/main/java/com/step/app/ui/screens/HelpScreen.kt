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
import com.step.app.ui.components.FontAwesomeIcons
import com.step.app.ui.theme.*

data class HelpChatMessage(
    val id: String,
    val sender: String, // "USER" or "JAGO"
    val text: String,
    val timestamp: String = "Just now"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen() {
    var selectedLanguage by remember { mutableStateOf("English") }
    var showLanguageMenu by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }

    val languages = listOf("English", "हिन्दी (Hindi)", "ଓଡ଼ିଆ (Odia)", "తెలుగు (Telugu)", "தமிழ் (Tamil)")

    val quickChips = listOf(
        "Check my status",
        "What documents do I need?",
        "Why was my payment delayed?"
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
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(PrimarySurfaceLight)
                                .border(1.5.dp, PrimaryDeepOrange, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.Headset,
                                contentDescription = null,
                                tint = PrimaryDeepOrange,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "JAGO — Scholarship Assistant",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = "MoTA AI Sovereign Voice & Text RAG",
                                fontSize = 11.sp,
                                color = StatusDisbursed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Language Selector
                    Box {
                        Surface(
                            color = SurfaceCard,
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderMedium),
                            modifier = Modifier.clickable { showLanguageMenu = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedLanguage,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
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
            // Chat Message Stream
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(messages) { msg ->
                    ChatBubbleItem(message = msg)
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
                                    messages.add(HelpChatMessage("u_${System.currentTimeMillis()}", "USER", chip))
                                    // Generate contextual JAGO answer
                                    val reply = when (chip) {
                                        "Check my status" -> "Your Post-Matric Scholarship is successfully Disbursed (UTR: RBI492810488219). Your Top Class application is Sanctioned in PFMS queue. Your NOS application has a pending defect (D-402)."
                                        "What documents do I need?" -> "For most MoTA scholarships, you need: 1. ST Caste Certificate, 2. Current Year Family Income Certificate, 3. Previous Academic Year Marksheet, and 4. Aadhaar-seeded Bank Passbook."
                                        else -> "DBT disbursements follow a 75% Central / 25% State funding ratio. If state nodal verification is delayed or Aadhaar is not NPCI mapped, payments stay in clearing. You can escalate via the 30-day Citizen Charter SLA ticket."
                                    }
                                    messages.add(HelpChatMessage("j_${System.currentTimeMillis()}", "JAGO", reply))
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
                                    messages.add(HelpChatMessage("u_${System.currentTimeMillis()}", "USER", text))
                                    messages.add(
                                        HelpChatMessage(
                                            "j_${System.currentTimeMillis()}",
                                            "JAGO",
                                            "Under Ministry of Tribal Affairs guidelines, you cannot receive two central scholarships simultaneously for the same academic year, but you can upgrade to Top Class Education if you secure admission in an IIT, NIT, or premier institute."
                                        )
                                    )
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
                }
            }
        }
    }
}

@Composable
private fun ChatBubbleItem(message: HelpChatMessage) {
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
            }
        }
    }
}
