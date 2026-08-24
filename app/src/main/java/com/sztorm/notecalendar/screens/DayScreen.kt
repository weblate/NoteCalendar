package com.sztorm.notecalendar.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sztorm.notecalendar.AppNotificationManager
import com.sztorm.notecalendar.AppPermissionManager
import com.sztorm.notecalendar.ILogger
import com.sztorm.notecalendar.NoteData
import com.sztorm.notecalendar.R
import com.sztorm.notecalendar.ThemeColors
import com.sztorm.notecalendar.components.DayNote
import com.sztorm.notecalendar.components.InfiniteHorizontalPager
import com.sztorm.notecalendar.components.TimePickerDialog
import com.sztorm.notecalendar.components.TimedContent
import com.sztorm.notecalendar.getLocalizedGenitiveCaseName
import com.sztorm.notecalendar.getLocalizedName
import com.sztorm.notecalendar.repositories.NoteRepository
import com.sztorm.notecalendar.viewmodels.DayScreenEvent
import com.sztorm.notecalendar.viewmodels.DayScreenNote
import com.sztorm.notecalendar.viewmodels.DayScreenState
import com.sztorm.notecalendar.viewmodels.DayScreenViewModel
import com.sztorm.notecalendar.viewmodels.DayScreenViewModelFactory
import com.sztorm.notecalendar.viewmodels.MainEvent
import com.sztorm.notecalendar.viewmodels.MainViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import kotlin.time.Duration as KotlinDuration

enum class DayNoteMode {
    Reading,
    Editing,
    Adding
}

@Composable
fun DayScreen(
    logger: ILogger,
    mainViewModel: MainViewModel,
    permissionManager: AppPermissionManager,
    notificationManager: AppNotificationManager,
    noteRepository: NoteRepository,
    isCreateOrEditRequested: Boolean = false
) {
    val currentDate = remember { mainViewModel.state.dayScreenDate }
    val initialDayNote = noteRepository
        .getBy(currentDate)
        ?.let { noteData ->
            DayScreenNote(
                date = currentDate,
                textValue = TextFieldValue(text = noteData.text),
                reminderDateTime = noteData.reminderDateTime
                    .ifEmpty { null }
                    ?.let { OffsetDateTime.parse(it) }
            )
        }
    val viewModel = viewModel<DayScreenViewModel>(
        factory = DayScreenViewModelFactory(
            initialState = DayScreenState(
                note = initialDayNote,
                prevNote = noteRepository
                    .getBy(currentDate.minusDays(1))
                    ?.let { noteData ->
                        DayScreenNote(
                            date = currentDate.minusDays(1),
                            textValue = TextFieldValue(text = noteData.text),
                            reminderDateTime = noteData.reminderDateTime
                                .ifEmpty { null }
                                ?.let { OffsetDateTime.parse(it) }
                        )
                    },
                nextNote = noteRepository
                    .getBy(currentDate.plusDays(1))
                    ?.let { noteData ->
                        DayScreenNote(
                            date = currentDate.plusDays(1),
                            textValue = TextFieldValue(text = noteData.text),
                            reminderDateTime = noteData.reminderDateTime
                                .ifEmpty { null }
                                ?.let { OffsetDateTime.parse(it) }
                        )
                    },
                noteMode = DayNoteMode.Reading,
                noteBackup = null,
                isReminderDialogOpen = false
            )
        )
    )
    val focusRequester = remember { FocusRequester() }

    Box(modifier = Modifier.fillMaxSize()) {
        InfiniteHorizontalPager(
            verticalAlignment = Alignment.Top,
            key = { currentDate.plusDays(it.toLong()) },
            onPageChange = { page ->
                val date = currentDate.plusDays(page.toLong())
                mainViewModel.onEvent(MainEvent.DayScreenDateChange(date))
                viewModel.onEvent(
                    DayScreenEvent.DateChange(
                        note = noteRepository.getBy(date)?.let { noteData ->
                            DayScreenNote(
                                date = currentDate,
                                textValue = TextFieldValue(text = noteData.text),
                                reminderDateTime = noteData.reminderDateTime
                                    .ifEmpty { null }
                                    ?.let { OffsetDateTime.parse(it) }
                            )
                        },
                        prevNote = noteRepository
                            .getBy(date.minusDays(1))?.let { noteData ->
                                DayScreenNote(
                                    date = date.minusDays(1),
                                    textValue = TextFieldValue(text = noteData.text),
                                    reminderDateTime = noteData.reminderDateTime
                                        .ifEmpty { null }
                                        ?.let { OffsetDateTime.parse(it) }
                                )
                            },
                        nextNote = noteRepository
                            .getBy(date.plusDays(1))?.let { noteData ->
                                DayScreenNote(
                                    date = date.plusDays(1),
                                    textValue = TextFieldValue(text = noteData.text),
                                    reminderDateTime = noteData.reminderDateTime
                                        .ifEmpty { null }
                                        ?.let { OffsetDateTime.parse(it) }
                                )
                            },
                    )
                )
                viewModel.onEvent(
                    DayScreenEvent.NoteModeChange(DayNoteMode.Reading)
                )
            }
        ) {
            val date = currentDate.plusDays(it.toLong())

            DayPageLayout(
                logger = logger,
                modifier = Modifier.fillMaxSize(),
                mainViewModel = mainViewModel,
                viewModel = viewModel,
                focusRequester = focusRequester,
                date = date
            )
        }
        ActionButtons(
            logger = logger,
            mainViewModel = mainViewModel,
            viewModel = viewModel,
            noteRepository = noteRepository,
            permissionManager = permissionManager,
            notificationManager = notificationManager,
            focusRequester = focusRequester
        )
        LaunchedEffect(Unit) {
            if (isCreateOrEditRequested) {
                val note = viewModel.state.note

                if (note == null) {
                    viewModel.onEvent(
                        DayScreenEvent.NoteModeChange(DayNoteMode.Adding)
                    )
                    viewModel.onEvent(
                        DayScreenEvent.NoteChange(
                            DayScreenNote(
                                date = mainViewModel.state.dayScreenDate,
                                textValue = TextFieldValue()
                            )
                        )
                    )
                } else {
                    viewModel.onEvent(
                        DayScreenEvent.NoteModeChange(DayNoteMode.Editing)
                    )
                    viewModel.onEvent(
                        DayScreenEvent.NoteChange(
                            DayScreenNote(
                                date = note.date,
                                textValue = note.textValue.copy(
                                    selection = TextRange(note.textValue.text.length)
                                ),
                                reminderDateTime = note.reminderDateTime
                            )
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.DayDateText(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    date: LocalDate
) {
    val themeColors = viewModel.state.themeColors

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth()
            .weight(0.6f)
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.5f)
        )
        BasicText(
            text = date.dayOfMonth.toString(),
            style = LocalTextStyle.current.copy(
                color = themeColors.textColor,
                textAlign = TextAlign.Center,
                fontSize = 140.sp
            ),
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .weight(4f)
        )
        BasicText(
            text = date.month.getLocalizedGenitiveCaseName(),
            style = LocalTextStyle.current.copy(
                color = themeColors.textColor,
                textAlign = TextAlign.Center,
                fontSize = 36.sp
            ),
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.5f)
        )
        BasicText(
            text = date.dayOfWeek.getLocalizedName(),
            style = LocalTextStyle.current.copy(
                color = themeColors.textColor,
                textAlign = TextAlign.Center,
                fontSize = 24.sp
            ),
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}

@Composable
private fun ActionButton(
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    onClick: () -> Unit,
    themeColors: ThemeColors,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn() + expandIn(),
        exit = shrinkOut() + scaleOut()
    ) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = themeColors.primaryColor,
            contentColor = themeColors.buttonTextColor,
            modifier = modifier.padding(8.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun ActionButton(
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    onClick: () -> Unit,
    themeColors: ThemeColors,
    icon: Painter
) = ActionButton(
    modifier = modifier,
    visible = visible,
    onClick = onClick,
    themeColors = themeColors,
    content = {
        Icon(painter = icon, contentDescription = "")
    }
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BoxScope.ActionButtons(
    @Suppress("unused") logger: ILogger,
    mainViewModel: MainViewModel,
    viewModel: DayScreenViewModel,
    noteRepository: NoteRepository,
    permissionManager: AppPermissionManager,
    notificationManager: AppNotificationManager,
    focusRequester: FocusRequester
) {
    val themeColors = mainViewModel.state.themeColors
    val dialogColors = CardDefaults.cardColors().copy(
        containerColor = themeColors.backgroundColor,
        contentColor = themeColors.backgroundColor,
    )
    val timePickerColors = TimePickerDefaults.colors().copy(
        clockDialColor = themeColors.backgroundColorVariant,
        selectorColor = themeColors.primaryColor,
        containerColor = themeColors.textColor,
        periodSelectorBorderColor = themeColors.textColor,
        clockDialSelectedContentColor = themeColors.buttonTextColor,
        clockDialUnselectedContentColor = themeColors.textColor,
        periodSelectorSelectedContainerColor = themeColors.primaryColor,
        periodSelectorUnselectedContainerColor = themeColors.backgroundColorVariant,
        periodSelectorSelectedContentColor = themeColors.buttonTextColor,
        periodSelectorUnselectedContentColor = themeColors.textColor,
        timeSelectorSelectedContainerColor = themeColors.primaryColor,
        timeSelectorUnselectedContainerColor = themeColors.backgroundColorVariant,
        timeSelectorSelectedContentColor = themeColors.buttonTextColor,
        timeSelectorUnselectedContentColor = themeColors.textColor,
    )
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    TimePickerDialog(
        title = "Set reminder", // TODO: add to strings.xml,
        initialTime = OffsetDateTime.now().toLocalTime(),
        isOpen = viewModel.state.isReminderDialogOpen,
        titleColor = themeColors.textColor,
        buttonColor = themeColors.primaryColor,
        dialogColors = dialogColors,
        timePickerColors = timePickerColors,
        onConfirm = { localTime ->
            val offsetNow = OffsetDateTime.now()
            val localNow = LocalDateTime.now()
            val dateTime = OffsetDateTime.of(
                if (localTime > localNow.toLocalTime()) localNow.toLocalDate()
                else localNow.toLocalDate().plusDays(1),
                localTime,
                offsetNow.offset
            )
            val note = viewModel.state.note

            if (note != null) {
                val noteData = note.let { noteRepository.getBy(it.date) }

                if (noteData != null) {
                    noteRepository.update(noteData.copy(reminderDateTime = dateTime.toString()))
                }
                viewModel.onEvent(
                    DayScreenEvent.NoteChange(note.copy(reminderDateTime = dateTime))
                )
            }
            viewModel.onEvent(DayScreenEvent.ReminderDialogStateChange(false))
        },
        onDismiss = {
            viewModel.onEvent(DayScreenEvent.ReminderDialogStateChange(false))
        }
    )
    Row(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(8.dp)
    ) {
        ActionButton(
            onClick = {
                viewModel.onEvent(DayScreenEvent.ReminderDialogStateChange(true))
            },
            visible = viewModel.state.noteMode == DayNoteMode.Reading &&
                viewModel.state.note != null,
            themeColors = themeColors,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(R.drawable.icon_outline_rounded_notifications),
                    contentDescription = ""
                )
                val note = viewModel.state.note
                val deleteReminder = {
                    val note = viewModel.state.note
                    val noteData = note?.let { noteRepository.getBy(it.date) }

                    if (note != null && noteData != null) {
                        noteRepository.update(noteData.copy(reminderDateTime = ""))
                        viewModel.onEvent(
                            DayScreenEvent.NoteChange(
                                note.copy(reminderDateTime = null)
                            )
                        )
                    }
                }
                if (note?.reminderDateTime != null) {
                    if (note.reminderDateTime > OffsetDateTime.now()) {
                        TimedContent(note.reminderDateTime) { remaining ->
                            val locale = Locale.current.platformLocale
                            val text = remaining.toComponents { hours, minutes, s, _ ->
                                String.format(
                                    locale, "%02d:%02d:%02d", hours, minutes, s
                                )
                            }
                            Text(text = text)

                            if (remaining == KotlinDuration.ZERO) {
                                deleteReminder()
                            }
                        }
                    } else {
                        deleteReminder()
                    }
                }
            }
        }
        ActionButton(
            onClick = {
                val note = viewModel.state.noteBackup
                val noteData = note?.let { noteRepository.getBy(it.date) }

                if (note != null) {
                    if (noteData == null) {
                        noteRepository.add(
                            NoteData(
                                date = mainViewModel.state.dayScreenDate.toString(),
                                text = note.textValue.text
                            )
                        )
                    } else {
                        noteRepository.update(noteData.copy(text = note.textValue.text))
                    }
                }
                viewModel.onEvent(
                    DayScreenEvent.NoteChange(note)
                )
            },
            visible = viewModel.state.noteMode == DayNoteMode.Reading &&
                viewModel.state.note == null &&
                viewModel.state.noteBackup != null,
            themeColors = themeColors,
            icon = painterResource(R.drawable.icon_rounded_undo)
        )
        ActionButton(
            onClick = {
                val note = viewModel.state.note

                if (note == null) {
                    viewModel.onEvent(
                        DayScreenEvent.NoteModeChange(DayNoteMode.Adding)
                    )
                    viewModel.onEvent(
                        DayScreenEvent.NoteChange(
                            DayScreenNote(
                                date = mainViewModel.state.dayScreenDate,
                                textValue = TextFieldValue()
                            )
                        )
                    )
                } else {
                    viewModel.onEvent(
                        DayScreenEvent.NoteModeChange(DayNoteMode.Editing)
                    )
                    viewModel.onEvent(
                        DayScreenEvent.NoteChange(
                            DayScreenNote(
                                date = note.date,
                                textValue = note.textValue.copy(
                                    selection = TextRange(note.textValue.text.length)
                                ),
                                reminderDateTime = note.reminderDateTime
                            )
                        )
                    )
                }
            },
            visible = viewModel.state.noteMode == DayNoteMode.Reading,
            themeColors = themeColors,
            icon = painterResource(
                if (viewModel.state.note == null) R.drawable.icon_rounded_plus
                else R.drawable.icon_rounded_edit
            )
        )
        ActionButton(
            onClick = {
                val note = viewModel.state.note
                val noteData = note?.let { noteRepository.getBy(it.date) }

                if (noteData != null) {
                    noteRepository.delete(noteData)
                }
                viewModel.onEvent(DayScreenEvent.NoteChange(null))
                viewModel.onEvent(
                    DayScreenEvent.NoteModeChange(DayNoteMode.Reading)
                )
                focusRequester.freeFocus()
                keyboardController?.hide()
                focusManager.clearFocus()
            },
            visible = viewModel.state.noteMode == DayNoteMode.Editing,
            themeColors = themeColors,
            icon = painterResource(R.drawable.icon_rounded_delete)
        )
        ActionButton(
            onClick = {
                val note = viewModel.state.note
                val noteMode = viewModel.state.noteMode

                if (noteMode == DayNoteMode.Editing) {
                    val noteData = note?.let { noteRepository.getBy(it.date) }

                    if (noteData != null) {
                        viewModel.onEvent(
                            DayScreenEvent.NoteChange(
                                DayScreenNote(
                                    date = note.date,
                                    textValue = TextFieldValue(noteData.text),
                                    reminderDateTime = note.reminderDateTime
                                )
                            )
                        )
                    }
                } else if (noteMode == DayNoteMode.Adding) {
                    viewModel.onEvent(DayScreenEvent.NoteChange(null))
                }
                viewModel.onEvent(
                    DayScreenEvent.NoteModeChange(DayNoteMode.Reading)
                )
                focusRequester.freeFocus()
                keyboardController?.hide()
                focusManager.clearFocus()
            },
            visible = viewModel.state.noteMode == DayNoteMode.Editing ||
                viewModel.state.noteMode == DayNoteMode.Adding,
            themeColors = themeColors,
            icon = painterResource(R.drawable.icon_rounded_edit_off)
        )
        ActionButton(
            onClick = {
                val note = viewModel.state.note

                if (note != null) {
                    val noteData = note.let { noteRepository.getBy(it.date) }

                    if (noteData == null) {
                        noteRepository.add(
                            NoteData(
                                date = mainViewModel.state.dayScreenDate.toString(),
                                text = note.textValue.text
                            )
                        )
                    } else {
                        noteRepository.update(noteData.copy(text = note.textValue.text))
                    }
                    viewModel.onEvent(
                        DayScreenEvent.NoteChange(
                            DayScreenNote(note.date, note.textValue, note.reminderDateTime)
                        )
                    )
                    viewModel.onEvent(
                        DayScreenEvent.NoteModeChange(DayNoteMode.Reading)
                    )
                }
                focusRequester.freeFocus()
                keyboardController?.hide()
                focusManager.clearFocus()
            },
            visible = viewModel.state.noteMode == DayNoteMode.Editing ||
                viewModel.state.noteMode == DayNoteMode.Adding,
            themeColors = themeColors,
            icon = painterResource(R.drawable.icon_rounded_check)
        )
    }
}

@Composable
fun DayPageLayout(
    @Suppress("unused") logger: ILogger,
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel,
    viewModel: DayScreenViewModel,
    focusRequester: FocusRequester,
    date: LocalDate
) {
    val themeColors = mainViewModel.state.themeColors
    val dayScreenDate = mainViewModel.state.dayScreenDate
    val note = when {
        date > dayScreenDate -> viewModel.state.nextNote
        date < dayScreenDate -> viewModel.state.prevNote
        else -> viewModel.state.note
    }
    val noteMode =
        if (date == dayScreenDate) viewModel.state.noteMode
        else DayNoteMode.Reading
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = modifier) {
        DayDateText(
            viewModel = mainViewModel,
            date = date
        )
        Column(
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            AnimatedVisibility(
                visible = note != null ||
                    noteMode == DayNoteMode.Editing ||
                    noteMode == DayNoteMode.Adding,
                enter = scaleIn() + expandIn(),
                exit = shrinkOut() + scaleOut()
            ) {
                DayNote(
                    color = themeColors.noteColor,
                    bendTint = themeColors.noteColorVariant,
                    bendWidth = with(LocalDensity.current) { 32.dp.toPx() },
                    bendShadowWidth = with(LocalDensity.current) { 1.dp.toPx() },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp)
                    ) {
                        if (note != null) {
                            BasicTextField(
                                value = note.textValue,
                                onValueChange = {
                                    if (viewModel.state.noteMode != DayNoteMode.Reading) {
                                        viewModel.onEvent(
                                            DayScreenEvent.NoteChange(
                                                DayScreenNote(
                                                    date = date,
                                                    textValue = it,
                                                    reminderDateTime = note.reminderDateTime
                                                )
                                            )
                                        )
                                    }
                                },
                                readOnly = noteMode == DayNoteMode.Reading,
                                minLines = 1,
                                maxLines = Int.MAX_VALUE,
                                textStyle = TextStyle(
                                    color = themeColors.noteTextColor,
                                    fontSize = 24.sp,
                                    lineHeight = 26.sp,
                                ),
                                cursorBrush = SolidColor(themeColors.secondaryColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                                    .padding(end = 24.dp)
                                    .then(
                                        when (date == dayScreenDate) {
                                            true -> Modifier.focusRequester(focusRequester)
                                            false -> Modifier
                                        }
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.height(72.dp))
                        LaunchedEffect(noteMode) {
                            when (noteMode) {
                                DayNoteMode.Reading -> {
                                    focusRequester.freeFocus()
                                    keyboardController?.hide()
                                }

                                DayNoteMode.Editing, DayNoteMode.Adding -> {
                                    focusRequester.requestFocus()
                                    keyboardController?.show()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}