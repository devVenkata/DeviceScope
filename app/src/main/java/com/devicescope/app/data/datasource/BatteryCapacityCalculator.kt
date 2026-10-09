package com.devicescope.app.data.datasource

import com.devicescope.app.data.local.SessionRecord
import com.devicescope.app.data.model.BatteryCapacityEstimate

class BatteryCapacityCalculator {

    fun calculate(
        sessions: List<SessionRecord>
    ): BatteryCapacityEstimate {

        if (sessions.isEmpty()) {
            return BatteryCapacityEstimate(
                estimatedCapacityMah = null,
                sessionCount = 0,
                measurementQuality = "No data"
            )
        }

        /*
         * Ignore very small charging sessions.
         *
         * A session with only a few percentage points
         * is too noisy for capacity estimation.
         */
        val validSessions =
            sessions.filter { session ->

                val percentageGain =
                    session.endPercentage -
                            session.startPercentage

                percentageGain >= 10 &&
                        session.estimatedCapacityMah > 0
            }

        if (validSessions.isEmpty()) {
            return BatteryCapacityEstimate(
                estimatedCapacityMah = null,
                sessionCount = sessions.size,
                measurementQuality = "Collecting data"
            )
        }

        /*
         * Calculate the average capacity.
         */
        val averageCapacity =
            validSessions
                .map { it.estimatedCapacityMah }
                .average()
                .toFloat()

        val quality =
            when {
                validSessions.size >= 8 ->
                    "Good"

                validSessions.size >= 4 ->
                    "Medium"

                else ->
                    "Low"
            }

        return BatteryCapacityEstimate(
            estimatedCapacityMah =
                averageCapacity,

            sessionCount =
                validSessions.size,

            measurementQuality =
                quality
        )
    }
}