package com.sztorm.notecalendar.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.sztorm.notecalendar.LogTags
import com.sztorm.notecalendar.MainEvent
import com.sztorm.notecalendar.MainViewModel
import com.sztorm.notecalendar.R
import com.sztorm.notecalendar.ThemeFile
import com.sztorm.notecalendar.components.colorpicker.ColorPickerDefaults
import com.sztorm.notecalendar.components.colorpicker.ColorPickerProperties
import com.sztorm.notecalendar.components.colorpicker.ColorPickerTab
import com.sztorm.notecalendar.components.colorpicker.ColorPickerTexts
import com.sztorm.notecalendar.components.colorpicker.ColorPickerType
import com.sztorm.notecalendar.components.preferences.CategoryPreference
import com.sztorm.notecalendar.components.preferences.ColorPickerPreference
import com.sztorm.notecalendar.components.preferences.Preference
import com.sztorm.notecalendar.components.preferences.SubpreferenceScreen
import com.sztorm.notecalendar.repositories.FileRepository
import com.sztorm.notecalendar.repositories.LoadResult
import com.sztorm.notecalendar.repositories.SaveResult
import com.sztorm.notecalendar.repositories.UserPreferencesRepository
import com.sztorm.notecalendar.ui.DarkThemeColors
import com.sztorm.notecalendar.ui.LightThemeColors
import com.sztorm.notecalendar.ui.getDefaultThemeColors
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun ThemeSettingsScreen(
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