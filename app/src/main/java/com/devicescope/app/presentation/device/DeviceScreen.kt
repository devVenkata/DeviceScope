package com.devicescope.app.presentation.device

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DeviceScreen(
    viewModel: DeviceViewModel = viewModel()
) {
    val deviceInfo by viewModel.deviceInfo.collectAsState()

    if (deviceInfo == null) {
        Text(
            text = "Loading device information...",
            modifier = Modifier.padding(24.dp)
        )
        return
    }

    val info = deviceInfo!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Device Information",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Complete information available from Android",
            style = MaterialTheme.typography.bodyMedium
        )

        DeviceInfoCard(
            title = "Device",
            items = listOf(
                "Manufacturer" to info.manufacturer,
                "Brand" to info.brand,
                "Model" to info.model,
                "Device" to info.device,
                "Product" to info.product,
                "Board" to info.board,
                "Hardware" to info.hardware,
                "Bootloader" to info.bootloader
            )
        )

        DeviceInfoCard(
            title = "Android",
            items = listOf(
                "Android Version" to info.androidVersion,
                "API Level" to info.apiLevel.toString(),
                "Security_Patch" to info.securityPatch,
                "Build ID" to info.buildId,
                "Build_Display" to info.buildDisplay
            )
        )

        DeviceInfoCard(
            title = "Architecture",
            items = listOf(
                "64-bit ABIs" to info.supportAbis64.joinToString(", "),
                "32-bit ABIs" to info.supportAbis32.joinToString(", ")
            )
        )
    }
}

@Composable
private fun DeviceInfoCard(
    title: String,
    items: List<Pair<String, String>>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            items.forEach { (label, value) ->

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}