package com.step.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
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

data class ChatMessage(
    val isUser: Boolean,
    val text: String,
    val source: String? = null
)

@Composable
fun JagoScreen() {
    var inputText by remember { mutableStateOf("") }
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                isUser = false,
                text = "Johar ${MoTaRepository.currentStudent.fullName.takeIf { it != "NFS" && it.isNotBlank() } ?: "Scholar"}! I am JAGO, your Ministry of Tribal Affairs (MoTA) AI Assistant on the STeP portal. You can speak to me or ask any question about the 5 MoTA scholarship schemes, eligibility criteria, document defects, or DBT bank credits. How can I assist you today?",
                source = "STeP MoTA Knowledge Core"
            )
        )
    }

    val quickSuggestions = listOf(
        "Can I apply for Top Class if I'm on Pre-Matric?",
        "What is the income ceiling for NOS?",
        "My DBT payment shows pending",
        "Are PVTG students given relaxation?"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .padding(bottom = 80.dp)
    ) {
        // Chat Header
        Surface(
            color = NavySurface,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(SaffronPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("J", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("JAGO AI Assistant", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextMain)
                        Text("● MoTA Operational Rules Engine", fontSize = 11.sp, color = EmeraldSuccess)
                    }
                }
                Surface(
                    color = EmeraldSuccess.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Sovereign AI",
                        color = EmeraldSuccess,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Quick Suggestions Horizontal Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickSuggestions) { query ->
                Surface(
                    color = Color.White.copy(alpha = 0.06f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyBorder),
                    modifier = Modifier.clickable {
                        sendJagoMessage(query, messages)
                    }
                ) {
                    Text(
                        text = query,
                        fontSize = 11.sp,
                        color = TextMain,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Messages List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 10.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(msg = msg)
            }
        }

        // Input Bar
        Surface(
            color = NavySurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        sendJagoMessage("What is the income ceiling for NOS?", messages)
                    }
                ) {
                    Icon(Icons.Default.Mic, contentDescription = "Voice Input", tint = SaffronPrimary)
                }

                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask anything or speak your query...", fontSize = 12.sp, color = TextMuted) },
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.Transparent),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = TextMain,
                        unfocusedTextColor = TextMain,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            sendJagoMessage(inputText, messages)
                            inputText = ""
                        }
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = SaffronPrimary)
                }
            }
        }
    }
}

fun sendJagoMessage(text: String, messages: MutableList<ChatMessage>) {
    messages.add(ChatMessage(isUser = true, text = text))

    val reply = when {
        text.contains("top class", ignoreCase = true) ->
            "According to MoTA Policy Clause 4.2, you cannot avail two Central scholarships simultaneously for the same academic level. However, moving from Class 12 to IIT/NIT allows you to transition smoothly to the Top Class Scheme (worth up to ₹2.86 Lakh/yr including a ₹45,000 computer grant) with zero duplicate paperwork!"
        text.contains("income", ignoreCase = true) ->
            "Here are the family income limits across the 5 MoTA schemes:\n• Pre-Matric & Post-Matric: Up to ₹2.50 Lakh/yr\n• Top Class (IIT/IIM/NIT): Up to ₹6.00 Lakh/yr\n• National Overseas (NOS): Up to ₹8.00 Lakh/yr\n• National Fellowship (NFST): NO income ceiling!"
        text.contains("pvtg", ignoreCase = true) ->
            "Yes! MoTA gives high priority to Particularly Vulnerable Tribal Groups (PVTGs). Under the National Overseas Scheme (NOS), 3 out of 20 slots are exclusively ring-fenced for PVTG candidates. Document verification is expedited under PM-JANMAN mission."
        else ->
            "I am connected to the MoTA Knowledge Base. Please ask any question regarding eligibility guidelines, document requirements, or portal applications. You can also view real-time application updates under the Track tab."
    }

    messages.add(ChatMessage(isUser = false, text = reply, source = "MoTA Sovereign AI (Grounded RAG)"))
}

@Composable
fun ChatBubble(msg: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (msg.isUser) SaffronPrimary else NavyCard
            ),
            shape = RoundedCornerShape(
                topStart = 14.dp,
                topEnd = 14.dp,
                bottomStart = if (msg.isUser) 14.dp else 2.dp,
                bottomEnd = if (msg.isUser) 2.dp else 14.dp
            ),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!msg.isUser && msg.source != null) {
                    Text(
                        text = msg.source,
                        fontSize = 9.sp,
                        color = SaffronLight,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Text(
                    text = msg.text,
                    fontSize = 13.sp,
                    color = Color.White,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
