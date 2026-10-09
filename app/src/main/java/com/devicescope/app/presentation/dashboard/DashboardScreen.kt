package com.devicescope.app.presentation.dashboard

import com.devicescope.app.presentation.dashboard.model.defaultDashboardCards
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun DashboardScreen(
    onDeviceClick: () -> Unit,
    onBatteryClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(
                MaterialTheme.colorScheme.background
            )
            .padding(20.dp),

        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // --------------------------------
        // HEADER
        // --------------------------------

        Column {

            Text(
                text = "DeviceScope",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = "Know your device. Test it. Understand it.",
                style = MaterialTheme.typography.bodyMedium
            )
        }


        // --------------------------------
        // DEVICE HEALTH
        // --------------------------------

        Card(
            modifier = Modifier.fillMaxWidth(),

            shape = RoundedCornerShape(24.dp),

            colors = CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {

            Column(
                modifier = Modifier.padding(20.dp),

                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Text(
                    text = "DEVICE HEALTH",
                    style = MaterialTheme.typography.labelLarge
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text(
                    text = "87",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "GOOD CONDITION",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }


        // --------------------------------
        // QUICK ACCESS TITLE
        // --------------------------------

        Text(
            text = "Quick Access",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            defaultDashboardCards
                .filter { it.enabled }
                .sortedBy { it.position }
                .forEach { cardConfig ->

                    DashboardCardRenderer(
                        config = cardConfig,
                        modifier = Modifier.fillMaxWidth(),
                        onDeviceClick = onDeviceClick,
                        onBatteryClick = onBatteryClick
                    )
                }
        }

        Spacer(
            modifier = Modifier.height(4.dp)
        )


        // --------------------------------
        // FULL DIAGNOSTIC
        // --------------------------------

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),

            shape = RoundedCornerShape(18.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxSize(),

                horizontalArrangement =
                    Arrangement.Center,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(
                    text = "Run Full Diagnostic",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


// ==================================================
// REUSABLE DASHBOARD CARD
// ==================================================

@Composable
fun DashboardCard(
    title: String,
    subtitle: String,

    modifier: Modifier = Modifier,

    onClick: () -> Unit
) {

    Card(

        modifier = modifier
            .clickable {
                onClick()
            },

        shape = RoundedCornerShape(20.dp)
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}