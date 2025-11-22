package com.sztorm.notecalendar

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen(val route: String) {
    @Serializable
    data class Day(val isCreateOrEditRequested: Boolean = false) : Screen("day")

    @Serializable
    data object Week : Screen("week")

    @Serializable
    data object Month : Screen("month")

    @Serializable
    data object Settings : Screen("settings") {
        @Serializable
        data object CustomTheme : Screen("settings/customTheme")
    }
}