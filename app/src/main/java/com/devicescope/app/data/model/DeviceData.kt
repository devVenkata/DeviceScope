package com.devicescope.app.data.model

import android.view.Display

data class DeviceData(
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

    val supportedAbis64: List<String>,
    val supportAbis32: List<String>
)
