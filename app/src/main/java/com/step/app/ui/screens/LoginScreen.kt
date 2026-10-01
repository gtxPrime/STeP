package com.step.app.ui.screens

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.step.app.data.MoTaRepository
import com.step.app.data.StudentProfile
import com.step.app.digilocker.DigiLockerSandboxManager
import com.step.app.digilocker.DigiLockerSandboxResult
import com.step.app.firebase.FirebaseManager
import com.step.app.ui.components.FontAwesomeIcons
import com.step.app.ui.theme.*
import kotlinx.coroutines.launch

private const val TAG = "STePGoogleLogin"

enum class LoginStep {
    LANGUAGE_SELECTION,
    SIGN_IN,
    PROFILE_SETUP,
    DIGILOCKER_SETUP,
    COMPLETED
}

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    var currentStep by remember { mutableStateOf(LoginStep.LANGUAGE_SELECTION) }
    var selectedLanguage by remember { mutableStateOf("English") }
    var isPvtgVoiceAssistEnabled by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // User editable profile fields
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("") }
    var selectedCommunity by remember { mutableStateOf("") }
    var homeState by remember { mutableStateOf("") }
    var institutionName by remember { mutableStateOf("") }
    var educationLevel by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var maskedAccount by remember { mutableStateOf("") }
    var aadhaarLast4 by remember { mutableStateOf("") }
    var annualIncome by remember { mutableStateOf("") }

    // DigiLocker Sandbox state
    val testCerts = remember { DigiLockerSandboxManager.getSandboxTestCertificates() }
    var selectedDocType by remember { mutableStateOf("CASTC") }
    var certNumber by remember { mutableStateOf("OD/ST/2022/49201") }
    var isPullingDoc by remember { mutableStateOf(false) }
    val pulledDocs = remember { mutableStateListOf<DigiLockerSandboxResult>() }

    // Web Client ID
    val webClientId = remember {
        try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) context.getString(resId) else FirebaseManager.WEB_CLIENT_ID
        } catch (_: Exception) {
            FirebaseManager.WEB_CLIENT_ID
        }
    }

    // Google Sign-In Client
    val googleSignInClient = remember(webClientId) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .requestProfile()
            .build()
        activity?.let { GoogleSignIn.getClient(it, gso) }
    }

    val googleAuthLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isLoading = false
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
                Log.i(TAG, "Google Sign-In SUCCESS: ${account.displayName} (${account.email})")

                fullName = account.displayName ?: account.givenName ?: "ST Scholar"
                email = account.email ?: "student@step.gov.in"
                photoUrl = account.photoUrl?.toString().orEmpty()

                FirebaseManager.loginWithGoogleAccount(context, account) { isNewUser ->
                    if (isNewUser) {
                        currentStep = LoginStep.PROFILE_SETUP
                    } else {
                        onLoginSuccess()
                    }
                }
            } catch (e: ApiException) {
                Log.e(TAG, "Google Sign-In failed (${e.statusCode}): ${e.message}")
                errorMessage = "Google Sign-In failed (${e.statusCode}): ${e.localizedMessage}"
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In error: ${e.message}")
                errorMessage = e.message ?: "Authentication failed"
            }
        } else {
            errorMessage = "Sign-in was cancelled"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Sovereign Emblem with Saffron Gradient
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFE8590C), Color(0xFFD9480F))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "STeP",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = PrimarySurfaceLight,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryDeepOrange.copy(alpha = 0.25f))
            ) {
                Text(
                    text = "MINISTRY OF TRIBAL AFFAIRS • GOVT. OF INDIA",
                    color = PrimaryDeepOrangeDark,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "STeP Portal",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark,
                letterSpacing = (-0.3).sp
            )
            Text(
                text = "Sovereign Tribal Education & Scholarship Platform",
                fontSize = 12.sp,
                color = TextSubtle,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // STEP 0: LANGUAGE SELECTION & PVTG VOICE ONBOARDING MODE
            if (currentStep == LoginStep.LANGUAGE_SELECTION) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderLight, RoundedCornerShape(18.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = PrimarySurfaceLight,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "ACCESSIBILITY & INCLUSION",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryDeepOrangeDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Select Language / भाषा चुनें",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = TextDark
                        )
                        Text(
                            text = "Choose your regional language for scholarship guidance and voice assistance.",
                            fontSize = 11.5.sp,
                            color = TextSubtle,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                        )

                        val languageList = listOf(
                            "English" to "Default",
                            "हिन्दी" to "Hindi",
                            "ଓଡ଼ିଆ" to "Odia",
                            "मराठी" to "Marathi",
                            "తెలుగు" to "Telugu",
                            "தமிழ்" to "Tamil",
                            "ᱥᱟᱱᱛᱟᱲᱤ" to "Santhali",
                            "गोंडी" to "Gondi"
                        )

                        // 2-Column Grid for Language Options
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            languageList.chunked(2).forEach { rowPair ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowPair.forEach { (nativeName, engName) ->
                                        val isSelected = selectedLanguage == engName || selectedLanguage == nativeName
                                        Surface(
                                            color = if (isSelected) PrimarySurfaceLight else BackgroundWhite,
                                            shape = RoundedCornerShape(12.dp),
                                            border = androidx.compose.foundation.BorderStroke(
                                                if (isSelected) 1.5.dp else 1.dp,
                                                if (isSelected) PrimaryDeepOrange else BorderLight
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { selectedLanguage = engName }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                RadioButton(
                                                    selected = isSelected,
                                                    onClick = { selectedLanguage = engName },
                                                    colors = RadioButtonDefaults.colors(selectedColor = PrimaryDeepOrange),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = nativeName,
                                                        fontSize = 13.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = TextDark
                                                    )
                                                    Text(
                                                        text = engName,
                                                        fontSize = 9.sp,
                                                        color = TextSubtle
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // PVTG Voice Onboarding Mode Card
                        Surface(
                            color = BackgroundWhite,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(PrimarySurfaceLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = FontAwesomeIcons.Solid.Microphone,
                                        contentDescription = null,
                                        tint = PrimaryDeepOrange,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "PVTG Voice-Guided Mode",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextDark
                                    )
                                    Text(
                                        text = "Voice prompts & spoken form filling",
                                        fontSize = 10.sp,
                                        color = TextSubtle
                                    )
                                }
                                Switch(
                                    checked = isPvtgVoiceAssistEnabled,
                                    onCheckedChange = { isPvtgVoiceAssistEnabled = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = PrimaryDeepOrange
                                    ),
                                    modifier = Modifier.height(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = { currentStep = LoginStep.SIGN_IN },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryDeepOrange),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Text(
                                text = "Continue to Sign In →",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // STEP 1: INITIAL SIGN IN CARD
            if (currentStep == LoginStep.SIGN_IN) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderLight, RoundedCornerShape(18.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Student Single Sign-On",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextDark
                        )
                        Text(
                            text = "Sign in to access your scholarship applications, DigiLocker certificates, and DBT bank status.",
                            fontSize = 12.sp,
                            color = TextBody,
                            textAlign = TextAlign.Center,
                            lineHeight = 17.sp,
                            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                        )

                        if (errorMessage != null) {
                            Surface(
                                color = StatusRejectedBg,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Text(
                                    text = errorMessage!!,
                                    color = StatusRejected,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        // Authentic Google Sign In Button
                        Button(
                            onClick = {
                                errorMessage = null
                                if (googleSignInClient != null) {
                                    isLoading = true
                                    googleSignInClient.signOut().addOnCompleteListener {
                                        googleAuthLauncher.launch(googleSignInClient.signInIntent)
                                    }
                                } else {
                                    fullName = "NAS"
                                    email = "NAS"
                                    currentStep = LoginStep.PROFILE_SETUP
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(12.dp))
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = PrimaryDeepOrange,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = com.step.app.R.drawable.ic_google_logo),
                                        contentDescription = "Google",
                                        modifier = Modifier.size(19.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Continue with Google",
                                        color = Color(0xFF374151),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Info pill indicating automatic account recognition
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.ShieldCheck,
                                contentDescription = null,
                                tint = StatusDisbursed,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Auto-detects existing account or guides new scholar setup",
                                color = TextSubtle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // STEP 2: PROFILE DETAILS SETUP
            AnimatedVisibility(visible = currentStep == LoginStep.PROFILE_SETUP) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderLight, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Step 1 of 2: Scholar Profile",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryDeepOrange,
                                maxLines = 1,
                                softWrap = false
                            )
                            Surface(
                                color = PrimarySurfaceLight,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "50% Complete",
                                    color = PrimaryDeepOrangeDark,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Set Up Your Sovereign Profile",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Client-side encrypted with AES-256 before sovereign synchronization.",
                            fontSize = 11.sp,
                            color = TextSubtle
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Full Name
                        Text("Full Name", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            placeholder = { Text("e.g. Student Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Email
                        Text("Email Address", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = { Text("e.g. student@gov.in") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // ST Community Selection
                        Text("ST Tribe / Community", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                        Spacer(modifier = Modifier.height(6.dp))
                        val tribes = listOf("Santhal", "Munda", "Gond", "Bodo", "Bhil", "Khasi", "Oraon", "Other ST")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            tribes.take(4).forEach { t ->
                                FilterChip(
                                    selected = selectedCommunity == t,
                                    onClick = { selectedCommunity = t },
                                    label = { Text(t, fontSize = 11.sp) }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            tribes.drop(4).forEach { t ->
                                FilterChip(
                                    selected = selectedCommunity == t,
                                    onClick = { selectedCommunity = t },
                                    label = { Text(t, fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Home State
                        Text("Home State", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = homeState,
                            onValueChange = { homeState = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Enrolled School / Institution
                        Text("Enrolled School / College", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = institutionName,
                            onValueChange = { institutionName = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (fullName.isBlank()) fullName = "NAS"
                                if (email.isBlank()) email = "NAS"

                                val targetUid = FirebaseManager.currentUser?.uid ?: ("usr_" + email.lowercase().trim().replace(Regex("[^a-zA-Z0-9]"), "_"))
                                val newProfile = StudentProfile(
                                    uid = targetUid,
                                    apaarId = "NAS",
                                    digilockerId = "NAS",
                                    fullName = fullName,
                                    email = email,
                                    photoUrl = photoUrl,
                                    community = "Scheduled Tribe (ST)",
                                    subTribe = selectedCommunity.ifBlank { "NAS" },
                                    institution = institutionName.ifBlank { "NAS" },
                                    educationLevel = educationLevel.ifBlank { "NAS" },
                                    annualIncome = annualIncome.toLongOrNull() ?: 0L,
                                    bankName = bankName.ifBlank { "NAS" },
                                    maskedAccount = maskedAccount.ifBlank { "NAS" },
                                    ifsc = "NAS",
                                    aadhaarLast4 = aadhaarLast4.ifBlank { "NAS" },
                                    state = homeState.ifBlank { "NAS" },
                                    npciAadhaarSeeded = false
                                )
                                MoTaRepository.currentStudent = newProfile
                                FirebaseManager.saveStudentProfileToFirestore(newProfile, targetUid, context)

                                currentStep = LoginStep.DIGILOCKER_SETUP
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryDeepOrange),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("Save Profile & Connect DigiLocker", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(imageVector = FontAwesomeIcons.Solid.ArrowRight, contentDescription = null, modifier = Modifier.size(13.dp))
                        }
                    }
                }
            }

            // STEP 3: DIGILOCKER SANDBOX INTEGRATION
            AnimatedVisibility(visible = currentStep == LoginStep.DIGILOCKER_SETUP) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderLight, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Step 2: DigiLocker Sandbox",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryDeepOrange,
                                maxLines = 1,
                                softWrap = false
                            )
                            Surface(
                                color = StatusDisbursedBg,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "STAGE 1 SANDBOX*",
                                    color = StatusDisbursed,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Connect Sovereign DigiLocker Vault",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Pull authentic certificates directly from stage1.digitallocker.gov.in. Verified documents save instantly to the National MoTA Repository.",
                            fontSize = 11.sp,
                            color = TextSubtle,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Select Certificate
                        Text("Select Document to Pull:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark)
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            testCerts.forEach { cert ->
                                val isVerified = pulledDocs.any { 
                                    it.docTypeCode.equals(cert.docType, ignoreCase = true) || 
                                    it.docType.contains(cert.docType, ignoreCase = true) || 
                                    it.docType.contains(cert.name.replace("*", "").trim(), ignoreCase = true)
                                }
                                val isSelected = selectedDocType == cert.docType
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isVerified) StatusDisbursedBg else if (isSelected) PrimarySurfaceLight else BackgroundWhite
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            1.5.dp,
                                            if (isVerified) StatusDisbursed else if (isSelected) PrimaryDeepOrange else BorderLight,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            selectedDocType = cert.docType
                                            certNumber = cert.defaultCertNumber
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isVerified) FontAwesomeIcons.Solid.CircleCheck else FontAwesomeIcons.Solid.ShieldCheck,
                                            contentDescription = null,
                                            tint = if (isVerified) StatusDisbursed else if (isSelected) PrimaryDeepOrange else TextSubtle,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = cert.name,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isVerified) StatusDisbursed else TextDark
                                                )
                                                if (isVerified) {
                                                    Surface(
                                                        color = StatusDisbursed.copy(alpha = 0.15f),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = FontAwesomeIcons.Solid.Check,
                                                                contentDescription = null,
                                                                tint = StatusDisbursed,
                                                                modifier = Modifier.size(8.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(3.dp))
                                                            Text(
                                                                text = "VERIFIED",
                                                                color = StatusDisbursed,
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                            Text(
                                                text = if (isVerified) "Pulled & Verified via DigiLocker Sandbox" else cert.issuerName,
                                                fontSize = 10.sp,
                                                color = if (isVerified) StatusDisbursed.copy(alpha = 0.85f) else TextSubtle
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Certificate Number Input
                        Text("Registration / Certificate Number", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = certNumber,
                            onValueChange = { certNumber = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Pull Button
                        Button(
                            onClick = {
                                isPullingDoc = true
                                scope.launch {
                                    val res = DigiLockerSandboxManager.pullCertificateFromSandbox(
                                        docType = selectedDocType,
                                        certificateNumber = certNumber,
                                        candidateName = fullName.ifBlank { "NAS" }
                                    )
                                    isPullingDoc = false
                                    if (res.success) {
                                        if (pulledDocs.none { it.docTypeCode.equals(res.docTypeCode, ignoreCase = true) || it.docType == res.docType }) {
                                            pulledDocs.add(res)
                                        }

                                        // Seed & sync student sovereign testing credentials
                                        val current = MoTaRepository.currentStudent
                                        val updated = current.copy(
                                            apaarId = if (current.apaarId.isBlank() || current.apaarId == "NAS") "APAAR-2026-9842-1082" else current.apaarId,
                                            digilockerId = if (current.digilockerId.isBlank() || current.digilockerId == "NAS") "DL-MOTA-98421" else current.digilockerId,
                                            aadhaarLast4 = if (current.aadhaarLast4.isBlank() || current.aadhaarLast4 == "NAS") "9842" else current.aadhaarLast4,
                                            bankName = if (current.bankName.isBlank() || current.bankName == "NAS") "State Bank of India (PFMS Active)" else current.bankName,
                                            maskedAccount = if (current.maskedAccount.isBlank() || current.maskedAccount == "NAS") "•••• •••• 4819" else current.maskedAccount,
                                            ifsc = if (current.ifsc.isBlank() || current.ifsc == "NAS") "SBIN0000037" else current.ifsc,
                                            institution = if (current.institution.isBlank() || current.institution == "NAS") "Govt. Autonomous College, Baripada" else current.institution,
                                            educationLevel = if (current.educationLevel.isBlank() || current.educationLevel == "NAS") "Higher Secondary (Science)" else current.educationLevel,
                                            state = if (current.state.isBlank() || current.state == "NAS") "Odisha" else current.state,
                                            community = "Scheduled Tribe (ST)",
                                            subTribe = if (current.subTribe.isBlank() || current.subTribe == "NAS") "Santhal" else current.subTribe,
                                            annualIncome = if (current.annualIncome <= 0L) 145000L else current.annualIncome,
                                            npciAadhaarSeeded = true
                                        )
                                        MoTaRepository.currentStudent = updated
                                        FirebaseManager.saveStudentProfileToFirestore(updated, updated.uid, context)

                                        // Auto-advance to next unverified certificate
                                        val nextUnverified = testCerts.firstOrNull { c -> 
                                            pulledDocs.none { 
                                                it.docTypeCode.equals(c.docType, ignoreCase = true) || 
                                                it.docType.contains(c.docType, ignoreCase = true) ||
                                                it.docType.contains(c.name.replace("*", "").trim(), ignoreCase = true)
                                            } 
                                        }
                                        if (nextUnverified != null) {
                                            selectedDocType = nextUnverified.docType
                                            certNumber = nextUnverified.defaultCertNumber
                                        }
                                    }
                                }
                            },
                            enabled = !isPullingDoc && certNumber.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryDeepOrange),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            if (isPullingDoc) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Connecting to stage1.digitallocker...", fontSize = 12.sp)
                            } else {
                                Icon(imageVector = FontAwesomeIcons.Solid.ShieldCheck, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pull & Verify via DigiLocker Sandbox", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Complete Setup Button
                        Button(
                            onClick = {
                                val currentStudent = MoTaRepository.currentStudent
                                FirebaseManager.completeRegistration(currentStudent, context)
                                onLoginSuccess()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusDisbursed),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("Complete & Enter STeP Portal", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(imageVector = FontAwesomeIcons.Solid.ArrowRight, contentDescription = null, modifier = Modifier.size(13.dp))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = {
                                val currentStudent = MoTaRepository.currentStudent
                                FirebaseManager.completeRegistration(currentStudent, context)
                                onLoginSuccess()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Skip DigiLocker for now and enter portal",
                                fontSize = 11.sp,
                                color = TextSubtle
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Unified Sovereign Trust Bar (Single row, evenly spaced, never wraps!)
            Surface(
                color = SurfaceCard,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FooterTrustItem("APAAR ID")
                    Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(BorderMedium))
                    FooterTrustItem("DigiLocker")
                    Box(modifier = Modifier.size(3.dp).clip(CircleShape).background(BorderMedium))
                    FooterTrustItem("NPCI DBT")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Profile Data: Authenticated via Google SSO • Verified via DigiLocker Sandbox",
                fontSize = 10.sp,
                color = TextSubtle,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun FooterTrustItem(label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = FontAwesomeIcons.Solid.Check,
            contentDescription = null,
            tint = StatusDisbursed,
            modifier = Modifier.size(9.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = TextSubtle,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false
        )
    }
}

