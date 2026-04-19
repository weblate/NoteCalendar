package com.sztorm.notecalendar.repositories

import com.orm.SugarRecord
import com.orm.SugarContext
import com.sztorm.notecalendar.ILogger
import com.sztorm.notecalendar.LogTags.NOTE_REPOSITORY
import com.sztorm.notecalendar.NoteData
import com.sztorm.notecalendar.component1
import com.sztorm.notecalendar.component2
import java.time.LocalDate
import java.time.YearMonth

interface NoteRepository {
    fun add(note: NoteData)
    fun addAll(notes: List<NoteData>)
    fun update(note: NoteData)
    fun delete(note: NoteData)
    fun deleteAll(): Int
    fun getAll(): List<NoteData>
    fun getBy(date: LocalDate): NoteData?
    fun getBy(yearMonth: YearMonth): List<NoteData>
}

/**
 * [NoteRepositoryImpl] is ready to use when [SugarContext] is initialized.
 **/
class NoteRepositoryImpl(val logger: ILogger) : NoteRepository {
    override fun add(note: NoteData) {
        note.save()
        logger.info("$NOTE_REPOSITORY added note ${note.date}")
    }

    override fun addAll(notes: List<NoteData>) {
        notes.forEach { it.save() }
        logger.info("$NOTE_REPOSITORY added notes (${notes.size})")
    }

    override fun update(note: NoteData) {
        note.update()
        logger.info("$NOTE_REPOSITORY updated note ${note.date}")
    }

    override fun delete(note: NoteData) {
        val date = note.date
        note.delete()
        logger.info("$NOTE_REPOSITORY deleted note $date")
    }

    override fun deleteAll(): Int {
        val count = SugarRecord.deleteAll(NoteData::class.java)
        logger.info("$NOTE_REPOSITORY deleted notes ($count)")

        return count
    }

    override fun getAll(): List<NoteData> {
        val result = SugarRecord.listAll(NoteData::class.java)
        logger.info("$NOTE_REPOSITORY got all notes (${result.size})")

        return result
    }

    override fun getBy(date: LocalDate): NoteData? {
        val result = SugarRecord
            .find(
                NoteData::class.java,
                "date = ?", date.toString()
            )
            .firstOrNull()
        val msg = if (result == null) "did not get" else "got"
        logger.info("$NOTE_REPOSITORY $msg note of $date")

        return result
    }

    override fun getBy(yearMonth: YearMonth): List<NoteData> {
        val (year, month) = yearMonth
        val yearString = year.toString().padStart(length = 4, padChar = '0')
        val monthString = month.value.toString().padStart(length = 2, padChar = '0')
        val result = SugarRecord.find(
            NoteData::class.java,
            "date LIKE ?", "%$yearString-$monthString-%"
        )
        logger.info("$NOTE_REPOSITORY got notes of $yearMonth (${result.size})")

        return result
    }
}