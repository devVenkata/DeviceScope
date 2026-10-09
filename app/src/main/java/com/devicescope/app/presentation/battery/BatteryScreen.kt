package com.devicescope.app.presentation.battery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun BatteryScreen(
    viewModel: BatteryViewModel = viewModel()
) {
    val batteryInfo by viewModel.batteryInfo.collectAsStateWithLifecycle()

    val batteryTimeEstimate by viewModel.batteryTimeEstimate.collectAsStateWithLifecycle()

    val lastChargingSession by
    viewModel.lastChargingSession.collectAsStateWithLifecycle()

    val capacityEstimate by
    viewModel.capacityEstimate.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "Battery",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        if (batteryInfo == null) {
            Text(
                text = "Loading battery information..."
            )
            return@Column
        }

        val info = batteryInfo!!

        BatterySection(title = "Battery Status") {

            val percentage = info.percentage ?: 0

            val progressColor =
                if (percentage < 20) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = batteryTimeEstimate.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            LinearProgressIndicator(
                progress = {
                    (percentage / 100f).coerceIn(0f, 1f)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = "Low",
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "Full",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            BatteryRow(
                label = "Status",
                value = info.status
            )

            BatteryRow(
                label = "Power Source",
                value = info.powerSource
            )
        }

        BatterySection(title = "Battery Health") {

            BatteryRow(
                label = "Health",
                value = info.health
            )

            BatteryRow(
                label = "Health Percentage",
                value = info.healthPercentage?.let {
                    String.format("%.1f%%", it)
                }
            )

            BatteryRow(
                label = "Technology",
                value = info.technology
            )
        }

        BatterySection(title = "Temperature & Voltage") {

            BatteryRow(
                label = "Temperature",
                value = info.temperatureCelsius?.let {
                    String.format("%.1f °C", it)
                }
            )

            BatteryRow(
                label = "Voltage",
                value = info.voltageVolts?.let {
                    String.format("%.3f V", it)
                }
            )
        }

        BatterySection(title = "Current & Power") {

            BatteryRow(
                label = "Current",
                value = info.currentMilliAmps?.let {
                    "$it mA"
                }
            )

            BatteryRow(
                label = "Average Current",
                value = info.averageCurrentMilliAmps?.let {
                    "$it mA"
                }
            )

            BatteryRow(
                label = "Power",
                value = info.powerWatts?.let {
                    String.format("%.2f W", it)
                }
            )

            BatteryRow(
                label = "Charging Type",
                value = info.chargingType
            )

            BatteryRow(
                label = "Charging Power",
                value = info.chargingPowerWatts?.let {
                    String.format("%.2f W", it)
                }
            )
        }

        BatterySection(title = "Charge Information") {

            BatteryRow(
                label = "Charge Counter",
                value = info.chargeCounterMah?.let {
                    String.format("%.0f mAh", it)
                }
            )

            BatteryRow(
                label = "Remaining Energy",
                value = info.remainingEnergyWh?.let {
                    String.format("%.3f Wh", it)
                }
            )

            BatteryRow(
                label = "Cycle Count",
                value = info.cycleCount?.toString()
            )
        }

        BatterySection(title = "Battery Capacity") {

            BatteryRow(
                label = "Charge Counter",
                value = info.chargeCounterMah?.let {
                    String.format(
                        "%.0f mAh",
                        it
                    )
                }
            )

            BatteryRow(
                label = "Design Capacity",
                value = info.designCapacityMah?.let {
                    String.format(
                        "%.0f mAh",
                        it
                    )
                } ?: "Not available"
            )

            BatteryRow(
                label = "Estimated Capacity",
                value = when {

                    capacityEstimate.estimatedCapacityMah != null -> {
                        String.format(
                            "%.0f mAh",
                            capacityEstimate.estimatedCapacityMah
                        )
                    }

                    lastChargingSession != null -> {
                        String.format(
                            "%.0f mAh",
                            lastChargingSession!!.estimatedCapacityMah
                        )
                    }

                    else -> {
                        "Collecting data..."
                    }
                }
            )

            BatteryRow(
                label = "Charging Sessions",
                value = capacityEstimate.sessionCount.toString()
            )

            BatteryRow(
                label = "Measurement Quality",
                value = capacityEstimate.measurementQuality
            )

            BatteryRow(
                label = "Capacity",
                value = info.capacityPercentage?.let {
                    String.format(
                        "%.1f%%",
                        it
                    )
                } ?: "Not available"
            )

            BatteryRow(
                label = "Capacity Loss",
                value = info.capacityLossPercentage?.let {
                    String.format(
                        "%.1f%%",
                        it
                    )
                } ?: "Not available"
            )

            BatteryRow(
                label = "Health Percentage",
                value = info.healthPercentage?.let {
                    String.format(
                        "%.1f%%",
                        it
                    )
                } ?: "Not available"
            )
        }

    }
}

@Composable
private fun BatterySection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            content()
        }
    }
}

@Composable
private fun BatteryRow(
    label: String,
    value: String?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = value ?: "Not available",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
