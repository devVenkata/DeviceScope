package com.devicescope.app.presentation.dashboard

import android.icu.text.UnicodeSetIterator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.devicescope.app.presentation.dashboard.model.DashboardCardConfig
import com.devicescope.app.presentation.dashboard.model.DashboardCardType

@Composable
fun DashboardCardRenderer(
    config: DashboardCardConfig,
    modifier: Modifier = Modifier,
    onDeviceClick: () -> Unit,
    onBatteryClick: () -> Unit
) {

    val title: String
    val subtitle: String

    when (config.type) {

        DashboardCardType.DEVICE -> {
            title = "Device"
            subtitle = "System information"
        }

        DashboardCardType.BATTERY -> {
            title = "Battery"
            subtitle = "Power status"
        }

        DashboardCardType.CPU -> {
            title = "CPU"
            subtitle = "Processor information"
        }

        DashboardCardType.MEMORY -> {
            title = "Memory"
            subtitle = "RAM information"
        }

        DashboardCardType.STORAGE -> {
            title = "Storage"
            subtitle = "Internal storage"
        }

        DashboardCardType.DISPLAY -> {
            title = "Display"
            subtitle = "Screen information"
        }

        DashboardCardType.NETWORK -> {
            title = "Network"
            subtitle = "Connectivity"
        }

        DashboardCardType.THERMAL -> {
            title = "Thermal"
            subtitle = "Temperature information"
        }

        DashboardCardType.SECURITY -> {
            title = "Security"
            subtitle = "Security information"
        }

        DashboardCardType.UPTIME -> {
            title = "Uptime"
            subtitle = "System uptime"
        }
    }

    DashboardCard(
        title = title,
        subtitle = subtitle,
        modifier = modifier,
        onClick = {
            when (config.type) {

                DashboardCardType.DEVICE -> {
                    onDeviceClick()
                }

                DashboardCardType.BATTERY -> {
                    onBatteryClick()
                }

                else -> {
                    // Other cards will become functional later.
                }
            }
        }
    )
}