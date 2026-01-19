package com.sztorm.notecalendar.components

import androidx.compose.material3.CardColors
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter

data class ExportNoteBackupPreferenceColors(
    val titleColor: Color,
    val summaryColor: Color,
    val iconColorFilter: ColorFilter?,
    val dialogColors: ExportNoteBackupPreferenceDialogColors
)

data class ExportNoteBackupPreferenceDialogColors(
    val titleColor: Color,
    val buttonColor: Color,
    val cardColors: CardColors,
)