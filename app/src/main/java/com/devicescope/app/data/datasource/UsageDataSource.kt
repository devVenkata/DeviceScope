
package com.devicescope.app.data.datasource

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.provider.Settings
import java.util.Calendar

data class AppUsageItem(
    val packageName: String,
    val appName: String,
    val totalTimeMillis: Long,
    val lastTimeUsed: Long,
    val launchCount: Int
)

data class DailyScreenUsage(
    val dateStartMillis: Long,
    val screenOnTimeMillis: Long
)

data class AppDailyUsage(
    val dateStartMillis: Long,
    val usageMillis: Long?,
    val dataAvailable: Boolean
)

class UsageDataSource(private val context: Context) {

    fun hasUsageAccess(): Boolean {
        val appOps = context.getSystemService(
            Context.APP_OPS_SERVICE
        ) as AppOpsManager

        return appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        ) == AppOpsManager.MODE_ALLOWED
    }

    fun openUsageAccessSettings() {
        context.startActivity(
            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }

    fun getUsageForDay(dateStartMillis: Long): List<AppUsageItem> {
        if (!hasUsageAccess()) return emptyList()

        val manager = context.getSystemService(
            Context.USAGE_STATS_SERVICE
        ) as UsageStatsManager

        val start = Calendar.getInstance().apply {
            timeInMillis = dateStartMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val end = Calendar.getInstance().apply {
            timeInMillis = start
            add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis.coerceAtMost(System.currentTimeMillis())

        if (end <= start) return emptyList()

        val stats = manager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            start,
            end
        )

        val launchCounts = getLaunchCounts(manager, start, end)

        return stats
            .filter {
                it.totalTimeInForeground > 0 &&
                        it.packageName != context.packageName
            }
            .groupBy { it.packageName }
            .mapNotNull { (packageName, entries) ->
                val totalTime = entries.sumOf {
                    it.totalTimeInForeground
                }

                if (totalTime <= 0L) {
                    return@mapNotNull null
                }

                AppUsageItem(
                    packageName = packageName,
                    appName = getAppName(packageName),
                    totalTimeMillis = totalTime,
                    lastTimeUsed = entries.maxOf {
                        it.lastTimeUsed
                    },
                    launchCount = launchCounts[packageName] ?: 0
                )
            }
            .sortedByDescending { it.totalTimeMillis }
    }

    fun getAppName(packageName: String): String {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(
                packageName,
                0
            )

            context.packageManager
                .getApplicationLabel(appInfo)
                .toString()
        } catch (_: Exception) {
            packageName
        }
    }

    fun getAppInstallTime(packageName: String): Long? {
        return try {
            val packageInfo =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.packageManager.getPackageInfo(
                        packageName,
                        android.content.pm.PackageManager.PackageInfoFlags.of(0L)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    context.packageManager.getPackageInfo(packageName, 0)
                }

            android.util.Log.d(
                "DeviceScopeUsage",
                "Package: $packageName, firstInstallTime: ${packageInfo.firstInstallTime}"
            )

            packageInfo.firstInstallTime.takeIf { it > 0L }
        } catch (e: Exception) {
            android.util.Log.e(
                "DeviceScopeUsage",
                "Could not get install time for $packageName",
                e
            )
            null
        }
    }



    fun getDailyUsageHistory(
        packageName: String,
        installTimeMillis: Long
    ): List<AppDailyUsage> {
        if (!hasUsageAccess()) return emptyList()

        val manager = context.getSystemService(
            Context.USAGE_STATS_SERVICE
        ) as UsageStatsManager

        val now = System.currentTimeMillis()

        val installDay = Calendar.getInstance().apply {
            timeInMillis = installTimeMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val result = mutableListOf<AppDailyUsage>()
        val day = today.clone() as Calendar

        while (day.timeInMillis >= installDay.timeInMillis) {
            val start = day.timeInMillis

            val end = (day.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, 1)
            }.timeInMillis.coerceAtMost(now)

            val stats = if (end > start) {
                manager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    start,
                    end
                )
            } else {
                emptyList()
            }

            val matchingStats = stats.filter {
                it.packageName == packageName
            }

            val available = matchingStats.isNotEmpty()

            val usage = if (available) {
                matchingStats.sumOf {
                    it.totalTimeInForeground
                }
            } else {
                null
            }

            result.add(
                AppDailyUsage(
                    dateStartMillis = start,
                    usageMillis = usage,
                    dataAvailable = available
                )
            )

            day.add(Calendar.DAY_OF_YEAR, -1)
        }

        return result
    }

    private fun getLaunchCounts(
        manager: UsageStatsManager,
        start: Long,
        end: Long
    ): Map<String, Int> {
        val counts = mutableMapOf<String, Int>()
        val events = manager.queryEvents(start, end)
        val event = UsageEvents.Event()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)

            val isLaunchEvent = if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
            ) {
                event.eventType ==
                        UsageEvents.Event.ACTIVITY_RESUMED
            } else {
                @Suppress("DEPRECATION")
                event.eventType ==
                        UsageEvents.Event.MOVE_TO_FOREGROUND
            }

            if (isLaunchEvent) {
                counts[event.packageName] =
                    (counts[event.packageName] ?: 0) + 1
            }
        }

        return counts
    }


    fun getLastSevenDaysScreenUsage(): List<DailyScreenUsage> {
        if (!hasUsageAccess()) return emptyList()

        val manager = context.getSystemService(
            Context.USAGE_STATS_SERVICE
        ) as UsageStatsManager

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return (6 downTo 0).map { daysAgo ->
            val dayStart = (today.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, -daysAgo)
            }.timeInMillis

            val dayEnd = (today.clone() as Calendar).apply {
                if (daysAgo == 0) {
                    timeInMillis = System.currentTimeMillis()
                } else {
                    add(Calendar.DAY_OF_YEAR, -(daysAgo - 1))
                }
            }.timeInMillis

            DailyScreenUsage(
                dateStartMillis = dayStart,
                screenOnTimeMillis = getScreenOnTime(
                    manager = manager,
                    start = dayStart,
                    end = dayEnd
                )
            )
        }
    }

    private fun getScreenOnTime(
        manager: UsageStatsManager,
        start: Long,
        end: Long
    ): Long {
        val events = manager.queryEvents(start, end)
        val event = UsageEvents.Event()

        var screenOn = false
        var screenOnStartedAt = 0L
        var totalMillis = 0L

        while (events.hasNextEvent()) {
            events.getNextEvent(event)

            when (event.eventType) {
                UsageEvents.Event.SCREEN_INTERACTIVE -> {
                    if (!screenOn) {
                        screenOn = true
                        screenOnStartedAt = event.timeStamp
                    }
                }

                UsageEvents.Event.SCREEN_NON_INTERACTIVE -> {
                    if (screenOn) {
                        totalMillis += (
                                event.timeStamp - screenOnStartedAt
                                ).coerceAtLeast(0L)

                        screenOn = false
                    }
                }
            }
        }

        if (screenOn) {
            totalMillis += (end - screenOnStartedAt).coerceAtLeast(0L)
        }

        return totalMillis
    }

    fun getScreenUsageForWeek(
        selectedDateMillis: Long
    ): List<DailyScreenUsage> {
        if (!hasUsageAccess()) return emptyList()

        val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedDateMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // Start the week on Monday.
            firstDayOfWeek = Calendar.MONDAY
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }

        val weekStart = calendar.timeInMillis
        val now = System.currentTimeMillis()

        return (0..6).map { dayOffset ->
            val dayStart = (calendar.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, dayOffset)
            }.timeInMillis

            val dayEnd = (Calendar.getInstance().apply {
                timeInMillis = dayStart
                add(Calendar.DAY_OF_YEAR, 1)
            }.timeInMillis).coerceAtMost(now)

            val screenTime = if (dayStart < now) {
                val manager = context.getSystemService(
                    Context.USAGE_STATS_SERVICE
                ) as UsageStatsManager

                getScreenOnTime(
                    manager = manager,
                    start = dayStart,
                    end = dayEnd
                )
            } else {
                0L
            }

            DailyScreenUsage(
                dateStartMillis = dayStart,
                screenOnTimeMillis = screenTime
            )
        }
    }

    fun getUnlockCountForDay(dateStartMillis: Long): Int {
        if (!hasUsageAccess()) return 0

        // Keyguard events are available on Android 9 and newer.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            return 0
        }

        val manager = context.getSystemService(
            Context.USAGE_STATS_SERVICE
        ) as UsageStatsManager

        val start = Calendar.getInstance().apply {
            timeInMillis = dateStartMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val end = Calendar.getInstance().apply {
            timeInMillis = start
            add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis.coerceAtMost(System.currentTimeMillis())

        if (end <= start) return 0

        val events = manager.queryEvents(start, end)
        val event = UsageEvents.Event()
        var unlockCount = 0

        while (events.hasNextEvent()) {
            events.getNextEvent(event)

            if (
                event.eventType ==
                UsageEvents.Event.KEYGUARD_HIDDEN
            ) {
                unlockCount++
            }
        }

        return unlockCount
    }

}
