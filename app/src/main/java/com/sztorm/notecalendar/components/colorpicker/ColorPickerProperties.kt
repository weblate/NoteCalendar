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

sealed class ColorPickerTab(val text: String) {
    class ColorCodes(text: String = "#", val pickerType: ColorPickerType) : ColorPickerTab(text)
    class Hsl(text: String = "HSL") : ColorPickerTab(text)
    class Hsv(text: String = "HSV") : ColorPickerTab(text)
    class Rgb(text: String = "RGB") : ColorPickerTab(text)
}

data class ColorPickerProperties(
    val tabs: List<ColorPickerTab> = listOf(
        ColorPickerTab.ColorCodes(pickerType = ColorPickerType.HsvTriangle),
        ColorPickerTab.Rgb(),
        ColorPickerTab.Hsv(),
        ColorPickerTab.Hsl(),
    ),
    val texts: ColorPickerTexts = ColorPickerTexts.english()
)