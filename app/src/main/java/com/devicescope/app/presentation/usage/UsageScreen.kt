package com.devicescope.app.presentation.usage

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import com.devicescope.app.data.datasource.AppUsageItem
import com.devicescope.app.data.datasource.DailyScreenUsage
import com.devicescope.app.data.datasource.UsageDataSource
import com.devicescope.app.data.datasource.AppDailyUsage
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.core.graphics.drawable.toBitmap

@Composable
fun UsageScreen() {
    val context = LocalContext.current
    val source = remember(context) {
        UsageDataSource(context)
    }

    var selectedDate by remember {
        mutableLongStateOf(
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        )
    }

    var weeklyUsage by remember {
        mutableStateOf<List<DailyScreenUsage>>(emptyList())
    }

    var unlockCount by remember {
        mutableIntStateOf(0)
    }

    var selectedPeriod by remember {
        mutableStateOf("Week")
    }

    var hasAccess by remember {
        mutableStateOf(source.hasUsageAccess())
    }

    var apps by remember {
        mutableStateOf<List<AppUsageItem>>(emptyList())
    }

    var selectedApp by remember {
        mutableStateOf<AppUsageItem?>(null)
    }

    var loading by remember {
        mutableStateOf(false)
    }


    var appInstallTime by remember {
        mutableStateOf<Long?>(null)
    }

    var appHistory by remember {
        mutableStateOf<List<AppDailyUsage>>(emptyList())
    }

    var historyLoading by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()


    var refreshTrigger by remember {
        mutableIntStateOf(0)
    }

    fun refreshAccess() {
        hasAccess = source.hasUsageAccess()
    }

    LaunchedEffect(hasAccess, selectedDate, refreshTrigger) {
        if (!hasAccess) {
            apps = emptyList()
            weeklyUsage = emptyList()
            unlockCount = 0
            loading = false
            return@LaunchedEffect
        }

        // Refresh usage data every 60 seconds while the Usage screen is active.
        while (true) {
            loading = true
            try {
                val dayApps = withContext(Dispatchers.IO) {
                    source.getUsageForDay(selectedDate)
                }

                val weekData = withContext(Dispatchers.IO) {
                    source.getScreenUsageForWeek(selectedDate)
                }

                val dailyUnlocks = withContext(Dispatchers.IO) {
                    source.getUnlockCountForDay(selectedDate)
                }

                apps = dayApps
                weeklyUsage = weekData
                unlockCount = dailyUnlocks
            } catch (_: Exception) {
                // Keep the last successfully loaded data if a refresh fails.
            } finally {
                loading = false
            }

            delay(60_000L)
        }
    }

    BackHandler(enabled = selectedApp != null) {
        selectedApp = null
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Fixed header
        if (selectedApp == null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Digital Wellbeing",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Your device activity at a glance",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            if (selectedApp != null) {

                AppUsageDetails(
                    app = selectedApp!!,
                    installTime = appInstallTime,
                    history = appHistory,
                    loading = historyLoading,
                    onBack = {
                        selectedApp = null
                    }
                )

                return@Column
            }

            if (!hasAccess) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = "Usage Access required",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "Allow DeviceScope to access app usage " +
                                    "statistics to see screen time and activity details."
                        )

                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = {
                                source.openUsageAccessSettings()
                            }
                        ) {
                            Text("Grant Usage Access")
                        }

                        Spacer(Modifier.height(4.dp))

                        Button(
                            onClick = {
                                refreshAccess()
                                refreshTrigger++
                            }
                        ) {
                            Text("Check Permission")
                        }
                    }
                }

                return@Column
            }

            // Keep the dashboard visible while data refreshes.
            // The Apps section will show its own loading state.

            val hasApps = apps.isNotEmpty()

            // Dashboard calculations
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val weekStart = Calendar.getInstance().apply {
                timeInMillis = selectedDate
                firstDayOfWeek = Calendar.MONDAY
                set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val weekEnd = Calendar.getInstance().apply {
                timeInMillis = weekStart
                add(Calendar.DAY_OF_YEAR, 6)
            }.timeInMillis

            val currentWeekStart = Calendar.getInstance().apply {
                timeInMillis = todayStart
                firstDayOfWeek = Calendar.MONDAY
                set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            }.timeInMillis

            val canGoForward = weekStart < currentWeekStart


            val elapsedDays = weeklyUsage.filter {
                it.dateStartMillis >= weekStart &&
                        it.dateStartMillis <= todayStart
            }

            val weeklyTotal = elapsedDays.sumOf {
                it.screenOnTimeMillis
            }

            val daysElapsed = elapsedDays.size.coerceAtLeast(1)
            val dailyAverage = weeklyTotal / daysElapsed

            val selectedScreenOnTime = weeklyUsage
                .firstOrNull {
                    it.dateStartMillis == selectedDate
                }
                ?.screenOnTimeMillis ?: 0L

            val totalTime = apps.sumOf {
                it.totalTimeMillis
            }

            val totalLaunches = apps.sumOf {
                it.launchCount
            }

            // Dashboard header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Screen time",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Week / Day selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(14.dp)
                            )
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Week", "Day").forEach { period ->
                            val isSelected = selectedPeriod == period

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant
                                        },
                                        shape = RoundedCornerShape(11.dp)
                                    )
                                    .clickable {
                                        selectedPeriod = period
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = period,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Main screen-time figure
                    Text(
                        text = if (selectedPeriod == "Week") {
                            formatDuration(weeklyTotal)
                        } else {
                            formatDuration(selectedScreenOnTime)
                        },
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = if (selectedPeriod == "Week") {
                            "Total screen time this week"
                        } else {
                            SimpleDateFormat(
                                "EEEE, d MMMM",
                                Locale.getDefault()
                            ).format(Date(selectedDate))
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Summary tiles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = androidx.compose.material3.CardDefaults.cardColors(
                                containerColor =
                                    MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (selectedPeriod == "Week") {
                                        "Daily average"
                                    } else {
                                        "App usage"
                                    },
                                    style = MaterialTheme.typography.labelMedium
                                )

                                Text(
                                    text = formatDuration(
                                        if (selectedPeriod == "Week") {
                                            dailyAverage
                                        } else {
                                            totalTime
                                        }
                                    ),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = androidx.compose.material3.CardDefaults.cardColors(
                                containerColor =
                                    MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (selectedPeriod == "Week") {
                                        "Days tracked"
                                    } else {
                                        "Unlocks"
                                    },
                                    style = MaterialTheme.typography.labelMedium
                                )

                                Text(
                                    text = if (selectedPeriod == "Week") {
                                        weeklyUsage
                                            .filter { it.dateStartMillis <= todayStart }
                                            .size
                                            .toString() + " days"
                                    } else {
                                        unlockCount.toString()
                                    },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    androidx.compose.material3.HorizontalDivider()

                    // Week date range and navigation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = SimpleDateFormat(
                                "d MMM",
                                Locale.getDefault()
                            ).format(Date(weekStart)),
                            style = MaterialTheme.typography.titleSmall
                        )

                        Text(
                            text = "—",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = SimpleDateFormat(
                                "d MMM",
                                Locale.getDefault()
                            ).format(Date(weekEnd)),
                            style = MaterialTheme.typography.titleSmall
                        )

                        Spacer(Modifier.weight(1f))

                        Text(
                            text = "‹",
                            modifier = Modifier
                                .clickable {
                                    selectedDate = Calendar.getInstance().apply {
                                        timeInMillis = selectedDate
                                        add(Calendar.DAY_OF_YEAR, -7)
                                    }.timeInMillis
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "›",
                            modifier = Modifier
                                .clickable(enabled = canGoForward) {
                                    selectedDate = Calendar.getInstance().apply {
                                        timeInMillis = selectedDate
                                        add(Calendar.DAY_OF_YEAR, 7)
                                    }.timeInMillis
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.headlineSmall,
                            color = if (canGoForward) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.3f
                                )
                            }
                        )
                    }

                    Text(
                        text = "Daily screen time",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Monday–Sunday chart
                    val maxScreenTime = weeklyUsage
                        .maxOfOrNull { it.screenOnTimeMillis }
                        ?.coerceAtLeast(1L) ?: 1L

                    if (weeklyUsage.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(154.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            weeklyUsage.forEach { day ->
                                val isSelected =
                                    day.dateStartMillis == selectedDate

                                val dayLabel = SimpleDateFormat(
                                    "EEE",
                                    Locale.getDefault()
                                ).format(Date(day.dateStartMillis))

                                val fraction = (
                                        day.screenOnTimeMillis.toFloat() /
                                                maxScreenTime.toFloat()
                                        ).coerceIn(0f, 1f)

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            selectedDate = day.dateStartMillis
                                        }
                                        .padding(horizontal = 2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    Text(
                                        text = formatDuration(
                                            day.screenOnTimeMillis
                                        ),
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1
                                    )

                                    Spacer(Modifier.height(8.dp))

                                    Column(
                                        modifier = Modifier
                                            .height(88.dp)
                                            .fillMaxWidth(),
                                        verticalArrangement = Arrangement.Bottom,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Spacer(
                                            modifier = Modifier
                                                .fillMaxWidth(
                                                    if (isSelected) 0.68f else 0.48f
                                                )
                                                .height(
                                                    (88 * fraction)
                                                        .coerceAtLeast(3f).dp
                                                )
                                                .background(
                                                    color = if (isSelected) {
                                                        MaterialTheme.colorScheme.tertiary
                                                    } else {
                                                        MaterialTheme.colorScheme.primary
                                                    },
                                                    shape = RoundedCornerShape(
                                                        topStart = 6.dp,
                                                        topEnd = 6.dp
                                                    )
                                                )
                                        )
                                    }

                                    Spacer(Modifier.height(8.dp))

                                    Text(
                                        text = dayLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) {
                                            FontWeight.Bold
                                        } else {
                                            FontWeight.Normal
                                        },
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        Text("Screen-time history is unavailable.")
                    }

                    Text(
                        text = "Select a day in the chart to view its app activity.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Apps heading. Usage data refreshes automatically.
            Text(
                text = "Apps",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            if (!hasApps) {
                Text(
                    text = "No app usage is available for this date. " +
                            "Try another day or use some apps and refresh.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                // Individual apps appear below the weekly history.
                apps.forEach { app ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()

                            .clickable {
                                selectedApp = app
                                appInstallTime = null
                                appHistory = emptyList()
                                historyLoading = true

                                scope.launch {
                                    try {
                                        val result = withContext(Dispatchers.IO) {
                                            val installTime = source.getAppInstallTime(app.packageName)

                                            val history = if (installTime != null) {
                                                source.getDailyUsageHistory(
                                                    app.packageName,
                                                    installTime
                                                )
                                            } else {
                                                emptyList()
                                            }

                                            installTime to history
                                        }

                                        appInstallTime = result.first
                                        appHistory = result.second
                                    } catch (e: Exception) {
                                        appInstallTime = null
                                        appHistory = emptyList()
                                    } finally {
                                        historyLoading = false
                                    }
                                }
                            }

                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            UsageAppIcon(
                                packageName = app.packageName,
                                displayName = resolveAppDisplayName(
                                    context = context,
                                    packageName = app.packageName,
                                    fallback = app.appName
                                )
                            )

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = resolveAppDisplayName(
                                        context = context,
                                        packageName = app.packageName,
                                        fallback = app.appName
                                    ),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(Modifier.height(6.dp))

                                Text(
                                    text = formatDuration(app.totalTimeMillis),
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Spacer(Modifier.height(8.dp))

                                LinearProgressIndicator(
                                    progress = {
                                        (
                                                app.totalTimeMillis.toFloat() /
                                                        totalTime.coerceAtLeast(1L)
                                                ).coerceIn(0f, 1f)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = remember(app.packageName) {
                                        getAppAccentColor(context, app.packageName)
                                    },
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }

                    }

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun AppUsageDetails(
    app: AppUsageItem,
    installTime: Long?,
    history: List<AppDailyUsage>,
    loading: Boolean,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Button(onClick = onBack) {
            Text("Back to Usage")
        }

        Text(
            text = resolveAppDisplayName(
                context = context,
                packageName = app.packageName,
                fallback = app.appName
            ),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = app.packageName,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )

        // Selected day's app usage
        Card(Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Selected day's app usage",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = formatDuration(app.totalTimeMillis),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Additional app details
        Card(Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                UsageDetailRow(
                    label = "Last used",
                    value = if (app.lastTimeUsed > 0) {
                        DateFormat.getDateTimeInstance()
                            .format(Date(app.lastTimeUsed))
                    } else {
                        "Not available"
                    }
                )

                UsageDetailRow(
                    label = "Activity resume events",
                    value = app.launchCount.toString()
                )

                UsageDetailRow(
                    label = "Package",
                    value = app.packageName
                )
            }
        }

        // Installation date
        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "Installation date",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = installTime?.let {
                SimpleDateFormat(
                    "d MMM yyyy",
                    Locale.getDefault()
                ).format(Date(it))
            } ?: "Unavailable",
            style = MaterialTheme.typography.bodyLarge
        )

        // Daily usage history
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Daily usage history",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Recorded usage from today back to the installation date.",
            style = MaterialTheme.typography.bodySmall
        )

        when {
            loading -> {
                Text("Loading usage history...")
            }

            history.isEmpty() -> {
                Text("No historical usage data is available.")
            }

            else -> {
                history.forEach { day ->
                    val dateLabel = SimpleDateFormat(
                        "EEE, d MMM yyyy",
                        Locale.getDefault()
                    ).format(Date(day.dateStartMillis))

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dateLabel,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = if (day.dataAvailable) {
                                    formatDuration(day.usageMillis ?: 0L)
                                } else {
                                    "Unavailable"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Usage data is provided by Android. Some older dates " +
                    "may be unavailable, and recorded values may be aggregated.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}


@Composable
private fun UsageDetailRow(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatDuration(millis: Long): String {
    val totalMinutes = millis / 60_000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        else -> "${minutes}m"
    }
}


@Composable
private fun UsageAppIcon(
    packageName: String,
    displayName: String
) {
    val context = LocalContext.current
    val iconBitmap = remember(packageName) {
        try {
            context.packageManager
                .getApplicationIcon(packageName)
                .toBitmap(width = 96, height = 96)
                .asImageBitmap()
        } catch (_: Exception) {
            null
        }
    }

    if (iconBitmap != null) {
        Image(
            bitmap = iconBitmap,
            contentDescription = displayName,
            modifier = Modifier.size(56.dp),
            contentScale = ContentScale.Fit
        )
    } else {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayName.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun resolveAppDisplayName(
    context: android.content.Context,
    packageName: String,
    fallback: String
): String {
    return try {
        val applicationInfo = context.packageManager.getApplicationInfo(packageName, 0)
        context.packageManager.getApplicationLabel(applicationInfo).toString()
    } catch (_: Exception) {
        fallback.takeIf { it.isNotBlank() && it != packageName }
            ?: packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() }
    }
}


/**
 * Finds a representative accent color from the installed app's icon.
 * It samples visible pixels and avoids transparent and near-white pixels
 * so the progress bar reflects the icon rather than its empty background.
 */
private fun getAppAccentColor(
    context: android.content.Context,
    packageName: String
): Color {
    return try {
        val drawable = context.packageManager.getApplicationIcon(packageName)
        val bitmap = drawable.toBitmap(width = 48, height = 48)
        val buckets = HashMap<Int, Int>()

        for (y in 0 until bitmap.height step 2) {
            for (x in 0 until bitmap.width step 2) {
                val pixel = bitmap.getPixel(x, y)
                val alpha = android.graphics.Color.alpha(pixel)
                val red = android.graphics.Color.red(pixel)
                val green = android.graphics.Color.green(pixel)
                val blue = android.graphics.Color.blue(pixel)

                if (alpha < 160) continue

                val maxChannel = maxOf(red, green, blue)
                val minChannel = minOf(red, green, blue)
                val saturationRange = maxChannel - minChannel

                // Ignore near-white and near-gray pixels so a white icon
                // background does not dominate the bar color.
                if (minChannel > 225) continue
                if (saturationRange < 24 && maxChannel > 150) continue

                val key = ((red / 24) shl 16) or
                        ((green / 24) shl 8) or
                        (blue / 24)
                buckets[key] = (buckets[key] ?: 0) + 1
            }
        }

        val bestBucket = buckets.maxByOrNull { it.value }?.key
            ?: return Color(0xFF536DAA)

        val red = (((bestBucket shr 16) and 0xFF) * 24 + 12).coerceAtMost(255)
        val green = (((bestBucket shr 8) and 0xFF) * 24 + 12).coerceAtMost(255)
        val blue = ((bestBucket and 0xFF) * 24 + 12).coerceAtMost(255)

        Color(
            red = red / 255f,
            green = green / 255f,
            blue = blue / 255f
        )
    } catch (_: Exception) {
        Color(0xFF536DAA)
    }
}

