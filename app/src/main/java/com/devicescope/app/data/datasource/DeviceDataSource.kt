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
        return DeviceData(
            manufacturer = getManufacturer(),
            brand = getBrand(),
            model = getModel(),
            device = getDevice(),
            product = getProduct(),
            board = getBoard(),
            hardware = getHardware(),
            androidVersion = getAndroidVersion(),
            apiLevel = getApiLevel(),
            buildId = getBuildId()
        )
    }
}