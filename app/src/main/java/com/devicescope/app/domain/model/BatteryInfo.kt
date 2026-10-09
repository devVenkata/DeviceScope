package com.devicescope.app.domain.model

data class BatteryInfo(

    // -----------------------------------------
    // BASIC STATUS
    // -----------------------------------------

    val percentage: Int?,

    val status: String?,

    val isCharging: Boolean?,

    val isFull: Boolean?,

    val isPresent: Boolean?,

    val isLow: Boolean?,


    // -----------------------------------------
    // POWER SOURCE
    // -----------------------------------------

    val powerSource: String?,


    // -----------------------------------------
    // HEALTH
    // -----------------------------------------

    val health: String?,

    val healthPercentage: Float?,


    // -----------------------------------------
    // TEMPERATURE
    // -----------------------------------------

    val temperatureCelsius: Float?,


    // -----------------------------------------
    // ELECTRICAL
    // -----------------------------------------

    val voltageVolts: Float?,

    val currentMilliAmps: Int?,

    val averageCurrentMilliAmps: Int?,

    val powerWatts: Float?,


    // -----------------------------------------
    // CHARGE / ENERGY
    // -----------------------------------------

    val chargeCounterMah: Float?,

    val remainingEnergyWh: Float?,


    // -----------------------------------------
    // CAPACITY
    // -----------------------------------------

    val designCapacityMah: Float?,

    val estimatedCapacityMah: Float?,

    val capacityPercentage: Float?,

    val capacityLossPercentage: Float?,


    // -----------------------------------------
    // LIFECYCLE
    // -----------------------------------------

    val cycleCount: Int?,


    // -----------------------------------------
    // TECHNOLOGY
    // -----------------------------------------

    val technology: String?,


    // -----------------------------------------
    // CHARGING
    // -----------------------------------------

    val chargingType: String?,

    val chargingPowerWatts: Float?,


    // -----------------------------------------
    // AVAILABILITY
    // -----------------------------------------

    val cycleCountAvailable: Boolean,

    val chargeCounterAvailable: Boolean,

    val currentAvailable: Boolean,

    val averageCurrentAvailable: Boolean,

    val remainingEnergyAvailable: Boolean,

    val designCapacityAvailable: Boolean,

    val estimatedCapacityAvailable: Boolean,

    val healthPercentageAvailable: Boolean
)