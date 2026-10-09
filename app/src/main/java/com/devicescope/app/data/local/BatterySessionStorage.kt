package com.devicescope.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

private val Context.batteryDataStore by preferencesDataStore(
    name = "battery_data"
)

class BatterySessionStorage(
    private val context: Context
) {

    private companion object {

        val SESSION_ACTIVE =
            androidx.datastore.preferences.core.booleanPreferencesKey(
                "session_active"
            )

        val START_PERCENTAGE =
            intPreferencesKey(
                "start_percentage"
            )

        val START_CHARGE_COUNTER =
            floatPreferencesKey(
                "start_charge_counter"
            )

        val SESSION_HISTORY =
            stringPreferencesKey(
                "session_history"
            )
    }

    suspend fun saveSessionStart(
        percentage: Int,
        chargeCounterMah: Float
    ) {
        context.batteryDataStore.edit { preferences ->

            preferences[SESSION_ACTIVE] = true

            preferences[START_PERCENTAGE] =
                percentage

            preferences[START_CHARGE_COUNTER] =
                chargeCounterMah
        }
    }

    suspend fun getSessionStart(): Pair<Int, Float>? {

        val preferences =
            context.batteryDataStore.data.first()

        val active =
            preferences[SESSION_ACTIVE] ?: false

        if (!active) {
            return null
        }

        val percentage =
            preferences[START_PERCENTAGE]
                ?: return null

        val chargeCounter =
            preferences[START_CHARGE_COUNTER]
                ?: return null

        return Pair(
            percentage,
            chargeCounter
        )
    }

    suspend fun saveCompletedSession(
        startPercentage: Int,
        startChargeCounterMah: Float,
        endPercentage: Int,
        endChargeCounterMah: Float,
        estimatedCapacityMah: Float
    ) {

        context.batteryDataStore.edit { preferences ->

            val existingJson =
                preferences[SESSION_HISTORY]
                    ?: "[]"

            val history =
                JSONArray(existingJson)

            val session =
                JSONObject().apply {

                    put(
                        "startPercentage",
                        startPercentage
                    )

                    put(
                        "startChargeCounterMah",
                        startChargeCounterMah
                    )

                    put(
                        "endPercentage",
                        endPercentage
                    )

                    put(
                        "endChargeCounterMah",
                        endChargeCounterMah
                    )

                    put(
                        "estimatedCapacityMah",
                        estimatedCapacityMah
                    )
                }

            history.put(session)

            /*
             * Keep only the latest 20 sessions.
             */
            while (history.length() > 20) {
                history.remove(0)
            }

            preferences[SESSION_HISTORY] =
                history.toString()
        }
    }

    suspend fun getCompletedSessions(): List<SessionRecord> {

        val preferences =
            context.batteryDataStore.data.first()

        val json =
            preferences[SESSION_HISTORY]
                ?: "[]"

        val history =
            JSONArray(json)

        val sessions =
            mutableListOf<SessionRecord>()

        for (index in 0 until history.length()) {

            val item =
                history.getJSONObject(index)

            sessions.add(
                SessionRecord(
                    startPercentage =
                        item.getInt(
                            "startPercentage"
                        ),

                    startChargeCounterMah =
                        item.getDouble(
                            "startChargeCounterMah"
                        ).toFloat(),

                    endPercentage =
                        item.getInt(
                            "endPercentage"
                        ),

                    endChargeCounterMah =
                        item.getDouble(
                            "endChargeCounterMah"
                        ).toFloat(),

                    estimatedCapacityMah =
                        item.getDouble(
                            "estimatedCapacityMah"
                        ).toFloat()
                )
            )
        }

        return sessions
    }

    suspend fun clearSession() {

        context.batteryDataStore.edit { preferences ->

            preferences.remove(
                SESSION_ACTIVE
            )

            preferences.remove(
                START_PERCENTAGE
            )

            preferences.remove(
                START_CHARGE_COUNTER
            )
        }
    }
}

data class SessionRecord(
    val startPercentage: Int,
    val startChargeCounterMah: Float,
    val endPercentage: Int,
    val endChargeCounterMah: Float,
    val estimatedCapacityMah: Float
)