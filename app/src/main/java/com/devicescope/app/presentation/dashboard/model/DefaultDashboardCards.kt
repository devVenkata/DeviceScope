package com.devicescope.app.presentation.dashboard.model

val defaultDashboardCards = listOf(

    DashboardCardConfig(
        type = DashboardCardType.DEVICE,
        position = 0
    ),

    DashboardCardConfig(
        type = DashboardCardType.BATTERY,
        position = 1
    ),

    DashboardCardConfig(
        type = DashboardCardType.CPU,
        position = 2
    ),

    DashboardCardConfig(
        type = DashboardCardType.MEMORY,
        position = 3
    )
)