package com.devicescope.app.data.model

data class BatteryData(

    // -----------------------------------------
    // BASIC BATTERY STATUS
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
    // BATTERY HEALTH
    // -----------------------------------------

    val health: String?,

    val healthPercentage: Float?,


    // -----------------------------------------
    // TEMPERATURE
    // -----------------------------------------

    val temperatureCelsius: Float?,


    // -----------------------------------------
    // VOLTAGE
    // -----------------------------------------

    val voltageVolts: Float?,


    // -----------------------------------------
    // CURRENT
    // -----------------------------------------

    val currentMilliAmps: Int?,

    val averageCurrentMilliAmps: Int?,


    // -----------------------------------------
    // POWER
    // -----------------------------------------

    val powerWatts: Float?,


    // -----------------------------------------
    // CHARGE COUNTER
    // -----------------------------------------

    val chargeCounterMah: Float?,


    // -----------------------------------------
    // REMAINING ENERGY
    // -----------------------------------------

    val remainingEnergyWh: Float?,


    // -----------------------------------------
    // BATTERY CAPACITY
    // -----------------------------------------

    val designCapacityMah: Float?,

    val estimatedCapacityMah: Float?,

    val capacityPercentage: Float?,

    val capacityLossPercentage: Float?,


    // -----------------------------------------
    // BATTERY LIFECYCLE
    // -----------------------------------------

    val cycleCount: Int?,


    // -----------------------------------------
    // BATTERY TECHNOLOGY
    // -----------------------------------------

    val technology: String?,


    // -----------------------------------------
    // CHARGING
    // -----------------------------------------

    val chargingType: String?,

    val chargingPowerWatts: Float?,


    // -----------------------------------------
    // DATA AVAILABILITY
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