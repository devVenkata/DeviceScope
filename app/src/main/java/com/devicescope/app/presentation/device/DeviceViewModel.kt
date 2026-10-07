package com.devicescope.app.presentation.device

import androidx.lifecycle.ViewModel
import com.devicescope.app.data.datasource.DeviceDataSource
import com.devicescope.app.data.repository.DeviceRepository
import com.devicescope.app.domain.model.DeviceInfo
import com.devicescope.app.domain.usecase.GetDeviceInfoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DeviceViewModel: ViewModel() {

    private val dataSource = DeviceDataSource()

    private val repository = DeviceRepository(
        dataSource = dataSource
    )

    private val getDeviceInfoUseCase = GetDeviceInfoUseCase(
        repository = repository
    )

    private val _deviceInfo = MutableStateFlow<DeviceInfo?>(null)

    val deviceInfo: StateFlow<DeviceInfo?> =
        _deviceInfo.asStateFlow()

    init {
        loadDeviceInfo()
    }

    private fun loadDeviceInfo() {
        _deviceInfo.value = getDeviceInfoUseCase()
    }
}