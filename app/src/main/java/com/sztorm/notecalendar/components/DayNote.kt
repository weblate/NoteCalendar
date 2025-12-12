package com.sztorm.notecalendar.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun DayNotePreview() = DayNote(
    color = Color(0.7f, 0.3f, 0f, 1f),
    bendTint = Color.White,
    bendShadowWidth = 20f,
) { }

@Composable
fun DayNote(
    modifier: Modifier = Modifier,
    color: Color,
    bendTint: Color,
    bendFraction: Float = 0.15f,
    bendShadowWidth: Float = 5f,
    content: @Composable (ColumnScope.() -> Unit)
) {
    Column(
        modifier = modifier
            .drawWithCache {
                val width = size.width
                val height = size.height
                val cardPath = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(0f, height)
                    lineTo(width, height)
                    lineTo(width, height * bendFraction)
                    lineTo(width - height * bendFraction, 0f)
                    close()
                }
                val bendPath = Path().apply {
                    moveTo(width, height * bendFraction)
                    lineTo(width - height * bendFraction, 0f)
                    lineTo(width - height * bendFraction, height * bendFraction)
                    close()
                }
                val bendGradient = Brush.linearGradient(
                    0f to bendTint,
                    0.5f to lerp(color, bendTint, 0.5f),
                    1f to color,
                    start = Offset(width, 0f),
                    end = Offset(width - height * bendFraction, height * bendFraction)
                )
                val bendShadowPathA = Path().apply {
                    moveTo(width - height * bendFraction - bendShadowWidth, 0f)
                    lineTo(width - height * bendFraction, 0f)
                    lineTo(width - height * bendFraction, height * bendFraction)
                    lineTo(
                        width - height * bendFraction - bendShadowWidth, height * bendFraction
                    )
                    close()
                }
                val bendShadowGradientA = Brush.linearGradient(
                    0f to lerp(Color.Black, color, 0.7f),
                    1f to color,
                    start = Offset(width - height * bendFraction, 0f),
                    end = Offset(width - height * bendFraction - bendShadowWidth, 0f)
                )
                val bendShadowPathB = Path().apply {
                    moveTo(width - height * bendFraction, height * bendFraction)
                    lineTo(
                        width - height * bendFraction, height * bendFraction + bendShadowWidth
                    )
                    lineTo(width, height * bendFraction + bendShadowWidth)
                    lineTo(width, height * bendFraction)
                    close()
                }
                val bendShadowGradientB = Brush.linearGradient(
                    0f to lerp(Color.Black, color, 0.7f),
                    1f to color,
                    start = Offset(width, height * bendFraction),
                    end = Offset(width, height * bendFraction + bendShadowWidth)
                )
                val bendShadowGradientC = Brush.radialGradient(
                    0f to lerp(Color.Black, color, 0.7f),
                    1f to color,
                    center = Offset(
                        width - height * bendFraction,
                        height * bendFraction
                    ),
                    radius = bendShadowWidth
                )
                onDrawBehind {
                    drawPath(
                        path = cardPath,
                        color = color
                    )
                    drawPath(
                        path = bendShadowPathA,
                        brush = bendShadowGradientA
                    )
                    drawArc(
                        brush = bendShadowGradientC,
                        startAngle = 90f,
                        sweepAngle = 90f,
                        topLeft = Offset(
                            width - height * bendFraction - bendShadowWidth,
                            height * bendFraction - bendShadowWidth
                        ),
                        size = Size(bendShadowWidth * 2f, bendShadowWidth * 2f),
                        useCenter = true
                    )
                    drawPath(
                        path = bendShadowPathB,
                        brush = bendShadowGradientB
                    )
                    drawPath(
                        path = bendPath,
                        brush = bendGradient
                    )
                }
            }
            .fillMaxSize(),
        content = content
    )
}