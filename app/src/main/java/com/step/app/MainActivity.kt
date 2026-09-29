package com.step.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.step.app.data.ApplicationRecord
import com.step.app.data.MoTaRepository
import com.step.app.data.Scheme
import com.step.app.data.TimelineStep
import com.step.app.firebase.FirebaseManager
import com.step.app.ui.components.DeficiencyDialog
import com.step.app.ui.components.FontAwesomeIcons
import com.step.app.ui.components.GrievanceDialog
import com.step.app.ui.screens.*
import com.step.app.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Auto-restore user session, profile, and dynamic data immediately on launch
        FirebaseManager.autoRestoreSession(this)

        setContent {
            STePTheme {
                if (!FirebaseManager.isGoogleLoggedIn) {
                    LoginScreen(
                        onLoginSuccess = {
                            // Handled by state reactivity
                        }
                    )
                } else {
                    STePMainApp(
                        onLogout = {
                            FirebaseManager.logout(this@MainActivity)
                        }
                    )
                }
            }
        }
    }
}

enum class MainTab(val title: String) {
    HOME("Home"),
    APPLY("Apply"),
    TRACK("Track"),
    HELP("Help")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun STePMainApp(
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }

    // Sub-Screen Navigation State
    var activeSchemeForDetail by remember { mutableStateOf<Scheme?>(null) }
    var activeSchemeForApply by remember { mutableStateOf<Scheme?>(null) }
    var activeAppForDetail by remember { mutableStateOf<ApplicationRecord?>(null) }
    var showProfile by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }
    var showWizard by remember { mutableStateOf(false) }
    var showDocumentWallet by remember { mutableStateOf(false) }

    // Interactive Dialogs
    var showDeficiencyDialog by remember { mutableStateOf(false) }
    var showGrievanceDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        FirebaseManager.initDynamicFirestore()
        MoTaRepository.loadDatasetsIfEmpty(context)
    }

    // Handle Android system back button when sub-screens are open
    BackHandler(
        enabled = activeSchemeForDetail != null || activeSchemeForApply != null ||
                activeAppForDetail != null || showProfile || showNotifications ||
                showWizard || showDocumentWallet
    ) {
        when {
            activeSchemeForApply != null -> activeSchemeForApply = null
            activeSchemeForDetail != null -> activeSchemeForDetail = null
            activeAppForDetail != null -> activeAppForDetail = null
            showDocumentWallet -> showDocumentWallet = false
            showProfile -> showProfile = false
            showNotifications -> showNotifications = false
            showWizard -> showWizard = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Only show 4-tab bar when on root screens
            if (activeSchemeForDetail == null && activeSchemeForApply == null &&
                activeAppForDetail == null && !showProfile && !showNotifications &&
                !showWizard && !showDocumentWallet
            ) {
                Surface(
                    color = BackgroundWhite,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    NavigationBar(
                        containerColor = BackgroundWhite,
                        contentColor = PrimaryDeepOrange,
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .height(64.dp)
                    ) {
                        // TAB 1: HOME
                        val homeScale by animateFloatAsState(
                            targetValue = if (selectedTab == MainTab.HOME) 1.15f else 1.0f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                            label = "homeScale"
                        )
                        NavigationBarItem(
                            selected = selectedTab == MainTab.HOME,
                            onClick = { selectedTab = MainTab.HOME },
                            icon = {
                                Icon(
                                    imageVector = FontAwesomeIcons.Solid.House,
                                    contentDescription = "Home",
                                    modifier = Modifier
                                        .size(20.dp)
                                        .graphicsLayer(scaleX = homeScale, scaleY = homeScale)
                                )
                            },
                            label = {
                                Text(
                                    text = "Home",
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTab == MainTab.HOME) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryDeepOrange,
                                selectedTextColor = PrimaryDeepOrange,
                                unselectedIconColor = TextSubtle,
                                unselectedTextColor = TextSubtle,
                                indicatorColor = PrimarySurfaceLight
                            )
                        )

                        // TAB 2: APPLY
                        val applyScale by animateFloatAsState(
                            targetValue = if (selectedTab == MainTab.APPLY) 1.15f else 1.0f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                            label = "applyScale"
                        )
                        NavigationBarItem(
                            selected = selectedTab == MainTab.APPLY,
                            onClick = { selectedTab = MainTab.APPLY },
                            icon = {
                                Icon(
                                    imageVector = FontAwesomeIcons.Solid.FilePen,
                                    contentDescription = "Apply",
                                    modifier = Modifier
                                        .size(20.dp)
                                        .graphicsLayer(scaleX = applyScale, scaleY = applyScale)
                                )
                            },
                            label = {
                                Text(
                                    text = "Apply",
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTab == MainTab.APPLY) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryDeepOrange,
                                selectedTextColor = PrimaryDeepOrange,
                                unselectedIconColor = TextSubtle,
                                unselectedTextColor = TextSubtle,
                                indicatorColor = PrimarySurfaceLight
                            )
                        )

                        // TAB 3: TRACK
                        val trackScale by animateFloatAsState(
                            targetValue = if (selectedTab == MainTab.TRACK) 1.15f else 1.0f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                            label = "trackScale"
                        )
                        NavigationBarItem(
                            selected = selectedTab == MainTab.TRACK,
                            onClick = { selectedTab = MainTab.TRACK },
                            icon = {
                                Icon(
                                    imageVector = FontAwesomeIcons.Solid.Timeline,
                                    contentDescription = "Track",
                                    modifier = Modifier
                                        .size(20.dp)
                                        .graphicsLayer(scaleX = trackScale, scaleY = trackScale)
                                )
                            },
                            label = {
                                Text(
                                    text = "Track",
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTab == MainTab.TRACK) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryDeepOrange,
                                selectedTextColor = PrimaryDeepOrange,
                                unselectedIconColor = TextSubtle,
                                unselectedTextColor = TextSubtle,
                                indicatorColor = PrimarySurfaceLight
                            )
                        )

                        // TAB 4: HELP
                        val helpScale by animateFloatAsState(
                            targetValue = if (selectedTab == MainTab.HELP) 1.15f else 1.0f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                            label = "helpScale"
                        )
                        NavigationBarItem(
                            selected = selectedTab == MainTab.HELP,
                            onClick = { selectedTab = MainTab.HELP },
                            icon = {
                                Icon(
                                    imageVector = FontAwesomeIcons.Solid.Headset,
                                    contentDescription = "Help",
                                    modifier = Modifier
                                        .size(20.dp)
                                        .graphicsLayer(scaleX = helpScale, scaleY = helpScale)
                                )
                            },
                            label = {
                                Text(
                                    text = "Help",
                                    fontSize = 11.sp,
                                    fontWeight = if (selectedTab == MainTab.HELP) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryDeepOrange,
                                selectedTextColor = PrimaryDeepOrange,
                                unselectedIconColor = TextSubtle,
                                unselectedTextColor = TextSubtle,
                                indicatorColor = PrimarySurfaceLight
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val currentScreenKey = when {
                activeSchemeForApply != null -> "APPLY_${activeSchemeForApply!!.id}"
                activeSchemeForDetail != null -> "SCHEME_DETAIL_${activeSchemeForDetail!!.id}"
                activeAppForDetail != null -> "APP_DETAIL_${activeAppForDetail!!.applicationId}"
                showDocumentWallet -> "WALLET"
                showProfile -> "PROFILE"
                showNotifications -> "NOTIFICATIONS"
                showWizard -> "WIZARD"
                else -> "TAB_${selectedTab.name}"
            }

            AnimatedContent(
                targetState = currentScreenKey,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                            slideInHorizontally(animationSpec = tween(220, easing = FastOutSlowInEasing)) { fullWidth -> fullWidth / 6 })
                        .togetherWith(
                            fadeOut(animationSpec = tween(160, easing = FastOutLinearInEasing)) +
                                    slideOutHorizontally(animationSpec = tween(160, easing = FastOutLinearInEasing)) { fullWidth -> -fullWidth / 6 }
                        )
                },
                label = "ScreenAnimatedTransition"
            ) { _ ->
            when {
                // 1. Sub-screen: Apply Flow (4 Steps)
                activeSchemeForApply != null -> {
                    val schemeToApply = activeSchemeForApply!!
                    ApplyFlowScreen(
                        scheme = schemeToApply,
                        onBack = { activeSchemeForApply = null },
                        onSubmitSuccess = {
                            val newApp = ApplicationRecord(
                                applicationId = "APP-${schemeToApply.code}-${System.currentTimeMillis().toString().takeLast(5)}",
                                schemeId = schemeToApply.id,
                                schemeTitle = schemeToApply.title,
                                academicYear = "2026-27",
                                sourcePortal = schemeToApply.portal,
                                stage = "SUBMITTED",
                                stageText = "Submitted via STeP Unified Portal",
                                currentStepIndex = 0,
                                sanctionAmount = schemeToApply.maxBenefitAmount,
                                nextActionText = "Awaiting institutional verification",
                                verificationConfidence = 98,
                                steps = listOf(
                                    TimelineStep("Submitted", "Today", true, "Applied via STeP Unified Portal"),
                                    TimelineStep("Verified", "Pending", false, "Institute and Nodal Verification"),
                                    TimelineStep("Sanctioned", "Pending", false, "MoTA Central Sanction Order"),
                                    TimelineStep("Disbursed", "Pending", false, "Direct Benefit Transfer via APB")
                                ),
                                dbtDetails = null,
                                deficiency = null
                            )
                            FirebaseManager.submitApplicationToFirestore(newApp)
                            activeSchemeForApply = null
                            activeSchemeForDetail = null
                            selectedTab = MainTab.TRACK
                            scope.launch {
                                snackbarHostState.showSnackbar("Application submitted and registered successfully!")
                            }
                        }
                    )
                }

                // 2. Sub-screen: Scheme Detail
                activeSchemeForDetail != null -> {
                    SchemeDetailScreen(
                        scheme = activeSchemeForDetail!!,
                        onBack = { activeSchemeForDetail = null },
                        onStartApply = { scheme ->
                            activeSchemeForApply = scheme
                        }
                    )
                }

                // 3. Sub-screen: Application Detail
                activeAppForDetail != null -> {
                    ApplicationDetailScreen(
                        application = activeAppForDetail!!,
                        onBack = { activeAppForDetail = null },
                        onExplainDeficiency = { showDeficiencyDialog = true },
                        onEscalateGrievance = { showGrievanceDialog = true }
                    )
                }

                // 4. Sub-screen: Document Wallet
                showDocumentWallet -> {
                    DocumentWalletScreen(
                        onBack = { showDocumentWallet = false },
                        onAddDocument = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Opening camera scanner to capture new certificate...")
                            }
                        }
                    )
                }

                // 5. Sub-screen: Scholar Profile
                showProfile -> {
                    ProfileScreen(
                        onBack = { showProfile = false },
                        onNavigateToDocumentWallet = { showDocumentWallet = true },
                        onLogout = onLogout
                    )
                }

                // 6. Sub-screen: Notification Center
                showNotifications -> {
                    NotificationCenterScreen(
                        onBack = { showNotifications = false }
                    )
                }

                // 7. Sub-screen: 5-Question Eligibility Wizard
                showWizard -> {
                    EligibilityWizardScreen(
                        onBack = { showWizard = false },
                        onSelectSchemeToApply = { scheme ->
                            showWizard = false
                            activeSchemeForApply = scheme
                        }
                    )
                }

                // 8. Main Root 4-Tab Screen Controller
                else -> {
                    when (selectedTab) {
                        MainTab.HOME -> {
                            HomeScreen(
                                onNavigateToProfile = { showProfile = true },
                                onNavigateToNotifications = { showNotifications = true },
                                onNavigateToWizard = { showWizard = true },
                                onNavigateToApplicationDetail = { app -> activeAppForDetail = app },
                                onResolvePendingAction = { action ->
                                    if (action.id == "act_nos_defect") {
                                        showDeficiencyDialog = true
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Opening e-District portal renewal for income certificate...")
                                        }
                                    }
                                }
                            )
                        }

                        MainTab.APPLY -> {
                            ApplyScreen(
                                onNavigateToWizard = { showWizard = true },
                                onNavigateToSchemeDetail = { scheme -> activeSchemeForDetail = scheme }
                            )
                        }

                        MainTab.TRACK -> {
                            TrackScreen(
                                onNavigateToApplicationDetail = { app -> activeAppForDetail = app }
                            )
                        }

                        MainTab.HELP -> {
                            HelpScreen()
                        }
                    }
                }
            }
        }

            // Interactive Dialogs
            if (showDeficiencyDialog) {
                DeficiencyDialog(
                    onDismiss = { showDeficiencyDialog = false }
                )
            }

            if (showGrievanceDialog) {
                GrievanceDialog(
                    onDismiss = { showGrievanceDialog = false },
                    onSubmit = {
                        showGrievanceDialog = false
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                "RTI SLA Ticket #MOTA-GR-9921 lodged! 30-day statutory countdown timer initiated."
                            )
                        }
                    }
                )
            }
        }
    }
}
