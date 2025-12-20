package com.sztorm.notecalendar.components.colorpicker

import com.sztorm.mathkit.ColorRGBA32
import com.sztorm.notecalendar.toColor
import kotlin.String

enum class ColorPickerType {
    HslSquare,
    HsvTriangle,
    RgbCircle
}

interface ColorCodeType {
    fun toString(state: ColorPickerState): String
    fun parseOrNull(code: CharSequence): Any?
    fun onPaste(parsedCode: Any, state: ColorPickerState): Unit?

    object Hex : ColorCodeType {
        override fun toString(state: ColorPickerState) = state.toHexCodeFormat(false)
        override fun parseOrNull(code: CharSequence) = parseHexCodeOrNull(code)
        override fun onPaste(parsedCode: Any, state: ColorPickerState) =
            (parsedCode as ColorRGBA32).let {
                val color = it.toColor()
                state.rgb = color.toRgbColor()
                state.alpha = 1f
            }
    }

    object HexAlpha : ColorCodeType {
        override fun toString(state: ColorPickerState) = state.toHexCodeFormat(true)
        override fun parseOrNull(code: CharSequence) = parseHexCodeOrNull(code)
        override fun onPaste(parsedCode: Any, state: ColorPickerState) =
            (parsedCode as ColorRGBA32).let {
                val color = it.toColor()
                state.rgb = color.toRgbColor()
                state.alpha = color.alpha
            }
    }

    object Hsl : ColorCodeType {
        override fun toString(state: ColorPickerState) = state.toHslFormat(false)
        override fun parseOrNull(code: CharSequence) = parseHslCodeOrNull(code)
        override fun onPaste(parsedCode: Any, state: ColorPickerState) =
            (parsedCode as HslaColor).let {
                state.hsl = it.hsl
                state.alpha = 1f
            }
    }

    object Hsla : ColorCodeType {
        override fun toString(state: ColorPickerState) = state.toHslFormat(true)
        override fun parseOrNull(code: CharSequence) = parseHslCodeOrNull(code)
        override fun onPaste(parsedCode: Any, state: ColorPickerState) =
            (parsedCode as HslaColor).let {
                state.hsl = it.hsl
                state.alpha = it.alpha
            }
    }

    object Hsv : ColorCodeType {
        override fun toString(state: ColorPickerState) = state.toHsvFormat(false)
        override fun parseOrNull(code: CharSequence) = parseHsvCodeOrNull(code)
        override fun onPaste(parsedCode: Any, state: ColorPickerState) =
            (parsedCode as HsvaColor).let {
                state.hsv = it.hsv
                state.alpha = 1f
            }
    }

    object Hsva : ColorCodeType {
        override fun toString(state: ColorPickerState) = state.toHsvFormat(true)
        override fun parseOrNull(code: CharSequence) = parseHsvCodeOrNull(code)
        override fun onPaste(parsedCode: Any, state: ColorPickerState) =
            (parsedCode as HsvaColor).let {
                state.hsv = it.hsv
                state.alpha = it.alpha
            }
    }

    object Rgb : ColorCodeType {
        override fun toString(state: ColorPickerState) = state.toRgbFormat(false)
        override fun parseOrNull(code: CharSequence) = parseRgbCodeOrNull(code)
        override fun onPaste(parsedCode: Any, state: ColorPickerState) =
            (parsedCode as RgbaColor).let {
                state.rgb = it.rgb
                state.alpha = 1f
            }
    }

    object Rgba : ColorCodeType {
        override fun toString(state: ColorPickerState) = state.toRgbFormat(true)
        override fun parseOrNull(code: CharSequence) = parseRgbCodeOrNull(code)
        override fun onPaste(parsedCode: Any, state: ColorPickerState) =
            (parsedCode as RgbaColor).let {
                state.rgb = it.rgb
                state.alpha = it.alpha
            }
    }
}

sealed class ColorPickerTab(
    val pickerType: ColorPickerType,
    val supportsAlphaPicking: Boolean
) {
    class ColorCodes(
        pickerType: ColorPickerType = ColorPickerType.RgbCircle,
        supportsAlphaPicking: Boolean = false,
        val codes: List<ColorCodeType> = listOf(
            ColorCodeType.Hex,
            ColorCodeType.Rgb,
            ColorCodeType.Hsl,
            ColorCodeType.Hsv,
        )
    ) : ColorPickerTab(pickerType, supportsAlphaPicking)

    class Hsl(
        pickerType: ColorPickerType = ColorPickerType.HslSquare,
        supportsAlphaPicking: Boolean = false
    ) : ColorPickerTab(pickerType, supportsAlphaPicking)

    class Hsv(
        pickerType: ColorPickerType = ColorPickerType.HsvTriangle,
        supportsAlphaPicking: Boolean = false
    ) : ColorPickerTab(pickerType, supportsAlphaPicking)

    class Rgb(
        pickerType: ColorPickerType = ColorPickerType.RgbCircle,
        supportsAlphaPicking: Boolean = false
    ) : ColorPickerTab(pickerType, supportsAlphaPicking)
}

data class ColorPickerProperties(
    val tabs: List<ColorPickerTab> = listOf(
        ColorPickerTab.ColorCodes(
            supportsAlphaPicking = true,
            pickerType = ColorPickerType.HsvTriangle,
            codes = listOf(
                ColorCodeType.Hex,
                ColorCodeType.Rgb,
                ColorCodeType.Hsl,
                ColorCodeType.Hsv,
                ColorCodeType.HexAlpha,
                ColorCodeType.Rgba,
                ColorCodeType.Hsla,
                ColorCodeType.Hsva,
            )
        ),
        ColorPickerTab.Rgb(supportsAlphaPicking = true),
        ColorPickerTab.Hsv(supportsAlphaPicking = true),
        ColorPickerTab.Hsl(supportsAlphaPicking = true),
    ),
    val texts: ColorPickerTexts = ColorPickerTexts.english()
)