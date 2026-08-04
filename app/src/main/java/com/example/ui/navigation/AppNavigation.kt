package com.example.ui.navigation

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.QueuePlayNext
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.example.ai.ExtractedReportData
import com.example.data.ReportRepository
import com.example.ui.queue.BatchQueueScreen
import com.example.ui.review.ReviewEditScreen
import com.example.ui.scanner.CameraScannerScreen
import com.example.ui.simulator.TargetFormSimulatorScreen

sealed class NavTab(val title: String, val icon: ImageVector) {
    object Scanner : NavTab("AI Scan", Icons.Default.CameraAlt)
    object Review : NavTab("Review", Icons.Default.EditNote)
    object Queue : NavTab("Batch Bot", Icons.Default.QueuePlayNext)
    object Simulator : NavTab("Simulator", Icons.Default.Assignment)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    repository: ReportRepository
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    var latestExtractedData by remember { mutableStateOf<ExtractedReportData?>(null) }
    var latestBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val tabs = listOf(
        NavTab.Scanner,
        NavTab.Review,
        NavTab.Queue,
        NavTab.Simulator
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Masik Prativedan Autofiller",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = selectedTab,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "TabTransition"
        ) { tabIndex ->
            when (tabIndex) {
                0 -> CameraScannerScreen(
                    onScanCompleted = { data, bitmap ->
                        latestExtractedData = data
                        latestBitmap = bitmap
                        selectedTab = 1 // Auto switch to Review tab
                    }
                )
                1 -> ReviewEditScreen(
                    extractedData = latestExtractedData,
                    scannedBitmap = latestBitmap,
                    repository = repository,
                    onQueueSuccess = {
                        selectedTab = 2 // Auto switch to Queue tab
                    }
                )
                2 -> BatchQueueScreen(
                    repository = repository
                )
                3 -> TargetFormSimulatorScreen()
            }
        }
    }
}
