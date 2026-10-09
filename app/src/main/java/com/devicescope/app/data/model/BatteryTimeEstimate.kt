package com.devicescope.app.data.model

data class BatteryTimeEstimate(
    val minutes: Long?,
    val label: String,
    val isAvailable: Boolean
)