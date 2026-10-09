package com.devicescope.app.presentation.battery

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.devicescope.app.data.datasource.BatteryTimeEstimator
import com.devicescope.app.data.datasource.BatteryCapacityCalculator
import com.devicescope.app.data.datasource.BatteryChargingTracker
import com.devicescope.app.data.datasource.BatteryDataSource
import com.devicescope.app.data.local.BatterySessionStorage
import com.devicescope.app.data.model.BatteryTimeEstimate
import com.devicescope.app.data.model.BatteryCapacityEstimate
import com.devicescope.app.data.model.BatteryChargingSession
import com.devicescope.app.data.repository.BatteryRepository
import com.devicescope.app.domain.model.BatteryInfo
import com.devicescope.app.domain.usecase.GetBatteryInfoUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BatteryViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val dataSource = BatteryDataSource(
        context
    )

    private val repository = BatteryRepository(
        dataSource
    )

    private val getBatteryInfoUseCase = GetBatteryInfoUseCase(
        repository
    )

    private val sessionStorage =
        BatterySessionStorage(context)

    private val chargingTracker =
        BatteryChargingTracker(sessionStorage)

    private val capacityCalculator =
        BatteryCapacityCalculator()

    private val batteryManager =
        context.getSystemService(
            Context.BATTERY_SERVICE
        ) as BatteryManager

    private val batteryTimeEstimator =
        BatteryTimeEstimator(batteryManager)

    private val _batteryTimeEstimate =
        MutableStateFlow(
            BatteryTimeEstimate(
                minutes = null,
                label = "Calculating...",
                isAvailable = false
            )
        )

    val batteryTimeEstimate: StateFlow<BatteryTimeEstimate> =
        _batteryTimeEstimate.asStateFlow()

    private val _batteryInfo =
        MutableStateFlow<BatteryInfo?>(null)

    val batteryInfo: StateFlow<BatteryInfo?> =
        _batteryInfo.asStateFlow()

    private val _lastChargingSession =
        MutableStateFlow<BatteryChargingSession?>(null)

    val lastChargingSession: StateFlow<BatteryChargingSession?> =
        _lastChargingSession.asStateFlow()

    private val _capacityEstimate =
        MutableStateFlow(
            BatteryCapacityEstimate(
                estimatedCapacityMah = null,
                sessionCount = 0,
                measurementQuality = "No data"
            )
        )

    val capacityEstimate: StateFlow<BatteryCapacityEstimate> =
        _capacityEstimate.asStateFlow()

    private fun updateCapacityEstimate() {

        viewModelScope.launch {

            val sessions =
                sessionStorage.getCompletedSessions()

            val estimate =
                capacityCalculator.calculate(
                    sessions
                )

            _capacityEstimate.value =
                estimate
        }
    }

    private val currentSamples = ArrayDeque<Int>()

    private val maxCurrentSamples = 10

    private val batteryReceiver = object : BroadcastReceiver() {

        override fun onReceive(
            context: Context?,
            intent: Intent?
        ) {
            updateBatteryInfo()
        }
    }

    init {
        updateBatteryInfo()
        updateCapacityEstimate()
        registerBatteryReceiver()
        startLiveUpdates()
    }

    private fun startLiveUpdates() {

        viewModelScope.launch {

            while (true) {

                delay(1000)

                updateBatteryInfo()
            }
        }
    }

    private fun updateBatteryInfo() {

        val newInfo = getBatteryInfoUseCase()

        _batteryTimeEstimate.value =
            batteryTimeEstimator.addSample(
                percentage = newInfo.percentage,
                isCharging = newInfo.isCharging,
                chargeCounterMah = newInfo.chargeCounterMah,
                currentMilliAmps = newInfo.currentMilliAmps
            )

        // Update the UI immediately
        newInfo.currentMilliAmps?.let { current ->

            currentSamples.addLast(current)

            if (currentSamples.size > maxCurrentSamples) {
                currentSamples.removeFirst()
            }
        }

        val calculatedAverageCurrent =
            if (currentSamples.isNotEmpty()) {
                currentSamples.average().toInt()
            } else {
                null
            }

        val estimatedRemainingEnergy =
            if (
                newInfo.chargeCounterMah != null &&
                newInfo.voltageVolts != null
            ) {
                (newInfo.chargeCounterMah / 1000f) *
                        newInfo.voltageVolts
            } else {
                null
            }

        val updatedInfo = newInfo.copy(
            averageCurrentMilliAmps =
                newInfo.averageCurrentMilliAmps
                    ?: calculatedAverageCurrent,

            remainingEnergyWh =
                newInfo.remainingEnergyWh
                    ?: estimatedRemainingEnergy,

            averageCurrentAvailable =
                newInfo.averageCurrentAvailable ||
                        calculatedAverageCurrent != null,

            remainingEnergyAvailable =
                newInfo.remainingEnergyAvailable ||
                        estimatedRemainingEnergy != null
        )

        // IMPORTANT: update UI immediately
        _batteryInfo.value = updatedInfo

        // Charging-session persistence happens separately
        viewModelScope.launch {

            val completedSession =
                chargingTracker.update(
                    percentage = newInfo.percentage,
                    chargeCounterMah =
                        newInfo.chargeCounterMah,
                    isCharging =
                        newInfo.isCharging
                )

            if (completedSession != null) {

                _lastChargingSession.value =
                    completedSession

                updateCapacityEstimate()
            }
        }
    }

    private fun registerBatteryReceiver() {

        val filter = IntentFilter(
            Intent.ACTION_BATTERY_CHANGED
        )

        ContextCompat.registerReceiver(
            context,
            batteryReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onCleared() {

        context.unregisterReceiver(
            batteryReceiver
        )

        super.onCleared()
    }
}