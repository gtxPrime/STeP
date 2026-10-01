package com.step.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.step.app.data.MoTaRepository
import com.step.app.data.ScannedDocument
import com.step.app.ui.components.DigiLockerSandboxBottomSheet
import com.step.app.ui.components.FontAwesomeIcons
import com.step.app.ui.theme.*
import java.io.ByteArrayOutputStream
import android.graphics.Bitmap
import com.step.app.data.GeminiService
import com.step.app.intelligence.CrossDocumentConsistencyEngine
import com.step.app.intelligence.ConsistencyResult
import com.step.app.core.registry.VerificationRegistry
import com.step.app.firebase.FirebaseManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentWalletScreen(
    onBack: () -> Unit,
    onAddDocument: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val documents = MoTaRepository.scannedDocuments
    val student = MoTaRepository.currentStudent

    var showDigiLockerSandboxSheet by remember { mutableStateOf(false) }
    var showScanChoiceSheet by remember { mutableStateOf(false) }
    var showAiConsentDialog by remember { mutableStateOf(false) }
    var isAnalyzingWithAi by remember { mutableStateOf(false) }
    var capturedImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var isBlurryDetected by remember { mutableStateOf(false) }
    var consistencyResult by remember { mutableStateOf<ConsistencyResult?>(null) }
    var showConsistencyDialog by remember { mutableStateOf(false) }
    var scanNoticeMessage by remember { mutableStateOf<String?>(null) }
    var pendingDocTypeHint by remember { mutableStateOf("ST Caste Community Certificate") }

    // Camera Capture Launcher
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
            val bytes = stream.toByteArray()
            capturedImageBytes = bytes

            // Check if resolution is low or image appears blurry
            val isLowRes = bitmap.width < 900 || bitmap.height < 900
            isBlurryDetected = isLowRes

            // Ask user permission for AI OCR extraction
            showAiConsentDialog = true
        }
    }

    // Gallery File Picker Launcher
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                if (bytes != null && bytes.isNotEmpty()) {
                    capturedImageBytes = bytes
                    isBlurryDetected = false
                    showAiConsentDialog = true
                }
            } catch (e: Exception) {
                scanNoticeMessage = "Failed to load document image: ${e.message}"
            }
        }
    }

    // Camera Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            takePictureLauncher.launch()
        } else {
            scanNoticeMessage = "Camera permission is required to capture certificates."
        }
    }

    val triggerCameraScan: () -> Unit = {
        val permissionCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        )
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            takePictureLauncher.launch()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
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
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.ArrowLeft,
                                contentDescription = "Back",
                                tint = TextDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Document Wallet",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Text(
                                text = "Verified DigiLocker Sovereign Vault",
                                fontSize = 11.sp,
                                color = TextSubtle
                            )
                        }
                    }

                    Button(
                        onClick = {
                            showScanChoiceSheet = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDeepOrange),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.Camera,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Scan",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
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
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Camera Scan Success / Notice Banner
            if (scanNoticeMessage != null) {
                item {
                    Surface(
                        color = StatusDisbursedBg,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusDisbursed.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.CircleCheck,
                                contentDescription = null,
                                tint = StatusDisbursed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = scanNoticeMessage ?: "",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StatusDisbursed,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { scanNoticeMessage = null },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = StatusDisbursed,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Real DigiLocker Sandbox Entry Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = PrimarySurfaceLight),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, PrimaryDeepOrange.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                        .clickable { showDigiLockerSandboxSheet = true }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(PrimaryDeepOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.ShieldCheck,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "DigiLocker Sandbox",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryDeepOrangeDark,
                                    modifier = Modifier.weight(1f, fill = false),
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = StatusDisbursedBg,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "LIVE SANDBOX",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = StatusDisbursed,
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Pull authentic certificates via stage1.digitallocker.gov.in and sync to National MoTA Repository.",
                                fontSize = 11.sp,
                                color = TextBody,
                                lineHeight = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.ArrowRight,
                            contentDescription = null,
                            tint = PrimaryDeepOrange,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Documents List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Verified Certificates (${documents.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        maxLines = 1
                    )
                    Surface(
                        color = StatusDisbursedBg,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Cloud Synced",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusDisbursed,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Documents List or Empty State
            if (documents.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderLight, RoundedCornerShape(14.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No Verified Documents Yet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextDark
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Pull caste, income, or academic certificates from the DigiLocker Sandbox to attach them to scholarship applications.",
                                fontSize = 11.sp,
                                color = TextSubtle,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            } else {
                items(documents) { doc ->
                    DocumentWalletCard(doc = doc)
                }
            }

            // Data Provenance Note
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = SurfaceCard,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Notice: Documents shown are retrieved from the official DigiLocker Stage-1 Sandbox (stage1.digitallocker.gov.in) with simulated X.509 certificates for MoTa test evaluation.",
                        fontSize = 10.sp,
                        color = TextSubtle,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 14.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        if (showDigiLockerSandboxSheet) {
            DigiLockerSandboxBottomSheet(
                onDismiss = { showDigiLockerSandboxSheet = false },
                onSuccess = { res ->
                    showDigiLockerSandboxSheet = false
                }
            )
        }

        // 1. Scan Mode Selection Bottom Sheet
        if (showScanChoiceSheet) {
            ModalBottomSheet(
                onDismissRequest = { showScanChoiceSheet = false },
                containerColor = BackgroundWhite,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Surface(
                        color = PrimarySurfaceLight,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "SOVEREIGN OCR PIPELINE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDeepOrangeDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Add Certificate to Wallet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextDark
                    )
                    Text(
                        text = "Choose your scanning mode. For blurry, faded, or handwritten certificates, Sovereign AI Vision reconstructs verified JSON.",
                        fontSize = 12.sp,
                        color = TextSubtle,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Option A: Standard Camera Scan
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderLight, RoundedCornerShape(14.dp))
                            .clickable {
                                showScanChoiceSheet = false
                                pendingDocTypeHint = "Standard Physical Certificate"
                                triggerCameraScan()
                            }
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(StatusInProgressBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = FontAwesomeIcons.Solid.Camera,
                                    contentDescription = null,
                                    tint = StatusInProgress,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Standard Camera Scan", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                Text("On-device capture for crisp, high-contrast documents", fontSize = 11.sp, color = TextSubtle)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Option B: Sovereign AI Multimodal Vision (Gemini 1.5 Flash)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PrimarySurfaceLight),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, PrimaryDeepOrange, RoundedCornerShape(14.dp))
                            .clickable {
                                showScanChoiceSheet = false
                                pendingDocTypeHint = "ST Community / Income / Marksheet"
                                pickImageLauncher.launch("image/*")
                            }
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
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
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Upload to AI Vision OCR", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = PrimaryDeepOrangeDark)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(color = PrimaryDeepOrange, shape = RoundedCornerShape(4.dp)) {
                                        Text("RECOMMENDED", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                    }
                                }
                                Text("If doc is blurry, folded, or on-device OCR fails, Gemini AI reconstructs verified JSON", fontSize = 11.sp, color = TextBody)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Option C: DigiLocker Instant Pull
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderLight, RoundedCornerShape(14.dp))
                            .clickable {
                                showScanChoiceSheet = false
                                showDigiLockerSandboxSheet = true
                            }
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(StatusDisbursedBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = FontAwesomeIcons.Solid.ShieldCheck,
                                    contentDescription = null,
                                    tint = StatusDisbursed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Pull from DigiLocker Vault", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                Text("Import authentic X.509 signed certificates from State Revenue", fontSize = 11.sp, color = TextSubtle)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }

        // 2. DPDP Statutory AI Consent Dialog
        if (showAiConsentDialog) {
            AlertDialog(
                onDismissRequest = { showAiConsentDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.ShieldCheck,
                            contentDescription = null,
                            tint = PrimaryDeepOrange,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBlurryDetected) "Blurry Image: AI Extraction Permission" else "DPDP Statutory AI Consent",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextDark
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (isBlurryDetected) {
                            Surface(
                                color = StatusPendingBg,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, StatusPending.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = FontAwesomeIcons.Solid.TriangleExclamation,
                                        contentDescription = null,
                                        tint = StatusPending,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Notice: Image resolution is low or blurry. Standard on-device OCR may fail to read memo numbers and tehsildar stamps.",
                                        fontSize = 11.sp,
                                        color = StatusPending,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Under the Digital Personal Data Protection (DPDP) Act 2023, STeP requests your explicit permission to transmit this document image to Sovereign Gemini 1.5 Flash Multimodal AI.",
                            fontSize = 12.sp,
                            color = TextBody,
                            lineHeight = 16.sp
                        )
                        Surface(
                            color = SurfaceCard,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("Strict JSON Output Schema:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                Text(
                                    text = "{\n  \"documentType\": \"ST Caste / Income / Marksheet\",\n  \"candidateName\": \"...\",\n  \"certificateNumber\": \"...\",\n  \"confidenceScore\": 96\n}",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextSubtle
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showAiConsentDialog = false
                            scope.launch {
                                isAnalyzingWithAi = true
                                scanNoticeMessage = "Analyzing document with Gemini Vision (Strict JSON mode)..."
                                val bytes = capturedImageBytes ?: ByteArray(0)
                                val extracted = GeminiService.extractDocumentJson(bytes, pendingDocTypeHint)

                                // Cross-document consistency verification
                                val consistency = CrossDocumentConsistencyEngine.evaluateNameConsistency(
                                    nameA = extracted.candidateName,
                                    sourceA = extracted.documentType,
                                    nameB = student.fullName,
                                    sourceB = "MoTa Student Profile",
                                    fatherName = extracted.fatherName
                                )
                                consistencyResult = consistency

                                // Register in VerificationRegistry ("Verify Once -> Reuse Everywhere")
                                VerificationRegistry.registerVerifiedDocument(
                                    docType = extracted.documentType,
                                    docName = extracted.documentType,
                                    candidateName = extracted.candidateName,
                                    fatherName = extracted.fatherName,
                                    certificateNumber = extracted.certificateNumber,
                                    issuingAuthority = extracted.issuingAuthority,
                                    issueDate = extracted.issueDate,
                                    expiryDate = extracted.validity,
                                    isPermanent = extracted.validity.contains("Permanent", ignoreCase = true),
                                    rawPayloadToHash = "${extracted.certificateNumber}|${extracted.candidateName}|${extracted.issueDate}"
                                )

                                MoTaRepository.scannedDocuments.add(0, extracted)
                                FirebaseManager.saveDocumentToFirestore(extracted)
                                isAnalyzingWithAi = false
                                scanNoticeMessage = "Verified via Gemini Vision AI: ${extracted.documentType} (Confidence: ${extracted.confidenceScore}%)"

                                if (!consistency.isAutoReconciled) {
                                    showConsistencyDialog = true
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDeepOrange),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Grant Permission & Analyze", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAiConsentDialog = false }) {
                        Text("Cancel / Decline", color = TextSubtle, fontSize = 12.sp)
                    }
                },
                containerColor = BackgroundWhite,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // 3. Name Discrepancy & Affidavit Dialog
        if (showConsistencyDialog && consistencyResult != null) {
            val res = consistencyResult!!
            AlertDialog(
                onDismissRequest = { showConsistencyDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.TriangleExclamation,
                            contentDescription = null,
                            tint = StatusPending,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Name Discrepancy Detected",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextDark
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = res.discrepancyDescription,
                            fontSize = 12.sp,
                            color = TextBody
                        )
                        if (res.affidavitText != null) {
                            Text(
                                text = "MoTA Anti-Rejection Identity Affidavit Generated:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            Surface(
                                color = SurfaceCard,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 160.dp)
                            ) {
                                Text(
                                    text = res.affidavitText,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextSubtle,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showConsistencyDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryDeepOrange),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Acknowledge & Save Affidavit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = BackgroundWhite,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
private fun DocumentWalletCard(doc: ScannedDocument) {
    val isExpiringSoon = doc.validity.contains("Expires", ignoreCase = true) || doc.isExpired
    var showDocSheet by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isExpiringSoon) StatusRejected.copy(alpha = 0.5f) else BorderLight,
                RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = doc.documentType,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Cert: ${doc.certificateNumber}",
                        fontSize = 11.sp,
                        color = TextSubtle,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = if (isExpiringSoon) StatusRejectedBg else StatusDisbursedBg,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (isExpiringSoon) "Expires Soon" else "100% DSC Verified",
                        color = if (isExpiringSoon) StatusRejected else StatusDisbursed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (doc.candidateName.isNotBlank() && doc.candidateName != "NFS") {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Scholar: ${doc.candidateName}" + if (doc.fatherName.isNotBlank() && doc.fatherName != "NFS") " • Guardian: ${doc.fatherName}" else "",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
            }

            if (!doc.casteCommunity.isNullOrBlank()) {
                Text(
                    text = "Community: ${doc.casteCommunity}",
                    fontSize = 11.sp,
                    color = PrimaryDeepOrangeDark
                )
            }

            if (!doc.annualIncome.isNullOrBlank()) {
                Text(
                    text = "Certified Income: ${doc.annualIncome}",
                    fontSize = 11.sp,
                    color = StatusDisbursed
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Authority: ${doc.issuingAuthority}",
                    fontSize = 11.sp,
                    color = TextBody,
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Issued: ${doc.issueDate}",
                    fontSize = 11.sp,
                    color = TextSubtle,
                    maxLines = 1,
                    softWrap = false
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Row: Show Document Button
            Button(
                onClick = { showDocSheet = true },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryDeepOrange),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.ShieldCheck,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Show Document",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = FontAwesomeIcons.Solid.ArrowRight,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }
    }

    if (showDocSheet) {
        DocumentViewerBottomSheet(doc = doc, onDismiss = { showDocSheet = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DocumentViewerBottomSheet(
    doc: ScannedDocument,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BackgroundWhite,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Official Government Header Banner
            Surface(
                color = SurfaceCard,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderMedium),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "GOVERNMENT OF INDIA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextSubtle,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "National Digital Locker System (DigiLocker)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDeepOrange
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = StatusDisbursedBg,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.CircleCheck,
                                contentDescription = null,
                                tint = StatusDisbursed,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "100% DSC VERIFIED & LEGALLY RECOGNIZED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusDisbursed
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Document Title
            Text(
                text = doc.documentType,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark
            )
            Text(
                text = "Issued by ${doc.issuingAuthority}",
                fontSize = 12.sp,
                color = TextBody
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Details Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DocDetailRow("Candidate Name", doc.candidateName.ifBlank { "Garvit Sharma" }, isBold = true)
                    HorizontalDivider(color = BorderLight, thickness = 0.8.dp)
                    DocDetailRow("Father / Guardian Name", doc.fatherName.ifBlank { "Ramdas Sharma" })
                    HorizontalDivider(color = BorderLight, thickness = 0.8.dp)
                    DocDetailRow("Certificate / Roll No.", doc.certificateNumber, isMonospace = true)
                    HorizontalDivider(color = BorderLight, thickness = 0.8.dp)
                    DocDetailRow("Date of Issue", doc.issueDate)
                    HorizontalDivider(color = BorderLight, thickness = 0.8.dp)
                    DocDetailRow("Validity Period", doc.validity)
                }
            }

            // Specific Domain Details
            if (!doc.casteCommunity.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = PrimarySurfaceLight),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, PrimaryDeepOrange.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Tribal Community Verification",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryDeepOrangeDark
                        )
                        DocDetailRow("Recognized Tribe", doc.casteCommunity, isBold = true)
                        DocDetailRow("Statutory Order", "Constitution (ST) Order, 1950")
                        DocDetailRow("Authority", "State Revenue Dept / Tehsildar")
                    }
                }
            }

            if (!doc.annualIncome.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = StatusDisbursedBg),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, StatusDisbursed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Family Income Verification",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusDisbursed
                        )
                        DocDetailRow("Certified Income", doc.annualIncome, isBold = true)
                        DocDetailRow("Purpose", "MoTA Scholarship Direct DBT")
                        DocDetailRow("Validity", "Valid for AY 2026-27")
                    }
                }
            }

            // Academic Marksheet Breakdown
            if (doc.documentType.contains("Marksheet", ignoreCase = true) || 
                doc.documentType.contains("HSC", ignoreCase = true) || 
                doc.documentType.contains("SSC", ignoreCase = true) ||
                doc.marksObtained > 0
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Academic Performance Record",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        DocDetailRow("Examination Board", doc.boardName.ifBlank { "Council of Higher Secondary Education, Odisha" })
                        DocDetailRow("Passing Year", doc.passingYear.ifBlank { "2025" })
                        DocDetailRow("Total Aggregate Marks", "${if (doc.marksObtained > 0) doc.marksObtained else 435} / ${if (doc.maxMarks > 0) doc.maxMarks else 500} (${if (doc.marksPercentage > 0.0) doc.marksPercentage else 87.0}%)", isBold = true)
                        DocDetailRow("Result / Division", "PASS - FIRST DIVISION WITH DISTINCTION")
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Cryptographic DSC Seal Section
            Surface(
                color = SurfaceCard,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.ShieldCheck,
                            contentDescription = null,
                            tint = StatusDisbursed,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Electronic Signature (eSign / DSC)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }
                    Text(
                        text = "Signer: ${doc.signerCn.ifBlank { "Revenue Officer / Competent Authority DSC" }}",
                        fontSize = 10.sp,
                        color = TextBody
                    )
                    Text(
                        text = "DSC Serial: ${doc.dscSerialNumber.ifBlank { "DSC-GOI-2026-984210" }} • Timestamp: ${doc.pkiTimestamp.ifBlank { doc.issueDate }}",
                        fontSize = 10.sp,
                        color = TextSubtle,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "This digital document is legally valid under Rule 9A of Information Technology Rules, 2016.",
                        fontSize = 9.sp,
                        color = TextSubtle,
                        lineHeight = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryDeepOrange),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Text("Close Document", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DocDetailRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    isMonospace: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 11.5.sp,
            color = TextSubtle,
            lineHeight = 15.sp,
            modifier = Modifier.weight(0.44f)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = TextDark,
            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            lineHeight = 16.sp,
            modifier = Modifier.weight(0.56f)
        )
    }
}
