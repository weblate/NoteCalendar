package com.sztorm.notecalendar.components

import androidx.compose.material3.CardColors
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter

data class ImportNoteBackupPreferenceColors(
    val titleColor: Color,
    val summaryColor: Color,
    val iconColorFilter: ColorFilter?,
    val dialogColors: ImportNoteBackupPreferenceDialogColors
)

data class ImportNoteBackupPreferenceDialogColors(
    val titleColor: Color,
    val errorTextColor: Color,
    val textContentColor: Color,
    val textButtonColor: Color,
    val cardColors: CardColors,
)