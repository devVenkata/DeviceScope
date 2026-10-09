package com.devicescope.app.data.model

data class BatteryChargingSession(
    val startPercentage: Int,
    val startChargeCounterMah: Float,

    val endPercentage: Int,
    val endChargeCounterMah: Float,

    val estimatedCapacityMah: Float
)