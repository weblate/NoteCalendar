package com.sztorm.notecalendar.screens

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.sztorm.notecalendar.AppNotificationManager
import com.sztorm.notecalendar.ILogger
import com.sztorm.notecalendar.LogTags
import com.sztorm.notecalendar.viewmodels.MainViewModel
import com.sztorm.notecalendar.R
import com.sztorm.notecalendar.components.ExportNoteBackupPreference
import com.sztorm.notecalendar.components.ExportNoteBackupPreferenceDefaults
import com.sztorm.notecalendar.components.ExportNoteBackupPreferenceDialogTexts
import com.sztorm.notecalendar.components.ExportNoteBackupPreferenceTexts
import com.sztorm.notecalendar.components.ImportNoteBackupPreference
import com.sztorm.notecalendar.components.ImportNoteBackupPreferenceDefaults
import com.sztorm.notecalendar.components.ImportNoteBackupPreferenceDialogTexts
import com.sztorm.notecalendar.components.ImportNoteBackupPreferenceTexts
import com.sztorm.notecalendar.components.PasswordStrengthTexts
import com.sztorm.notecalendar.components.preferences.ConfirmationPreference
import com.sztorm.notecalendar.components.preferences.SubpreferenceScreen
import com.sztorm.notecalendar.repositories.FileRepository
import com.sztorm.notecalendar.repositories.NoteRepository
import com.sztorm.notecalendar.toLocalDateOrNull

@Composable
fun NotesSettingsScreen(
    logger: ILogger,
    viewModel: MainViewModel,
    noteRepository: NoteRepository,
    fileRepository: FileRepository,
    notificationManager: AppNotificationManager,
    navController: NavController
) {
    val themeColors = viewModel.state.themeColors
    val dialogColors = CardDefaults.cardColors().copy(
        containerColor = themeColors.backgroundColor,
        contentColor = themeColors.backgroundColor,
    )
    SubpreferenceScreen(
        title = stringResource(R.string.Settings_Notes),
        iconTint = themeColors.textColor,
        onBackButtonClick = { navController.navigateUp() }
    ) {
        ConfirmationPreference(
            title = stringResource(R.string.Settings_Notes_DeleteAllNotes),
            dialogTitle = stringResource(R.string.Settings_Notes_DeleteAllNotes_DialogTitle),
            dialogMessage = stringResource(R.string.Settings_Notes_DeleteAllNotes_DialogMessage),
            icon = painterResource(R.drawable.icon_outline_rounded_delete_forever),
            iconColorFilter = ColorFilter.tint(themeColors.secondaryColor),
            onConfirm = {
                logger.info("${LogTags.APPLICATION} Delete all notes confirmed.")
                noteRepository.getAll().forEach { noteData ->
                    noteData.date.toLocalDateOrNull()?.let {
                        notificationManager.cancelScheduledNotification(it)
                    }
                }
                noteRepository.deleteAll()
            },
            titleColor = themeColors.textColor,
            dialogTitleColor = themeColors.textColor,
            dialogMessageColor = themeColors.textColor,
            dialogButtonColor = themeColors.primaryColor,
            dialogColors = dialogColors
        )
        ImportNoteBackupPreference(
            logger = logger,
            fileRepository = fileRepository,
            noteRepository = noteRepository,
            texts = ImportNoteBackupPreferenceTexts(
                title = stringResource(R.string.Settings_Notes_ImportNotesBackup),
                summary = null,
                dialogTexts = ImportNoteBackupPreferenceDialogTexts(
                    title = stringResource(R.string.Settings_Notes_ImportNotesBackup_DialogTitle),
                    password = stringResource(R.string.Password),
                    incorrectPassword = stringResource(R.string.Password_Error)
                )
            ),
            colors = ImportNoteBackupPreferenceDefaults.colors().let {
                it.copy(
                    titleColor = themeColors.textColor,
                    iconColorFilter = ColorFilter.tint(themeColors.secondaryColor),
                    dialogColors = it.dialogColors.copy(
                        titleColor = themeColors.textColor,
                        textContentColor = themeColors.textColor,
                        textButtonColor = themeColors.primaryColor,
                        cardColors = dialogColors
                    )
                )
            },
            icon = painterResource(R.drawable.icon_outline_rounded_folder_open),
            dialogModifier = Modifier.verticalScroll(rememberScrollState()),
        )
        ExportNoteBackupPreference(
            logger = logger,
            fileRepository = fileRepository,
            noteRepository = noteRepository,
            texts = ExportNoteBackupPreferenceTexts(
                title = stringResource(R.string.Settings_Notes_ExportNotesBackup),
                summary = null,
                dialogTexts = ExportNoteBackupPreferenceDialogTexts(
                    title = stringResource(R.string.Settings_Notes_ExportNotesBackup_DialogTitle),
                    encryptData = stringResource(R.string.EncryptData),
                    encryptionAlgorithm = stringResource(R.string.EncryptionAlgorithm),
                    password = stringResource(R.string.Password)
                ),
                passwordStrengthTexts = PasswordStrengthTexts(
                    weak = stringResource(R.string.PasswordStrength_Weak),
                    moderate = stringResource(R.string.PasswordStrength_Moderate),
                    strong = stringResource(R.string.PasswordStrength_Strong)
                )
            ),
            colors = ExportNoteBackupPreferenceDefaults.colors().let {
                it.copy(
                    titleColor = themeColors.textColor,
                    iconColorFilter = ColorFilter.tint(themeColors.secondaryColor),
                    dialogColors = it.dialogColors.copy(
                        titleColor = themeColors.textColor,
                        textContentColor = themeColors.textColor,
                        textButtonColor = themeColors.primaryColor,
                        cardColors = dialogColors
                    )
                )
            },
            icon = painterResource(R.drawable.icon_outline_rounded_save_as),
            dialogModifier = Modifier.verticalScroll(rememberScrollState()),
        )
    }
}