package com.sztorm.notecalendar.components.colorpicker

import androidx.compose.ui.graphics.Color
import com.sztorm.mathkit.AngleF
import com.sztorm.notecalendar.toHsl
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.component3
import kotlin.math.min

data class HslColor(val hue: Float, val saturation: Float, val lightness: Float) {
    init {
        require(hue in 0f..360f) { "hue should be in [0, 360] range." }
        require(saturation in 0f..1f) { "saturation should be in [0, 1] range." }
        require(lightness in 0f..1f) { "lightness should be in [0, 1] range." }
    }

    @Suppress("unused")
    fun toColor() = Color.hsl(hue, saturation, lightness)

    fun toHsv(): HsvColor {
        val value = (lightness + saturation * min(lightness, 1f - lightness))
            .coerceIn(0f, 1f)
        val saturation = when (value) {
            0f -> 0f
            else -> 2f * (1f - lightness / value)
        }.coerceIn(0f, 1f)

        return HsvColor(hue, saturation, value)
    }

    fun toRgb(): RgbColor {
        val (r, g, b) = Color.hsl(hue, saturation, lightness)

        return RgbColor(r, g, b)
    }

    companion object {
        private val hslCodeRegex = Regex(
            """[hH][sS][lL]\(\s*([+-]?[0-9]*\.*[0-9]+)\s*,\s*([+-]?[0-9]*\.*[0-9]+)%?\s*,\s*([+-]?[0-9]*\.*[0-9]+)%?\s*\)"""
        )

        fun parseHslCodeOrNull(hslCode: CharSequence): HslColor? {
            hslCodeRegex
                .matchEntire(hslCode.trim())
                ?.let { matchResult ->
                    val groupValues = matchResult.groupValues
                    val h = groupValues.getOrNull(1)?.toFloatOrNull()
                    val s = groupValues.getOrNull(2)?.toFloatOrNull()
                    val l = groupValues.getOrNull(3)?.toFloatOrNull()

                    if (h != null && s != null && l != null) {
                        return HslColor(
                            hue = AngleF.fromDegrees(h).getMinimalPositiveCoterminal().degrees,
                            saturation = (s * 0.01f).coerceIn(0f, 1f),
                            lightness = (l * 0.01f).coerceIn(0f, 1f),
                        )
                    }
                }
            return null
        }
    }
}

fun Color.toHslColor() = toHsl()
    .let { (h, s, l) -> HslColor(h, s, l) }