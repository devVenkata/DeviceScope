package com.devicescope.app.data.repository

import com.devicescope.app.data.datasource.BatteryDataSource
import com.devicescope.app.data.model.BatteryData

class BatteryRepository(
    private val dataSource: BatteryDataSource
) {

    fun getBatteryData(): BatteryData {
        return dataSource.getBatteryData()
    }
}