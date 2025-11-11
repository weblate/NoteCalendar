package com.sztorm.notecalendar.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sztorm.mathkit.AngleF
import com.sztorm.mathkit.ComplexF
import com.sztorm.mathkit.Vector2F
import com.sztorm.mathkit.euclidean2d.Annulus
import com.sztorm.mathkit.euclidean2d.Circle
import com.sztorm.mathkit.euclidean2d.MutableAnnulus
import com.sztorm.mathkit.euclidean2d.MutableCircle
import com.sztorm.mathkit.euclidean2d.MutableRegularTriangle
import com.sztorm.mathkit.euclidean2d.MutableRoundedRectangle
import com.sztorm.mathkit.euclidean2d.MutableSquare
import com.sztorm.mathkit.euclidean2d.RegularTriangle
import com.sztorm.mathkit.euclidean2d.RoundedRectangle
import com.sztorm.mathkit.euclidean2d.Square
import com.sztorm.mathkit.inverseLerp
import com.sztorm.mathkit.lerp
import com.sztorm.notecalendar.lineTo
import com.sztorm.notecalendar.moveTo
import com.sztorm.notecalendar.toCanvasSpace
import com.sztorm.notecalendar.toHsl
import com.sztorm.notecalendar.toHsv
import com.sztorm.notecalendar.toOffset
import com.sztorm.notecalendar.toVector2F
import java.text.DecimalFormatSymbols
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

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
}

data class HsvColor(val hue: Float, val saturation: Float, val value: Float) {
    init {
        require(hue in 0f..360f) { "hue should be in [0, 360] range." }
        require(saturation in 0f..1f) { "saturation should be in [0, 1] range." }
        require(value in 0f..1f) { "value should be in [0, 1] range." }
    }

    @Suppress("unused")
    fun toColor() = Color.hsv(hue, saturation, value)

    fun toHsl(): HslColor {
        val lightness = (value * (1f - saturation * 0.5f)).coerceIn(0f, 1f)
        val saturation = when (lightness) {
            0f, 1f -> 0f
            else -> (value - lightness) / min(lightness, 1f - lightness)
        }.coerceIn(0f, 1f)

        return HslColor(hue, saturation, lightness)
    }

    fun toRgb(): RgbColor {
        val (r, g, b) = Color.hsv(hue, saturation, value)

        return RgbColor(r, g, b)
    }
}

data class RgbColor(val red: Float, val green: Float, val blue: Float) {
    init {
        require(red in 0f..360f) { "red should be in [0, 360] range." }
        require(green in 0f..1f) { "green should be in [0, 1] range." }
        require(blue in 0f..1f) { "blue should be in [0, 1] range." }
    }

    fun toColor() = Color(red, green, blue)

    fun toHsl(): HslColor = toColor().toHslColor()

    fun toHsv(): HsvColor = toColor().toHsvColor()
}

private fun Color.toHslColor(): HslColor {
    val (h, s, l) = toHsl()

    return HslColor(h, s, l)
}

private fun Color.toHsvColor(): HsvColor {
    val (h, s, v) = toHsv()

    return HsvColor(h, s, v)
}

private fun Color.toRgbColor() = RgbColor(red, green, blue)

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

data class ColorPickerColors(val c: Color)

private enum class CurrentAction {
    None,
    ChangingHue,
    ChangingSV,
    ChangingSL,
    ChangingColor,
    ChangingValue,
}

@Suppress("unused")
fun ColorPickerState(color: Color): ColorPickerState = ColorPickerStateImpl(color)

@Composable
fun rememberColorPickerState(color: Color): ColorPickerState =
    rememberSaveable(saver = ColorPickerStateImpl.Saver()) {
        ColorPickerStateImpl(color)
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPicker(
    state: ColorPickerState,
    modifier: Modifier = Modifier,
    colors: ColorPickerColors = ColorPickerColors(c = Color.Unspecified)
    // TODO: colorPickerValues or smth like that for labels like Red, Blue, Green, Hue, ..
) {
    val navController = rememberNavController()
    val initialTab = TabType.ColorCodes
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(initialTab.ordinal) }

    Column(modifier = modifier.fillMaxWidth()) {
        when (TabType.entries[selectedTabIndex]) {
            TabType.ColorCodes -> HsvColorPicker(state)
            TabType.Rgb -> RgbColorPicker(state)
            TabType.Hsv -> HsvColorPicker(state)
            TabType.Hsl -> HslColorPicker(state)
        }
        Spacer(modifier = Modifier.height(8.dp))
        PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
            TabType.entries.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = {
                        navController.navigate(tab.route)
                        selectedTabIndex = index
                    },
                    text = {
                        Text(
                            text = tab.label,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }
        NavHost(
            navController = navController,
            startDestination = initialTab.route
        ) {
            composable(TabType.ColorCodes.route) { ColorCodesTab(state) }
            composable(TabType.Rgb.route) { RgbTab(state) }
            composable(TabType.Hsv.route) { HsvTab(state) }
            composable(TabType.Hsl.route) { HslTab(state) }
        }
    }
}

private fun triangleAreaDoubled(a: Vector2F, b: Vector2F, c: Vector2F) =
    abs(a.x * b.y - b.x * a.y + b.x * c.y - c.x * b.y + c.x * a.y - a.x * c.y)

private fun triagleHeightOfBaseAB(a: Vector2F, b: Vector2F, c: Vector2F): Float {
    val areaDoubled = triangleAreaDoubled(a, b, c)
    val baseLength = a.distanceTo(b)

    return areaDoubled / baseLength
}

private fun Annulus.hueFrom(position: Vector2F) = (position - center)
    .toComplexF()
    .normalizedOrElse { ComplexF.ONE }
    .phaseAngle
    .getMinimalPositiveCoterminal()
    .degrees
    .coerceIn(0f, 360f)

private fun RegularTriangle.colorSaturationFrom(position: Vector2F): Float {
    val abpAreaDoubled = triangleAreaDoubled(pointA, pointB, position)
    val cbpAreaDoubled = triangleAreaDoubled(pointC, pointB, position)
    val abcpAreaDoubled = abpAreaDoubled + cbpAreaDoubled

    return when {
        abcpAreaDoubled < 0.1f -> 0.5f
        else -> {
            val result = (cbpAreaDoubled / abcpAreaDoubled).coerceIn(0f, 1f)

            when {
                result < 0.01f -> 0f
                result > 0.99f -> 1f
                else -> result
            }
        }
    }
}

private fun RegularTriangle.colorValueFrom(position: Vector2F): Float {
    val triangleHeight = circumradius + inradius
    val height = triagleHeightOfBaseAB(pointA, pointC, position)
    val result = (1f - height / triangleHeight).coerceIn(0f, 1f)

    return when {
        result < 0.01f -> 0f
        result > 0.99f -> 1f
        else -> result
    }
}

private fun RegularTriangle.colorPosition(saturation: Float, value: Float): Vector2F =
    lerp(pointB, lerp(pointC, pointA, saturation), value)

private fun Square.colorSaturationFrom(position: Vector2F): Float {
    val abpHeight = triagleHeightOfBaseAB(pointA, pointB, position)

    return (1f - abpHeight / sideLength).coerceIn(0f, 1f)
}

private fun Square.colorLightnessFrom(position: Vector2F): Float {
    val apdHeight = triagleHeightOfBaseAB(pointA, pointD, position)

    return (apdHeight / sideLength).coerceIn(0f, 1f)
}

private fun Square.colorPosition(saturation: Float, lightness: Float) = lerp(
    lerp(pointD, pointC, lightness),
    lerp(pointA, pointB, lightness),
    saturation
)

private fun Circle.hueFrom(position: Vector2F): Float = (position - center)
    .toComplexF()
    .normalizedOrElse { ComplexF.ONE }
    .phaseAngle
    .getMinimalPositiveCoterminal()
    .degrees
    .coerceIn(0f, 360f)

private fun Circle.saturationFrom(position: Vector2F) =
    (center.distanceTo(position) / radius).coerceIn(0f, 1f)

private fun Circle.colorPosition(hue: Float, saturation: Float) = ComplexF.fromPolar(
    magnitude = saturation * radius,
    phase = AngleF.fromDegrees(hue).radians
).toVector2F() + center

private fun RoundedRectangle.colorValueFrom(position: Vector2F) =
    inverseLerp(cornerCenterB.x, cornerCenterA.x, position.x).coerceIn(0f, 1f)

private fun RoundedRectangle.valuePosition(value: Float) =
    lerp(cornerCenterB, cornerCenterA, value)

private fun DrawScope.drawHueRing(ring: Annulus, canvasSize: Size) {
    val radius = (ring.innerRadius + ring.outerRadius) * 0.5f
    val stroke = Stroke(width = ring.width)
    val brush = Brush.sweepGradient(
        listOf(
            Color.Red,
            Color.Magenta,
            Color.Blue,
            Color.Cyan,
            Color.Green,
            Color.Yellow,
            Color.Red,
        ),
        ring.center.toCanvasSpace(canvasSize).toOffset()
    )
    drawCircle(
        brush = brush,
        radius = radius,
        style = stroke,
        center = ring.center.toCanvasSpace(canvasSize).toOffset()
    )
}

private fun DrawScope.drawHsvTriangle(triangle: RegularTriangle, canvasSize: Size, hue: Float) {
    val pointA = triangle.pointA.toCanvasSpace(canvasSize)
    val pointB = triangle.pointB.toCanvasSpace(canvasSize)
    val pointC = triangle.pointC.toCanvasSpace(canvasSize)
    val pointAOpposite = (pointB + pointC) * 0.5f
    val pointBOpposite = (pointA + pointC) * 0.5f
    val hueColor = Color.hsv(hue, saturation = 1f, value = 1f)
    val hueToTransparentGradient = Brush.linearGradient(
        listOf(hueColor, hueColor.copy(alpha = 0f)),
        start = pointA.toOffset(),
        end = pointAOpposite.toOffset()
    )
    val blackToTransparentGradient = Brush.linearGradient(
        listOf(Color.Black, Color.Transparent),
        start = pointB.toOffset(),
        end = pointBOpposite.toOffset()
    )
    val trianglePath = Path().apply {
        moveTo(pointA)
        lineTo(pointB)
        lineTo(pointC)
        close()
    }
    val brush = SolidColor(Color.White)
        .let { Brush.composite(it, hueToTransparentGradient, BlendMode.SrcOver) }
        .let { Brush.composite(it, blackToTransparentGradient, BlendMode.SrcOver) }

    drawPath(trianglePath, brush)
}

private fun DrawScope.drawHslSquare(square: Square, canvasSize: Size, hue: Float) {
    val pointA = square.pointA.toCanvasSpace(canvasSize)
    val pointB = square.pointB.toCanvasSpace(canvasSize)
    val pointC = square.pointC.toCanvasSpace(canvasSize)
    val pointD = square.pointD.toCanvasSpace(canvasSize)
    val hueColor = Color.hsv(hue, saturation = 1f, value = 1f)
    val hueToGrayGradient = Brush.linearGradient(
        listOf(hueColor, Color.Gray),
        start = ((pointA + pointB) * 0.5f).toOffset(),
        end = ((pointC + pointD) * 0.5f).toOffset()
    )
    val whiteToBlackGradient = Brush.linearGradient(
        listOf(Color.White, Color.Transparent, Color.Black),
        start = ((pointB + pointC) * 0.5f).toOffset(),
        end = ((pointD + pointA) * 0.5f).toOffset(),
    )
    val squarePath = Path().apply {
        moveTo(pointD)
        lineTo(pointA)
        lineTo(pointB)
        lineTo(pointC)
        close()
    }
    val brush = hueToGrayGradient
        .let { Brush.composite(it, whiteToBlackGradient, BlendMode.SrcOver) }

    drawPath(squarePath, brush)
}

private fun DrawScope.drawRgbCircle(circle: Circle, canvasSize: Size) {
    val hueGradient = Brush.sweepGradient(
        colors = listOf(
            Color.Red,
            Color.Magenta,
            Color.Blue,
            Color.Cyan,
            Color.Green,
            Color.Yellow,
            Color.Red,
        ),
        center = circle.center.toCanvasSpace(canvasSize).toOffset()
    )
    val whiteGradient = Brush.radialGradient(
        colors = listOf(Color.White, Color.Transparent),
        center = circle.center.toCanvasSpace(canvasSize).toOffset()
    )
    val brush = hueGradient
        .let { Brush.composite(it, whiteGradient, BlendMode.SrcOver) }
    drawCircle(
        brush = brush,
        radius = circle.radius,
        center = circle.center.toCanvasSpace(canvasSize).toOffset()
    )
}

private fun DrawScope.drawColorValueRectangle(
    rectangle: RoundedRectangle, canvasSize: Size, state: ColorPickerState
) {
    val sameColorFraction = rectangle.cornerRadius / rectangle.width
    val valueColor = state.hsv.copy(value = 1f).toColor()
    val valueGradient = Brush.horizontalGradient(
        0f to Color.Black,
        sameColorFraction to Color.Black,
        1f - sameColorFraction to valueColor,
        1f to valueColor
    )
    drawRoundRect(
        brush = valueGradient,
        topLeft = Vector2F(rectangle.pointC.x, rectangle.pointB.y)
            .toCanvasSpace(canvasSize).toOffset(),
        size = Size(rectangle.width, rectangle.height),
        cornerRadius = rectangle.cornerRadius.let { CornerRadius(it, it) }
    )
}

private fun DrawScope.drawCircleSelector(
    position: Vector2F, radius: Float, canvasSize: Size, state: ColorPickerState
) {
    val t = (lerp(1f, 0f, state.hsv.value) +
        lerp(0f, 1f, state.hsv.saturation))
        .coerceIn(0f, 1f)

    drawCircle(
        color = Color.hsv(0f, 0f, if (t > 0.5) 1f else 0f),
        radius = radius,
        center = position.toCanvasSpace(canvasSize).toOffset(),
        style = Stroke(width = 2.dp.toPx())
    )
}

private fun DrawScope.drawColorSelector(
    position: Vector2F, canvasSize: Size, state: ColorPickerState
) {
    val radius = size.width * 0.04f
    val path = Path().apply {
        moveTo(position.toCanvasSpace(canvasSize))
        arcTo(
            rect = Rect(
                center = (position + Vector2F(0f, radius * 1.5f))
                    .toCanvasSpace(canvasSize).toOffset(),
                radius = radius
            ),
            startAngleDegrees = 30f,
            sweepAngleDegrees = -240f,
            forceMoveTo = false
        )
        close()
    }
    val t = (lerp(1f, 0f, state.hsv.value) +
        lerp(0f, 1f, state.hsv.saturation))
        .coerceIn(0f, 1f)

    drawPath(
        path = path,
        color = state.rgb.toColor()
    )
    drawPath(
        path = path,
        color = Color.hsv(0f, 0f, if (t > 0.5) 1f else 0f),
        style = Stroke(width = 2.dp.toPx())
    )
}

@Composable
private fun HsvColorPicker(state: ColorPickerState) {
    var currentAction by remember { mutableStateOf(CurrentAction.None) }
    val hueRing = remember {
        MutableAnnulus(
            center = Vector2F.ZERO,
            orientation = ComplexF.ONE,
            innerRadius = 1f,
            outerRadius = 2f,
        )
    }
    val hsvTriangle = remember {
        MutableRegularTriangle(
            center = Vector2F.ZERO,
            orientation = AngleF.fromDegrees(-90f).toComplexF(),
            sideLength = 100f
        )
    }
    var canvasSize by remember { mutableStateOf(Size(1f, 1f)) }
    val onInputStart = { startPosition: Offset ->
        val position = startPosition
            .toVector2F()
            .toCanvasSpace(canvasSize)

        when (position) {
            in hueRing -> {
                currentAction = CurrentAction.ChangingHue
                state.hsv = state.hsv.copy(hue = hueRing.hueFrom(position))
            }

            in hsvTriangle -> {
                currentAction = CurrentAction.ChangingSV
                state.hsv = state.hsv.copy(
                    saturation = hsvTriangle.colorSaturationFrom(position),
                    value = hsvTriangle.colorValueFrom(position)
                )
            }
        }
    }
    val onInputUpdate = { change: PointerInputChange, _: Offset ->
        val position = change.position
            .toVector2F()
            .toCanvasSpace(canvasSize)

        when (currentAction) {
            CurrentAction.ChangingHue -> {
                state.hsv = state.hsv.copy(hue = hueRing.hueFrom(position))
            }

            CurrentAction.ChangingSV -> {
                val selectorPosition = hsvTriangle.closestPointTo(position)
                state.hsv = state.hsv.copy(
                    saturation = hsvTriangle.colorSaturationFrom(selectorPosition),
                    value = hsvTriangle.colorValueFrom(selectorPosition)
                )
            }

            else -> {}
        }
    }
    val onInputEnd = {
        currentAction = CurrentAction.None
    }
    Row(modifier = Modifier.fillMaxWidth())
    {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1F)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onInputStart(it); },
                        onPress = { onInputStart(it); }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = onInputStart,
                        onDragEnd = onInputEnd,
                        onDragCancel = onInputEnd,
                        onDrag = onInputUpdate
                    )
                }
        ) {
            canvasSize = size
            hueRing.set(
                center = center.toVector2F().toCanvasSpace(canvasSize),
                innerRadius = size.width * 0.38f,
                outerRadius = size.width * 0.48f,
            )
            hsvTriangle.set(
                center = center.toVector2F().toCanvasSpace(canvasSize),
                orientation = AngleF.fromDegrees(state.hsv.hue - 90f).toComplexF(),
                sideLength = hueRing.innerRadius * sqrt(3f)
            )
            drawHsvTriangle(
                triangle = hsvTriangle,
                canvasSize = size,
                hue = state.hsv.hue
            )
            drawHueRing(
                ring = hueRing,
                canvasSize = canvasSize
            )
            drawColorSelector(
                position = hsvTriangle.colorPosition(state.hsv.saturation, state.hsv.value),
                canvasSize = size,
                state = state
            )
        }
    }
}

@Composable
private fun HslColorPicker(state: ColorPickerState) {
    var currentAction by remember { mutableStateOf(CurrentAction.None) }
    val hueRing = remember {
        MutableAnnulus(
            center = Vector2F.ZERO,
            orientation = ComplexF.ONE,
            innerRadius = 1f,
            outerRadius = 2f,
        )
    }
    val hslSquare = remember {
        MutableSquare(
            center = Vector2F.ZERO,
            orientation = AngleF.fromDegrees(-90f).toComplexF(),
            sideLength = 100f
        )
    }
    var canvasSize by remember { mutableStateOf(Size(1f, 1f)) }
    val onInputStart = { startPosition: Offset ->
        val position = startPosition
            .toVector2F()
            .toCanvasSpace(canvasSize)

        when (position) {
            in hueRing -> {
                currentAction = CurrentAction.ChangingHue
                state.hsl = state.hsl.copy(hue = hueRing.hueFrom(position))
            }

            in hslSquare -> {
                currentAction = CurrentAction.ChangingSL
                state.hsl = state.hsl.copy(
                    saturation = hslSquare.colorSaturationFrom(position),
                    lightness = hslSquare.colorLightnessFrom(position)
                )
            }
        }
    }
    val onInputUpdate = { change: PointerInputChange, _: Offset ->
        val position = change.position
            .toVector2F()
            .toCanvasSpace(canvasSize)

        when (currentAction) {
            CurrentAction.ChangingHue -> {
                state.hsl = state.hsl.copy(hue = hueRing.hueFrom(position))
            }

            CurrentAction.ChangingSL -> {
                val selectorPosition = hslSquare.closestPointTo(position)
                state.hsl = state.hsl.copy(
                    saturation = hslSquare.colorSaturationFrom(selectorPosition),
                    lightness = hslSquare.colorLightnessFrom(selectorPosition)
                )
            }

            else -> {}
        }
    }
    val onInputEnd = {
        currentAction = CurrentAction.None
    }
    Row(modifier = Modifier.fillMaxWidth())
    {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1F)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onInputStart(it); },
                        onPress = { onInputStart(it); }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = onInputStart,
                        onDragEnd = onInputEnd,
                        onDragCancel = onInputEnd,
                        onDrag = onInputUpdate
                    )
                }
        ) {
            canvasSize = size
            hueRing.set(
                center = center.toVector2F().toCanvasSpace(canvasSize),
                innerRadius = size.width * 0.38f,
                outerRadius = size.width * 0.48f,
            )
            hslSquare.set(
                center = center.toVector2F().toCanvasSpace(canvasSize),
                orientation = AngleF.fromDegrees(state.hsv.hue - 90f).toComplexF(),
                sideLength = hueRing.innerRadius * sqrt(2f)
            )
            drawHslSquare(
                square = hslSquare,
                canvasSize = size,
                hue = state.hsv.hue
            )
            drawHueRing(
                ring = hueRing,
                canvasSize = canvasSize
            )
            drawColorSelector(
                position = hslSquare.colorPosition(state.hsl.saturation, state.hsl.lightness),
                canvasSize = size,
                state = state
            )
        }
    }
}

@Composable
private fun RgbColorPicker(state: ColorPickerState) {
    var currentAction by remember { mutableStateOf(CurrentAction.None) }
    val rgbCircle = remember {
        MutableCircle(
            center = Vector2F.ZERO,
            orientation = ComplexF.ONE,
            radius = 1f,
        )
    }
    val valueRectangle = remember {
        MutableRoundedRectangle(
            center = Vector2F.ZERO,
            orientation = ComplexF.ONE,
            width = 1f,
            height = 1f,
            cornerRadius = 0f
        )
    }
    var canvasSize by remember { mutableStateOf(Size(1f, 1f)) }
    val onInputStart = { startPosition: Offset ->
        val position = startPosition
            .toVector2F()
            .toCanvasSpace(canvasSize)

        when (position) {
            in rgbCircle -> {
                currentAction = CurrentAction.ChangingColor
                state.hsv = state.hsv.copy(
                    hue = rgbCircle.hueFrom(position),
                    saturation = rgbCircle.saturationFrom(position)
                )
            }

            in valueRectangle -> {
                currentAction = CurrentAction.ChangingValue
                state.hsv = state.hsv.copy(value = valueRectangle.colorValueFrom(position))
            }
        }
    }
    val onInputUpdate = { change: PointerInputChange, _: Offset ->
        val position = change.position
            .toVector2F()
            .toCanvasSpace(canvasSize)

        when (currentAction) {
            CurrentAction.ChangingColor -> {
                val selectorPosition = rgbCircle.closestPointTo(position)
                state.hsv = state.hsv.copy(
                    hue = rgbCircle.hueFrom(selectorPosition),
                    saturation = rgbCircle.saturationFrom(selectorPosition)
                )
            }

            CurrentAction.ChangingValue -> {
                val selectorPosition = valueRectangle.closestPointTo(position)
                state.hsv = state.hsv.copy(value = valueRectangle.colorValueFrom(selectorPosition))
            }

            else -> {}
        }
    }
    val onInputEnd = {
        currentAction = CurrentAction.None
    }
    Row(modifier = Modifier.fillMaxWidth())
    {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1F)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onInputStart(it); },
                        onPress = { onInputStart(it); }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = onInputStart,
                        onDragEnd = onInputEnd,
                        onDragCancel = onInputEnd,
                        onDrag = onInputUpdate
                    )
                }
        ) {
            canvasSize = size
            rgbCircle.set(
                center = Vector2F(size.width * 0.5f, size.height * 0.45f)
                    .toCanvasSpace(canvasSize),
                radius = size.width * 0.43f,
            )
            valueRectangle.set(
                center = Vector2F(size.width * 0.5f, size.height * 0.95f)
                    .toCanvasSpace(canvasSize),
                width = size.width * 0.9f,
                height = size.height * 0.075f,
                cornerRadius = size.height * 0.075f * 0.499f
            )
            drawRgbCircle(
                circle = rgbCircle,
                canvasSize = canvasSize
            )
            drawColorValueRectangle(
                rectangle = valueRectangle,
                canvasSize = canvasSize,
                state = state
            )
            drawCircleSelector(
                position = valueRectangle.valuePosition(state.hsv.value),
                radius = valueRectangle.cornerRadius,
                canvasSize = canvasSize,
                state = state
            )
            drawColorSelector(
                position = rgbCircle.colorPosition(state.hsv.hue, state.hsv.saturation),
                canvasSize = canvasSize,
                state = state
            )
        }
    }
}

private enum class TabType(val route: String, val label: String) {
    ColorCodes(route = "colorcodes", label = "#"),
    Rgb(route = "rgb", label = "RGB"),
    Hsv(route = "hsv", label = "HSV"),
    Hsl(route = "hsl", label = "HSL"),
}

@Composable
private fun ColorCodesTab(state: ColorPickerState) {
    // TODO: hexcode, RGB, HSV, HSL
    // copy, paste on each
}

private fun Float.formatUpToTwoDecimalPlaces(): String {
    val locale = Locale.current.platformLocale
    val result = "%.2f".format(locale, this)
    val separator = DecimalFormatSymbols(locale).decimalSeparator

    return when {
        result.endsWith(separator + "00") -> result.dropLast(3)
        result.endsWith(separator + "0") -> result.dropLast(2)
        result.endsWith('0') -> result.dropLast(1)
        else -> result
    }
}

@Composable
private fun ColorComponentSlider(
    text: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    textColor: Color = Color.Unspecified,
) {
    var textState by remember(value) {
        mutableStateOf(value.formatUpToTwoDecimalPlaces())
    }
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = text,
                color = textColor
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Slider(
                    value = value,
                    onValueChange = onValueChange,
                    valueRange = valueRange
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.width(with(LocalDensity.current) {
                85.sp.toDp()
            })) {
                val focusManager = LocalFocusManager.current

                OutlinedTextField(
                    value = textState,
                    onValueChange = { textState = it.substring(0, min(6, it.length)) },
                    keyboardActions = KeyboardActions(
                        onDone = {
                            textState.toFloatOrNull().let {
                                when (it) {
                                    null -> textState = value.formatUpToTwoDecimalPlaces()
                                    else -> onValueChange(it.coerceIn(valueRange))
                                }
                            }
                            focusManager.clearFocus()
                        }
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Decimal,
                    ),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                    modifier = Modifier.onFocusChanged {
                        textState = value.formatUpToTwoDecimalPlaces()
                    }
                )
            }
        }
    }
}

@Composable
private fun RgbTab(state: ColorPickerState) {
    // TODO: Switch for 0..255 and 0..1
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row {
            ColorComponentSlider(
                text = "Red", // TODO move to strings.xml
                value = state.rgb.red,
                valueRange = 0f..1f,
                onValueChange = { state.rgb = state.rgb.copy(red = it) },
                textColor = Color.Black
            )
        }
        Row {
            ColorComponentSlider(
                text = "Green", // TODO move to strings.xml
                value = state.rgb.green,
                valueRange = 0f..1f,
                onValueChange = { state.rgb = state.rgb.copy(green = it) },
                textColor = Color.Black
            )
        }
        Row {
            ColorComponentSlider(
                text = "Blue", // TODO move to strings.xml
                value = state.rgb.blue,
                valueRange = 0f..1f,
                onValueChange = { state.rgb = state.rgb.copy(blue = it) },
                textColor = Color.Black
            )
        }
    }
}

@Composable
private fun HsvTab(state: ColorPickerState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row {
            ColorComponentSlider(
                text = "Hue", // TODO move to strings.xml
                value = state.hsv.hue,
                valueRange = 0f..360f,
                onValueChange = { state.hsv = state.hsv.copy(hue = it) },
                textColor = Color.Black
            )
        }
        Row {
            ColorComponentSlider(
                text = "Saturation", // TODO move to strings.xml
                value = state.hsv.saturation,
                valueRange = 0f..1f,
                onValueChange = { state.hsv = state.hsv.copy(saturation = it) },
                textColor = Color.Black
            )
        }
        Row {
            ColorComponentSlider(
                text = "Value", // TODO move to strings.xml
                value = state.hsv.value,
                valueRange = 0f..1f,
                onValueChange = { state.hsv = state.hsv.copy(value = it) },
                textColor = Color.Black
            )
        }
    }
}

@Composable
private fun HslTab(state: ColorPickerState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row {
            ColorComponentSlider(
                text = "Hue", // TODO move to strings.xml
                value = state.hsl.hue,
                valueRange = 0f..360f,
                onValueChange = { state.hsl = state.hsl.copy(hue = it) },
                textColor = Color.Black
            )
        }
        Row {
            ColorComponentSlider(
                text = "Saturation", // TODO move to strings.xml
                value = state.hsl.saturation,
                valueRange = 0f..1f,
                onValueChange = { state.hsl = state.hsl.copy(saturation = it) },
                textColor = Color.Black
            )
        }
        Row {
            ColorComponentSlider(
                text = "Lightness", // TODO move to strings.xml
                value = state.hsl.lightness,
                valueRange = 0f..1f,
                onValueChange = { state.hsl = state.hsl.copy(lightness = it) },
                textColor = Color.Black
            )
        }
    }
}