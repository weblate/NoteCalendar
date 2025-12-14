package com.sztorm.notecalendar.components.colorpicker

import android.content.ClipData
import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults.outlinedIconButtonColors
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sztorm.mathkit.AngleF
import com.sztorm.mathkit.ColorRGBA32
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
import com.sztorm.notecalendar.R
import com.sztorm.notecalendar.itemsSequence
import com.sztorm.notecalendar.lineTo
import com.sztorm.notecalendar.moveTo
import com.sztorm.notecalendar.toCanvasSpace
import com.sztorm.notecalendar.toColor
import com.sztorm.notecalendar.toColorRGBA32
import com.sztorm.notecalendar.toOffset
import com.sztorm.notecalendar.toVector2F
import kotlinx.coroutines.launch
import java.text.DecimalFormatSymbols
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

fun ColorRGBA32.Companion.parseHexCodeOrNull(hexCode: CharSequence): ColorRGBA32? {
    if (hexCode.length != 7 ||
        hexCode[0] != '#' ||
        hexCode
            .drop(1)
            .all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
            .not()
    ) {
        return null
    }
    val r = ((hexCode[1].digitToInt(16) shl 4) + hexCode[2].digitToInt(16))
        .toUByte()
    val g = ((hexCode[3].digitToInt(16) shl 4) + hexCode[4].digitToInt(16))
        .toUByte()
    val b = ((hexCode[5].digitToInt(16) shl 4) + hexCode[6].digitToInt(16))
        .toUByte()

    return ColorRGBA32(r, g, b, 0xffu)
}

private fun brushCompat(
    vararg brushBlendModePairs: Pair<Brush, BlendMode>,
    drawFunction: (Pair<Brush, BlendMode>) -> Unit
) = when {
    brushBlendModePairs.isEmpty() -> {}

    Build.VERSION.SDK_INT >= 29 -> {
        var (finalBrush, initialBlendMode) = brushBlendModePairs.first()

        for (i in 1 until brushBlendModePairs.size) {
            val (brush, blendMode) = brushBlendModePairs[i]
            finalBrush = finalBrush
                .let { Brush.composite(it, brush, blendMode) }
        }
        drawFunction(Pair(finalBrush, initialBlendMode))
    }

    else -> brushBlendModePairs.forEach {
        drawFunction(it)
    }
}

private val ColorPickerTab.route
    get() = when (this) {
        is ColorPickerTab.ColorCodes -> "colorcodes"
        is ColorPickerTab.Hsl -> "hsl"
        is ColorPickerTab.Hsv -> "hsv"
        is ColorPickerTab.Rgb -> "rgb"
    }

private enum class CurrentAction {
    None,
    ChangingHue,
    ChangingSV,
    ChangingSL,
    ChangingColor,
    ChangingValue,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPicker(
    state: ColorPickerState,
    modifier: Modifier = Modifier,
    colors: ColorPickerColors = ColorPickerDefaults.colors(),
    properties: ColorPickerProperties = ColorPickerProperties()
) {
    val navController = rememberNavController()
    val initialTab = properties.tabs.first()
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = colors.backgroundColor)
    ) {
        when (val selectedTab = properties.tabs[selectedTabIndex]) {
            is ColorPickerTab.ColorCodes -> when (selectedTab.pickerType) {
                ColorPickerType.HslSquare -> HslColorPicker(state)
                ColorPickerType.HsvTriangle -> HsvColorPicker(state)
                ColorPickerType.RgbCircle -> RgbColorPicker(state)
            }

            is ColorPickerTab.Hsl -> HslColorPicker(state)
            is ColorPickerTab.Hsv -> HsvColorPicker(state)
            is ColorPickerTab.Rgb -> RgbColorPicker(state)
        }
        Spacer(modifier = Modifier.height(8.dp))
        PrimaryTabRow(
            selectedTabIndex = selectedTabIndex,
            divider = { HorizontalDivider(color = colors.textFieldColors.unfocusedIndicatorColor) }
        ) {
            properties.tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = {
                        navController.navigate(tab.route)
                        selectedTabIndex = index
                    },
                    text = {
                        Text(
                            text = tab.text,
                            overflow = TextOverflow.Ellipsis,
                            color = colors.tabButtonColor
                        )
                    }
                )
            }
        }
        NavHost(
            navController = navController,
            startDestination = initialTab.route
        ) {
            properties.tabs.forEach { tab ->
                composable(tab.route) {
                    when (tab) {
                        is ColorPickerTab.ColorCodes -> ColorCodesTab(state, colors)
                        is ColorPickerTab.Hsl -> HslTab(state, colors, properties)
                        is ColorPickerTab.Hsv -> HsvTab(state, colors, properties)
                        is ColorPickerTab.Rgb -> RgbTab(state, colors, properties)
                    }
                }
            }
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

private fun DrawScope.drawHueRing(ring: Annulus) {
    val radius = (ring.innerRadius + ring.outerRadius) * 0.5f
    val stroke = Stroke(width = ring.width)
    val hueGradient = Brush.sweepGradient(
        listOf(
            Color.Red,
            Color.Magenta,
            Color.Blue,
            Color.Cyan,
            Color.Green,
            Color.Yellow,
            Color.Red,
        ),
        ring.center.toOffset()
    )
    drawCircle(
        brush = hueGradient,
        radius = radius,
        style = stroke,
        center = ring.center.toOffset(),
    )
}

private fun DrawScope.drawHueSelector(
    selectorTriangle: RegularTriangle, hueRing: Annulus, hue: Float
) {
    val canvasSize = size
    val pointA = selectorTriangle.pointA.toCanvasSpace(canvasSize)
    val pointB = selectorTriangle.pointB.toCanvasSpace(canvasSize)
    val pointC = selectorTriangle.pointC.toCanvasSpace(canvasSize)
    val heightRatio = ((hueRing.outerRadius - hueRing.innerRadius) / hueRing.outerRadius) * 1.01f
    val path = Path().apply {
        moveTo(hueRing.center)
        arcTo(
            rect = Rect(
                center = hueRing.center.toOffset(),
                radius = hueRing.outerRadius * 0.995f
            ),
            startAngleDegrees = -hue - 30f * heightRatio,
            sweepAngleDegrees = 60f * heightRatio,
            forceMoveTo = true
        )
        moveTo(pointB)
        lineTo(pointA)
        lineTo(pointC)
    }
    drawPath(
        path = path,
        color = Color.White,
        style = Stroke(width = 2.dp.toPx())
    )
}

private fun DrawScope.drawHsvTriangle(triangle: RegularTriangle, hue: Float) {
    val canvasSize = size
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
    brushCompat(
        SolidColor(Color.White) to BlendMode.SrcOver,
        hueToTransparentGradient to BlendMode.SrcOver,
        blackToTransparentGradient to BlendMode.SrcOver,
    ) { (brush, blendMode) ->
        drawPath(
            path = trianglePath,
            brush = brush,
            blendMode = blendMode
        )
    }
}

private fun DrawScope.drawHslSquare(square: Square, hue: Float) {
    val canvasSize = size
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
    brushCompat(
        hueToGrayGradient to BlendMode.SrcOver,
        whiteToBlackGradient to BlendMode.SrcOver,
    ) { (brush, blendMode) ->
        drawPath(
            path = squarePath,
            brush = brush,
            blendMode = blendMode
        )
    }
}

private fun DrawScope.drawRgbCircle(circle: Circle) {
    val canvasSize = size
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
    brushCompat(
        hueGradient to BlendMode.SrcOver,
        whiteGradient to BlendMode.SrcOver,
    ) { (brush, blendMode) ->
        drawCircle(
            brush = brush,
            radius = circle.radius,
            center = circle.center.toCanvasSpace(canvasSize).toOffset(),
            blendMode = blendMode
        )
    }
}

private fun DrawScope.drawColorValueRectangle(
    rectangle: RoundedRectangle, state: ColorPickerState
) {
    val canvasSize = size
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
    position: Vector2F, radius: Float, state: ColorPickerState
) {
    val canvasSize = size
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

private fun DrawScope.drawColorSelector(position: Vector2F, state: ColorPickerState) {
    val canvasSize = size
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
    val hueSelectorTriangle = remember {
        MutableRegularTriangle(
            center = Vector2F.ZERO,
            orientation = AngleF.fromDegrees(-90f).toComplexF(),
            sideLength = 100f
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
            hueSelectorTriangle.set(
                center = (ComplexF.fromPolar(
                    magnitude = lerp(
                        hueRing.innerRadius, hueRing.outerRadius, 0.6666667f
                    ),
                    phase = AngleF.fromDegrees(state.hsv.hue).radians
                ).toVector2F() + hueRing.center),
                orientation = AngleF.fromDegrees(state.hsv.hue + 90f).toComplexF(),
                sideLength = hueRing.width
            )
            hsvTriangle.set(
                center = center.toVector2F().toCanvasSpace(canvasSize),
                orientation = AngleF.fromDegrees(state.hsv.hue - 90f).toComplexF(),
                sideLength = hueRing.innerRadius * sqrt(3f)
            )
            drawHsvTriangle(
                triangle = hsvTriangle,
                hue = state.hsv.hue
            )
            drawHueRing(ring = hueRing)
            drawHueSelector(
                selectorTriangle = hueSelectorTriangle,
                hueRing = hueRing,
                hue = state.hsv.hue
            )
            drawColorSelector(
                position = hsvTriangle.colorPosition(state.hsv.saturation, state.hsv.value),
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
    val hueSelectorTriangle = remember {
        MutableRegularTriangle(
            center = Vector2F.ZERO,
            orientation = AngleF.fromDegrees(-90f).toComplexF(),
            sideLength = 100f
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
            hueSelectorTriangle.set(
                center = (ComplexF.fromPolar(
                    magnitude = lerp(
                        hueRing.innerRadius, hueRing.outerRadius, 0.6666667f
                    ),
                    phase = AngleF.fromDegrees(state.hsv.hue).radians
                ).toVector2F() + hueRing.center),
                orientation = AngleF.fromDegrees(state.hsv.hue + 90f).toComplexF(),
                sideLength = hueRing.width
            )
            hslSquare.set(
                center = center.toVector2F().toCanvasSpace(canvasSize),
                orientation = AngleF.fromDegrees(state.hsl.hue - 90f).toComplexF(),
                sideLength = hueRing.innerRadius * sqrt(2f)
            )
            drawHslSquare(
                square = hslSquare,
                hue = state.hsl.hue
            )
            drawHueRing(ring = hueRing)
            drawHueSelector(
                selectorTriangle = hueSelectorTriangle,
                hueRing = hueRing,
                hue = state.hsl.hue
            )
            drawColorSelector(
                position = hslSquare.colorPosition(state.hsl.saturation, state.hsl.lightness),
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
            drawRgbCircle(circle = rgbCircle)
            drawColorValueRectangle(
                rectangle = valueRectangle,
                state = state
            )
            drawCircleSelector(
                position = valueRectangle.valuePosition(state.hsv.value),
                radius = valueRectangle.cornerRadius,
                state = state
            )
            drawColorSelector(
                position = rgbCircle.colorPosition(state.hsv.hue, state.hsv.saturation),
                state = state
            )
        }
    }
}

@Composable
private fun ColorCodeRow(
    value: String,
    onValueChange: (String) -> Unit,
    onPaste: (ClipData) -> Unit,
    colors: ColorPickerColors,
    modifier: Modifier = Modifier,
) {
    val clipboardManager = LocalClipboard.current
    val scope = rememberCoroutineScope()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = true,
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
            colors = colors.textFieldColors,
            modifier = Modifier.width(205.dp * LocalDensity.current.fontScale)
        )
        OutlinedIconButton(
            onClick = {
                scope.launch {
                    clipboardManager.setClipEntry(
                        ClipEntry(ClipData.newPlainText("color hex code", value))
                    )
                }
            },
            border = BorderStroke(width = 1.dp, color = colors.iconButtonColor),
            colors = outlinedIconButtonColors(
                contentColor = colors.tabButtonColor,
            ),
            modifier = Modifier.padding(start = 4.dp)
        ) {
            Icon(
                imageVector = ImageVector
                    .vectorResource(R.drawable.icon_outline_content_copy_24),
                contentDescription = "copy",
                tint = colors.iconButtonColor
            )
        }
        OutlinedIconButton(
            onClick = {
                scope.launch {
                    clipboardManager.getClipEntry()?.let {
                        onPaste(it.clipData)
                    }
                }
            },
            border = BorderStroke(width = 1.dp, color = colors.iconButtonColor),
            colors = outlinedIconButtonColors(
                contentColor = colors.tabButtonColor,
            ),
            modifier = Modifier.padding(start = 4.dp)
        ) {
            Icon(
                imageVector = ImageVector
                    .vectorResource(R.drawable.icon_outline_content_paste_24),
                contentDescription = "paste",
                tint = colors.iconButtonColor
            )
        }
    }
}

@Composable
private fun ColorCodesTab(state: ColorPickerState, colors: ColorPickerColors) {
    var hexCodeState by remember(state.rgb) {
        val (r, g, b) = state.rgb.toColor().toColorRGBA32()
        mutableStateOf("#%02x%02x%02x".format(r.toInt(), g.toInt(), b.toInt()))
    }
    var rgbCodeState by remember(state.rgb) {
        val (r, g, b) = state.rgb.toColor().toColorRGBA32()
        mutableStateOf("rgb(%d, %d, %d)".format(r.toInt(), g.toInt(), b.toInt()))
    }
    var hsvCodeState by remember(state.rgb) {
        val (h, s, v) = state.hsv
        mutableStateOf("hsv(%.0f, %.0f%%, %.0f%%)".format(h, s * 100f, v * 100f))
    }
    var hslCodeState by remember(state.rgb) {
        val (h, s, l) = state.hsl
        mutableStateOf("hsl(%.0f, %.0f%%, %.0f%%)".format(h, s * 100f, l * 100f))
    }
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        ColorCodeRow(
            value = hexCodeState,
            onValueChange = { hexCodeState = it },
            onPaste = { clipData ->
                clipData
                    .itemsSequence()
                    .map { ColorRGBA32.parseHexCodeOrNull(it.text) }
                    .firstOrNull()
                    ?.let { state.rgb = it.toColor().toRgbColor() }
            },
            colors = colors,
            modifier = Modifier.padding(top = 8.dp)
        )
        ColorCodeRow(
            value = rgbCodeState,
            onValueChange = { rgbCodeState = it },
            onPaste = { clipData ->
                clipData
                    .itemsSequence()
                    .map { RgbColor.parseRgbCodeOrNull(it.text) }
                    .firstOrNull()
                    ?.let { state.rgb = it }
            },
            colors = colors,
            modifier = Modifier.padding(top = 8.dp)
        )
        ColorCodeRow(
            value = hsvCodeState,
            onValueChange = { hsvCodeState = it },
            onPaste = { clipData ->
                clipData
                    .itemsSequence()
                    .map { HsvColor.parseHsvCodeOrNull(it.text) }
                    .firstOrNull()
                    ?.let { state.hsv = it }
            },
            colors = colors,
            modifier = Modifier.padding(top = 8.dp)
        )
        ColorCodeRow(
            value = hslCodeState,
            onValueChange = { hslCodeState = it },
            onPaste = { clipData ->
                clipData
                    .itemsSequence()
                    .map { HslColor.parseHslCodeOrNull(it.text) }
                    .firstOrNull()
                    ?.let { state.hsl = it }
            },
            colors = colors,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
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

private fun Float.formatToInteger(): String = "%.0f".format(this)

@Composable
private fun ColorComponentSlider(
    text: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    format: (Float) -> String = { it.toString() },
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    textColor: Color = Color.Unspecified,
    textFieldColors: TextFieldColors,
    sliderColors: SliderColors
) = ColorComponentSlider(
    text = text,
    value = value,
    toString = format,
    stringToT = { it.toFloatOrNull() },
    toFloat = { it },
    floatToT = { it },
    valueRange = valueRange,
    onValueChange = onValueChange,
    prefix = prefix,
    suffix = suffix,
    textColor = textColor,
    textFieldColors = textFieldColors,
    sliderColors = sliderColors
)

@Composable
private fun ColorComponentSlider(
    text: String,
    value: Int,
    valueRange: IntRange,
    onValueChange: (Int) -> Unit,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    textColor: Color = Color.Unspecified,
    textFieldColors: TextFieldColors,
    sliderColors: SliderColors
) = ColorComponentSlider(
    text = text,
    value = value,
    toString = { it.toString() },
    stringToT = { it.toIntOrNull() },
    toFloat = { it.toFloat() },
    floatToT = { it.roundToInt() },
    valueRange = valueRange,
    onValueChange = onValueChange,
    steps = valueRange.last + 1 - valueRange.first,
    prefix = prefix,
    suffix = suffix,
    textColor = textColor,
    textFieldColors = textFieldColors,
    sliderColors = sliderColors
)

@Composable
private inline fun <reified T : Comparable<T>> ColorComponentSlider(
    text: String,
    value: T,
    crossinline toString: (T) -> String,
    crossinline stringToT: (String) -> T?,
    crossinline toFloat: (T) -> Float,
    crossinline floatToT: (Float) -> T,
    valueRange: ClosedRange<T>,
    noinline onValueChange: (T) -> Unit,
    steps: Int = 0,
    noinline prefix: @Composable (() -> Unit)? = null,
    noinline suffix: @Composable (() -> Unit)? = null,
    textColor: Color = Color.Unspecified,
    textFieldColors: TextFieldColors,
    sliderColors: SliderColors
) {
    var textState by remember(value) {
        mutableStateOf(
            TextFieldValue(
                annotatedString = AnnotatedString(text = toString(value))
            )
        )
    }
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.width(8.dp))
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
                    value = toFloat(value),
                    onValueChange = { onValueChange(floatToT(it)) },
                    valueRange = toFloat(valueRange.start)..toFloat(valueRange.endInclusive),
                    steps = steps,
                    colors = sliderColors
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.width(85.dp * LocalDensity.current.fontScale)) {
                val focusManager = LocalFocusManager.current

                OutlinedTextField(
                    value = textState,
                    onValueChange = {
                        textState = it.copy(
                            annotatedString = AnnotatedString(
                                text = it.text.substring(0, min(6, it.text.length))
                            )
                        )

                    },
                    keyboardActions = KeyboardActions(
                        onDone = {
                            stringToT(textState.text).let {
                                when (it) {
                                    null -> textState = TextFieldValue(
                                        annotatedString = AnnotatedString(text = toString(value))
                                    )

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
                    colors = textFieldColors,
                    prefix = prefix,
                    suffix = suffix,
                    modifier = Modifier.onFocusChanged {
                        textState = TextFieldValue(
                            annotatedString = AnnotatedString(text = toString(value))
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun RgbTab(
    state: ColorPickerState, colors: ColorPickerColors, properties: ColorPickerProperties
) {
    var selectedValueFormatIndex by remember { mutableIntStateOf(0) }
    fun is0To1Format() = selectedValueFormatIndex == 0
    val valueFormats = listOf("0..1", "0..255")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            valueFormats.forEachIndexed { index, label ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = valueFormats.size
                    ),
                    onClick = { selectedValueFormatIndex = index },
                    selected = index == selectedValueFormatIndex,
                    label = { Text(label) },
                    colors = colors.segmentedButtonColors
                )
            }
        }
        Row {
            when {
                is0To1Format() -> ColorComponentSlider(
                    text = properties.texts.red,
                    value = state.rgb.red,
                    valueRange = 0f..1f,
                    onValueChange = { state.rgb = state.rgb.copy(red = it) },
                    format = { it.formatUpToTwoDecimalPlaces() },
                    textColor = colors.labelColor,
                    sliderColors = colors.sliderColors,
                    textFieldColors = colors.textFieldColors
                )

                else -> ColorComponentSlider(
                    text = properties.texts.red,
                    value = (state.rgb.red * 255f).roundToInt(),
                    valueRange = 0..255,
                    onValueChange = { state.rgb = state.rgb.copy(red = it.toFloat() / 255f) },
                    textColor = colors.labelColor,
                    sliderColors = colors.sliderColors,
                    textFieldColors = colors.textFieldColors
                )
            }
        }
        Row {
            when {
                is0To1Format() -> ColorComponentSlider(
                    text = properties.texts.green,
                    value = state.rgb.green,
                    valueRange = 0f..1f,
                    onValueChange = { state.rgb = state.rgb.copy(green = it) },
                    format = { it.formatUpToTwoDecimalPlaces() },
                    textColor = colors.labelColor,
                    sliderColors = colors.sliderColors,
                    textFieldColors = colors.textFieldColors
                )

                else -> ColorComponentSlider(
                    text = properties.texts.green,
                    value = (state.rgb.green * 255f).roundToInt(),
                    valueRange = 0..255,
                    onValueChange = { state.rgb = state.rgb.copy(green = it.toFloat() / 255f) },
                    textColor = colors.labelColor,
                    sliderColors = colors.sliderColors,
                    textFieldColors = colors.textFieldColors
                )
            }
        }
        Row {
            when {
                is0To1Format() -> ColorComponentSlider(
                    text = properties.texts.blue,
                    value = state.rgb.blue,
                    valueRange = 0f..1f,
                    onValueChange = { state.rgb = state.rgb.copy(blue = it) },
                    format = { it.formatUpToTwoDecimalPlaces() },
                    textColor = colors.labelColor,
                    sliderColors = colors.sliderColors,
                    textFieldColors = colors.textFieldColors
                )

                else -> ColorComponentSlider(
                    text = properties.texts.blue,
                    value = (state.rgb.blue * 255).roundToInt(),
                    valueRange = 0..255,
                    onValueChange = { state.rgb = state.rgb.copy(blue = it.toFloat() / 255f) },
                    textColor = colors.labelColor,
                    sliderColors = colors.sliderColors,
                    textFieldColors = colors.textFieldColors
                )
            }
        }
    }
}

@Composable
private fun HsvTab(
    state: ColorPickerState, colors: ColorPickerColors, properties: ColorPickerProperties
) {
    var selectedValueFormatIndex by remember { mutableIntStateOf(0) }
    fun is0To1Format() = selectedValueFormatIndex == 0
    val valueFormats = listOf("0..1", "0%..100%")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            valueFormats.forEachIndexed { index, label ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = valueFormats.size
                    ),
                    onClick = { selectedValueFormatIndex = index },
                    selected = index == selectedValueFormatIndex,
                    label = { Text(label) },
                    colors = colors.segmentedButtonColors
                )
            }
        }
        Row {
            ColorComponentSlider(
                text = properties.texts.hsvHue,
                value = state.hsv.hue,
                valueRange = 0f..360f,
                onValueChange = { state.hsv = state.hsv.copy(hue = it) },
                format = { it.formatToInteger() },
                suffix = { Text("°") },
                textColor = colors.labelColor,
                sliderColors = colors.sliderColors,
                textFieldColors = colors.textFieldColors
            )
        }
        Row {
            when {
                is0To1Format() -> ColorComponentSlider(
                    text = properties.texts.hsvSaturation,
                    value = state.hsv.saturation,
                    valueRange = 0f..1f,
                    onValueChange = { state.hsv = state.hsv.copy(saturation = it) },
                    format = { it.formatUpToTwoDecimalPlaces() },
                    textColor = colors.labelColor,
                    sliderColors = colors.sliderColors,
                    textFieldColors = colors.textFieldColors
                )

                else -> ColorComponentSlider(
                    text = properties.texts.hsvSaturation,
                    value = state.hsv.saturation * 100f,
                    valueRange = 0f..100f,
                    onValueChange = { state.hsv = state.hsv.copy(saturation = it * 0.01f) },
                    format = { it.formatToInteger() },
                    suffix = { Text("%") },
                    textColor = colors.labelColor,
                    sliderColors = colors.sliderColors,
                    textFieldColors = colors.textFieldColors
                )
            }
        }
        Row {
            when {
                is0To1Format() -> ColorComponentSlider(
                    text = properties.texts.hsvValue,
                    value = state.hsv.value,
                    valueRange = 0f..1f,
                    onValueChange = { state.hsv = state.hsv.copy(value = it) },
                    format = { it.formatUpToTwoDecimalPlaces() },
                    textColor = colors.labelColor,
                    sliderColors = colors.sliderColors,
                    textFieldColors = colors.textFieldColors
                )

                else -> ColorComponentSlider(
                    text = properties.texts.hsvValue,
                    value = state.hsv.value * 100f,
                    valueRange = 0f..100f,
                    onValueChange = { state.hsv = state.hsv.copy(value = it * 0.01f) },
                    format = { it.formatToInteger() },
                    suffix = { Text("%") },
                    textColor = colors.labelColor,
                    sliderColors = colors.sliderColors,
                    textFieldColors = colors.textFieldColors
                )
            }
        }
    }
}

@Composable
private fun HslTab(
    state: ColorPickerState, colors: ColorPickerColors, properties: ColorPickerProperties
) {
    var selectedValueFormatIndex by remember { mutableIntStateOf(0) }
    fun is0To1Format() = selectedValueFormatIndex == 0
    val valueFormats = listOf("0..1", "0%..100%")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            valueFormats.forEachIndexed { index, label ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = valueFormats.size
                    ),
                    onClick = { selectedValueFormatIndex = index },
                    selected = index == selectedValueFormatIndex,
                    label = { Text(label) },
                    colors = colors.segmentedButtonColors
                )
            }
        }
        Row {
            ColorComponentSlider(
                text = properties.texts.hslHue,
                value = state.hsl.hue,
                valueRange = 0f..360f,
                onValueChange = { state.hsl = state.hsl.copy(hue = it) },
                format = { it.formatToInteger() },
                suffix = { Text("°") },
                textColor = colors.labelColor,
                sliderColors = colors.sliderColors,
                textFieldColors = colors.textFieldColors
            )
        }
        Row {
            when {
                is0To1Format() -> ColorComponentSlider(
                    text = properties.texts.hslSaturation,
                    value = state.hsl.saturation,
                    valueRange = 0f..1f,
                    onValueChange = { state.hsl = state.hsl.copy(saturation = it) },
                    format = { it.formatUpToTwoDecimalPlaces() },
                    textColor = colors.labelColor,
                    sliderColors = colors.sliderColors,
                    textFieldColors = colors.textFieldColors
                )

                else -> ColorComponentSlider(
                    text = properties.texts.hslSaturation,
                    value = state.hsl.saturation * 100f,
                    valueRange = 0f..100f,
                    onValueChange = { state.hsl = state.hsl.copy(saturation = it * 0.01f) },
                    format = { it.formatToInteger() },
                    suffix = { Text("%") },
                    textColor = colors.labelColor,
                    sliderColors = colors.sliderColors,
                    textFieldColors = colors.textFieldColors
                )
            }
        }
        Row {
            when {
                is0To1Format() -> ColorComponentSlider(
                    text = properties.texts.hslLightness,
                    value = state.hsl.lightness,
                    valueRange = 0f..1f,
                    onValueChange = { state.hsl = state.hsl.copy(lightness = it) },
                    format = { it.formatUpToTwoDecimalPlaces() },
                    textColor = colors.labelColor,
                    sliderColors = colors.sliderColors,
                    textFieldColors = colors.textFieldColors
                )

                else -> ColorComponentSlider(
                    text = properties.texts.hslLightness,
                    value = state.hsl.lightness * 100f,
                    valueRange = 0f..100f,
                    onValueChange = { state.hsl = state.hsl.copy(lightness = it * 0.01f) },
                    format = { it.formatToInteger() },
                    suffix = { Text("%") },
                    textColor = colors.labelColor,
                    sliderColors = colors.sliderColors,
                    textFieldColors = colors.textFieldColors
                )
            }
        }
    }
}