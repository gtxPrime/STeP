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

    var currentStep by remember { mutableStateOf(LoginStep.SIGN_IN) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // User editable profile fields
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("") }
    var selectedCommunity by remember { mutableStateOf("Santhal") }
    var homeState by remember { mutableStateOf("Odisha") }
    var institutionName by remember { mutableStateOf("EMRS Baripada, Mayurbhanj, Odisha") }
    var educationLevel by remember { mutableStateOf("Class 12 (Science)") }
    var bankName by remember { mutableStateOf("State Bank of India") }
    var maskedAccount by remember { mutableStateOf("•••• •••• 4920") }
    var aadhaarLast4 by remember { mutableStateOf("9842") }
    var annualIncome by remember { mutableStateOf("145000") }

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
                                    fullName = "Birsa Munda"
                                    email = "birsa.munda@student.gov.in"
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
                            text = "Client-side encrypted with AES-256 before synchronization to Firebase.",
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
                            placeholder = { Text("e.g. Birsa Munda") },
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
                            placeholder = { Text("e.g. birsa@student.gov.in") },
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
                                if (fullName.isBlank()) fullName = "ST Scholar"
                                if (email.isBlank()) email = "student@step.gov.in"

                                val targetUid = FirebaseManager.currentUser?.uid ?: ("usr_" + email.lowercase().trim().replace(Regex("[^a-zA-Z0-9]"), "_"))
                                val newProfile = StudentProfile(
                                    uid = targetUid,
                                    apaarId = "9842-1084-2026",
                                    digilockerId = "DL-ST-883921",
                                    fullName = fullName,
                                    email = email,
                                    photoUrl = photoUrl,
                                    community = "Scheduled Tribe (ST)",
                                    subTribe = selectedCommunity,
                                    institution = institutionName,
                                    educationLevel = educationLevel,
                                    annualIncome = annualIncome.toLongOrNull() ?: 145000L,
                                    bankName = bankName,
                                    maskedAccount = maskedAccount,
                                    ifsc = "SBIN0001234",
                                    aadhaarLast4 = aadhaarLast4,
                                    state = homeState,
                                    npciAadhaarSeeded = true
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
                                    text = "STAGE 1 SANDBOX",
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
                            text = "Pull authentic certificates directly from stage1.digitallocker.gov.in. Verified documents save instantly to Firebase Cloud Firestore.",
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
                                val isVerified = pulledDocs.any { it.docType == cert.docType }
                                val isSelected = selectedDocType == cert.docType
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isVerified) StatusDisbursedBg else if (isSelected) PrimarySurfaceLight else BackgroundWhite
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            1.dp,
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
                                                        Text(
                                                            text = "VERIFIED",
                                                            color = StatusDisbursed,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
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
                                        candidateName = fullName.ifBlank { "Birsa Munda" }
                                    )
                                    isPullingDoc = false
                                    if (res.success) {
                                        if (pulledDocs.none { it.docType == res.docType }) {
                                            pulledDocs.add(res)
                                        }
                                        // Auto-advance to next unverified certificate
                                        val nextUnverified = testCerts.firstOrNull { c -> pulledDocs.none { it.docType == c.docType } }
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

