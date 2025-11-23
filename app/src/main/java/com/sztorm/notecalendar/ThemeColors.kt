package com.sztorm.notecalendar

import androidx.annotation.ColorInt
import androidx.compose.material3.lightColorScheme
import androidx.core.graphics.ColorUtils
import java.time.DayOfWeek
import androidx.compose.ui.graphics.Color as CColor

class ThemeColors(
    @field:ColorInt val primaryColor: Int,
    @field:ColorInt val secondaryColor: Int,
    @field:ColorInt val inactiveItemColor: Int,
    @field:ColorInt val inactiveItemColorVariant: Int,
    @field:ColorInt val noteColor: Int,
    @field:ColorInt val noteColorVariant: Int,
    @field:ColorInt val textColor: Int,
    @field:ColorInt val buttonTextColor: Int,
    @field:ColorInt val noteTextColor: Int,
    @field:ColorInt val backgroundColor: Int
) {
    val colorScheme = lightColorScheme().copy(
        primary = CColor(primaryColor),
        secondary = CColor(secondaryColor),
        background = CColor(backgroundColor),
        surface = CColor(backgroundColor),
        surfaceVariant = CColor(backgroundColor),
        surfaceTint = CColor(backgroundColor),
        surfaceContainer = CColor(backgroundColor)
    )
    val backgroundColorVariant: Int = ColorUtils.blendARGB(
        backgroundColor, 0xffffffff.toInt(), 0.06f) // TODO: make it modifiable from settigns
    val inactiveTextColor: Int = ColorUtils.setAlphaComponent(textColor, 255 / 3)

    fun getTextColorOf(dayOfWeek: DayOfWeek, firstDayOfWeek: DayOfWeek): Int {
        val sixthDayOfWeek: DayOfWeek = firstDayOfWeek - 2
        val seventhDayOfWeek: DayOfWeek = firstDayOfWeek - 1

        return when (dayOfWeek) {
            seventhDayOfWeek -> primaryColor
            sixthDayOfWeek -> secondaryColor
            else -> textColor
        }
    }
}