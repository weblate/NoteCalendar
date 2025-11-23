package com.sztorm.notecalendar

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mikepenz.aboutlibraries.LibsBuilder
import com.sztorm.notecalendar.components.preferences.CategoryPreference
import com.sztorm.notecalendar.components.preferences.ColorPickerPreference
import com.sztorm.notecalendar.components.preferences.ConfirmationPreference
import com.sztorm.notecalendar.components.preferences.ListPreference
import com.sztorm.notecalendar.components.preferences.Preference
import com.sztorm.notecalendar.components.preferences.SubpreferenceScreen
import com.sztorm.notecalendar.components.preferences.SwitchPreference
import com.sztorm.notecalendar.components.preferences.TimePickerPreference
import com.sztorm.notecalendar.helpers.ContextHelper.Companion.getColorCompat
import com.sztorm.notecalendar.helpers.ContextHelper.Companion.getColorFromAttr
import com.sztorm.notecalendar.repositories.NoteRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.temporal.WeekFields
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    mainActivity: MainActivity,
    noteRepository: NoteRepository
) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = Screen.Settings.route) {
        composable(
            route = Screen.Settings.route,
            enterTransition = {
                slideIntoContainer(
                    towards = SlideDirection.Right,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = SlideDirection.Left,
                    animationSpec = tween(300)
                )
            }
        ) {
            RootSettingsLayout(viewModel, mainActivity, noteRepository, navController)
        }
        composable(
            route = Screen.Settings.CustomTheme.route,
            enterTransition = {
                slideIntoContainer(
                    towards = SlideDirection.Left,
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = SlideDirection.Right,
                    animationSpec = tween(300)
                )
            }
        ) {
            CustomThemeSettingsLayout(viewModel, mainActivity, navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootSettingsLayout(
    viewModel: MainViewModel,
    mainActivity: MainActivity,
    noteRepository: NoteRepository,
    navController: NavController
) {
    val themeColors = viewModel.state.themeColors
    // TODO: move these to Color.kt and just set color values without resources
    val lightThemeValues = remember {
        ThemeColors(
            primaryColor = mainActivity.getColorCompat(R.color.primary_light),
            secondaryColor = mainActivity.getColorCompat(R.color.secondary_light),
            inactiveItemColor = mainActivity.getColorCompat(R.color.inactive_light),
            inactiveItemColorVariant = mainActivity.getColorCompat(R.color.inactive_variant_light),
            noteColor = mainActivity.getColorCompat(R.color.note_light_primary),
            noteColorVariant = mainActivity.getColorCompat(R.color.note_light_secondary),
            textColor = mainActivity.getColorCompat(R.color.black_cool),
            buttonTextColor = mainActivity.getColorCompat(R.color.white_cool),
            noteTextColor = mainActivity.getColorCompat(R.color.black_cool),
            backgroundColor = mainActivity.getColorCompat(R.color.background_light)
        )
    }
    val darkThemeValues = remember {
        ThemeColors(
            primaryColor = mainActivity.getColorCompat(R.color.primary_dark),
            secondaryColor = mainActivity.getColorCompat(R.color.secondary_dark),
            inactiveItemColor = mainActivity.getColorCompat(R.color.inactive_dark),
            inactiveItemColorVariant = mainActivity.getColorCompat(R.color.inactive_variant_dark),
            noteColor = mainActivity.getColorCompat(R.color.note_dark_primary),
            noteColorVariant = mainActivity.getColorCompat(R.color.note_dark_secondary),
            textColor = mainActivity.getColorCompat(R.color.white_cool),
            buttonTextColor = mainActivity.getColorCompat(R.color.white_cool),
            noteTextColor = mainActivity.getColorCompat(R.color.white_cool),
            backgroundColor = mainActivity.getColorCompat(R.color.background_dark)
        )
    }
    val defaultThemeValues = remember {
        ThemeColors(
            primaryColor = mainActivity.getColorFromAttr(R.attr.colorPrimary),
            secondaryColor = mainActivity.getColorFromAttr(R.attr.colorSecondary),
            inactiveItemColor = mainActivity.getColorFromAttr(R.attr.colorInactiveItem),
            inactiveItemColorVariant = mainActivity.getColorFromAttr(R.attr.colorInactiveItemVariant),
            noteColor = mainActivity.getColorFromAttr(R.attr.colorNote),
            noteColorVariant = mainActivity.getColorFromAttr(R.attr.colorNoteVariant),
            textColor = mainActivity.getColorFromAttr(R.attr.colorText),
            buttonTextColor = mainActivity.getColorFromAttr(R.attr.colorButtonText),
            noteTextColor = mainActivity.getColorFromAttr(R.attr.colorText),
            backgroundColor = mainActivity.getColorFromAttr(R.attr.colorBackground)
        )
    }
    var turnOnNotifications by remember {
        mutableStateOf(false)
    }
    var firstDayOfWeekIndexPair by remember {
        mutableStateOf(WeekFields.of(Locale.getDefault()).firstDayOfWeek.let {
            it to it.ordinal
        })
    }
    var startingViewIndexPair by remember {
        mutableStateOf(Pair(StartingViewType.DAY_VIEW, 0))
    }
    var notificationTime by remember {
        mutableStateOf(LocalTime.of(8, 0))
    }
    LaunchedEffect(Unit) {
        turnOnNotifications = mainActivity.settings.getTurnOnNotifications()
        firstDayOfWeekIndexPair = mainActivity.settings
            .getFirstDayOfWeek()
            .let { it to it.ordinal }
        startingViewIndexPair = mainActivity.settings
            .getStartingView()
            .let { it to it.ordinal }
        notificationTime = mainActivity.settings.getNotificationTime()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        CategoryPreference(
            title = stringResource(R.string.Settings_Header_Theme),
            titleColor = Color(themeColors.secondaryColor)
        ) { enabled ->
            Preference(
                icon = painterResource(R.drawable.icon_palette),
                iconColorFilter = ColorFilter.tint(Color(themeColors.secondaryColor)),
                title = stringResource(R.string.Settings_SetCustomTheme),
                titleColor = Color(themeColors.textColor),
                enabled = enabled,
                onClick = { navController.navigate(Screen.Settings.CustomTheme.route) }
            )
            Preference(
                title = stringResource(R.string.Settings_SetLightTheme),
                titleColor = Color(themeColors.textColor),
                enabled = enabled,
                onClick = {
                    mainActivity.lifecycleScope.launch {
                        mainActivity.settings.setThemeColors(lightThemeValues)
                    }.invokeOnCompletion {
                        viewModel.onEvent(
                            MainEvent.ThemeChange(lightThemeValues)
                        )
                    }
                }
            )
            Preference(
                title = stringResource(R.string.Settings_SetDarkTheme),
                titleColor = Color(themeColors.textColor),
                enabled = enabled,
                onClick = {
                    mainActivity.lifecycleScope.launch {
                        mainActivity.settings.setThemeColors(darkThemeValues)
                    }.invokeOnCompletion {
                        viewModel.onEvent(
                            MainEvent.ThemeChange(darkThemeValues)
                        )
                    }
                }
            )
            Preference(
                title = stringResource(R.string.Settings_SetDefaultTheme),
                titleColor = Color(themeColors.textColor),
                summary = stringResource(R.string.Settings_Summary_SetDefaultTheme),
                summaryColor = Color(themeColors.textColor),
                enabled = enabled,
                onClick = {
                    mainActivity.lifecycleScope.launch {
                        mainActivity.settings.setThemeColors(defaultThemeValues)
                    }.invokeOnCompletion {
                        viewModel.onEvent(
                            MainEvent.ThemeChange(defaultThemeValues)
                        )
                    }
                }
            )
        }
        CategoryPreference(
            title = stringResource(R.string.Settings_Header_Notes),
            titleColor = Color(themeColors.secondaryColor)
        ) { enabled ->
            ConfirmationPreference(
                title = stringResource(R.string.Settings_DeleteAllNotes),
                dialogTitle = stringResource(R.string.Settings_DeleteAllNotes_Alert_Title),
                dialogMessage = stringResource(R.string.Settings_DeleteAllNotes_Alert_Message),
                onConfirm = {
                    noteRepository.deleteAll()
                    mainActivity.notificationManager.cancelScheduledNotification()
                    Timber.i("${LogTags.NOTIFICATIONS} Canceled notification when \"delete all notes\" was confirmed.")
                },
                titleColor = Color(themeColors.textColor),
                dialogTitleColor = Color(themeColors.textColor),
                dialogMessageColor = Color(themeColors.textColor),
                dialogButtonColor = Color(themeColors.primaryColor),
                dialogColors = CardDefaults.cardColors().copy(
                    containerColor = Color(themeColors.backgroundColor),
                    contentColor = Color(themeColors.backgroundColor),
                ),
                enabled = enabled,
            )
            // TODO: Settings_DeleteNotesDateRange (Datepicker)
        }
        CategoryPreference(
            title = stringResource(R.string.Settings_Header_Notifications),
            titleColor = Color(themeColors.secondaryColor)
        ) { enabled ->
            SwitchPreference(
                title = stringResource(R.string.Settings_EnableNotifications),
                checked = turnOnNotifications,
                onCheckedChange = {
                    turnOnNotifications = it
                    mainActivity.lifecycleScope.launch {
                        if (it) {
                            if (mainActivity.notificationManager.tryScheduleNotification(
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
                            mainActivity.notificationManager.cancelScheduledNotification()
                            Timber.i("${LogTags.NOTIFICATIONS} Canceled notification when \"Turn on notifications\" was set to false.")
                        }
                        mainActivity.settings.setTurnOnNotifications(it)
                    }
                },
                textColor = Color(themeColors.textColor),
                enabled = enabled,
            )
            TimePickerPreference(
                title = stringResource(R.string.Settings_NotificationTime),
                titleColor = Color(themeColors.textColor),
                initialTime = notificationTime,
                onConfirm = {
                    notificationTime = it

                    mainActivity.lifecycleScope.launch {
                        mainActivity.settings.setNotificationTime(it)

                        if (mainActivity.notificationManager.tryScheduleNotification(
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
                buttonColor = Color(themeColors.primaryColor),
                dialogColors = CardDefaults.cardColors().copy(
                    containerColor = Color(themeColors.backgroundColor),
                    contentColor = Color(themeColors.backgroundColor),
                ),
                timePickerColors = TimePickerDefaults.colors().copy(
                    selectorColor = Color(themeColors.primaryColor),
                ),
                enabled = enabled && turnOnNotifications,
            )
        }
        CategoryPreference(
            title = stringResource(R.string.Settings_Header_Other),
            titleColor = Color(themeColors.secondaryColor)
        ) { enabled ->
            ListPreference(
                title = stringResource(R.string.Settings_FirstDayOfWeek),
                options = DayOfWeek.entries.map { it.getLocalizedName() to it },
                initialSelectedOptionIndex = firstDayOfWeekIndexPair.second,
                onConfirm = { index, value ->
                    firstDayOfWeekIndexPair = Pair(value, index)
                    mainActivity.lifecycleScope.launch {
                        mainActivity.settings.setFirstDayOfWeek(value)
                    }
                },
                titleColor = Color(themeColors.textColor),
                summaryColor = Color(themeColors.textColor),
                buttonColor = Color(themeColors.primaryColor),
                dialogColors = CardDefaults.cardColors().copy(
                    containerColor = Color(themeColors.backgroundColor),
                    contentColor = Color(themeColors.backgroundColor),
                ),
                enabled = enabled
            )
            ListPreference(
                title = stringResource(R.string.Settings_StartingView),
                options = StartingViewType.entries.map { it.getLocalizedName() to it },
                initialSelectedOptionIndex = startingViewIndexPair.second,
                onConfirm = { index, value ->
                    startingViewIndexPair = Pair(value, index)
                    mainActivity.lifecycleScope.launch {
                        mainActivity.settings.setStartingView(value)
                    }
                },
                titleColor = Color(themeColors.textColor),
                summaryColor = Color(themeColors.textColor),
                buttonColor = Color(themeColors.primaryColor),
                dialogColors = CardDefaults.cardColors().copy(
                    containerColor = Color(themeColors.backgroundColor),
                    contentColor = Color(themeColors.backgroundColor),
                ),
                enabled = enabled
            )
        }
        val licensesTitle = stringResource(R.string.Settings_Licenses)

        Preference(
            title = licensesTitle,
            titleColor = Color(themeColors.textColor),
            onClick = {
                mainActivity.startActivity(
                    LibsBuilder()
                        .withActivityTitle(licensesTitle)
                        .withEdgeToEdge(true)
                        .withSearchEnabled(true)
                        .intent(mainActivity)
                )
            }
        )
        // TODO: About application
    }
}

@Composable
fun CustomThemeSettingsLayout(
    viewModel: MainViewModel,
    mainActivity: MainActivity,
    navController: NavController
) {
    val themeColors = viewModel.state.themeColors
    val defaultThemeValues = remember {
        ThemeColors(
            primaryColor = mainActivity.getColorFromAttr(R.attr.colorPrimary),
            secondaryColor = mainActivity.getColorFromAttr(R.attr.colorSecondary),
            inactiveItemColor = mainActivity.getColorFromAttr(R.attr.colorInactiveItem),
            inactiveItemColorVariant = mainActivity.getColorFromAttr(R.attr.colorInactiveItemVariant),
            noteColor = mainActivity.getColorFromAttr(R.attr.colorNote),
            noteColorVariant = mainActivity.getColorFromAttr(R.attr.colorNoteVariant),
            textColor = mainActivity.getColorFromAttr(R.attr.colorText),
            buttonTextColor = mainActivity.getColorFromAttr(R.attr.colorButtonText),
            noteTextColor = mainActivity.getColorFromAttr(R.attr.colorText),
            backgroundColor = mainActivity.getColorFromAttr(R.attr.colorBackground)
        )
    }
    SubpreferenceScreen(
        title = stringResource(R.string.Settings_Header_CustomTheme),
        titleColor = Color(themeColors.textColor),
        iconTint = Color(themeColors.textColor),
        onBackButtonClick = { navController.navigateUp() },
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        ColorPickerPreference(
            title = stringResource(R.string.PrimaryColor),
            titleColor = Color(themeColors.textColor),
            initialColor = Color(themeColors.primaryColor),
            defaultColor = Color(defaultThemeValues.primaryColor),
            outlineColor = Color(themeColors.textColor),
            buttonColor = Color(themeColors.primaryColor),
            dialogColors = CardDefaults.cardColors().copy(
                containerColor = Color(themeColors.backgroundColor),
                contentColor = Color(themeColors.backgroundColor),
            ),
            onConfirm = { color ->
                mainActivity.lifecycleScope.launch {
                    mainActivity.settings.setPrimaryColor(color.toArgb())
                    viewModel.onEvent(
                        MainEvent.ThemeChange(
                            mainActivity.settings.getThemeColors()
                        )
                    )
                }
            },
        )
        ColorPickerPreference(
            title = stringResource(R.string.SecondaryColor),
            titleColor = Color(themeColors.textColor),
            initialColor = Color(themeColors.secondaryColor),
            defaultColor = Color(defaultThemeValues.secondaryColor),
            outlineColor = Color(themeColors.textColor),
            buttonColor = Color(themeColors.primaryColor),
            dialogColors = CardDefaults.cardColors().copy(
                containerColor = Color(themeColors.backgroundColor),
                contentColor = Color(themeColors.backgroundColor),
            ),
            onConfirm = { color ->
                mainActivity.lifecycleScope.launch {
                    mainActivity.settings.setSecondaryColor(color.toArgb())
                    viewModel.onEvent(
                        MainEvent.ThemeChange(
                            mainActivity.settings.getThemeColors()
                        )
                    )
                }
            },
        )
        ColorPickerPreference(
            title = stringResource(R.string.InactiveItemColor),
            titleColor = Color(themeColors.textColor),
            initialColor = Color(themeColors.inactiveItemColor),
            defaultColor = Color(defaultThemeValues.inactiveItemColor),
            outlineColor = Color(themeColors.textColor),
            buttonColor = Color(themeColors.primaryColor),
            dialogColors = CardDefaults.cardColors().copy(
                containerColor = Color(themeColors.backgroundColor),
                contentColor = Color(themeColors.backgroundColor),
            ),
            onConfirm = { color ->
                mainActivity.lifecycleScope.launch {
                    mainActivity.settings.setInactiveItemColor(color.toArgb())
                    viewModel.onEvent(
                        MainEvent.ThemeChange(
                            mainActivity.settings.getThemeColors()
                        )
                    )
                }
            },
        )
        ColorPickerPreference(
            title = stringResource(R.string.InactiveItemColorVariant),
            titleColor = Color(themeColors.textColor),
            initialColor = Color(themeColors.inactiveItemColorVariant),
            defaultColor = Color(defaultThemeValues.inactiveItemColorVariant),
            outlineColor = Color(themeColors.textColor),
            buttonColor = Color(themeColors.primaryColor),
            dialogColors = CardDefaults.cardColors().copy(
                containerColor = Color(themeColors.backgroundColor),
                contentColor = Color(themeColors.backgroundColor),
            ),
            onConfirm = { color ->
                mainActivity.lifecycleScope.launch {
                    mainActivity.settings.setInactiveItemColorVariant(color.toArgb())
                    viewModel.onEvent(
                        MainEvent.ThemeChange(
                            mainActivity.settings.getThemeColors()
                        )
                    )
                }
            },
        )
        ColorPickerPreference(
            title = stringResource(R.string.NoteColor),
            titleColor = Color(themeColors.textColor),
            initialColor = Color(themeColors.noteColor),
            defaultColor = Color(defaultThemeValues.noteColor),
            outlineColor = Color(themeColors.textColor),
            buttonColor = Color(themeColors.primaryColor),
            dialogColors = CardDefaults.cardColors().copy(
                containerColor = Color(themeColors.backgroundColor),
                contentColor = Color(themeColors.backgroundColor),
            ),
            onConfirm = { color ->
                mainActivity.lifecycleScope.launch {
                    mainActivity.settings.setNoteColor(color.toArgb())
                    viewModel.onEvent(
                        MainEvent.ThemeChange(
                            mainActivity.settings.getThemeColors()
                        )
                    )
                }
            },
        )
        ColorPickerPreference(
            title = stringResource(R.string.NoteColorVariant),
            titleColor = Color(themeColors.textColor),
            initialColor = Color(themeColors.noteColorVariant),
            defaultColor = Color(defaultThemeValues.noteColorVariant),
            outlineColor = Color(themeColors.textColor),
            buttonColor = Color(themeColors.primaryColor),
            dialogColors = CardDefaults.cardColors().copy(
                containerColor = Color(themeColors.backgroundColor),
                contentColor = Color(themeColors.backgroundColor),
            ),
            onConfirm = { color ->
                mainActivity.lifecycleScope.launch {
                    mainActivity.settings.setNoteColorVariant(color.toArgb())
                    viewModel.onEvent(
                        MainEvent.ThemeChange(
                            mainActivity.settings.getThemeColors()
                        )
                    )
                }
            },
        )
        ColorPickerPreference(
            title = stringResource(R.string.TextColor),
            titleColor = Color(themeColors.textColor),
            initialColor = Color(themeColors.textColor),
            defaultColor = Color(defaultThemeValues.textColor),
            outlineColor = Color(themeColors.textColor),
            buttonColor = Color(themeColors.primaryColor),
            dialogColors = CardDefaults.cardColors().copy(
                containerColor = Color(themeColors.backgroundColor),
                contentColor = Color(themeColors.backgroundColor),
            ),
            onConfirm = { color ->
                mainActivity.lifecycleScope.launch {
                    mainActivity.settings.setTextColor(color.toArgb())
                    viewModel.onEvent(
                        MainEvent.ThemeChange(
                            mainActivity.settings.getThemeColors()
                        )
                    )
                }
            },
        )
        ColorPickerPreference(
            title = stringResource(R.string.ButtonTextColor),
            titleColor = Color(themeColors.textColor),
            initialColor = Color(themeColors.buttonTextColor),
            defaultColor = Color(defaultThemeValues.buttonTextColor),
            outlineColor = Color(themeColors.textColor),
            buttonColor = Color(themeColors.primaryColor),
            dialogColors = CardDefaults.cardColors().copy(
                containerColor = Color(themeColors.backgroundColor),
                contentColor = Color(themeColors.backgroundColor),
            ),
            onConfirm = { color ->
                mainActivity.lifecycleScope.launch {
                    mainActivity.settings.setButtonTextColor(color.toArgb())
                    viewModel.onEvent(
                        MainEvent.ThemeChange(
                            mainActivity.settings.getThemeColors()
                        )
                    )
                }
            },
        )
        ColorPickerPreference(
            title = stringResource(R.string.NoteTextColor),
            titleColor = Color(themeColors.textColor),
            initialColor = Color(themeColors.noteTextColor),
            defaultColor = Color(defaultThemeValues.noteTextColor),
            outlineColor = Color(themeColors.textColor),
            buttonColor = Color(themeColors.primaryColor),
            dialogColors = CardDefaults.cardColors().copy(
                containerColor = Color(themeColors.backgroundColor),
                contentColor = Color(themeColors.backgroundColor),
            ),
            onConfirm = { color ->
                mainActivity.lifecycleScope.launch {
                    mainActivity.settings.setNoteTextColor(color.toArgb())
                    viewModel.onEvent(
                        MainEvent.ThemeChange(
                            mainActivity.settings.getThemeColors()
                        )
                    )
                }
            },
        )
        ColorPickerPreference(
            title = stringResource(R.string.BackgroundColor),
            titleColor = Color(themeColors.textColor),
            initialColor = Color(themeColors.backgroundColor),
            defaultColor = Color(defaultThemeValues.backgroundColor),
            outlineColor = Color(themeColors.textColor),
            buttonColor = Color(themeColors.primaryColor),
            dialogColors = CardDefaults.cardColors().copy(
                containerColor = Color(themeColors.backgroundColor),
                contentColor = Color(themeColors.backgroundColor),
            ),
            onConfirm = { color ->
                mainActivity.lifecycleScope.launch {
                    mainActivity.settings.setBackgroundColor(color.toArgb())
                    viewModel.onEvent(
                        MainEvent.ThemeChange(
                            mainActivity.settings.getThemeColors()
                        )
                    )
                }
            },
        )
    }
}