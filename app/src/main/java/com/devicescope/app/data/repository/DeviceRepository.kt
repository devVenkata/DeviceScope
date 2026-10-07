package com.devicescope.app.data.repository

import com.devicescope.app.data.datasource.DeviceDataSource
import com.devicescope.app.data.model.DeviceData

class DeviceRepository(
    private val dataSource: DeviceDataSource
) {
    fun getDeviceData(): DeviceData {
        return dataSource.getDeviceData()
    }
}