package com.devicescope.app.domain.usecase

import com.devicescope.app.data.repository.BatteryRepository
import com.devicescope.app.domain.model.BatteryInfo

class GetBatteryInfoUseCase(
    private val repository: BatteryRepository
) {

    operator fun invoke(): BatteryInfo {

        val data = repository.getBatteryData()

        return BatteryInfo(

            // -----------------------------------------
            // BASIC STATUS
            // -----------------------------------------

            percentage = data.percentage,

            status = data.status,

            isCharging = data.isCharging,

            isFull = data.isFull,

            isPresent = data.isPresent,

            isLow = data.isLow,


            // -----------------------------------------
            // POWER SOURCE
            // -----------------------------------------

            powerSource = data.powerSource,


            // -----------------------------------------
            // HEALTH
            // -----------------------------------------

            health = data.health,

            healthPercentage = data.healthPercentage,


            // -----------------------------------------
            // TEMPERATURE
            // -----------------------------------------

            temperatureCelsius =
                data.temperatureCelsius,


            // -----------------------------------------
            // ELECTRICAL
            // -----------------------------------------

            voltageVolts =
                data.voltageVolts,

            currentMilliAmps =
                data.currentMilliAmps,

            averageCurrentMilliAmps =
                data.averageCurrentMilliAmps,

            powerWatts =
                data.powerWatts,


            // -----------------------------------------
            // CHARGE / ENERGY
            // -----------------------------------------

            chargeCounterMah =
                data.chargeCounterMah,

            remainingEnergyWh =
                data.remainingEnergyWh,


            // -----------------------------------------
            // CAPACITY
            // -----------------------------------------

            designCapacityMah =
                data.designCapacityMah,

            estimatedCapacityMah =
                data.estimatedCapacityMah,

            capacityPercentage =
                data.capacityPercentage,

            capacityLossPercentage =
                data.capacityLossPercentage,


            // -----------------------------------------
            // LIFECYCLE
            // -----------------------------------------

            cycleCount =
                data.cycleCount,


            // -----------------------------------------
            // TECHNOLOGY
            // -----------------------------------------

            technology =
                data.technology,


            // -----------------------------------------
            // CHARGING
            // -----------------------------------------

            chargingType =
                data.chargingType,

            chargingPowerWatts =
                data.chargingPowerWatts,


            // -----------------------------------------
            // AVAILABILITY
            // -----------------------------------------

            cycleCountAvailable =
                data.cycleCountAvailable,

            chargeCounterAvailable =
                data.chargeCounterAvailable,

            currentAvailable =
                data.currentAvailable,

            averageCurrentAvailable =
                data.averageCurrentAvailable,

            remainingEnergyAvailable =
                data.remainingEnergyAvailable,

            designCapacityAvailable =
                data.designCapacityAvailable,

            estimatedCapacityAvailable =
                data.estimatedCapacityAvailable,

            healthPercentageAvailable =
                data.healthPercentageAvailable
        )
    }
}