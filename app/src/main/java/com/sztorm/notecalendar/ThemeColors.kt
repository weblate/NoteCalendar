package com.sztorm.notecalendar

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import java.time.DayOfWeek

class ThemeColors(
    val primaryColor: Color,
    val secondaryColor: Color,
    val inactiveElementColor: Color,
    val noteColor: Color,
    val noteColorVariant: Color,
    val textColor: Color,
    val buttonTextColor: Color,
    val noteTextColor: Color,
    val backgroundColor: Color,
    val backgroundColorVariant: Color
) {
    val inactiveTextColor = textColor.copy(alpha = 0.3333333f)

    fun getTextColorOf(dayOfWeek: DayOfWeek, firstDayOfWeek: DayOfWeek): Color {
        val sixthDayOfWeek: DayOfWeek = firstDayOfWeek - 2
        val seventhDayOfWeek: DayOfWeek = firstDayOfWeek - 1

        return when (dayOfWeek) {
            seventhDayOfWeek -> primaryColor
            sixthDayOfWeek -> secondaryColor
            else -> textColor
        }
    }

    fun getColorScheme(isDarkTheme: Boolean) = when (isDarkTheme) {
        true -> darkColorScheme(
            primary = primaryColor,
            onPrimary = buttonTextColor,
            secondary = secondaryColor,
            onSecondary = buttonTextColor,
            tertiary = noteColor,
            onTertiary = buttonTextColor,
            background = backgroundColor,
            onBackground = textColor,
            surface = backgroundColor,
            onSurface = textColor,
        )

        false -> lightColorScheme(
            primary = primaryColor,
            onPrimary = buttonTextColor,
            secondary = secondaryColor,
            onSecondary = buttonTextColor,
            tertiary = noteColor,
            onTertiary = buttonTextColor,
            background = backgroundColor,
            onBackground = textColor,
            surface = backgroundColor,
            onSurface = textColor,
        )
    }
}