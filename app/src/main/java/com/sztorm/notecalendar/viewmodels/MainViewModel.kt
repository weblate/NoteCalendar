package com.sztorm.notecalendar.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.sztorm.notecalendar.ThemeColors
import java.time.LocalDate

class MainViewModel(initialState: MainState) : ViewModel() {
    var state by mutableStateOf(initialState)
        private set

    fun onEvent(event: MainEvent) {
        state = when (event) {
            is MainEvent.ThemeChange -> state.copy(themeColors = event.themeColors)
            is MainEvent.DayScreenDateChange -> state.copy(dayScreenDate = event.dayScreenDate)
        }
    }
}

sealed class MainEvent {
    data class ThemeChange(val themeColors: ThemeColors) : MainEvent()
    data class DayScreenDateChange(val dayScreenDate: LocalDate) : MainEvent()
}

data class MainState(
    val themeColors: ThemeColors,
    val dayScreenDate: LocalDate
)