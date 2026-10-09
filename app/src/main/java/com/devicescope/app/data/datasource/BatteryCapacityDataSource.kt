package com.devicescope.app.data.datasource

import android.content.Context
import java.io.File

class BatteryCapacityDataSource(
    private val context: Context
) {

    fun getDesignCapacityMah(): Float? {

        /*
         * -----------------------------------------------------
         * SOURCE 1
         * -----------------------------------------------------
         *
         * Some Android/Linux devices expose battery
         * information through power_supply sysfs.
         *
         * Normal applications may not be allowed to read
         * these files, so failure is expected.
         */

        val sysfsCapacity = readSysfsDesignCapacity()

        if (sysfsCapacity != null) {
            return sysfsCapacity
        }

        /*
         * -----------------------------------------------------
         * SOURCE 2
         * -----------------------------------------------------
         *
         * No supported public Android API currently provides
         * a universal factory design-capacity value.
         *
         * Do not guess it.
         */

        return null
    }

    private fun readSysfsDesignCapacity(): Float? {

        val possiblePaths = listOf(
            "/sys/class/power_supply/battery/charge_full_design",
            "/sys/class/power_supply/battery/energy_full_design",
            "/sys/class/power_supply/bms/charge_full_design",
            "/sys/class/power_supply/bms/energy_full_design"
        )

        for (path in possiblePaths) {

            val file = File(path)

            if (!file.exists() || !file.canRead()) {
                continue
            }

            val rawValue =
                try {
                    file.readText().trim()
                } catch (_: Exception) {
                    null
                }

            if (rawValue.isNullOrBlank()) {
                continue
            }

            val value =
                rawValue.toFloatOrNull()
                    ?: continue

            if (value <= 0f) {
                continue
            }

            /*
             * charge_full_design is normally reported in µAh.
             *
             * energy_full_design is normally reported in µWh,
             * so we cannot directly treat both as mAh.
             *
             * Therefore only accept the charge-based source
             * here for now.
             */
            if (path.endsWith("charge_full_design")) {
                return value / 1000f
            }
        }

        return null
    }
}