package com.sanket.tools.nexpaddesktop.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sanket.tools.nexpaddesktop.connection.ActiveTransport
import com.sanket.tools.nexpaddesktop.connection.DriverInstallState
import com.sanket.tools.nexpaddesktop.ui.components.DesktopVerticalScrollbar
import com.sanket.tools.nexpaddesktop.ui.components.GradientDivider
import com.sanket.tools.nexpaddesktop.ui.components.home.BackgroundGamingCard
import com.sanket.tools.nexpaddesktop.ui.components.home.ConnectionSection
import com.sanket.tools.nexpaddesktop.ui.components.home.DeveloperOptionsCard
import com.sanket.tools.nexpaddesktop.ui.components.home.PortConfigurationCard
import com.sanket.tools.nexpaddesktop.ui.components.home.DeviceSection
import com.sanket.tools.nexpaddesktop.ui.components.home.DriverWarningBanner
import com.sanket.tools.nexpaddesktop.ui.components.home.DriverSuccessBanner
import com.sanket.tools.nexpaddesktop.ui.components.home.DriverSuccessDialog
import com.sanket.tools.nexpaddesktop.ui.components.home.HomeHeader
import com.sanket.tools.nexpaddesktop.viewmodel.DesktopViewModel
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sanket.tools.nexpaddesktop.ui.theme.NeonPalette

/**
 * NEXPAD Desktop Home Screen.
 * Industry-level modular architecture composing high-performance, reusable composables.
 */
@Composable
fun HomeScreen(
    viewModel: DesktopViewModel
) {
    HomeScreen(
        isDriverConnected = viewModel.isDriverConnected,
        connectedDeviceName = viewModel.connectedDeviceName,
        activeTransport = viewModel.activeTransport,
        isAoaDriverNeeded = viewModel.isAoaDriverNeeded,
        driverInstallState = viewModel.driverInstallState,
        driverInstallProgress = viewModel.driverInstallProgress,
        driverInstallStatusText = viewModel.driverInstallStatusText,
        showDriverSuccessToast = viewModel.showDriverSuccessToast,
        driverSuccessToastMessage = viewModel.driverSuccessToastMessage,
        onDismissDriverSuccessToast = { viewModel.dismissDriverSuccessToast() },
        showDriverSuccessDialog = viewModel.showDriverSuccessDialog,
        onDismissDriverSuccessDialog = { viewModel.dismissDriverSuccessDialog() },
        onInstallAoaDriver = viewModel.onInstallAoaDriver,
        isDeveloperOptionsEnabled = viewModel.isDeveloperOptionsEnabled,
        onDeveloperOptionsChange = { viewModel.updateDeveloperOptionsEnabled(it) },
        streamingPort = viewModel.streamingPort,
        onStreamingPortChange = { viewModel.updateStreamingPort(it) },
        onMinimizeToTray = { viewModel.minimizeToTray() }
    )
}

@Composable
fun HomeScreen(
    isDriverConnected: Boolean,
    connectedDeviceName: String? = null,
    activeTransport: ActiveTransport = ActiveTransport.NONE,
    isAoaDriverNeeded: Boolean = false,
    driverInstallState: DriverInstallState = DriverInstallState.IDLE,
    driverInstallProgress: Float = 0f,
    driverInstallStatusText: String = "",
    showDriverSuccessToast: Boolean = false,
    driverSuccessToastMessage: String = "",
    onDismissDriverSuccessToast: () -> Unit = {},
    showDriverSuccessDialog: Boolean = false,
    onDismissDriverSuccessDialog: () -> Unit = {},
    onInstallAoaDriver: () -> Unit = {},
    isDeveloperOptionsEnabled: Boolean = false,
    onDeveloperOptionsChange: (Boolean) -> Unit = {},
    streamingPort: Int = 9999,
    onStreamingPortChange: (Int) -> Unit = {},
    onMinimizeToTray: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(start = 32.dp, top = 32.dp, bottom = 32.dp, end = 24.dp)
        ) {
            // ── Header (Title & Server Status Pill) ──
            HomeHeader()

            Spacer(modifier = Modifier.height(16.dp))

            // ── Driver Success Toast / Banner ──
            AnimatedVisibility(
                visible = showDriverSuccessToast,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -20 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -20 })
            ) {
                Column {
                    DriverSuccessBanner(
                        message = driverSuccessToastMessage,
                        onDismiss = onDismissDriverSuccessToast
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // ── ViGEmBus Driver Warning Banner ──
            if (!isDriverConnected) {
                DriverWarningBanner()
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Background Gaming Mode Hero Card ──
            BackgroundGamingCard(
                onMinimizeToTray = onMinimizeToTray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Divider ──
            GradientDivider()

            Spacer(modifier = Modifier.height(24.dp))

            // ── Connected Device Section / Radar ──
            DeviceSection(
                connectedDeviceName = connectedDeviceName,
                activeTransport = activeTransport
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Divider ──
            GradientDivider()

            Spacer(modifier = Modifier.height(24.dp))

            // ── Hardware Connection Section ──
            ConnectionSection(
                activeTransport = activeTransport,
                isDeviceConnected = connectedDeviceName != null,
                isAoaDriverNeeded = isAoaDriverNeeded,
                driverInstallState = driverInstallState,
                driverInstallProgress = driverInstallProgress,
                driverInstallStatusText = driverInstallStatusText,
                onInstallAoaDriver = onInstallAoaDriver
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ── Divider ──
            GradientDivider()

            Spacer(modifier = Modifier.height(24.dp))

            // ── System Preferences Section ──
            Text(
                text = "System Preferences",
                color = NeonPalette.CardIdleText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(10.dp))

            DeveloperOptionsCard(
                isDeveloperOptionsEnabled = isDeveloperOptionsEnabled,
                onDeveloperOptionsChange = onDeveloperOptionsChange,
                showSectionHeader = false
            )

            Spacer(modifier = Modifier.height(14.dp))

            PortConfigurationCard(
                currentPort = streamingPort,
                onPortChange = onStreamingPortChange
            )
        }

        // ── Smooth Desktop Vertical Scrollbar ──
        DesktopVerticalScrollbar(
            adapter = rememberScrollbarAdapter(scrollState),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .padding(vertical = 6.dp, horizontal = 4.dp),
            thickness = 10.dp,
            trackWidth = 14.dp,
            showTrackGutter = true
        )

        // ── High-Visibility Driver Success Popup Alert Dialog ──
        DriverSuccessDialog(
            isOpen = showDriverSuccessDialog,
            onDismiss = onDismissDriverSuccessDialog
        )
    }
}
