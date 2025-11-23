package com.sztorm.notecalendar

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import java.time.DayOfWeek

class ThemeColors(
    val primaryColor: Color,
    val secondaryColor: Color,
    val inactiveItemColor: Color,
    val inactiveItemColorVariant: Color,
    val noteColor: Color,
    val noteColorVariant: Color,
    val textColor: Color,
    val buttonTextColor: Color,
    val noteTextColor: Color,
    val backgroundColor: Color
) {
    val backgroundColorVariant =
        lerp(backgroundColor, Color.White, 0.06f) // TODO: make it modifiable from settigns
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

    fun toColorScheme() = lightColorScheme().copy(
        primary = primaryColor,
        secondary = secondaryColor,
        background = backgroundColor,
        surface = backgroundColor,
        surfaceVariant = backgroundColor,
        surfaceTint = backgroundColor,
        surfaceContainer = backgroundColor
    )
}