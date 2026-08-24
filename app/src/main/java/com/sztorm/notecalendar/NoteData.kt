package com.sztorm.notecalendar

import com.orm.SugarRecord
import com.orm.dsl.Unique
import java.time.LocalDate
import java.time.OffsetDateTime

data class NoteData(
    @Unique val date: String = "",
    val text: String = "",
    val reminderDateTime: String = ""
) : SugarRecord() {
    fun toNote() = Note(
        date = LocalDate.parse(date),
        text = text,
        reminderDateTime = reminderDateTime
            .ifEmpty { null }
            ?.let { OffsetDateTime.parse(it) }
    )
}
