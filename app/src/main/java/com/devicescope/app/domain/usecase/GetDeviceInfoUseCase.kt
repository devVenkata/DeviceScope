package com.devicescope.app.domain.usecase

import com.devicescope.app.data.repository.DeviceRepository
import com.devicescope.app.domain.model.DeviceInfo

data class GetDeviceInfoUseCase(
    private val repository: DeviceRepository
) {
    operator fun invoke(): DeviceInfo {
        val data = repository.getDeviceData()

        return DeviceInfo(
            manufacturer = data.manufacturer,
            brand = data.brand,
            model = data.model,
            device = data.device,
            product = data.product,
            board = data.board,
            hardware = data.hardware,
            androidVersion = data.androidVersion,
            apiLevel = data.apiLevel,
            buildId = data.buildId
        )
    }
}
