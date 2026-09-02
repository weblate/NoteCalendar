package com.sztorm.notecalendar

import java.time.LocalDate
import java.time.OffsetDateTime

data class Note(
    val date: LocalDate,
    val text: String,
    val reminderDateTime: OffsetDateTime? = null
) {
    fun toNoteData() = NoteData(date.toString(), text, reminderDateTime?.toString() ?: "")

    @Suppress("unused")
    fun toReminderNoteOrNull() = reminderDateTime
        ?.let { ReminderNote(date, text, reminderDateTime) }
}

fun NoteData.toNote() = Note(
    date = LocalDate.parse(date),
    text = text,
    reminderDateTime = reminderDateTime
        .ifEmpty { null }
        ?.let { OffsetDateTime.parse(it) }
)

data class ReminderNote(
    val date: LocalDate,
    val text: String,
    val reminderDateTime: OffsetDateTime
) {
    @Suppress("unused")
    fun toNote() = Note(date, text, reminderDateTime)
}