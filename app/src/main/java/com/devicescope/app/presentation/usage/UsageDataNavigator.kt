
package com.devicescope.app.presentation.usage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun UsageDateNavigator(
    selectedDate: Long,
    onDateChange: (Long) -> Unit
) {
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val selected = Calendar.getInstance().apply {
        timeInMillis = selectedDate
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val canGoForward = selected.before(today)

    val label = when {
        selected.timeInMillis == today.timeInMillis -> "Today"
        selected.timeInMillis == (today.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }.timeInMillis -> "Yesterday"

        else -> SimpleDateFormat(
            "EEE, d MMM yyyy",
            Locale.getDefault()
        ).format(Date(selected.timeInMillis))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                val previous = (selected.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, -1)
                }
                onDateChange(previous.timeInMillis)
            }
        ) {
            Text(
                text = "‹",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium
        )

        IconButton(
            onClick = {
                val next = (selected.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
                onDateChange(next.timeInMillis)
            },
            enabled = canGoForward
        ) {
            Text(
                text = "›",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}
