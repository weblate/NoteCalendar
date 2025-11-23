package com.sztorm.notecalendar.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp

@Composable
fun DayNote(
    modifier: Modifier = Modifier,
    color: Color,
    content: @Composable (ColumnScope.() -> Unit)
) {
    fun cardPath(width: Float, height: Float, bendFraction: Float) = Path().apply {
        moveTo(0f, 0f)
        lineTo(0f, height)
        lineTo(width, height)
        lineTo(width, height * bendFraction)
        lineTo(width - height * bendFraction, 0f)
        close()
    }

    fun bendPath(width: Float, height: Float, bendFraction: Float) = Path().apply {
        moveTo(width, height * bendFraction)
        lineTo(width - height * bendFraction, 0f)
        lineTo(width - height * bendFraction, height * bendFraction)
        close()
    }

    fun bendShadowPath(
        width: Float, height: Float, bendFraction: Float, shadowFraction: Float
    ) = Path().apply {
        val fraction = bendFraction + shadowFraction
        moveTo(width, height * fraction)
        lineTo(width - height * fraction, height * fraction)
        lineTo(width - height * fraction, 0f)
    }

    Column(
        modifier = modifier
            .drawWithCache
            {
                val bendFraction = 0.15f
                val shadowFraction = 0.003f
                val width = size.width
                val height = size.height
                val cardPath = cardPath(width, height, bendFraction)
                val bendPath = bendPath(width, height, bendFraction)
                val bendGradient = Brush.linearGradient(
                    0f to lerp(color, Color.White, 0.3f),
                    0.5f to lerp(color, Color.White, 0.3f),
                    1f to color,
                    start = Offset(width, 0f),
                    end = Offset(width - height * bendFraction, height * bendFraction)
                )
                val bendShadowPath =
                    bendShadowPath(width, height, bendFraction, shadowFraction * 0.5f)
                val bendShadowStyle = Stroke(
                    width = height * shadowFraction,
                    pathEffect = PathEffect.cornerPathEffect(radius = height * shadowFraction)
                )
                val bendShadowColor = lerp(color, Color.Black, 0.05f)
                onDrawBehind {
                    drawPath(
                        path = cardPath,
                        color = color
                    )
                    drawPath(
                        path = bendShadowPath,
                        color = bendShadowColor,
                        style = bendShadowStyle
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