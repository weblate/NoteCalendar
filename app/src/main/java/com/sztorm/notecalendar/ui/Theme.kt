package com.sztorm.notecalendar.ui

import android.app.Activity
import android.view.View
import android.view.Window
import androidx.compose.foundation.LocalIndication
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.sztorm.notecalendar.ThemeColors
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat

private fun applySystemBarsColor(
    isBackgroundLight: Boolean,
    window: Window,
    view: View,
    primaryColor: Color
) {
    WindowCompat.getInsetsController(window, view).apply {
        isAppearanceLightStatusBars = isBackgroundLight
        isAppearanceLightNavigationBars = isBackgroundLight
    }
    ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { view, insets ->
        view.setBackgroundColor(primaryColor.toArgb())
        insets
    }
}

@Composable
fun AppTheme(colorScheme: ColorScheme, content: @Composable () -> Unit) {
    val view = LocalView.current
    val isBackgroundLight = colorScheme.background.luminance() > 0.5
    val selectBackgroundColor =
        if (isBackgroundLight) Color(0x40000000)
        else Color(0x40ffffff)

    SideEffect {
        val window = (view.context as Activity).window
        applySystemBarsColor(isBackgroundLight, window, view, colorScheme.primary)
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
    ) {
        CompositionLocalProvider(
            LocalIndication provides ripple(color = selectBackgroundColor),
            content = content,
        )
    }
}