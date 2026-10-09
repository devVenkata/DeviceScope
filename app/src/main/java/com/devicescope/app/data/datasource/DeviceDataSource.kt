package com.devicescope.app.data.datasource

import android.os.Build
import com.devicescope.app.data.model.DeviceData

class DeviceDataSource {

    fun getManufacturer(): String {
        return Build.MANUFACTURER
    }

    fun getBrand(): String {
        return Build.BRAND
    }

    fun getModel(): String {
        return Build.MODEL
    }
    fun getDevice(): String {
        return Build.DEVICE
    }

    fun getProduct(): String {
        return Build.PRODUCT
    }

    fun getBoard(): String {
        return Build.BOARD
    }

    fun getHardware(): String {
        return Build.HARDWARE
    }

    fun getAndroidVersion(): String {
        return Build.VERSION.RELEASE
    }

    fun getApiLevel(): Int {
        return Build.VERSION.SDK_INT
    }

    fun getBuildId(): String {
        return Build.ID
    }

    fun getDeviceData(): DeviceData {

        val supportedAbis64 = Build.SUPPORTED_64_BIT_ABIS.toList()
        val supportAbis32 = Build.SUPPORTED_32_BIT_ABIS.toList()

        return DeviceData(
            manufacturer = getManufacturer(),
            brand = getBrand(),
            model = getModel(),
            device = getDevice(),
            product = getProduct(),
            board = getBoard(),
            hardware = getHardware(),
            bootloader = Build.BOOTLOADER,

            androidVersion = getAndroidVersion(),
            apiLevel = getApiLevel(),
            securityPatch = Build.VERSION.SECURITY_PATCH,
            buildId = getBuildId(),
            buildDisplay = Build.DISPLAY,
            buildFingerprint = Build.FINGERPRINT,

            supportedAbis64 = supportedAbis64,
            supportAbis32 = supportAbis32
        )
    }
}