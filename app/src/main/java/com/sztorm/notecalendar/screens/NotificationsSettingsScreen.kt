package com.sztorm.notecalendar.screens

import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.sztorm.notecalendar.AppNotificationManager
import com.sztorm.notecalendar.LogTags
import com.sztorm.notecalendar.MainViewModel
import com.sztorm.notecalendar.R
import com.sztorm.notecalendar.ScheduleNoteNotificationArguments
import com.sztorm.notecalendar.components.preferences.SubpreferenceScreen
import com.sztorm.notecalendar.components.preferences.SwitchPreference
import com.sztorm.notecalendar.components.preferences.TimePickerPreference
import com.sztorm.notecalendar.repositories.NoteRepository
import com.sztorm.notecalendar.repositories.UserPreferencesRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSettingsScreen(
    viewModel: MainViewModel,
    noteRepository: NoteRepository,
    preferencesRepository: UserPreferencesRepository,
    notificationManager: AppNotificationManager,
    navController: NavController
) {
    val coroutineScope = rememberCoroutineScope()
    val themeColors = viewModel.state.themeColors
    var turnOnNotifications by remember {
        mutableStateOf(false)
    }
    var notificationTime by remember {
        mutableStateOf(LocalTime.of(8, 0))
    }
    LaunchedEffect(Unit) {
        turnOnNotifications = preferencesRepository.getTurnOnNotifications()
        notificationTime = preferencesRepository.getNotificationTime()
    }
    SubpreferenceScreen(
        title = stringResource(R.string.Settings_Header_Notifications),
        iconTint = themeColors.textColor,
        onBackButtonClick = { navController.navigateUp() }
    ) {
        SwitchPreference(
            title = stringResource(R.string.Settings_EnableNotifications),
            checked = turnOnNotifications,
            onCheckedChange = {
                turnOnNotifications = it
                coroutineScope.launch {
                    if (it) {
                        if (notificationManager.tryScheduleNotification(
                                args = ScheduleNoteNotificationArguments(
                                    grantPermissions = true,
                                    turnOnNotifications = true
                                ),
                                noteRepository = noteRepository
                            )
                        ) {
                            Timber.i("${LogTags.NOTIFICATIONS} Scheduled notification when \"Turn on notifications\" was set to true.")
                        }
                    } else {
                        notificationManager.cancelScheduledNotification()
                        Timber.i("${LogTags.NOTIFICATIONS} Canceled notification when \"Turn on notifications\" was set to false.")
                    }
                    preferencesRepository.setTurnOnNotifications(it)
                }
            },
            textColor = themeColors.textColor
        )
        TimePickerPreference(
            title = stringResource(R.string.Settings_NotificationTime),
            titleColor = themeColors.textColor,
            initialTime = notificationTime,
            onConfirm = {
                // Without it Compose will not update the UI text.
                @Suppress("AssignedValueIsNeverRead")
                notificationTime = it

                coroutineScope.launch {
                    preferencesRepository.setNotificationTime(it)

                    if (notificationManager.tryScheduleNotification(
                            args = ScheduleNoteNotificationArguments(
                                grantPermissions = true,
                                turnOnNotifications = true
                            ),
                            noteRepository = noteRepository
                        )
                    ) {
                        Timber.i("${LogTags.NOTIFICATIONS} Scheduled notification when \"Notification time\" was changed.")
                    }
                }
            },
            buttonColor = themeColors.primaryColor,
            dialogColors = CardDefaults.cardColors().copy(
                containerColor = themeColors.backgroundColor,
                contentColor = themeColors.backgroundColor,
            ),
            timePickerColors = TimePickerDefaults.colors().copy(
                selectorColor = themeColors.primaryColor,
            ),
            enabled = turnOnNotifications,
        )
    }
}