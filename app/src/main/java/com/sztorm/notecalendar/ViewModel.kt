package com.sztorm.notecalendar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MainViewModel(initialState: MainState): ViewModel() {
    var state by mutableStateOf(initialState)
        private set

    fun onEvent(event: MainEvent) {
        when(event) {
            is MainEvent.ThemeChange -> {
                state = state.copy(themeColors = event.themeColors)
            }
        }
    }
}

sealed class MainEvent {
    data class ThemeChange(val themeColors: ThemeColors): MainEvent()
}

data class MainState(
    val themeColors: ThemeColors
)