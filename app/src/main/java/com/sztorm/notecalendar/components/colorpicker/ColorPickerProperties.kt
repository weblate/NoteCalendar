package com.sztorm.notecalendar.components.colorpicker

import kotlin.String

data class ColorPickerTexts(
    val alpha: String,
    val red: String,
    val green: String,
    val blue: String,
    val hslHue: String,
    val hslSaturation: String,
    val hslLightness: String,
    val hsvHue: String,
    val hsvSaturation: String,
    val hsvValue: String,
) {
    companion object {
        fun english() = ColorPickerTexts(
            alpha = "Alpha",
            red = "Red",
            green = "Green",
            blue = "Blue",
            hslHue = "Hue",
            hslSaturation = "Saturation",
            hslLightness = "Lightness",
            hsvHue = "Hue",
            hsvSaturation = "Saturation",
            hsvValue = "Value",
        )
    }
}

enum class ColorPickerType {
    HslSquare,
    HsvTriangle,
    RgbCircle
}

sealed class ColorPickerTab(val text: String, val supportsAlphaPicking: Boolean) {
    class ColorCodes(
        val pickerType: ColorPickerType,
        text: String = "#",
        supportsAlphaPicking: Boolean = false
    ) : ColorPickerTab(text, supportsAlphaPicking)

    class Hsl(
        text: String = "HSL", supportsAlphaPicking: Boolean = false
    ) : ColorPickerTab(text, supportsAlphaPicking)

    class Hsv(
        text: String = "HSV", supportsAlphaPicking: Boolean = false
    ) : ColorPickerTab(text, supportsAlphaPicking)

    class Rgb(
        text: String = "RGB", supportsAlphaPicking: Boolean = false
    ) : ColorPickerTab(text, supportsAlphaPicking)
}

data class ColorPickerProperties(
    val tabs: List<ColorPickerTab> = listOf(
        ColorPickerTab.ColorCodes(
            pickerType = ColorPickerType.HsvTriangle,
            supportsAlphaPicking = false
        ),
        ColorPickerTab.Rgb(supportsAlphaPicking = false),
        ColorPickerTab.Hsv(supportsAlphaPicking = false),
        ColorPickerTab.Hsl(supportsAlphaPicking = false),
    ),
    val texts: ColorPickerTexts = ColorPickerTexts.english()
)