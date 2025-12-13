package com.sztorm.notecalendar.components.colorpicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color

interface ColorPickerState {
    var alpha: Float
    var hsl: HslColor
    var hsv: HsvColor
    var rgb: RgbColor
}

private class ColorPickerStateImpl : ColorPickerState {
    val alphaState: MutableFloatState
    val hslState: MutableState<HslColor>
    val hsvState: MutableState<HsvColor>
    val rgbState: MutableState<RgbColor>

    constructor(color: Color) {
        alphaState = mutableFloatStateOf(color.alpha)
        hslState = mutableStateOf(color.toHslColor())
        hsvState = mutableStateOf(color.toHsvColor())
        rgbState = mutableStateOf(color.toRgbColor())
    }

    @Suppress("unused")
    constructor(hsl: HslColor, alpha: Float = 1f) {
        require(alpha in 0f..1f) { "alpha should be in [0, 1] range." }

        alphaState = mutableFloatStateOf(alpha)
        hslState = mutableStateOf(hsl)
        hsvState = mutableStateOf(hsl.toHsv())
        rgbState = mutableStateOf(hsl.toRgb())
    }

    @Suppress("unused")
    constructor(hsv: HsvColor, alpha: Float = 1f) {
        require(alpha in 0f..1f) { "alpha should be in [0, 1] range." }

        alphaState = mutableFloatStateOf(alpha)
        hslState = mutableStateOf(hsv.toHsl())
        hsvState = mutableStateOf(hsv)
        rgbState = mutableStateOf(hsv.toRgb())
    }

    @Suppress("unused")
    constructor(rgb: RgbColor, alpha: Float = 1f) {
        require(alpha in 0f..1f) { "alpha should be in [0, 1] range." }

        alphaState = mutableFloatStateOf(alpha)
        hslState = mutableStateOf(rgb.toHsl())
        hsvState = mutableStateOf(rgb.toHsv())
        rgbState = mutableStateOf(rgb)
    }

    private constructor(alpha: Float, hsl: HslColor, hsv: HsvColor, rgb: RgbColor) {
        alphaState = mutableFloatStateOf(alpha)
        hslState = mutableStateOf(hsl)
        hsvState = mutableStateOf(hsv)
        rgbState = mutableStateOf(rgb)
    }

    override var alpha: Float
        get() = alphaState.floatValue
        set(value) {
            alphaState.floatValue = value
        }

    override var hsl: HslColor
        get() = hslState.value
        set(value) {
            hslState.value = value
            hsvState.value = value.toHsv()
            rgbState.value = value.toRgb()
        }

    override var hsv: HsvColor
        get() = hsvState.value
        set(value) {
            hslState.value = value.toHsl()
            hsvState.value = value
            rgbState.value = value.toRgb()
        }

    override var rgb: RgbColor
        get() = rgbState.value
        set(value) {
            hslState.value = value.toHsl()
            hsvState.value = value.toHsv()
            rgbState.value = value
        }

    companion object {
        fun Saver(): Saver<ColorPickerStateImpl, out Any> =
            Saver(
                save = {
                    listOf(
                        it.alpha,
                        it.hsl.hue, it.hsl.saturation, it.hsl.lightness,
                        it.hsv.hue, it.hsv.saturation, it.hsv.value,
                        it.rgb.red, it.rgb.green, it.rgb.blue,
                    )
                },
                restore = {
                    ColorPickerStateImpl(
                        alpha = it[0],
                        hsl = HslColor(it[1], it[2], it[3]),
                        hsv = HsvColor(it[4], it[5], it[6]),
                        rgb = RgbColor(it[7], it[8], it[9]),
                    )
                }
            )
    }
}

@Suppress("unused")
fun ColorPickerState(color: Color): ColorPickerState = ColorPickerStateImpl(color)

@Composable
fun rememberColorPickerState(color: Color): ColorPickerState =
    rememberSaveable(saver = ColorPickerStateImpl.Saver()) {
        ColorPickerStateImpl(color)
    }