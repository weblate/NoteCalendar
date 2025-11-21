package com.sztorm.notecalendar

import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup

class ThemePainter(val values: ThemeColors) {
    fun paintStatusBarAndSetSystemInsets(
        window: Window, navigation: MaterialButtonToggleGroup, fragmentContainer: LinearLayout
    ) {
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { view, insets ->
            val systemInsets = insets.getInsets(
                WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars()
            )
            view.setBackgroundColor(values.primaryColor)
            view.setPadding(0, systemInsets.top, 0, 0)

            navigation.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                setMargins(systemInsets.left, 0, systemInsets.right, systemInsets.bottom)
            }
            fragmentContainer.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                setMargins(systemInsets.left, 0, systemInsets.right, systemInsets.bottom)
            }
            insets
        }
    }

    fun paintNavigationButton(button: MaterialButton) {
        button.strokeColor = values.navigationButtonStrokeColorStateList
        button.iconTint = values.navigationButtonIconColorStateList
        button.backgroundTintList = values.navigationButtonBackgroundColorStateList
        button.rippleColor = values.buttonRippleColorStateList
    }

    fun paintBackground(view: View) {
        view.setBackgroundColor(values.backgroundColor)
    }
}