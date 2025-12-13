package com.sztorm.notecalendar.components.colorpicker

import androidx.compose.ui.graphics.Color
import com.sztorm.mathkit.ColorRGBA32
import com.sztorm.notecalendar.toColor

data class RgbColor(val red: Float, val green: Float, val blue: Float) {
    init {
        require(red in 0f..360f) { "red should be in [0, 360] range." }
        require(green in 0f..1f) { "green should be in [0, 1] range." }
        require(blue in 0f..1f) { "blue should be in [0, 1] range." }
    }

    fun toColor() = Color(red, green, blue)

    fun toHsl() = toColor().toHslColor()

    fun toHsv() = toColor().toHsvColor()

    companion object {
        private val rgbCodeRegexInt =
            Regex("""[rR][gG][bB]\(\s*([+-]?[0-9]+)\s*,\s*([+-]?[0-9]+)\s*,\s*([+-]?[0-9]+)\s*\)""")
        private val rgbCodeRegexFloat = Regex(
            """[rR][gG][bB]\(\s*([+-]?[0-9]*\.[0-9]+)\s*,\s*([+-]?[0-9]*\.[0-9]+)\s*,\s*([+-]?[0-9]*\.[0-9]+)\s*\)"""
        )

        fun parseRgbCodeOrNull(rgbCode: CharSequence): RgbColor? {
            val trimmed = rgbCode.trim()

            rgbCodeRegexInt
                .matchEntire(trimmed)
                ?.let { matchResult ->
                    val groupValues = matchResult.groupValues
                    val r = groupValues.getOrNull(1)?.toIntOrNull()
                    val g = groupValues.getOrNull(2)?.toIntOrNull()
                    val b = groupValues.getOrNull(3)?.toIntOrNull()

                    if (r != null && g != null && b != null) {
                        return ColorRGBA32(
                            r = r.coerceIn(0, 255).toUByte(),
                            g = g.coerceIn(0, 255).toUByte(),
                            b = b.coerceIn(0, 255).toUByte(),
                            a = 255u
                        ).toColor()
                            .toRgbColor()
                    }
                }
            rgbCodeRegexFloat
                .matchEntire(trimmed)
                ?.let { matchResult ->
                    val groupValues = matchResult.groupValues
                    val red = groupValues.getOrNull(1)?.toFloatOrNull()
                    val green = groupValues.getOrNull(2)?.toFloatOrNull()
                    val blue = groupValues.getOrNull(3)?.toFloatOrNull()

                    if (red != null && green != null && blue != null) {
                        return RgbColor(
                            red.coerceIn(0f, 1f),
                            green.coerceIn(0f, 1f),
                            blue.coerceIn(0f, 1f),
                        )
                    }
                }
            return null
        }
    }
}

fun Color.toRgbColor() = RgbColor(red, green, blue)