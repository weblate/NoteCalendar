package com.sztorm.notecalendar.components

import androidx.annotation.FloatRange
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import com.sztorm.mathkit.euclidean2d.MutableAnnulus
import com.sztorm.mathkit.euclidean2d.MutableRegularTriangle
import com.sztorm.mathkit.euclidean2d.RegularTriangle
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

interface ColorPickerState {
    @get:FloatRange(from = 0.0, to = 360.0)
    @setparam:FloatRange(from = 0.0, to = 360.0)
    var hue: Float

    @get:FloatRange(from = 0.0, to = 1.0)
    @setparam:FloatRange(from = 0.0, to = 1.0)
    var saturation: Float

    @get:FloatRange(from = 0.0, to = 1.0)
    @setparam:FloatRange(from = 0.0, to = 1.0)
    var value: Float
}

private class ColorPickerStateImpl(
    initialHue: Float, initialSaturation: Float, initialValue: Float
) : ColorPickerState {
    init {
        require(initialHue in 0f..360f) { "initialHue should be in [0, 360] range." }
        require(initialSaturation in 0f..1f) {
            "initialSaturation should be in [0, 1] range."
        }
        require(initialValue in 0f..1f) { "initialValue should be in [0, 1] range." }
    }

    val hueState = mutableFloatStateOf(initialHue)
    val saturationState = mutableFloatStateOf(initialSaturation)
    val valueState = mutableFloatStateOf(initialValue)

    override var hue: Float
        get() = hueState.floatValue
        set(value) {
            hueState.floatValue = value
        }

    override var saturation: Float
        get() = saturationState.floatValue
        set(value) {
            saturationState.floatValue = value
        }

    override var value: Float
        get() = valueState.floatValue
        set(value) {
            valueState.floatValue = value
        }

    companion object {
        fun Saver(): Saver<ColorPickerStateImpl, out Any> =
            Saver(
                save = { listOf(it.hue, it.saturation, it.value) }, // TODO floatListOf?
                restore = {
                    ColorPickerStateImpl(
                        initialHue = it[0],
                        initialSaturation = it[1],
                        initialValue = it[2],
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
}

@Suppress("unused")
fun ColorPickerState(
    initialHue: Float, initialSaturation: Float, initialValue: Float
): ColorPickerState = ColorPickerStateImpl(initialHue, initialSaturation, initialValue)

@Composable
fun rememberColorPickerState(
    initialHue: Float = 0f,
    initialSaturation: Float = 1f,
    initialValue: Float = 1f
): ColorPickerState = rememberSaveable(saver = ColorPickerStateImpl.Saver()) {
    ColorPickerStateImpl(initialHue, initialSaturation, initialValue)
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
    val initialTab = TabType.COLOR_CODES
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(initialTab.ordinal) }

    Column(modifier = modifier.fillMaxWidth()) {
        when (TabType.entries[selectedTabIndex]) {
            TabType.COLOR_CODES -> HSVColorPicker(state)
            TabType.RGB -> HSVColorPicker(state)
            TabType.HSV -> HSVColorPicker(state)
            TabType.HSL -> HSLColorPicker(state)
        }
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
            TabType.entries.forEach { destination ->
                composable(destination.route) {
                    when (destination) {
                        TabType.COLOR_CODES -> ColorCodesTab(state)
                        TabType.RGB -> RgbTab(state)
                        TabType.HSV -> HsvTab(state)
                        TabType.HSL -> HslTab(state)
                    }
                }
            }
        }
    }
}

private fun triangleAreaDoubled(a: Vector2F, b: Vector2F, c: Vector2F) =
    abs(a.x * b.y - b.x * a.y + b.x * c.y - c.x * b.y + c.x * a.y - a.x * c.y)

private fun RegularTriangle.colorSaturationFrom(position: Vector2F): Float {
    val abpArea = triangleAreaDoubled(pointA, pointB, position)
    val cbpArea = triangleAreaDoubled(pointC, pointB, position)
    val abcpArea = abpArea + cbpArea

    return when {
        abcpArea < 0.1f -> 0.5f
        else -> {
            val result = (cbpArea / abcpArea).coerceIn(0f, 1f)

            when {
                result < 0.01f -> 0f
                result > 0.99f -> 1f
                else -> result
            }
        }
    }
}

private fun RegularTriangle.colorValueFrom(position: Vector2F): Float {
    fun heightFromTriangle(a: Vector2F, b: Vector2F, c: Vector2F): Float {
        val areaDoubled = triangleAreaDoubled(a, b, c)
        val baseLength = a.distanceTo(b)

        return areaDoubled / baseLength
    }

    val triangleHeight = circumradius + inradius
    val height = heightFromTriangle(pointA, pointC, position)
    val result = (1f - height / triangleHeight).coerceIn(0f, 1f)

    return when {
        result < 0.01f -> 0f
        result > 0.99f -> 1f
        else -> result
    }
}

private fun RegularTriangle.colorPosition(saturation: Float, value: Float): Vector2F =
    lerp(pointB, lerp(pointC, pointA, saturation), value)

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

private fun DrawScope.drawHSVTriangle(triangle: RegularTriangle, canvasSize: Size, hue: Float) {
    val pointA = triangle.pointA.toCanvasSpace(canvasSize)
    val pointB = triangle.pointB.toCanvasSpace(canvasSize)
    val pointC = triangle.pointC.toCanvasSpace(canvasSize)
    val pointAOpposite = (pointB + pointC) * 0.5f
    val pointBOpposite = (pointA + pointC) * 0.5f
    val valueColor = Color.hsv(hue, saturation = 1f, value = 1f)
    val valueBrush = Brush.linearGradient(
        listOf(valueColor, valueColor.copy(alpha = 0f)),
        start = pointA.toOffset(),
        end = pointAOpposite.toOffset()
    )
    val blackBrush = Brush.linearGradient(
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
    drawPath(trianglePath, Color.White)
    drawPath(trianglePath, blackBrush)
    drawPath(trianglePath, valueBrush)
}

private fun DrawScope.drawColorSelector(
    position: Vector2F, canvasSize: Size, selectedColor: Color, state: ColorPickerState
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
    val t = (lerp(1f, 0f, state.value) + lerp(0f, 1f, state.saturation))
        .coerceIn(0f, 1f)

    drawPath(
        path = path,
        color = selectedColor
    )
    drawPath(
        path = path,
        color = Color.hsv(0f, 0f, if (t > 0.5) 1f else 0f),
        style = Stroke(width = 2.dp.toPx())
    )
}

@Composable
private fun HSVColorPicker(state: ColorPickerState) {
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
                val orientation = (position - hueRing.center.toCanvasSpace(canvasSize))
                    .toComplexF()
                    .normalizedOrElse { ComplexF.ONE }
                state.hue = orientation.phaseAngle.getMinimalPositiveCoterminal().degrees
            }

            in hsvTriangle -> {
                currentAction = CurrentAction.ChangingSV
                state.saturation = hsvTriangle.colorSaturationFrom(position)
                state.value = hsvTriangle.colorValueFrom(position)
            }
        }
    }
    val onInputUpdate = { change: PointerInputChange, _: Offset ->
        val position = change.position
            .toVector2F()
            .toCanvasSpace(canvasSize)

        when (currentAction) {
            CurrentAction.ChangingHue -> {
                val orientation = (position - hueRing.center.toCanvasSpace(canvasSize))
                    .toComplexF()
                    .normalizedOrElse { ComplexF.ONE }
                state.hue = orientation.phaseAngle.getMinimalPositiveCoterminal().degrees
            }

            CurrentAction.ChangingSV -> {
                val selectorPosition = hsvTriangle.closestPointTo(position)
                state.saturation = hsvTriangle.colorSaturationFrom(selectorPosition)
                state.value = hsvTriangle.colorValueFrom(selectorPosition)
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
                center = center.toVector2F(),
                innerRadius = size.width * 0.38f,
                outerRadius = size.width * 0.48f,
            )
            hsvTriangle.set(
                center = center.toVector2F(),
                orientation = AngleF.fromDegrees(state.hue - 90f).toComplexF(),
                sideLength = hueRing.innerRadius * sqrt(3f)
            )
            val selectedColor = Color.hsv(state.hue, state.saturation, state.value)

            drawHSVTriangle(
                triangle = hsvTriangle,
                canvasSize = size,
                hue = state.hue
            )
            drawHueRing(
                ring = hueRing,
                canvasSize = canvasSize
            )
            drawColorSelector(
                position = hsvTriangle.colorPosition(state.saturation, state.value),
                canvasSize = size,
                selectedColor = selectedColor,
                state = state
            )
        }
    }
}

@Composable
private fun HSLColorPicker(state: ColorPickerState) {
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
                val orientation = (position - hueRing.center.toCanvasSpace(canvasSize))
                    .toComplexF()
                    .normalizedOrElse { ComplexF.ONE }
                state.hue = orientation.phaseAngle.getMinimalPositiveCoterminal().degrees
            }

            in hsvTriangle -> {
                currentAction = CurrentAction.ChangingSV
                state.saturation = hsvTriangle.colorSaturationFrom(position)
                state.value = hsvTriangle.colorValueFrom(position)
            }
        }
    }
    val onInputUpdate = { change: PointerInputChange, _: Offset ->
        val position = change.position
            .toVector2F()
            .toCanvasSpace(canvasSize)

        when (currentAction) {
            CurrentAction.ChangingHue -> {
                val orientation = (position - hueRing.center.toCanvasSpace(canvasSize))
                    .toComplexF()
                    .normalizedOrElse { ComplexF.ONE }
                state.hue = orientation.phaseAngle.getMinimalPositiveCoterminal().degrees
            }

            CurrentAction.ChangingSV -> {
                val selectorPosition = hsvTriangle.closestPointTo(position)
                state.saturation = hsvTriangle.colorSaturationFrom(selectorPosition)
                state.value = hsvTriangle.colorValueFrom(selectorPosition)
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
                center = center.toVector2F(),
                innerRadius = size.width * 0.38f,
                outerRadius = size.width * 0.48f,
            )
            hsvTriangle.set(
                center = center.toVector2F(),
                orientation = AngleF.fromDegrees(state.hue - 90f).toComplexF(),
                sideLength = hueRing.innerRadius * sqrt(3f)
            )
            val selectedColor = Color.hsv(state.hue, state.saturation, state.value)

            drawHSVTriangle(
                triangle = hsvTriangle,
                canvasSize = size,
                hue = state.hue
            )
            drawHueRing(
                ring = hueRing,
                canvasSize = canvasSize
            )
            drawColorSelector(
                position = hsvTriangle.colorPosition(state.saturation, state.value),
                canvasSize = size,
                selectedColor = selectedColor,
                state = state
            )
        }
    }
}

private enum class TabType(val route: String, val label: String) {
    COLOR_CODES(route = "colorcodes", label = "#"),
    RGB(route = "rgb", label = "RGB"),
    HSV(route = "hsv", label = "HSV"),
    HSL(route = "hsl", label = "HSL"),
}

@Composable
private fun ColorCodesTab(state: ColorPickerState) {
    // TODO: hexcode, RGB, HSV, HSL
    // copy, paste on each
}

private fun Float.formatUpToTwoDecimalPlaces(): String {
    val result = "%.2f".format(locale = Locale.current.platformLocale, this)
    val separator = DecimalFormatSymbols(Locale.current.platformLocale).decimalSeparator

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
    val color = Color.hsv(state.hue, state.saturation, state.value)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row {
            ColorComponentSlider(
                text = "Red", // TODO move to strings.xml
                value = color.red,
                valueRange = 0f..1f,
                onValueChange = {
                    val (h, s, v) = Color(it, color.green, color.blue).toHsv()
                    state.hue = h
                    state.saturation = s
                    state.value = v
                },
                textColor = Color.Black
            )
        }
        Row {
            ColorComponentSlider(
                text = "Green", // TODO move to strings.xml
                value = color.green,
                valueRange = 0f..1f,
                onValueChange = {
                    val (h, s, v) = Color(color.red, it, color.blue).toHsv()
                    state.hue = h
                    state.saturation = s
                    state.value = v
                },
                textColor = Color.Black
            )
        }
        Row {
            ColorComponentSlider(
                text = "Blue", // TODO move to strings.xml
                value = color.blue,
                valueRange = 0f..1f,
                onValueChange = {
                    val (h, s, v) = Color(color.red, color.green, it).toHsv()
                    state.hue = h
                    state.saturation = s
                    state.value = v
                },
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
                value = state.hue,
                valueRange = 0f..360f,
                onValueChange = { state.hue = it },
                textColor = Color.Black
            )
        }
        Row {
            ColorComponentSlider(
                text = "Saturation", // TODO move to strings.xml
                value = state.saturation,
                valueRange = 0f..1f,
                onValueChange = { state.saturation = it },
                textColor = Color.Black
            )
        }
        Row {
            ColorComponentSlider(
                text = "Value", // TODO move to strings.xml
                value = state.value,
                valueRange = 0f..1f,
                onValueChange = { state.value = it },
                textColor = Color.Black
            )
        }
    }
}

@Composable
private fun HslTab(state: ColorPickerState) {
    val (_, saturation, lightness) = Color.hsv(state.hue, state.saturation, state.value).toHsl()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row {
            ColorComponentSlider(
                text = "Hue", // TODO move to strings.xml
                value = state.hue,
                valueRange = 0f..360f,
                onValueChange = { state.hue = it },
                textColor = Color.Black
            )
        }
        Row {
            ColorComponentSlider(
                text = "Saturation", // TODO move to strings.xml
                value = saturation,
                valueRange = 0f..1f,
                onValueChange = {
                    val (h, s, v) = Color.hsl(state.hue, it, lightness).toHsv()
                    state.hue = h
                    state.saturation = s
                    state.value = v
                },
                textColor = Color.Black
            )
        }
        Row {
            ColorComponentSlider(
                text = "Lightness", // TODO move to strings.xml
                value = lightness,
                valueRange = 0f..1f,
                onValueChange = {
                    val (h, s, v) = Color.hsl(state.hue, saturation, it).toHsv()
                    state.hue = h
                    state.saturation = s
                    state.value = v
                },
                textColor = Color.Black
            )
        }
    }
}