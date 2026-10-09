package com.devicescope.app.domain.model

import android.graphics.NinePatch

data class DeviceInfo(
    val manufacturer: String,
    val brand: String,
    val model: String,
    val device: String,
    val product: String,
    val board: String,
    val hardware: String,
    val bootloader: String,

    val androidVersion: String,
    val apiLevel: Int,
    val securityPatch: String,
    val buildId: String,
    val buildDisplay: String,
    val buildFingerprint: String,

    val supportAbis64: List<String>,
    val supportAbis32: List<String>
)
