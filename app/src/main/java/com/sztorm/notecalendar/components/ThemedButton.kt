package com.sztorm.notecalendar.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults.outlinedIconButtonColors
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.sztorm.notecalendar.ThemeColors

@Composable
fun ThemedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    themeColors: ThemeColors,
    content: @Composable (RowScope.() -> Unit)
) = Button(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    colors = ButtonColors(
        containerColor = themeColors.primaryColor,
        contentColor = themeColors.buttonTextColor,
        disabledContainerColor = themeColors.inactiveItemColor,
        disabledContentColor = themeColors.buttonTextColor
    ),
    content = content
)

@Composable
fun ThemedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    themeColors: ThemeColors,
    text: String,
    icon: Painter,
    contentDescription: String = text,
) = ThemedButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    themeColors = themeColors
) {
    Icon(
        painter = icon,
        contentDescription = contentDescription,
        tint = themeColors.buttonTextColor
    )
    Text(text, color = themeColors.buttonTextColor)
}

@Composable
fun ThemedIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    themeColors: ThemeColors,
    icon: Painter,
    contentDescription: String,
) = OutlinedIconButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    colors = outlinedIconButtonColors(
        contentColor = themeColors.primaryColor,
    ),
    border = BorderStroke(2.dp, themeColors.primaryColor)
) {
    Icon(
        painter = icon,
        contentDescription = contentDescription,
        tint = themeColors.primaryColor,
    )
}