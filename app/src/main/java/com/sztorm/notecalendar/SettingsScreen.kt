package com.sztorm.notecalendar

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mikepenz.aboutlibraries.LibsBuilder
import com.sztorm.notecalendar.components.colorpicker.ColorPickerDefaults
import com.sztorm.notecalendar.components.colorpicker.ColorPickerProperties
import com.sztorm.notecalendar.components.colorpicker.ColorPickerTab
import com.sztorm.notecalendar.components.colorpicker.ColorPickerTexts
import com.sztorm.notecalendar.components.colorpicker.ColorPickerType
import com.sztorm.notecalendar.components.preferences.CategoryPreference
import com.sztorm.notecalendar.components.preferences.ColorPickerPreference
import com.sztorm.notecalendar.components.preferences.ConfirmationPreference
import com.sztorm.notecalendar.components.preferences.ListPreference
import com.sztorm.notecalendar.components.preferences.Preference
import com.sztorm.notecalendar.components.preferences.SubpreferenceScreen
import com.sztorm.notecalendar.components.preferences.SwitchPreference
import com.sztorm.notecalendar.components.preferences.TimePickerPreference
import com.sztorm.notecalendar.repositories.NoteRepository
import com.sztorm.notecalendar.ui.DarkThemeColors
import com.sztorm.notecalendar.ui.LightThemeColors
import com.sztorm.notecalendar.ui.getDefaultThemeColors
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
    val defaultThemeColors = getDefaultThemeColors(isSystemInDarkTheme())
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
            titleColor = themeColors.secondaryColor
        ) { enabled ->
            Preference(
                icon = painterResource(R.drawable.icon_palette),
                iconColorFilter = ColorFilter.tint(themeColors.secondaryColor),
                title = stringResource(R.string.Settings_SetCustomTheme),
                titleColor = themeColors.textColor,
                enabled = enabled,
                onClick = { navController.navigate(Screen.Settings.CustomTheme.route) }
            )
            Preference(
                title = stringResource(R.string.Settings_SetLightTheme),
                titleColor = themeColors.textColor,
                enabled = enabled,
                onClick = {
                    mainActivity.lifecycleScope.launch {
                        mainActivity.settings.setThemeColors(LightThemeColors)
                    }.invokeOnCompletion {
                        viewModel.onEvent(
                            MainEvent.ThemeChange(LightThemeColors)
                        )
                    }
                }
            )
            Preference(
                title = stringResource(R.string.Settings_SetDarkTheme),
                titleColor = themeColors.textColor,
                enabled = enabled,
                onClick = {
                    mainActivity.lifecycleScope.launch {
                        mainActivity.settings.setThemeColors(DarkThemeColors)
                    }.invokeOnCompletion {
                        viewModel.onEvent(
                            MainEvent.ThemeChange(DarkThemeColors)
                        )
                    }
                }
            )
            Preference(
                title = stringResource(R.string.Settings_SetDefaultTheme),
                titleColor = themeColors.textColor,
                summary = stringResource(R.string.Settings_Summary_SetDefaultTheme),
                summaryColor = themeColors.textColor,
                enabled = enabled,
                onClick = {
                    mainActivity.lifecycleScope.launch {
                        mainActivity.settings.setThemeColors(defaultThemeColors)
                    }.invokeOnCompletion {
                        viewModel.onEvent(
                            MainEvent.ThemeChange(defaultThemeColors)
                        )
                    }
                }
            )
        }
        CategoryPreference(
            title = stringResource(R.string.Settings_Header_Notes),
            titleColor = themeColors.secondaryColor
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
                titleColor = themeColors.textColor,
                dialogTitleColor = themeColors.textColor,
                dialogMessageColor = themeColors.textColor,
                dialogButtonColor = themeColors.primaryColor,
                dialogColors = CardDefaults.cardColors().copy(
                    containerColor = themeColors.backgroundColor,
                    contentColor = themeColors.backgroundColor,
                ),
                enabled = enabled,
            )
            // TODO: Settings_DeleteNotesDateRange (Datepicker)
        }
        CategoryPreference(
            title = stringResource(R.string.Settings_Header_Notifications),
            titleColor = themeColors.secondaryColor
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
                textColor = themeColors.textColor,
                enabled = enabled,
            )
            TimePickerPreference(
                title = stringResource(R.string.Settings_NotificationTime),
                titleColor = themeColors.textColor,
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
                buttonColor = themeColors.primaryColor,
                dialogColors = CardDefaults.cardColors().copy(
                    containerColor = themeColors.backgroundColor,
                    contentColor = themeColors.backgroundColor,
                ),
                timePickerColors = TimePickerDefaults.colors().copy(
                    selectorColor = themeColors.primaryColor,
                ),
                enabled = enabled && turnOnNotifications,
            )
        }
        CategoryPreference(
            title = stringResource(R.string.Settings_Header_Other),
            titleColor = themeColors.secondaryColor
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
                titleColor = themeColors.textColor,
                summaryColor = themeColors.textColor,
                buttonColor = themeColors.primaryColor,
                dialogColors = CardDefaults.cardColors().copy(
                    containerColor = themeColors.backgroundColor,
                    contentColor = themeColors.backgroundColor,
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
                titleColor = themeColors.textColor,
                summaryColor = themeColors.textColor,
                buttonColor = themeColors.primaryColor,
                dialogColors = CardDefaults.cardColors().copy(
                    containerColor = themeColors.backgroundColor,
                    contentColor = themeColors.backgroundColor,
                ),
                enabled = enabled
            )
        }
        val licensesTitle = stringResource(R.string.Settings_Licenses)

        Preference(
            title = licensesTitle,
            titleColor = themeColors.textColor,
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
    val defaultThemeColors = getDefaultThemeColors(isSystemInDarkTheme())
    val colorPickerColors = ColorPickerDefaults.colors().copy(
        backgroundColor = themeColors.backgroundColor,
        labelColor = themeColors.textColor,
        tabButtonColor = themeColors.primaryColor,
        iconButtonColor = themeColors.textColor,
    )
    val colorPickerProperties = ColorPickerProperties(
        tabs = listOf(
            ColorPickerTab.ColorCodes(
                pickerType = ColorPickerType.HsvTriangle
            ),
            ColorPickerTab.Rgb(),
            ColorPickerTab.Hsv(),
            ColorPickerTab.Hsl(),
        ),
        texts = ColorPickerTexts.english(), // TODO add required strings to strings.xml
    )
    SubpreferenceScreen(
        title = stringResource(R.string.Settings_Header_CustomTheme),
        titleColor = themeColors.textColor,
        iconTint = themeColors.textColor,
        onBackButtonClick = { navController.navigateUp() },
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        ColorPickerPreference(
            title = stringResource(R.string.PrimaryColor),
            titleColor = themeColors.textColor,
            initialColor = themeColors.primaryColor,
            defaultColor = defaultThemeColors.primaryColor,
            outlineColor = themeColors.textColor,
            buttonColor = themeColors.primaryColor,
            dialogColors = CardDefaults.cardColors().copy(
                containerColor = themeColors.backgroundColor,
                contentColor = themeColors.backgroundColor,
            ),
            colorPickerColors = colorPickerColors,
            colorPickerProperties = colorPickerProperties,
            onConfirm = { color ->
                mainActivity.lifecycleScope.launch {
                    mainActivity.settings.setPrimaryColor(color)
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
            titleColor = themeColors.textColor,
            initialColor = themeColors.secondaryColor,
            defaultColor = defaultThemeColors.secondaryColor,
            outlineColor = themeColors.textColor,
            buttonColor = themeColors.primaryColor,
            dialogColors = CardDefaults.cardColors().copy(
                containerColor = themeColors.backgroundColor,
                contentColor = themeColors.backgroundColor,
            ),
            colorPickerColors = colorPickerColors,
            colorPickerProperties = colorPickerProperties,
            onConfirm = { color ->
                mainActivity.lifecycleScope.launch {
                    mainActivity.settings.setSecondaryColor(color)
                    viewModel.onEvent(
                        MainEvent.ThemeChange(
                            mainActivity.settings.getThemeColors()
                        )
                    )
                }
            },
        )
        ColorPickerPreference(
            title = "Inactive element color", // TODO: add to strings.xml
            titleColor = themeColors.textColor,
            initialColor = themeColors.inactiveElementColor,
            defaultColor = defaultThemeColors.inactiveElementColor,
            outlineColor = themeColors.textColor,
            buttonColor = themeColors.primaryColor,
            dialogColors = CardDefaults.cardColors().copy(
                containerColor = themeColors.backgroundColor,
                contentColor = themeColors.backgroundColor,
            ),
            colorPickerColors = colorPickerColors,
            colorPickerProperties = colorPickerProperties,
            onConfirm = { color ->
                mainActivity.lifecycleScope.launch {
                    mainActivity.settings.setInactiveElementColor(color)
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
            titleColor = themeColors.textColor,
            initialColor = themeColors.noteColor,
            defaultColor = defaultThemeColors.noteColor,
            outlineColor = themeColors.textColor,
            buttonColor = themeColors.primaryColor,
            dialogColors = CardDefaults.cardColors().copy(
                containerColor = themeColors.backgroundColor,
                contentColor = themeColors.backgroundColor,
            ),
            colorPickerColors = colorPickerColors,
            colorPickerProperties = colorPickerProperties,
            onConfirm = { color ->
                mainActivity.lifecycleScope.launch {
                    mainActivity.settings.setNoteColor(color)
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
            titleColor = themeColors.textColor,
            initialColor = themeColors.noteColorVariant,
            defaultColor = defaultThemeColors.noteColorVariant,
            outlineColor = themeColors.textColor,
            buttonColor = themeColors.primaryColor,
            dialogColors = CardDefaults.cardColors().copy(
                containerColor = themeColors.backgroundColor,
                contentColor = themeColors.backgroundColor,
            ),
            colorPickerColors = colorPickerColors,
            colorPickerProperties = colorPickerProperties,
            onConfirm = { color ->
                mainActivity.lifecycleScope.launch {
                    mainActivity.settings.setNoteColorVariant(color)
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
            titleColor = themeColors.textColor,
            initialColor = themeColors.textColor,
            defaultColor = defaultThemeColors.textColor,
            outlineColor = themeColors.textColor,
            buttonColor = themeColors.primaryColor,
            dialogColors = CardDefaults.cardColors().copy(
                containerColor = themeColors.backgroundColor,
                contentColor = themeColors.backgroundColor,
            ),
            colorPickerColors = colorPickerColors,
            colorPickerProperties = colorPickerProperties,
            onConfirm = { color ->
                mainActivity.lifecycleScope.launch {
                    mainActivity.settings.setTextColor(color)
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
            titleColor = themeColors.textColor,
            initialColor = themeColors.buttonTextColor,
            defaultColor = defaultThemeColors.buttonTextColor,
            outlineColor = themeColors.textColor,
            buttonColor = themeColors.primaryColor,
            dialogColors = CardDefaults.cardColors().copy(
                containerColor = themeColors.backgroundColor,
                contentColor = themeColors.backgroundColor,
            ),
            colorPickerColors = colorPickerColors,
            colorPickerProperties = colorPickerProperties,
            onConfirm = { color ->
                mainActivity.lifecycleScope.launch {
                    mainActivity.settings.setButtonTextColor(color)
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
            titleColor = themeColors.textColor,
            initialColor = themeColors.noteTextColor,
            defaultColor = defaultThemeColors.noteTextColor,
            outlineColor = themeColors.textColor,
            buttonColor = themeColors.primaryColor,
            dialogColors = CardDefaults.cardColors().copy(
                containerColor = themeColors.backgroundColor,
                contentColor = themeColors.backgroundColor,
            ),
            colorPickerColors = colorPickerColors,
            colorPickerProperties = colorPickerProperties,
            onConfirm = { color ->
                mainActivity.lifecycleScope.launch {
                    mainActivity.settings.setNoteTextColor(color)
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
            titleColor = themeColors.textColor,
            initialColor = themeColors.backgroundColor,
            defaultColor = defaultThemeColors.backgroundColor,
            outlineColor = themeColors.textColor,
            buttonColor = themeColors.primaryColor,
            dialogColors = CardDefaults.cardColors().copy(
                containerColor = themeColors.backgroundColor,
                contentColor = themeColors.backgroundColor,
            ),
            colorPickerColors = colorPickerColors,
            colorPickerProperties = colorPickerProperties,
            onConfirm = { color ->
                mainActivity.lifecycleScope.launch {
                    mainActivity.settings.setBackgroundColor(color)
                    viewModel.onEvent(
                        MainEvent.ThemeChange(
                            mainActivity.settings.getThemeColors()
                        )
                    )
                }
            },
        )
        ColorPickerPreference(
            title = "Background color variant", // TODO: add to strings.xml
            titleColor = themeColors.textColor,
            initialColor = themeColors.backgroundColorVariant,
            defaultColor = defaultThemeColors.backgroundColorVariant,
            outlineColor = themeColors.textColor,
            buttonColor = themeColors.primaryColor,
            dialogColors = CardDefaults.cardColors().copy(
                containerColor = themeColors.backgroundColor,
                contentColor = themeColors.backgroundColor,
            ),
            colorPickerColors = colorPickerColors,
            colorPickerProperties = colorPickerProperties,
            onConfirm = { color ->
                mainActivity.lifecycleScope.launch {
                    mainActivity.settings.setBackgroundColorVariant(color)
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