package com.sztorm.notecalendar.repositories

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.sztorm.notecalendar.PreferenceKeys
import com.sztorm.notecalendar.R
import com.sztorm.notecalendar.StartingViewType
import com.sztorm.notecalendar.ThemeColors
import com.sztorm.notecalendar.helpers.ContextHelper.Companion.getColorFromAttr
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.temporal.WeekFields
import java.util.Locale

private const val PREFERENCES_NAME = "com.sztorm.notecalendar_preferences"
private val Context.preferences: DataStore<Preferences> by preferencesDataStore(
    name = PREFERENCES_NAME,
    produceMigrations = { listOf(SharedPreferencesMigration(context = it, PREFERENCES_NAME)) }
)

private const val HOUR_BITS: Int = 0b00000000_00000000_00000000_00011111
private const val HOUR_BITS_SIZE: Int = 5
private const val MINUTE_BITS: Int = 0b00000000_00000000_00000111_11100000

class UserPreferencesRepository(private val context: Context) {
    private suspend inline fun <reified T> getPreference(key: Preferences.Key<T>, default: T): T =
        context.preferences.data
            .catch { exception ->
                when (exception) {
                    is IOException -> emit(emptyPreferences())
                    else -> throw exception
                }
            }
            .map { it[key] ?: default }
            .first()

    private fun Int.asLocalTime(): LocalTime {
        val hour = this and HOUR_BITS
        val minute = (this and MINUTE_BITS) shr HOUR_BITS_SIZE

        return LocalTime.of(hour, minute)
    }

    private fun LocalTime.asInt(): Int = hour or (minute shl HOUR_BITS_SIZE)

    suspend fun getBackgroundColor(
        default: Color = Color(context.getColorFromAttr(R.attr.colorBackground))
    ) = Color(getPreference(PreferenceKeys.BackgroundColor, default.toArgb()))

    suspend fun getButtonTextColor(
        default: Color = Color(context.getColorFromAttr(R.attr.colorButtonText))
    ) = Color(getPreference(PreferenceKeys.ButtonTextColor, default.toArgb()))

    suspend fun getInactiveItemColor(
        default: Color = Color(context.getColorFromAttr(R.attr.colorInactiveItem))
    ) = Color(getPreference(PreferenceKeys.InactiveItemColor, default.toArgb()))

    suspend fun getInactiveItemColorVariant(
        default: Color = Color(context.getColorFromAttr(R.attr.colorInactiveItemVariant))
    ) = Color(getPreference(PreferenceKeys.InactiveItemColorVariant, default.toArgb()))

    suspend fun getNoteColor(
        default: Color = Color(context.getColorFromAttr(R.attr.colorNote))
    ) = Color(getPreference(PreferenceKeys.NoteColor, default.toArgb()))

    suspend fun getNoteColorVariant(
        default: Color = Color(context.getColorFromAttr(R.attr.colorNoteVariant))
    ) = Color(getPreference(PreferenceKeys.NoteColorVariant, default.toArgb()))

    suspend fun getNoteTextColor(
        default: Color = Color(context.getColorFromAttr(R.attr.colorNoteText))
    ) = Color(getPreference(PreferenceKeys.NoteTextColor, default.toArgb()))

    suspend fun getPrimaryColor(
        default: Color = Color(context.getColorFromAttr(R.attr.colorPrimary))
    ) = Color(getPreference(PreferenceKeys.PrimaryColor, default.toArgb()))

    suspend fun getSecondaryColor(
        default: Color = Color(context.getColorFromAttr(R.attr.colorSecondary))
    ) = Color(getPreference(PreferenceKeys.SecondaryColor, default.toArgb()))

    suspend fun getTextColor(
        default: Color = Color(context.getColorFromAttr(R.attr.colorText))
    ) = Color(getPreference(PreferenceKeys.TextColor, default.toArgb()))

    suspend fun getThemeColors() = ThemeColors(
        getPrimaryColor(),
        getSecondaryColor(),
        getInactiveItemColor(),
        getInactiveItemColorVariant(),
        getNoteColor(),
        getNoteColorVariant(),
        getTextColor(),
        getButtonTextColor(),
        getNoteTextColor(),
        getBackgroundColor()
    )

    suspend fun getTurnOnNotifications(default: Boolean = false): Boolean =
        getPreference(PreferenceKeys.TurnOnNotifications, default)

    suspend fun getFirstDayOfWeek(
        default: DayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
    ): DayOfWeek = getPreference(PreferenceKeys.FirstDayOfWeek, default.value.toString())
        .let { DayOfWeek.of(it.toInt()) }

    suspend fun getNotificationTime(
        default: LocalTime = LocalTime.of(8, 0)
    ): LocalTime = getPreference(PreferenceKeys.NotificationTime, default.asInt()).asLocalTime()

    suspend fun getStartingView(
        default: StartingViewType = StartingViewType.DAY_VIEW
    ): StartingViewType = getPreference(PreferenceKeys.StartingView, default.ordinal.toString())
        .let { StartingViewType.entries[it.toInt()] }

    suspend fun setBackgroundColor(value: Color) {
        context.preferences.edit {
            it[PreferenceKeys.BackgroundColor] = value.toArgb()
        }
    }

    suspend fun setButtonTextColor(value: Color) {
        context.preferences.edit {
            it[PreferenceKeys.ButtonTextColor] = value.toArgb()
        }
    }

    suspend fun setInactiveItemColor(value: Color) {
        context.preferences.edit {
            it[PreferenceKeys.InactiveItemColor] = value.toArgb()
        }
    }

    suspend fun setInactiveItemColorVariant(value: Color) {
        context.preferences.edit {
            it[PreferenceKeys.InactiveItemColorVariant] = value.toArgb()
        }
    }

    suspend fun setNoteColor(value: Color) {
        context.preferences.edit {
            it[PreferenceKeys.NoteColor] = value.toArgb()
        }
    }

    suspend fun setNoteColorVariant(value: Color) {
        context.preferences.edit {
            it[PreferenceKeys.NoteColorVariant] = value.toArgb()
        }
    }

    suspend fun setNoteTextColor(value: Color) {
        context.preferences.edit {
            it[PreferenceKeys.NoteTextColor] = value.toArgb()
        }
    }

    suspend fun setPrimaryColor(value: Color) {
        context.preferences.edit {
            it[PreferenceKeys.PrimaryColor] = value.toArgb()
        }
    }

    suspend fun setSecondaryColor(value: Color) {
        context.preferences.edit {
            it[PreferenceKeys.SecondaryColor] = value.toArgb()
        }
    }

    suspend fun setTextColor(value: Color) {
        context.preferences.edit {
            it[PreferenceKeys.TextColor] = value.toArgb()
        }
    }

    suspend fun setThemeColors(themeColors: ThemeColors) =
        with(themeColors) {
            setPrimaryColor(primaryColor)
            setSecondaryColor(secondaryColor)
            setInactiveItemColor(inactiveItemColor)
            setInactiveItemColorVariant(inactiveItemColorVariant)
            setNoteColor(noteColor)
            setNoteColorVariant(noteColorVariant)
            setTextColor(textColor)
            setButtonTextColor(buttonTextColor)
            setNoteTextColor(noteTextColor)
            setBackgroundColor(backgroundColor)
        }

    suspend fun setTurnOnNotifications(value: Boolean) {
        context.preferences.edit {
            it[PreferenceKeys.TurnOnNotifications] = value
        }
    }

    suspend fun setFirstDayOfWeek(value: DayOfWeek) {
        context.preferences.edit {
            it[PreferenceKeys.FirstDayOfWeek] = value.value.toString()
        }
    }

    suspend fun setNotificationTime(value: LocalTime) {
        context.preferences.edit {
            it[PreferenceKeys.NotificationTime] = value.asInt()
        }
    }

    suspend fun setStartingView(value: StartingViewType) {
        context.preferences.edit {
            it[PreferenceKeys.StartingView] = value.ordinal.toString()
        }
    }
}