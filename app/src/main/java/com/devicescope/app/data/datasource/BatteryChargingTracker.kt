package com.devicescope.app.data.datasource

import com.devicescope.app.data.local.BatterySessionStorage
import com.devicescope.app.data.model.BatteryChargingSession

class BatteryChargingTracker(
    private val storage: BatterySessionStorage
) {

    suspend fun update(
        percentage: Int?,
        chargeCounterMah: Float?,
        isCharging: Boolean?
    ): BatteryChargingSession? {

        if (
            percentage == null ||
            chargeCounterMah == null ||
            isCharging == null
        ) {
            return null
        }

        val existingSession =
            storage.getSessionStart()

        /*
         * Charging has started.
         */
        if (
            isCharging &&
            existingSession == null
        ) {

            storage.saveSessionStart(
                percentage = percentage,
                chargeCounterMah = chargeCounterMah
            )

            return null
        }

        /*
         * Charging is continuing.
         */
        if (isCharging) {
            return null
        }

        /*
         * Charging has stopped.
         */
        if (
            !isCharging &&
            existingSession != null
        ) {

            val startPercentage =
                existingSession.first

            val startChargeCounterMah =
                existingSession.second

            val endPercentage =
                percentage

            val endChargeCounterMah =
                chargeCounterMah

            val percentageGained =
                endPercentage - startPercentage

            val chargeAdded =
                endChargeCounterMah -
                        startChargeCounterMah

            /*
             * Ignore invalid sessions.
             */
            if (
                percentageGained <= 0 ||
                chargeAdded <= 0
            ) {
                storage.clearSession()
                return null
            }

            val estimatedCapacity =
                (chargeAdded / percentageGained) * 100f

            /*
             * Save the completed session
             * permanently.
             */
            storage.saveCompletedSession(
                startPercentage =
                    startPercentage,

                startChargeCounterMah =
                    startChargeCounterMah,

                endPercentage =
                    endPercentage,

                endChargeCounterMah =
                    endChargeCounterMah,

                estimatedCapacityMah =
                    estimatedCapacity
            )

            val session =
                BatteryChargingSession(
                    startPercentage =
                        startPercentage,

                    startChargeCounterMah =
                        startChargeCounterMah,

                    endPercentage =
                        endPercentage,

                    endChargeCounterMah =
                        endChargeCounterMah,

                    estimatedCapacityMah =
                        estimatedCapacity
                )

            /*
             * Clear the active session only
             * after saving the completed one.
             */
            storage.clearSession()

            return session
        }

        return null
    }
}