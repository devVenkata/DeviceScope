package com.devicescope.app.presentation.dashboard.model

data class DashboardCardConfig(

    val type: DashboardCardType,

    val position: Int,

    val enabled: Boolean = true,

    val size: CardSize = CardSize.MEDIUM
)