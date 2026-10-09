package com.devicescope.app.data.datasource

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import com.devicescope.app.data.model.BatteryData
import kotlin.math.abs

class BatteryDataSource(
    private val context: Context
) {

    private val batteryCapacityDataSource =
        BatteryCapacityDataSource(context)

    private val batteryManager: BatteryManager by lazy {
        context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    }

    fun getBatteryData(): BatteryData {

        val batteryIntent = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        // -------------------------------------------------
        // BATTERY LEVEL
        // -------------------------------------------------

        val level = batteryIntent?.getIntExtra(
            BatteryManager.EXTRA_LEVEL,
            -1
        ) ?: -1

        val scale = batteryIntent?.getIntExtra(
            BatteryManager.EXTRA_SCALE,
            -1
        ) ?: -1

        val percentage =
            if (level >= 0 && scale > 0) {
                ((level.toFloat() / scale.toFloat()) * 100f)
                    .toInt()
            } else {
                null
            }


        // -------------------------------------------------
        // STATUS
        // -------------------------------------------------

        val statusValue = batteryIntent?.getIntExtra(
            BatteryManager.EXTRA_STATUS,
            BatteryManager.BATTERY_STATUS_UNKNOWN
        ) ?: BatteryManager.BATTERY_STATUS_UNKNOWN

        val status = when (statusValue) {

            BatteryManager.BATTERY_STATUS_CHARGING ->
                "Charging"

            BatteryManager.BATTERY_STATUS_DISCHARGING ->
                "Discharging"

            BatteryManager.BATTERY_STATUS_FULL ->
                "Full"

            BatteryManager.BATTERY_STATUS_NOT_CHARGING ->
                "Not charging"

            else ->
                "Unknown"
        }

        val isCharging =
            statusValue == BatteryManager.BATTERY_STATUS_CHARGING ||
                    statusValue == BatteryManager.BATTERY_STATUS_FULL

        val isFull =
            statusValue == BatteryManager.BATTERY_STATUS_FULL


        // -------------------------------------------------
        // BATTERY PRESENT
        // -------------------------------------------------

        val isPresent = batteryIntent?.getBooleanExtra(
            BatteryManager.EXTRA_PRESENT,
            false
        )


        // -------------------------------------------------
        // LOW BATTERY
        // -------------------------------------------------

        val isLow =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

                batteryIntent?.getBooleanExtra(
                    BatteryManager.EXTRA_BATTERY_LOW,
                    false
                )

            } else {

                percentage?.let {
                    it <= 15
                }
            }


        // -------------------------------------------------
        // POWER SOURCE
        // -------------------------------------------------

        val pluggedValue = batteryIntent?.getIntExtra(
            BatteryManager.EXTRA_PLUGGED,
            0
        ) ?: 0

        val powerSource = when (pluggedValue) {

            BatteryManager.BATTERY_PLUGGED_AC ->
                "AC"

            BatteryManager.BATTERY_PLUGGED_USB ->
                "USB"

            BatteryManager.BATTERY_PLUGGED_WIRELESS ->
                "Wireless"

            else ->
                "Battery"
        }


        // -------------------------------------------------
        // HEALTH
        // -------------------------------------------------

        val healthValue = batteryIntent?.getIntExtra(
            BatteryManager.EXTRA_HEALTH,
            BatteryManager.BATTERY_HEALTH_UNKNOWN
        ) ?: BatteryManager.BATTERY_HEALTH_UNKNOWN

        val health = when (healthValue) {

            BatteryManager.BATTERY_HEALTH_GOOD ->
                "Good"

            BatteryManager.BATTERY_HEALTH_OVERHEAT ->
                "Overheat"

            BatteryManager.BATTERY_HEALTH_DEAD ->
                "Dead"

            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE ->
                "Over voltage"

            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE ->
                "Unspecified failure"

            BatteryManager.BATTERY_HEALTH_COLD ->
                "Cold"

            else ->
                "Unknown"
        }


        // -------------------------------------------------
        // TEMPERATURE
        // Android reports tenths of °C
        // -------------------------------------------------

        val temperatureRaw = batteryIntent?.getIntExtra(
            BatteryManager.EXTRA_TEMPERATURE,
            Int.MIN_VALUE
        )

        val temperatureCelsius =
            if (
                temperatureRaw != null &&
                temperatureRaw != Int.MIN_VALUE
            ) {
                temperatureRaw / 10f
            } else {
                null
            }


        // -------------------------------------------------
        // VOLTAGE
        // Android reports millivolts
        // -------------------------------------------------

        val voltageRaw = batteryIntent?.getIntExtra(
            BatteryManager.EXTRA_VOLTAGE,
            Int.MIN_VALUE
        )

        val voltageVolts =
            if (
                voltageRaw != null &&
                voltageRaw != Int.MIN_VALUE
            ) {
                voltageRaw / 1000f
            } else {
                null
            }


        // -------------------------------------------------
        // TECHNOLOGY
        // -------------------------------------------------

        val technology = batteryIntent?.getStringExtra(
            BatteryManager.EXTRA_TECHNOLOGY
        )


        // -------------------------------------------------
        // CURRENT NOW
        // µA -> mA
        // -------------------------------------------------

        val currentNowRaw = batteryManager.getIntProperty(
            BatteryManager.BATTERY_PROPERTY_CURRENT_NOW
        )

        val currentMilliAmps =
            if (currentNowRaw != Int.MIN_VALUE) {
                currentNowRaw / 1000
            } else {
                null
            }


        // -------------------------------------------------
        // AVERAGE CURRENT
        // µA -> mA
        // -------------------------------------------------

        val averageCurrentRaw = batteryManager.getIntProperty(
            BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE
        )

        val averageCurrentMilliAmps =
            if (averageCurrentRaw != Int.MIN_VALUE) {
                averageCurrentRaw / 1000
            } else {
                null
            }


        // -------------------------------------------------
        // CHARGE COUNTER
        // µAh -> mAh
        // -------------------------------------------------

        val chargeCounterRaw = batteryManager.getIntProperty(
            BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER
        )

        val chargeCounterMah =
            if (
                chargeCounterRaw != Int.MIN_VALUE &&
                chargeCounterRaw >= 0
            ) {
                chargeCounterRaw / 1000f
            } else {
                null
            }


        // -------------------------------------------------
        // REMAINING ENERGY
        // nWh -> Wh
        // -------------------------------------------------

        val energyCounterRaw = batteryManager.getLongProperty(
            BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER
        )

        val remainingEnergyWh =
            if (energyCounterRaw != Long.MIN_VALUE &&
                energyCounterRaw >= 0
            ) {
                energyCounterRaw / 1_000_000_000f
            } else {
                null
            }


        // -------------------------------------------------
        // CYCLE COUNT
        // Android 14 / API 34+
        // -------------------------------------------------

        val cycleCount =
            if (Build.VERSION.SDK_INT >= 34) {

                val value = batteryIntent?.getIntExtra(
                    BatteryManager.EXTRA_CYCLE_COUNT,
                    -1
                ) ?: -1

                if (value >= 0) {
                    value
                } else {
                    null
                }

            } else {
                null
            }


        // -------------------------------------------------
        // CALCULATED ELECTRICAL POWER
        //
        // Current sign varies by device/OEM.
        // For a simple magnitude display we use abs().
        // -------------------------------------------------

        val powerWatts =
            if (
                voltageVolts != null &&
                currentMilliAmps != null
            ) {

                voltageVolts *
                        (abs(currentMilliAmps) / 1000f)

            } else {
                null
            }


        // -------------------------------------------------
        // CAPACITY / HEALTH ESTIMATION
        //
        // Do NOT invent these yet.
        // We'll add supported capacity sources separately.
        // -------------------------------------------------

        val designCapacityMah =
            batteryCapacityDataSource.getDesignCapacityMah()

        val estimatedCapacityMah: Float? = null

        val capacityPercentage: Float? = null

        val capacityLossPercentage: Float? = null

        val healthPercentage: Float? = null


        // -------------------------------------------------
        // CHARGING POWER
        // -------------------------------------------------

        val chargingPowerWatts =
            if (isCharging && powerWatts != null) {
                powerWatts
            } else {
                null
            }


        // -------------------------------------------------
        // RETURN COMPLETE MODEL
        // -------------------------------------------------

        return BatteryData(

            percentage = percentage,

            status = status,

            isCharging = isCharging,

            isFull = isFull,

            isPresent = isPresent,

            isLow = isLow,

            powerSource = powerSource,

            health = health,

            healthPercentage = healthPercentage,

            temperatureCelsius = temperatureCelsius,

            voltageVolts = voltageVolts,

            currentMilliAmps = currentMilliAmps,

            averageCurrentMilliAmps =
                averageCurrentMilliAmps,

            powerWatts = powerWatts,

            chargeCounterMah = chargeCounterMah,

            remainingEnergyWh = remainingEnergyWh,

            designCapacityMah = designCapacityMah,

            estimatedCapacityMah =
                estimatedCapacityMah,

            capacityPercentage =
                capacityPercentage,

            capacityLossPercentage =
                capacityLossPercentage,

            cycleCount = cycleCount,

            technology = technology,

            chargingType = powerSource,

            chargingPowerWatts =
                chargingPowerWatts,

            cycleCountAvailable =
                cycleCount != null,

            chargeCounterAvailable =
                chargeCounterMah != null,

            currentAvailable =
                currentMilliAmps != null,

            averageCurrentAvailable =
                averageCurrentMilliAmps != null,

            remainingEnergyAvailable =
                remainingEnergyWh != null,

            designCapacityAvailable =
                designCapacityMah != null,

            estimatedCapacityAvailable =
                estimatedCapacityMah != null,

            healthPercentageAvailable =
                healthPercentage != null
        )
    }
}