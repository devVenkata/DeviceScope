package com.devicescope.app.data.model

data class BatteryCapacityEstimate(
    val estimatedCapacityMah: Float?,
    val sessionCount: Int,
    val measurementQuality: String
)