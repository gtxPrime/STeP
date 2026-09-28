package com.step.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.step.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeficiencyBottomSheet(
    onDismiss: () -> Unit
) {
    var selectedLang by remember { mutableStateOf("en") }

    val languages = listOf(
        "en" to "English",
        "hi" to "हिन्दी",
        "or" to "ଓଡ଼ିଆ",
        "mr" to "मराठी",
        "te" to "తెలుగు",
        "ta" to "தமிழ்"
    )

    val explanations = mapOf(
        "en" to Triple(
            "Your offer letter from Imperial College London has a temporary condition: they are waiting for your final college marksheets and student visa CAS number. MoTA rules need an unconditional letter before releasing your ₹22 Lakh scholarship.",
            listOf(
                "Step 1: Email Imperial College Admissions requesting an 'Unconditional Offer Letter for MoTA Scholarship Sponsorship'.",
                "Step 2: Download the official Imperial QS 2026 Ranking certificate (#2 Worldwide).",
                "Step 3: Upload both documents in the STeP app before 15 October 2026."
            ),
            "You have 16 days remaining. Your seat and scholarship allocation are 100% reserved!"
        ),
        "hi" to Triple(
            "इंपीरियल कॉलेज लंदन के पत्र में एक शर्त लिखी है—वे आपकी अंतिम वर्ष की मार्कशीट और वीज़ा नंबर (CAS) का इंतज़ार कर रहे हैं। ₹22 लाख की छात्रवृत्ति जारी करने के लिए 'बिना शर्त' (Unconditional) पत्र आवश्यक है।",
            listOf(
                "पहला कदम: कॉलेज को ईमेल भेजकर 'मंत्रालय छात्रवृत्ति हेतु Unconditional पत्र' मांगें।",
                "दूसरा कदम: कॉलेज की QS वर्ल्ड रैंकिंग 2026 (रैंक #2) का प्रमाण पत्र जोड़ें।",
                "तीसरा कदम: 15 अक्टूबर 2026 से पहले STeP ऐप पर दस्तावेज़ अपलोड कर दें।"
            ),
            "आपके पास 16 दिन बाकी हैं। समय पर दस्तावेज़ देने पर छात्रवृत्ति सुरक्षित है।"
        ),
        "or" to Triple(
            "ଇମ୍ପେରିଆଲ୍ କଲେଜ୍ ଲଣ୍ଡନ୍ ରୁ ଆପଣଙ୍କୁ ମିଳିଥିବା ଆଡମିଶନ ଲେଟରରେ ଗୋଟିଏ ସର୍ତ୍ତ ଲେଖାଅଛି। ଜନଜାତି ବ୍ୟାପାର ମନ୍ତ୍ରଣାଳୟର ₹୨୨ ଲକ୍ଷ ସ୍କଲାରସିପ୍ ପାଇଁ ସର୍ତ୍ତବିହୀନ (Unconditional) ପତ୍ର ଆବଶ୍ୟକ।",
            listOf(
                "ପଦକ୍ଷେପ ୧: କଲେଜ୍କୁ ଇମେଲ୍ କରି ଅନକଣ୍ଡିସନାଲ୍ ଅଫର୍ ଲେଟର ପାଇଁ ଅନୁରୋଧ କରନ୍ତୁ।",
                "ପଦକ୍ଷେପ ୨: କଲେଜର QS ୱାର୍ଲ୍ଡ ରାଙ୍କିଙ୍ଗ୍ (#୨) ପ୍ରମାଣପତ୍ର ସଂଲଗ୍ନ କରନ୍ତୁ।",
                "ପଦକ୍ଷେପ ୩: ଏହି STeP ଆପ୍ ମାଧ୍ୟମରେ ୧୫ ଅକ୍ଟୋବର ପୂର୍ବରୁ ଅପଲୋଡ୍ କରନ୍ତୁ।"
            ),
            "ଆପଣଙ୍କ ପାଖରେ ୧୬ ଦିନ ବାକି ଅଛି। ଆପଣଙ୍କ ସ୍କଲାରସିପ୍ ସମ୍ପୂର୍ଣ୍ଣ ସୁରକ୍ଷିତ ଅଛି!"
        )
    )

    val currentExplanation = explanations[selectedLang] ?: explanations["en"]!!

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BackgroundWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = BorderMedium,
                height = 4.dp,
                width = 36.dp
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Surface(
                color = StatusPendingBg,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "AI MULTILINGUAL DEFECT EXPLAINER",
                    color = StatusPending,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Application Scrutiny Clarification",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Language Selector Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(languages) { (code, label) ->
                    val isSelected = selectedLang == code
                    Surface(
                        color = if (isSelected) PrimaryDeepOrange else SurfaceCard,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) PrimaryDeepOrange else BorderLight
                        ),
                        modifier = Modifier.clickable { selectedLang = code }
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else TextBody,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bureaucratic defect notice
            Surface(
                color = SurfaceCard,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Official Bureaucratic Defect Notice:",
                        fontSize = 10.sp,
                        color = TextSubtle,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "DEF-D402: Uploaded conditional admission letter does not certify unencumbered status under MoTA Clause 7(ii)(b). QS ranking certificate snippet absent.",
                        fontSize = 11.sp,
                        color = StatusRejected,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Plain Language Meaning
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = FontAwesomeIcons.Solid.WandMagicSparkles,
                    contentDescription = null,
                    tint = PrimaryDeepOrange,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "What does this actually mean?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextDark
                )
            }

            Text(
                text = currentExplanation.first,
                fontSize = 12.sp,
                color = TextBody,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Actionable steps
            Text(
                text = "Actionable Steps to Cure:",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = TextDark
            )

            currentExplanation.second.forEach { step ->
                Text(
                    text = step,
                    fontSize = 11.sp,
                    color = TextBody,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Assurance card
            Surface(
                color = StatusDisbursedBg,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.ShieldCheck,
                        contentDescription = null,
                        tint = StatusDisbursed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = currentExplanation.third,
                        fontSize = 11.sp,
                        color = StatusDisbursed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryDeepOrange),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Understood, I'll Fix This",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Backward-compatible alias for existing references
@Composable
fun DeficiencyDialog(onDismiss: () -> Unit) = DeficiencyBottomSheet(onDismiss = onDismiss)
