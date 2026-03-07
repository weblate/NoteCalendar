package com.sztorm.notecalendar

import android.content.ClipData
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults.outlinedIconButtonColors
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mikepenz.aboutlibraries.LibsBuilder
import com.sztorm.notecalendar.components.ExportNoteBackupPreference
import com.sztorm.notecalendar.components.ExportNoteBackupPreferenceDefaults
import com.sztorm.notecalendar.components.ExportNoteBackupPreferenceTexts
import com.sztorm.notecalendar.components.ImportNoteBackupPreference
import com.sztorm.notecalendar.components.ImportNoteBackupPreferenceDefaults
import com.sztorm.notecalendar.components.ImportNoteBackupPreferenceTexts
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
import com.sztorm.notecalendar.components.preferences.PreferenceScreen
import com.sztorm.notecalendar.components.preferences.SubpreferenceScreen
import com.sztorm.notecalendar.components.preferences.SwitchPreference
import com.sztorm.notecalendar.components.preferences.TimePickerPreference
import com.sztorm.notecalendar.repositories.FileRepository
import com.sztorm.notecalendar.repositories.LoadResult
import com.sztorm.notecalendar.repositories.NoteRepository
import com.sztorm.notecalendar.repositories.SaveResult
import com.sztorm.notecalendar.repositories.UserPreferencesRepository
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
    fileRepository: FileRepository,
    noteRepository: NoteRepository,
    preferencesRepository: UserPreferencesRepository,
    notificationManager: AppNotificationManager
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
            RootSettingsScreen(viewModel, navController)
        }
        composable(
            route = Screen.Settings.Notes.route,
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
            NotesSettingsScreen(
                viewModel = viewModel,
                noteRepository = noteRepository,
                notificationManager = notificationManager,
                fileRepository = fileRepository,
                navController = navController
            )
        }
        composable(
            route = Screen.Settings.Calendar.route,
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
            CalendarSettingsScreen(
                viewModel = viewModel,
                preferencesRepository = preferencesRepository,
                navController = navController
            )
        }
        composable(
            route = Screen.Settings.Theme.route,
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
            ThemeSettingsScreen(
                viewModel = viewModel,
                fileRepository = fileRepository,
                preferencesRepository = preferencesRepository,
                navController = navController
            )
        }
        composable(
            route = Screen.Settings.Notifications.route,
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
            NotificationsSettingsScreen(
                viewModel = viewModel,
                noteRepository = noteRepository,
                preferencesRepository = preferencesRepository,
                notificationManager = notificationManager,
                navController = navController
            )
        }
        composable(
            route = Screen.Settings.About.route,
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
            AboutSettingsScreen(
                viewModel = viewModel,
                navController = navController
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RootSettingsScreen(viewModel: MainViewModel, navController: NavController) {
    val themeColors = viewModel.state.themeColors

    PreferenceScreen(
        title = "Settings", // TODO: add to strings.xml
        modifier = Modifier.verticalScroll(rememberScrollState()),
    ) {
        Preference(
            title = stringResource(R.string.Settings_Header_Notes),
            titleColor = themeColors.textColor,
            icon = painterResource(R.drawable.icon_outline_rounded_note_stack),
            iconColorFilter = ColorFilter.tint(themeColors.secondaryColor),
            onClick = { navController.navigate(Screen.Settings.Notes.route) }
        )
        Preference(
            title = "Calendar", // TODO: add to strings.xml,
            titleColor = themeColors.textColor,
            icon = painterResource(R.drawable.icon_outline_rounded_calendar_settings),
            iconColorFilter = ColorFilter.tint(themeColors.secondaryColor),
            onClick = { navController.navigate(Screen.Settings.Calendar.route) }
        )
        Preference(
            title = stringResource(R.string.Settings_Header_Theme),
            titleColor = themeColors.textColor,
            icon = painterResource(R.drawable.icon_palette),
            iconColorFilter = ColorFilter.tint(themeColors.secondaryColor),
            onClick = { navController.navigate(Screen.Settings.Theme.route) }
        )
        Preference(
            title = stringResource(R.string.Settings_Header_Notifications),
            titleColor = themeColors.textColor,
            icon = painterResource(R.drawable.icon_outline_rounded_notifications),
            iconColorFilter = ColorFilter.tint(themeColors.secondaryColor),
            onClick = { navController.navigate(Screen.Settings.Notifications.route) }
        )
        Preference(
            title = "About", // TODO: add to strings.xml
            titleColor = themeColors.textColor,
            icon = painterResource(R.drawable.icon_outline_rounded_info),
            iconColorFilter = ColorFilter.tint(themeColors.secondaryColor),
            onClick = { navController.navigate(Screen.Settings.About.route) }
        )
    }
}

@Composable
private fun NotesSettingsScreen(
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
        title = stringResource(R.string.Settings_Header_Notes),
        iconTint = themeColors.textColor,
        onBackButtonClick = { navController.navigateUp() }
    ) {
        ConfirmationPreference(
            title = stringResource(R.string.Settings_DeleteAllNotes),
            dialogTitle = stringResource(R.string.Settings_DeleteAllNotes_Alert_Title),
            dialogMessage = stringResource(R.string.Settings_DeleteAllNotes_Alert_Message),
            icon = painterResource(R.drawable.icon_outline_rounded_delete_forever),
            iconColorFilter = ColorFilter.tint(themeColors.secondaryColor),
            onConfirm = {
                noteRepository.deleteAll()
                notificationManager.cancelScheduledNotification()
                Timber.i("${LogTags.NOTIFICATIONS} Canceled notification when \"delete all notes\" was confirmed.")
            },
            titleColor = themeColors.textColor,
            dialogTitleColor = themeColors.textColor,
            dialogMessageColor = themeColors.textColor,
            dialogButtonColor = themeColors.primaryColor,
            dialogColors = dialogColors
        )
        ImportNoteBackupPreference(
            fileRepository = fileRepository,
            noteRepository = noteRepository,
            texts = ImportNoteBackupPreferenceTexts.english(), // TODO: add to strings.xml
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
            fileRepository = fileRepository,
            noteRepository = noteRepository,
            texts = ExportNoteBackupPreferenceTexts.english(), // TODO: add to strings.xml
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

@Composable
private fun CalendarSettingsScreen(
    viewModel: MainViewModel,
    preferencesRepository: UserPreferencesRepository,
    navController: NavController
) {
    val coroutineScope = rememberCoroutineScope()
    val themeColors = viewModel.state.themeColors
    var firstDayOfWeekIndexPair by remember {
        mutableStateOf(WeekFields.of(Locale.getDefault()).firstDayOfWeek.let {
            it to it.ordinal
        })
    }
    var startingViewIndexPair by remember {
        mutableStateOf(Pair(StartingViewType.DAY_VIEW, 0))
    }
    LaunchedEffect(Unit) {
        firstDayOfWeekIndexPair = preferencesRepository
            .getFirstDayOfWeek()
            .let { it to it.ordinal }
        startingViewIndexPair = preferencesRepository
            .getStartingView()
            .let { it to it.ordinal }
    }
    SubpreferenceScreen(
        title = "Calendar", // TODO: add to strings.xml
        iconTint = themeColors.textColor,
        onBackButtonClick = { navController.navigateUp() }
    ) {
        ListPreference(
            title = stringResource(R.string.Settings_FirstDayOfWeek),
            options = DayOfWeek.entries.map { it.getLocalizedName() to it },
            initialSelectedOptionIndex = firstDayOfWeekIndexPair.second,
            onConfirm = { index, value ->
                // Without it Compose will not update the UI text.
                @Suppress("AssignedValueIsNeverRead")
                firstDayOfWeekIndexPair = Pair(value, index)

                coroutineScope.launch {
                    preferencesRepository.setFirstDayOfWeek(value)
                }
            },
            titleColor = themeColors.textColor,
            summaryColor = themeColors.textColor,
            buttonColor = themeColors.primaryColor,
            dialogColors = CardDefaults.cardColors().copy(
                containerColor = themeColors.backgroundColor,
                contentColor = themeColors.backgroundColor,
            )
        )
        ListPreference(
            title = stringResource(R.string.Settings_StartingView),
            options = StartingViewType.entries.map { it.getLocalizedName() to it },
            initialSelectedOptionIndex = startingViewIndexPair.second,
            onConfirm = { index, value ->
                // Without it Compose will not update the UI text.
                @Suppress("AssignedValueIsNeverRead")
                startingViewIndexPair = Pair(value, index)

                coroutineScope.launch {
                    preferencesRepository.setStartingView(value)
                }
            },
            titleColor = themeColors.textColor,
            summaryColor = themeColors.textColor,
            buttonColor = themeColors.primaryColor,
            dialogColors = CardDefaults.cardColors().copy(
                containerColor = themeColors.backgroundColor,
                contentColor = themeColors.backgroundColor,
            )
        )
    }
}

@Composable
private fun ThemeSettingsScreen(
    viewModel: MainViewModel,
    fileRepository: FileRepository,
    preferencesRepository: UserPreferencesRepository,
    navController: NavController
) {
    val coroutineScope = rememberCoroutineScope()
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
    val dialogColors = CardDefaults.cardColors().copy(
        containerColor = themeColors.backgroundColor,
        contentColor = themeColors.backgroundColor,
    )

    SubpreferenceScreen(
        title = stringResource(R.string.Settings_Header_Theme),
        iconTint = themeColors.textColor,
        onBackButtonClick = { navController.navigateUp() },
    ) {
        CategoryPreference(
            title = "Preset theme", // TODO: add to strings.xml
            titleColor = themeColors.secondaryColor
        ) { enabled ->
            Preference(
                title = stringResource(R.string.Settings_SetLightTheme),
                titleColor = themeColors.textColor,
                icon = painterResource(R.drawable.icon_outline_rounded_sun),
                iconColorFilter = ColorFilter.tint(themeColors.secondaryColor),
                onClick = {
                    coroutineScope.launch {
                        preferencesRepository.setThemeColors(LightThemeColors)
                    }.invokeOnCompletion {
                        viewModel.onEvent(
                            MainEvent.ThemeChange(LightThemeColors)
                        )
                    }
                },
                enabled = enabled
            )
            Preference(
                title = stringResource(R.string.Settings_SetDarkTheme),
                titleColor = themeColors.textColor,
                icon = painterResource(R.drawable.icon_outline_rounded_moon),
                iconColorFilter = ColorFilter.tint(themeColors.secondaryColor),
                onClick = {
                    coroutineScope.launch {
                        preferencesRepository.setThemeColors(DarkThemeColors)
                    }.invokeOnCompletion {
                        viewModel.onEvent(
                            MainEvent.ThemeChange(DarkThemeColors)
                        )
                    }
                },
                enabled = enabled
            )
            Preference(
                title = stringResource(R.string.Settings_SetDefaultTheme),
                titleColor = themeColors.textColor,
                summary = stringResource(R.string.Settings_Summary_SetDefaultTheme),
                summaryColor = themeColors.textColor,
                icon = painterResource(R.drawable.icon_outline_rounded_sun_and_moon),
                iconColorFilter = ColorFilter.tint(themeColors.secondaryColor),
                onClick = {
                    coroutineScope.launch {
                        preferencesRepository.setThemeColors(defaultThemeColors)
                    }.invokeOnCompletion {
                        viewModel.onEvent(
                            MainEvent.ThemeChange(defaultThemeColors)
                        )
                    }
                },
                enabled = enabled
            )
            Preference(
                title = "Load theme", // TODO: add to strings.xml
                titleColor = themeColors.textColor,
                icon = painterResource(R.drawable.icon_outline_rounded_folder_open),
                iconColorFilter = ColorFilter.tint(themeColors.secondaryColor),
                onClick = {
                    fileRepository.loadThemeFile(
                        filetype = "application/json"
                    ) { result ->
                        when (result) {
                            is LoadResult.Success -> {
                                Timber.i("${LogTags.FILE_IO} Theme loaded.")

                                val themeColors = result.file.toThemeColors()
                                coroutineScope.launch {
                                    preferencesRepository.setThemeColors(themeColors)
                                }.invokeOnCompletion {
                                    viewModel.onEvent(
                                        MainEvent.ThemeChange(themeColors)
                                    )
                                }
                            }

                            is LoadResult.Failure ->
                                Timber.e("${LogTags.FILE_IO} ${result.message}")
                        }
                    }
                },
                enabled = enabled
            )
            Preference(
                title = "Save theme", // TODO: add to strings.xml
                titleColor = themeColors.textColor,
                icon = painterResource(R.drawable.icon_outline_rounded_save_as),
                iconColorFilter = ColorFilter.tint(themeColors.secondaryColor),
                onClick = {
                    fileRepository.saveThemeFile(
                        fileName = "theme.json",
                        filetype = "application/json",
                        file = ThemeFile.fromThemeColors(themeColors)
                    ) { result ->
                        when (result) {
                            is SaveResult.Success ->
                                Timber.i("${LogTags.FILE_IO} Theme saved.")

                            is SaveResult.Failure ->
                                Timber.e("${LogTags.FILE_IO} ${result.message}")
                        }
                    }
                },
                enabled = enabled
            )
        }
        CategoryPreference(
            title = stringResource(R.string.Settings_Header_CustomTheme),
            titleColor = themeColors.secondaryColor
        ) { enabled ->
            ColorPickerPreference(
                title = stringResource(R.string.PrimaryColor),
                titleColor = themeColors.textColor,
                initialColor = themeColors.primaryColor,
                defaultColor = defaultThemeColors.primaryColor,
                outlineColor = themeColors.textColor,
                buttonColor = themeColors.primaryColor,
                dialogColors = dialogColors,
                colorPickerColors = colorPickerColors,
                colorPickerProperties = colorPickerProperties,
                onConfirm = { color ->
                    coroutineScope.launch {
                        preferencesRepository.setPrimaryColor(color)
                        viewModel.onEvent(
                            MainEvent.ThemeChange(
                                preferencesRepository.getThemeColors()
                            )
                        )
                    }
                },
                enabled = enabled
            )
            ColorPickerPreference(
                title = stringResource(R.string.SecondaryColor),
                titleColor = themeColors.textColor,
                initialColor = themeColors.secondaryColor,
                defaultColor = defaultThemeColors.secondaryColor,
                outlineColor = themeColors.textColor,
                buttonColor = themeColors.primaryColor,
                dialogColors = dialogColors,
                colorPickerColors = colorPickerColors,
                colorPickerProperties = colorPickerProperties,
                onConfirm = { color ->
                    coroutineScope.launch {
                        preferencesRepository.setSecondaryColor(color)
                        viewModel.onEvent(
                            MainEvent.ThemeChange(
                                preferencesRepository.getThemeColors()
                            )
                        )
                    }
                },
                enabled = enabled
            )
            ColorPickerPreference(
                title = "Inactive element color", // TODO: add to strings.xml
                titleColor = themeColors.textColor,
                initialColor = themeColors.inactiveElementColor,
                defaultColor = defaultThemeColors.inactiveElementColor,
                outlineColor = themeColors.textColor,
                buttonColor = themeColors.primaryColor,
                dialogColors = dialogColors,
                colorPickerColors = colorPickerColors,
                colorPickerProperties = colorPickerProperties,
                onConfirm = { color ->
                    coroutineScope.launch {
                        preferencesRepository.setInactiveElementColor(color)
                        viewModel.onEvent(
                            MainEvent.ThemeChange(
                                preferencesRepository.getThemeColors()
                            )
                        )
                    }
                },
                enabled = enabled
            )
            ColorPickerPreference(
                title = stringResource(R.string.NoteColor),
                titleColor = themeColors.textColor,
                initialColor = themeColors.noteColor,
                defaultColor = defaultThemeColors.noteColor,
                outlineColor = themeColors.textColor,
                buttonColor = themeColors.primaryColor,
                dialogColors = dialogColors,
                colorPickerColors = colorPickerColors,
                colorPickerProperties = colorPickerProperties,
                onConfirm = { color ->
                    coroutineScope.launch {
                        preferencesRepository.setNoteColor(color)
                        viewModel.onEvent(
                            MainEvent.ThemeChange(
                                preferencesRepository.getThemeColors()
                            )
                        )
                    }
                },
                enabled = enabled
            )
            ColorPickerPreference(
                title = stringResource(R.string.NoteColorVariant),
                titleColor = themeColors.textColor,
                initialColor = themeColors.noteColorVariant,
                defaultColor = defaultThemeColors.noteColorVariant,
                outlineColor = themeColors.textColor,
                buttonColor = themeColors.primaryColor,
                dialogColors = dialogColors,
                colorPickerColors = colorPickerColors,
                colorPickerProperties = colorPickerProperties,
                onConfirm = { color ->
                    coroutineScope.launch {
                        preferencesRepository.setNoteColorVariant(color)
                        viewModel.onEvent(
                            MainEvent.ThemeChange(
                                preferencesRepository.getThemeColors()
                            )
                        )
                    }
                },
                enabled = enabled
            )
            ColorPickerPreference(
                title = stringResource(R.string.TextColor),
                titleColor = themeColors.textColor,
                initialColor = themeColors.textColor,
                defaultColor = defaultThemeColors.textColor,
                outlineColor = themeColors.textColor,
                buttonColor = themeColors.primaryColor,
                dialogColors = dialogColors,
                colorPickerColors = colorPickerColors,
                colorPickerProperties = colorPickerProperties,
                onConfirm = { color ->
                    coroutineScope.launch {
                        preferencesRepository.setTextColor(color)
                        viewModel.onEvent(
                            MainEvent.ThemeChange(
                                preferencesRepository.getThemeColors()
                            )
                        )
                    }
                },
                enabled = enabled
            )
            ColorPickerPreference(
                title = stringResource(R.string.ButtonTextColor),
                titleColor = themeColors.textColor,
                initialColor = themeColors.buttonTextColor,
                defaultColor = defaultThemeColors.buttonTextColor,
                outlineColor = themeColors.textColor,
                buttonColor = themeColors.primaryColor,
                dialogColors = dialogColors,
                colorPickerColors = colorPickerColors,
                colorPickerProperties = colorPickerProperties,
                onConfirm = { color ->
                    coroutineScope.launch {
                        preferencesRepository.setButtonTextColor(color)
                        viewModel.onEvent(
                            MainEvent.ThemeChange(
                                preferencesRepository.getThemeColors()
                            )
                        )
                    }
                },
                enabled = enabled
            )
            ColorPickerPreference(
                title = stringResource(R.string.NoteTextColor),
                titleColor = themeColors.textColor,
                initialColor = themeColors.noteTextColor,
                defaultColor = defaultThemeColors.noteTextColor,
                outlineColor = themeColors.textColor,
                buttonColor = themeColors.primaryColor,
                dialogColors = dialogColors,
                colorPickerColors = colorPickerColors,
                colorPickerProperties = colorPickerProperties,
                onConfirm = { color ->
                    coroutineScope.launch {
                        preferencesRepository.setNoteTextColor(color)
                        viewModel.onEvent(
                            MainEvent.ThemeChange(
                                preferencesRepository.getThemeColors()
                            )
                        )
                    }
                },
                enabled = enabled
            )
            ColorPickerPreference(
                title = stringResource(R.string.BackgroundColor),
                titleColor = themeColors.textColor,
                initialColor = themeColors.backgroundColor,
                defaultColor = defaultThemeColors.backgroundColor,
                outlineColor = themeColors.textColor,
                buttonColor = themeColors.primaryColor,
                dialogColors = dialogColors,
                colorPickerColors = colorPickerColors,
                colorPickerProperties = colorPickerProperties,
                onConfirm = { color ->
                    coroutineScope.launch {
                        preferencesRepository.setBackgroundColor(color)
                        viewModel.onEvent(
                            MainEvent.ThemeChange(
                                preferencesRepository.getThemeColors()
                            )
                        )
                    }
                },
                enabled = enabled
            )
            ColorPickerPreference(
                title = "Background color variant", // TODO: add to strings.xml
                titleColor = themeColors.textColor,
                initialColor = themeColors.backgroundColorVariant,
                defaultColor = defaultThemeColors.backgroundColorVariant,
                outlineColor = themeColors.textColor,
                buttonColor = themeColors.primaryColor,
                dialogColors = dialogColors,
                colorPickerColors = colorPickerColors,
                colorPickerProperties = colorPickerProperties,
                onConfirm = { color ->
                    coroutineScope.launch {
                        preferencesRepository.setBackgroundColorVariant(color)
                        viewModel.onEvent(
                            MainEvent.ThemeChange(
                                preferencesRepository.getThemeColors()
                            )
                        )
                    }
                },
                enabled = enabled
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationsSettingsScreen(
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

@Composable
private fun AboutLink(
    text: String,
    url: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    fontSize: TextUnit = 16.sp,
    color: Color = MaterialTheme.colorScheme.secondary
) = Text(
    text = buildAnnotatedString {
        withLink(
            LinkAnnotation.Url(
                url = url,
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = color,
                        textDecoration = TextDecoration.Underline
                    )
                )
            )
        ) {
            append(text)
        }
    },
    style = style,
    fontSize = fontSize,
    modifier = modifier
)

@Composable
private fun AboutCopyButton(
    iconColor: Color,
    borderColor: Color,
    pressedBackgroundColor: Color,
    onClick: () -> Unit
) = OutlinedIconButton(
    onClick = onClick,
    border = BorderStroke(width = 1.dp, color = borderColor),
    colors = outlinedIconButtonColors(
        contentColor = pressedBackgroundColor,
    ),
    modifier = Modifier
        .padding(end = 8.dp)
        .size(24.dp)
) {
    Icon(
        imageVector = ImageVector
            .vectorResource(R.drawable.icon_outline_content_copy),
        contentDescription = "copy",
        tint = iconColor,
        modifier = Modifier.size(16.dp)
    )
}

@Composable
private fun AboutSettingsScreen(
    viewModel: MainViewModel,
    navController: NavController
) {
    val themeColors = viewModel.state.themeColors
    val context = LocalContext.current
    val clipboardManager = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()

    SubpreferenceScreen(
        title = "About", // TODO: add to strings.xml
        iconTint = themeColors.textColor,
        onBackButtonClick = { navController.navigateUp() }
    ) {
        CategoryPreference(
            title = "Basic", // TODO: add to strings.xml
            titleColor = themeColors.secondaryColor
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row {
                    Text(
                        text = "Version", // TODO: add to strings.xml
                        fontSize = 20.sp
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    AboutCopyButton(
                        iconColor = themeColors.textColor,
                        borderColor = themeColors.textColor,
                        pressedBackgroundColor = themeColors.primaryColor,
                    ) {
                        coroutineScope.launch {
                            clipboardManager.setClipEntry(
                                ClipEntry(
                                    ClipData.newPlainText(
                                        "version", AppInfo.VERSION
                                    )
                                )
                            )
                        }
                    }
                    Text(
                        text = AppInfo.VERSION,
                        fontSize = 16.sp
                    )
                }
            }
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row {
                    Text(
                        text = "Contact", // TODO: add to strings.xml
                        fontSize = 20.sp
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    AboutCopyButton(
                        iconColor = themeColors.textColor,
                        borderColor = themeColors.textColor,
                        pressedBackgroundColor = themeColors.primaryColor,
                    ) {
                        coroutineScope.launch {
                            clipboardManager.setClipEntry(
                                ClipEntry(
                                    ClipData.newPlainText(
                                        "contact email", AppInfo.CONTACT_EMAIL
                                    )
                                )
                            )
                        }
                    }
                    Text(
                        text = AppInfo.CONTACT_EMAIL,
                        fontSize = 16.sp
                    )
                }
            }
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row {
                    Text(
                        text = "Source code", // TODO: add to strings.xml
                        fontSize = 20.sp
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    AboutCopyButton(
                        iconColor = themeColors.textColor,
                        borderColor = themeColors.textColor,
                        pressedBackgroundColor = themeColors.primaryColor,
                    ) {
                        coroutineScope.launch {
                            clipboardManager.setClipEntry(
                                ClipEntry(
                                    ClipData.newPlainText(
                                        "source code url", AppInfo.SOURCE_CODE_GITHUB_URL
                                    )
                                )
                            )
                        }
                    }
                    AboutLink(
                        text = AppInfo.SOURCE_CODE_GITHUB,
                        url = AppInfo.SOURCE_CODE_GITHUB_URL
                    )
                }
            }
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row {
                    Text(
                        text = "License", // TODO: add to strings.xml
                        fontSize = 20.sp
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    AboutCopyButton(
                        iconColor = themeColors.textColor,
                        borderColor = themeColors.textColor,
                        pressedBackgroundColor = themeColors.primaryColor,
                    ) {
                        coroutineScope.launch {
                            clipboardManager.setClipEntry(
                                ClipEntry(
                                    ClipData.newPlainText(
                                        "license url", AppInfo.LICENSE_URL
                                    )
                                )
                            )
                        }
                    }
                    AboutLink(
                        text = AppInfo.LICENSE,
                        url = AppInfo.LICENSE_URL
                    )
                }
            }
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row {
                    Text(
                        text = "Privacy policy", // TODO: add to strings.xml
                        fontSize = 20.sp
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    AboutCopyButton(
                        iconColor = themeColors.textColor,
                        borderColor = themeColors.textColor,
                        pressedBackgroundColor = themeColors.primaryColor,
                    ) {
                        coroutineScope.launch {
                            clipboardManager.setClipEntry(
                                ClipEntry(
                                    ClipData.newPlainText(
                                        "privacy policy url", AppInfo.PRIVACY_POLICY_URL
                                    )
                                )
                            )
                        }
                    }
                    AboutLink(
                        text = AppInfo.SOURCE_CODE_GITHUB,
                        url = AppInfo.PRIVACY_POLICY_URL
                    )
                }
            }
        }
        CategoryPreference(
            title = "Advanced", // TODO: add to strings.xml
            titleColor = themeColors.secondaryColor
        ) {
            Preference(
                title = "Library licenses", // TODO: add to strings.xml
                titleColor = themeColors.textColor,
                icon = painterResource(R.drawable.icon_outline_rounded_license),
                iconColorFilter = ColorFilter.tint(themeColors.secondaryColor),
                onClick = {
                    context.startActivity(
                        LibsBuilder()
                            .withActivityTitle("Library licenses") // TODO: add to strings.xml
                            .withEdgeToEdge(true)
                            .withSearchEnabled(true)
                            .intent(context)
                    )
                }
            )
        }
    }
}