package com.step.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.step.admin.firebase.AdminFirebaseManager
import com.step.admin.ui.components.FontAwesomeIcons
import com.step.admin.ui.screens.*
import com.step.admin.ui.theme.*

enum class AdminTab(val title: String) {
    DASHBOARD("Dashboard"),
    SCRUTINY("Scrutiny"),
    SCHEMES("Schemes"),
    OUTREACH("Outreach"),
    DOSSIERS("Dossiers")
}

class AdminMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Firebase listeners for applications and schemes
        AdminFirebaseManager.init()

        setContent {
            STePAdminTheme {
                AdminAppRoot()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAppRoot() {
    var selectedTab by remember { mutableStateOf(AdminTab.DASHBOARD) }
    var openCreateSchemeDialog by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            Surface(
                color = BackgroundWhite,
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                NavigationBar(
                    containerColor = BackgroundWhite,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .height(64.dp)
                ) {
                    NavigationBarItem(
                        selected = selectedTab == AdminTab.DASHBOARD,
                        onClick = { selectedTab = AdminTab.DASHBOARD },
                        icon = {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.House,
                                contentDescription = "Dashboard",
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        label = { Text("Home", fontSize = 11.sp, fontWeight = if (selectedTab == AdminTab.DASHBOARD) FontWeight.Bold else FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryDeepOrange,
                            selectedTextColor = PrimaryDeepOrange,
                            unselectedIconColor = TextSubtle,
                            unselectedTextColor = TextSubtle,
                            indicatorColor = PrimarySurfaceLight
                        )
                    )

                    NavigationBarItem(
                        selected = selectedTab == AdminTab.SCRUTINY,
                        onClick = { selectedTab = AdminTab.SCRUTINY },
                        icon = {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.Timeline,
                                contentDescription = "Scrutiny",
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        label = { Text("Scrutiny", fontSize = 11.sp, fontWeight = if (selectedTab == AdminTab.SCRUTINY) FontWeight.Bold else FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryDeepOrange,
                            selectedTextColor = PrimaryDeepOrange,
                            unselectedIconColor = TextSubtle,
                            unselectedTextColor = TextSubtle,
                            indicatorColor = PrimarySurfaceLight
                        )
                    )

                    NavigationBarItem(
                        selected = selectedTab == AdminTab.SCHEMES,
                        onClick = { selectedTab = AdminTab.SCHEMES },
                        icon = {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.BuildingColumns,
                                contentDescription = "Schemes",
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        label = { Text("Schemes", fontSize = 11.sp, fontWeight = if (selectedTab == AdminTab.SCHEMES) FontWeight.Bold else FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryDeepOrange,
                            selectedTextColor = PrimaryDeepOrange,
                            unselectedIconColor = TextSubtle,
                            unselectedTextColor = TextSubtle,
                            indicatorColor = PrimarySurfaceLight
                        )
                    )

                    NavigationBarItem(
                        selected = selectedTab == AdminTab.OUTREACH,
                        onClick = { selectedTab = AdminTab.OUTREACH },
                        icon = {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.ShieldCheck,
                                contentDescription = "Outreach",
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        label = { Text("Outreach", fontSize = 11.sp, fontWeight = if (selectedTab == AdminTab.OUTREACH) FontWeight.Bold else FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryDeepOrange,
                            selectedTextColor = PrimaryDeepOrange,
                            unselectedIconColor = TextSubtle,
                            unselectedTextColor = TextSubtle,
                            indicatorColor = PrimarySurfaceLight
                        )
                    )

                    NavigationBarItem(
                        selected = selectedTab == AdminTab.DOSSIERS,
                        onClick = { selectedTab = AdminTab.DOSSIERS },
                        icon = {
                            Icon(
                                imageVector = FontAwesomeIcons.Solid.User,
                                contentDescription = "Dossiers",
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        label = { Text("Dossiers", fontSize = 11.sp, fontWeight = if (selectedTab == AdminTab.DOSSIERS) FontWeight.Bold else FontWeight.Medium) },
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
        },
        containerColor = NavyBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            when (selectedTab) {
                AdminTab.DASHBOARD -> {
                    AdminDashboardScreen(
                        onNavigateToScrutiny = { selectedTab = AdminTab.SCRUTINY },
                        onNavigateToSchemes = { selectedTab = AdminTab.SCHEMES },
                        onNavigateToHeatmap = { selectedTab = AdminTab.OUTREACH },
                        onNavigateToDossiers = { selectedTab = AdminTab.DOSSIERS },
                        onCreateSchemeClicked = {
                            selectedTab = AdminTab.SCHEMES
                            openCreateSchemeDialog = true
                        }
                    )
                }
                AdminTab.SCRUTINY -> {
                    ScrutinyQueueScreen(
                        onBack = { selectedTab = AdminTab.DASHBOARD }
                    )
                }
                AdminTab.SCHEMES -> {
                    SchemesMasterScreen(
                        onBack = { selectedTab = AdminTab.DASHBOARD },
                        openCreateModalInitially = openCreateSchemeDialog,
                        onDismissCreateModal = { openCreateSchemeDialog = false }
                    )
                }
                AdminTab.OUTREACH -> {
                    SaturationHeatmapScreen(
                        onBack = { selectedTab = AdminTab.DASHBOARD }
                    )
                }
                AdminTab.DOSSIERS -> {
                    StudentsDirectoryScreen(
                        onBack = { selectedTab = AdminTab.DASHBOARD }
                    )
                }
            }
        }
    }
}
