package com.sztorm.notecalendar

import android.content.res.ColorStateList
import androidx.compose.ui.graphics.Color as CColor
import androidx.annotation.ColorInt
import androidx.compose.material3.lightColorScheme
import androidx.core.graphics.ColorUtils
import com.sztorm.notecalendar.helpers.ColorStateListHelper
import java.time.DayOfWeek

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
    val selectBackgroundColor: Int =
        if (ColorUtils.calculateLuminance(backgroundColor) < 0.5) 0x40ffffff
        else 0x40000000
    val backgroundColorVariant: Int = ColorUtils.blendARGB(
        backgroundColor, 0xffffffff.toInt(), 0.06f) // TODO: make it modifiable from settigns
    val inactiveTextColor: Int = ColorUtils.setAlphaComponent(textColor, 255 / 3)
    val buttonRippleColorStateList: ColorStateList = ColorStateListHelper
        .createRippleColorStateList(
            color = ColorUtils.setAlphaComponent(primaryColor, 255 / 10)
        )

    val navigationButtonStrokeColorStateList: ColorStateList = ColorStateListHelper
        .createToggleColorStateList(
            checkedColor = primaryColor,
            uncheckedColor = inactiveItemColorVariant
        )

    val navigationButtonIconColorStateList: ColorStateList = ColorStateListHelper
        .createToggleColorStateList(
            checkedColor = primaryColor,
            uncheckedColor = inactiveItemColor
        )

    val navigationButtonBackgroundColorStateList: ColorStateList = ColorStateListHelper
        .createToggleColorStateList(
            checkedColor = ColorUtils.setAlphaComponent(primaryColor, 255 / 10)
        )

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