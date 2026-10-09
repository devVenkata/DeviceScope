package com.devicescope.app.data.datasource

import android.os.BatteryManager
import com.devicescope.app.data.model.BatteryTimeEstimate
import kotlin.math.abs
import kotlin.math.roundToLong

class BatteryTimeEstimator(
    private val batteryManager: BatteryManager
) {

    private data class Sample(
        val timestamp: Long,
        val percentage: Int,
        val chargeCounterMah: Float?,
        val currentMilliAmps: Int?
    )

    private val samples = ArrayDeque<Sample>()

    private var lastChargingState: Boolean? = null

    private var lastEstimateMinutes: Long? = null

    private val maxSamples = 30

    fun addSample(
        percentage: Int?,
        isCharging: Boolean?,
        chargeCounterMah: Float?,
        currentMilliAmps: Int?,
        timestamp: Long = System.currentTimeMillis()
    ): BatteryTimeEstimate {

        if (percentage == null || isCharging == null) {
            return unavailable()
        }

        /*
         * Reset our measurement history whenever the battery
         * changes between charging and discharging.
         */
        if (
            lastChargingState != null &&
            lastChargingState != isCharging
        ) {
            samples.clear()
            lastEstimateMinutes = null
        }

        lastChargingState = isCharging

        /*
         * Full battery.
         */
        if (percentage >= 100) {

            samples.clear()

            lastEstimateMinutes = 0L

            return BatteryTimeEstimate(
                minutes = 0L,
                label = "Full",
                isAvailable = true
            )
        }

        /*
         * Store the latest battery measurement.
         */
        samples.addLast(
            Sample(
                timestamp = timestamp,
                percentage = percentage,
                chargeCounterMah = chargeCounterMah,
                currentMilliAmps = currentMilliAmps
            )
        )

        while (samples.size > maxSamples) {
            samples.removeFirst()
        }

        /*
         * -----------------------------------------------------
         * CHARGING
         * -----------------------------------------------------
         *
         * Prefer Android's own platform estimate.
         */
        if (isCharging) {

            val platformEstimate =
                getPlatformChargeTime()

            if (platformEstimate != null) {

                val smoothed =
                    smoothEstimate(platformEstimate)

                lastEstimateMinutes = smoothed

                return BatteryTimeEstimate(
                    minutes = smoothed,
                    label = formatTime(
                        smoothed,
                        "to full"
                    ),
                    isAvailable = true
                )
            }

            /*
             * Platform estimate unavailable.
             *
             * Fall back to percentage-based charging rate.
             */
            val fallback =
                calculatePercentageRateEstimate(
                    charging = true,
                    percentage = percentage
                )

            if (fallback != null) {

                val smoothed =
                    smoothEstimate(fallback)

                lastEstimateMinutes = smoothed

                return BatteryTimeEstimate(
                    minutes = smoothed,
                    label = formatTime(
                        smoothed,
                        "to full"
                    ),
                    isAvailable = true
                )
            }

            return unavailable()
        }

        /*
         * -----------------------------------------------------
         * DISCHARGING
         * -----------------------------------------------------
         *
         * First choice:
         *
         * Charge Counter / Current
         *
         * This does NOT require the battery percentage to change.
         */
        val currentBasedEstimate =
            calculateCurrentBasedDischargeEstimate(
                chargeCounterMah = chargeCounterMah,
                currentMilliAmps = currentMilliAmps
            )

        if (currentBasedEstimate != null) {

            val smoothed =
                smoothEstimate(currentBasedEstimate)

            lastEstimateMinutes = smoothed

            return BatteryTimeEstimate(
                minutes = smoothed,
                label = formatTime(
                    smoothed,
                    "remaining"
                ),
                isAvailable = true
            )
        }

        /*
         * -----------------------------------------------------
         * DISCHARGE FALLBACK
         * -----------------------------------------------------
         *
         * If current or charge counter is unavailable,
         * use percentage movement.
         */
        val percentageBasedEstimate =
            calculatePercentageRateEstimate(
                charging = false,
                percentage = percentage
            )

        if (percentageBasedEstimate != null) {

            val smoothed =
                smoothEstimate(percentageBasedEstimate)

            lastEstimateMinutes = smoothed

            return BatteryTimeEstimate(
                minutes = smoothed,
                label = formatTime(
                    smoothed,
                    "remaining"
                ),
                isAvailable = true
            )
        }

        return unavailable()
    }

    /*
     * Android platform charging-time estimate.
     */
    private fun getPlatformChargeTime(): Long? {

        val millis =
            batteryManager.computeChargeTimeRemaining()

        if (millis <= 0L) {
            return null
        }

        val minutes =
            millis / 60_000L

        return if (minutes > 0L) {
            minutes
        } else {
            1L
        }
    }

    /*
     * ---------------------------------------------------------
     * DISCHARGE TIME USING CURRENT
     * ---------------------------------------------------------
     *
     * Formula:
     *
     * remaining time =
     *
     * charge counter (mAh)
     * --------------------
     * average current (mA)
     *
     * Result is converted to minutes.
     */
    private fun calculateCurrentBasedDischargeEstimate(
        chargeCounterMah: Float?,
        currentMilliAmps: Int?
    ): Long? {

        if (chargeCounterMah == null) {
            return null
        }

        if (currentMilliAmps == null) {
            return null
        }

        if (chargeCounterMah <= 0f) {
            return null
        }

        /*
         * Current is normally negative while the battery
         * is discharging.
         *
         * We use the magnitude because some OEM devices
         * expose current direction differently.
         */
        val currentMagnitude =
            abs(currentMilliAmps)

        /*
         * Ignore extremely small values because they can
         * represent measurement noise.
         */
        if (currentMagnitude < 50) {
            return null
        }

        /*
         * Ignore clearly unreasonable readings.
         */
        if (currentMagnitude > 10_000) {
            return null
        }

        /*
         * Collect recent discharge current measurements.
         */
        val recentCurrents =
            samples
                .mapNotNull { it.currentMilliAmps }
                .map { abs(it) }
                .filter {
                    it >= 50 && it <= 10_000
                }

        if (recentCurrents.isEmpty()) {
            return null
        }

        /*
         * Sort the values and use the median.
         *
         * This is more resistant to sudden CPU spikes than
         * using only the latest current reading.
         */
        val sortedCurrents =
            recentCurrents.sorted()

        val medianCurrent =
            if (sortedCurrents.size % 2 == 1) {

                sortedCurrents[
                    sortedCurrents.size / 2
                ]

            } else {

                val middle =
                    sortedCurrents.size / 2

                (
                        sortedCurrents[middle - 1] +
                                sortedCurrents[middle]
                        ) / 2
            }

        if (medianCurrent <= 0) {
            return null
        }

        /*
         * mAh / mA = hours
         */
        val hours =
            chargeCounterMah / medianCurrent.toFloat()

        if (hours <= 0f) {
            return null
        }

        /*
         * Convert hours -> minutes.
         */
        val minutes =
            hours * 60f

        if (!minutes.isFinite()) {
            return null
        }

        return minutes.roundToLong()
    }

    /*
     * Percentage-based fallback.
     *
     * This is only used when current/charge-counter data
     * cannot provide an estimate.
     */
    private fun calculatePercentageRateEstimate(
        charging: Boolean,
        percentage: Int
    ): Long? {

        if (samples.size < 2) {
            return null
        }

        val newest =
            samples.last()

        /*
         * Look for a sample at least 60 seconds old.
         */
        val oldest =
            samples.firstOrNull { sample ->
                newest.timestamp - sample.timestamp >= 60_000L
            } ?: return null

        val elapsedMillis =
            newest.timestamp - oldest.timestamp

        if (elapsedMillis < 60_000L) {
            return null
        }

        val percentageChange =
            newest.percentage - oldest.percentage

        /*
         * Require at least 1% movement.
         */
        if (abs(percentageChange) < 1) {
            return null
        }

        val elapsedMinutes =
            elapsedMillis / 60_000.0

        if (elapsedMinutes <= 0.0) {
            return null
        }

        /*
         * -----------------------------------------------------
         * CHARGING
         * -----------------------------------------------------
         */
        if (charging) {

            if (percentageChange <= 0) {
                return null
            }

            val percentPerMinute =
                percentageChange / elapsedMinutes

            if (percentPerMinute <= 0.0) {
                return null
            }

            val remainingPercentage =
                100 - percentage

            return (
                    remainingPercentage /
                            percentPerMinute
                    ).roundToLong()
        }

        /*
         * -----------------------------------------------------
         * DISCHARGING
         * -----------------------------------------------------
         */
        if (percentageChange >= 0) {
            return null
        }

        val percentPerMinute =
            (-percentageChange) / elapsedMinutes

        if (percentPerMinute <= 0.0) {
            return null
        }

        return (
                percentage /
                        percentPerMinute
                ).roundToLong()
    }

    /*
     * Prevent the displayed value from jumping too much.
     */
    private fun smoothEstimate(
        newEstimate: Long
    ): Long {

        if (newEstimate <= 0L) {
            return 0L
        }

        val previous =
            lastEstimateMinutes
                ?: return newEstimate

        /*
         * If the new estimate is wildly different,
         * don't allow a huge visual jump immediately.
         */
        val difference =
            abs(newEstimate - previous)

        if (difference < 10L) {
            return newEstimate
        }

        return (
                previous * 0.7 +
                        newEstimate * 0.3
                ).roundToLong()
    }

    private fun unavailable(): BatteryTimeEstimate {

        return BatteryTimeEstimate(
            minutes = null,
            label = "Calculating...",
            isAvailable = false
        )
    }

    private fun formatTime(
        minutes: Long,
        suffix: String
    ): String {

        if (minutes <= 0L) {

            return if (suffix == "to full") {
                "Full"
            } else {
                "Less than 1m remaining"
            }
        }

        val days =
            minutes / (24 * 60)

        val hours =
            (minutes % (24 * 60)) / 60

        val remainingMinutes =
            minutes % 60

        val timeText =
            when {

                days > 0 && hours > 0 ->
                    "${days}d ${hours}h"

                days > 0 ->
                    "${days}d"

                hours > 0 &&
                        remainingMinutes > 0 ->
                    "${hours}h ${remainingMinutes}m"

                hours > 0 ->
                    "${hours}h"

                else ->
                    "${remainingMinutes}m"
            }

        return "$timeText $suffix"
    }
}