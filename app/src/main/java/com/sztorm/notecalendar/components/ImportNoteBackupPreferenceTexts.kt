package com.sztorm.notecalendar.components

data class ImportNoteBackupPreferenceTexts(
    val title: String,
    val summary: String?,
    val dialogTexts: ImportNoteBackupPreferenceDialogTexts
) {
    companion object {
        fun english() = ImportNoteBackupPreferenceTexts(
            title = "Import notes backup",
            summary = null,
            dialogTexts = ImportNoteBackupPreferenceDialogTexts.english()
        )
    }
}

data class ImportNoteBackupPreferenceDialogTexts(
    val title: String,
    val password: String,
    val incorrectPassword: String,
) {
    companion object {
        fun english() = ImportNoteBackupPreferenceDialogTexts(
            title = "Import notes backup?",
            password = "Password",
            incorrectPassword = "Password is incorrect"
        )
    }
}