package com.sztorm.notecalendar.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sztorm.notecalendar.screens.DayNoteMode
import java.time.LocalDate

class DayScreenViewModel(initialState: DayScreenState) : ViewModel() {
    var state by mutableStateOf(initialState)
        private set

    fun onEvent(event: DayScreenEvent) {
        state = when (event) {
            is DayScreenEvent.NoteChange -> state.copy(note = event.note, noteBackup = state.note)

            is DayScreenEvent.DateChange -> state.copy(
                note = event.note,
                prevNote = event.prevNote,
                nextNote = event.nextNote,
                noteBackup = null,
            )

            is DayScreenEvent.NoteModeChange -> state.copy(noteMode = event.noteMode)
        }
    }
}

class DayScreenViewModelFactory(val initialState: DayScreenState) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(DayScreenViewModel::class.java) ->
            DayScreenViewModel(initialState) as T

        else -> throw IllegalArgumentException("Invalid modelClass")
    }
}

sealed class DayScreenEvent {
    data class NoteChange(val note: DayScreenNote?) : DayScreenEvent()
    data class DateChange(
        val note: DayScreenNote?, val prevNote: DayScreenNote?, val nextNote: DayScreenNote?
    ) : DayScreenEvent()

    data class NoteModeChange(val noteMode: DayNoteMode) : DayScreenEvent()
}

data class DayScreenNote(
    val date: LocalDate,
    val textValue: TextFieldValue,
    val reminderDateTime: OffsetDateTime? = null
)

data class DayScreenState(
    val note: DayScreenNote?,
    val prevNote: DayScreenNote?,
    val nextNote: DayScreenNote?,
    val noteMode: DayNoteMode,
    val noteBackup: DayScreenNote?,
)