package com.sztorm.notecalendar.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sztorm.notecalendar.AppNotificationManager
import com.sztorm.notecalendar.AppPermissionManager
import com.sztorm.notecalendar.LogTags
import com.sztorm.notecalendar.MainEvent
import com.sztorm.notecalendar.MainViewModel
import com.sztorm.notecalendar.NoteData
import com.sztorm.notecalendar.R
import com.sztorm.notecalendar.ScheduleNoteNotificationArguments
import com.sztorm.notecalendar.components.DayNote
import com.sztorm.notecalendar.components.ThemedButton
import com.sztorm.notecalendar.components.ThemedIconButton
import com.sztorm.notecalendar.getLocalizedGenitiveCaseName
import com.sztorm.notecalendar.getLocalizedName
import com.sztorm.notecalendar.repositories.NoteRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import kotlin.math.absoluteValue
import kotlin.math.sign

enum class DayNoteState {
    Empty,
    Reading,
    Editing,
    Adding
}

enum class DayNoteTransitionState {
    Empty,
    Reading,
    Adding
}

@Composable
fun DayScreen(
    viewModel: MainViewModel,
    permissionManager: AppPermissionManager,
    notificationManager: AppNotificationManager,
    noteRepository: NoteRepository,
    isCreateOrEditRequested: Boolean = false
) {
    var prevDate: LocalDate by remember { mutableStateOf(viewModel.state.dayScreenDate) }
    var startDate: LocalDate by remember { mutableStateOf(prevDate) }
    var isCreateOrEditRequested by remember { mutableStateOf(isCreateOrEditRequested) }

    AnimatedContent(
        targetState = startDate,
        transitionSpec = {
            when {
                prevDate > startDate -> slideInHorizontally(
                    animationSpec = tween(durationMillis = 400),
                    initialOffsetX = { -it }
                ) togetherWith slideOutHorizontally(
                    animationSpec = tween(durationMillis = 400),
                    targetOffsetX = { it }
                )

                else -> slideInHorizontally(
                    animationSpec = tween(durationMillis = 400),
                    initialOffsetX = { it }
                ) togetherWith slideOutHorizontally(
                    animationSpec = tween(durationMillis = 400),
                    targetOffsetX = { -it }
                )
            }
        }
    ) { targetState ->
        DayPageLayout(
            draggableModifier = Modifier.draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { },
                onDragStopped = { velocity ->
                    if (velocity.absoluteValue > 0.5) {
                        isCreateOrEditRequested = false
                        prevDate = startDate
                        startDate = startDate.minusDays(velocity.sign.toLong())
                        viewModel.onEvent(
                            MainEvent.DayScreenDateChange(startDate)
                        )
                    }
                }
            ),
            viewModel = viewModel,
            permissionManager = permissionManager,
            notificationManager = notificationManager,
            noteRepository = noteRepository,
            date = targetState,
            isCreateOrEditRequested = isCreateOrEditRequested,
        )
    }
}

@Composable
fun DayPageLayout(
    modifier: Modifier = Modifier,
    draggableModifier: Modifier = Modifier,
    viewModel: MainViewModel,
    permissionManager: AppPermissionManager,
    notificationManager: AppNotificationManager,
    noteRepository: NoteRepository,
    date: LocalDate,
    isCreateOrEditRequested: Boolean
) {
    LaunchedEffect(Unit) {
        viewModel.onEvent(MainEvent.DayScreenDateChange(date))
    }
    val noteState = remember {
        mutableStateOf(noteRepository.getBy(date))
    }
    val undoNoteState: MutableState<NoteData?> = remember { mutableStateOf(null) }
    val themeColors = viewModel.state.themeColors
    val dayNoteState = remember {
        mutableStateOf(
            when (Pair(noteState.value != null, isCreateOrEditRequested)) {
                Pair(true, true) -> DayNoteState.Editing
                Pair(true, false) -> DayNoteState.Reading
                Pair(false, true) -> DayNoteState.Adding
                else -> DayNoteState.Empty
            }
        )
    }
    val noteTransitionState = when (dayNoteState.value) {
        DayNoteState.Empty -> DayNoteTransitionState.Empty
        DayNoteState.Adding -> DayNoteTransitionState.Adding
        else -> DayNoteTransitionState.Reading
    }
    val focusRequester = remember { FocusRequester() }

    Column(modifier = modifier) {
        Column(
            modifier = draggableModifier
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(10.dp)
        ) {
            val inTransition = updateTransition(
                targetState = noteTransitionState != DayNoteTransitionState.Empty
            )
            val inScaleY by inTransition.animateFloat(
                transitionSpec = { tween(durationMillis = 500) },
                targetValueByState = { expanded ->
                    if (expanded) 1f else 0f
                }
            )
            AnimatedContent(
                targetState = noteTransitionState,
                transitionSpec = {
                    when (targetState) {
                        DayNoteTransitionState.Empty -> {
                            slideInHorizontally(
                                animationSpec = tween(durationMillis = 500),
                                initialOffsetX = { -it }
                            ) togetherWith
                                fadeOut(animationSpec = tween(durationMillis = 400)) +
                                slideOutVertically(
                                    animationSpec = tween(durationMillis = 500),
                                    targetOffsetY = { (it * 0.1f).toInt() }
                                )
                        }

                        DayNoteTransitionState.Adding -> {
                            fadeIn(animationSpec = tween(durationMillis = 400)) togetherWith
                                ExitTransition.None
                        }

                        else -> {
                            EnterTransition.None togetherWith
                                fadeOut(animationSpec = tween(durationMillis = 400)) +
                                slideOutVertically(
                                    animationSpec = tween(durationMillis = 500),
                                    targetOffsetY = { it }
                                )
                        }
                    }
                },
                modifier = Modifier.graphicsLayer(scaleX = 1f)
            ) { targetState ->
                when (targetState) {
                    DayNoteTransitionState.Empty -> {
                        DayNoteEmptyLayout(
                            draggableModifier = draggableModifier,
                            viewModel = viewModel,
                            permissionManager = permissionManager,
                            notificationManager = notificationManager,
                            noteRepository = noteRepository,
                            dayNoteState = dayNoteState,
                            noteState = noteState,
                            undoNoteState = undoNoteState
                        )
                    }

                    DayNoteTransitionState.Reading -> {
                        DayNoteLayout(
                            modifier = Modifier.graphicsLayer(scaleY = inScaleY),
                            viewModel = viewModel,
                            permissionManager = permissionManager,
                            notificationManager = notificationManager,
                            noteRepository = noteRepository,
                            focusRequester = focusRequester,
                            dayNoteState = dayNoteState,
                            noteState = noteState,
                            undoNoteState = undoNoteState,
                        )
                    }

                    DayNoteTransitionState.Adding -> {
                        DayNoteAddLayout(
                            modifier = Modifier.graphicsLayer(scaleY = inScaleY),
                            viewModel = viewModel,
                            permissionManager = permissionManager,
                            notificationManager = notificationManager,
                            noteRepository = noteRepository,
                            focusRequester = focusRequester,
                            dayNoteState = dayNoteState,
                            noteState = noteState,
                            undoNoteState = undoNoteState,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DayNoteEmptyLayout(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    permissionManager: AppPermissionManager,
    notificationManager: AppNotificationManager,
    draggableModifier: Modifier,
    noteRepository: NoteRepository,
    dayNoteState: MutableState<DayNoteState>,
    noteState: MutableState<NoteData?>,
    undoNoteState: MutableState<NoteData?>,
) {
    val themeColors = viewModel.state.themeColors
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = modifier.fillMaxWidth()
        ) {
            ThemedButton(
                onClick = { dayNoteState.value = DayNoteState.Adding },
                themeColors = themeColors,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp, top = 5.dp, end = 10.dp, bottom = 0.dp),
                text = stringResource(R.string.AddNote),
                icon = painterResource(R.drawable.icon_note_add)
            )
            val undoNote = undoNoteState.value

            if (undoNote != null) {
                ThemedButton(
                    onClick = {
                        undoNoteState.value = null
                        noteState.value = undoNote
                        noteRepository.add(undoNote)
                        dayNoteState.value = DayNoteState.Reading
                        notificationManager.tryScheduleNotification(
                            args = ScheduleNoteNotificationArguments(note = undoNote),
                            permissionManager = permissionManager,
                            noteRepository = noteRepository,
                            coroutineScope = coroutineScope
                        ) { isSuccess ->
                            if (isSuccess) {

                                Timber.i("${LogTags.NOTIFICATIONS} Scheduled notification after note save")
                            }
                        }
                    },
                    themeColors = themeColors,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp, top = 5.dp, end = 10.dp, bottom = 0.dp),
                    text = stringResource(R.string.UndoDeletion),
                    icon = painterResource(R.drawable.icon_undo)
                )
            }
        }
        Spacer(modifier = draggableModifier.fillMaxSize())
    }
}

@Composable
fun DayNoteAddLayout(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    permissionManager: AppPermissionManager,
    notificationManager: AppNotificationManager,
    noteRepository: NoteRepository,
    focusRequester: FocusRequester,
    dayNoteState: MutableState<DayNoteState>,
    noteState: MutableState<NoteData?>,
    undoNoteState: MutableState<NoteData?>
) {
    val themeColors = viewModel.state.themeColors
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    var noteTextFieldState by remember { mutableStateOf(TextFieldState()) }

    DayNote(
        color = themeColors.noteColor,
        bendTint = themeColors.noteColorVariant,
        bendShadowWidth = with(LocalDensity.current) { 2.dp.toPx() },
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
        ) {
            ThemedButton(
                onClick = {
                    focusManager.clearFocus()
                    val noteData = NoteData(
                        date = viewModel.state.dayScreenDate.toString(),
                        text = noteTextFieldState.text.toString()
                    )
                    noteRepository.add(noteData)
                    noteState.value = noteData
                    undoNoteState.value = null
                    dayNoteState.value = DayNoteState.Reading
                    notificationManager.tryScheduleNotification(
                        args = ScheduleNoteNotificationArguments(note = noteData),
                        permissionManager = permissionManager,
                        noteRepository = noteRepository,
                        coroutineScope = coroutineScope
                    ) { isSuccess ->
                        if (isSuccess) {
                            Timber.i("${LogTags.NOTIFICATIONS} Scheduled notification after note save")
                        }
                    }
                },
                themeColors = themeColors,
                modifier = Modifier
                    .wrapContentSize()
                    .padding(start = 10.dp, top = 10.dp, end = 0.dp, bottom = 10.dp),
                text = stringResource(R.string.Save),
                icon = painterResource(R.drawable.icon_add)
            )
            ThemedButton(
                onClick = {
                    focusManager.clearFocus()
                    noteTextFieldState = TextFieldState()
                    dayNoteState.value = DayNoteState.Empty
                },
                themeColors = themeColors,
                modifier = Modifier
                    .wrapContentSize()
                    .padding(start = 5.dp, top = 10.dp, end = 0.dp, bottom = 10.dp),
                text = stringResource(R.string.Cancel),
                icon = painterResource(R.drawable.icon_cancel)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 10.dp, top = 0.dp, end = 10.dp, bottom = 10.dp)
        ) {
            BasicTextField(
                state = noteTextFieldState,
                lineLimits = TextFieldLineLimits.MultiLine(),
                textStyle = TextStyle(
                    color = themeColors.noteTextColor,
                    fontSize = 24.sp,
                    lineHeight = 26.sp,
                ),
                cursorBrush = SolidColor(themeColors.secondaryColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .then(
                        when (dayNoteState.value) {
                            DayNoteState.Empty -> Modifier.focusProperties {
                                canFocus = false
                            }

                            else -> Modifier
                        }
                    )
                    .focusRequester(focusRequester)
            )
        }
    }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
fun DayNoteLayout(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    permissionManager: AppPermissionManager,
    notificationManager: AppNotificationManager,
    noteRepository: NoteRepository,
    focusRequester: FocusRequester,
    dayNoteState: MutableState<DayNoteState>,
    noteState: MutableState<NoteData?>,
    undoNoteState: MutableState<NoteData?>
) {
    val themeColors = viewModel.state.themeColors
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    var noteTextFieldState by remember {
        mutableStateOf(TextFieldState(noteState.value?.text ?: ""))
    }
    DayNote(
        color = themeColors.noteColor,
        bendTint = themeColors.noteColorVariant,
        bendShadowWidth = with(LocalDensity.current) { 2.dp.toPx() },
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
        ) {
            if (dayNoteState.value == DayNoteState.Editing) {
                ThemedButton(
                    onClick = {
                        dayNoteState.value = DayNoteState.Reading
                        val note = NoteData(
                            date = viewModel.state.dayScreenDate.toString(),
                            text = noteTextFieldState.text.toString()
                        )
                        noteRepository.update(note)
                        noteState.value = note
                        focusManager.clearFocus()
                        notificationManager.tryScheduleNotification(
                            args = ScheduleNoteNotificationArguments(note = note),
                            permissionManager = permissionManager,
                            noteRepository = noteRepository,
                            coroutineScope = coroutineScope,
                        ) { isSuccess ->
                            if (isSuccess) {
                                Timber.i("${LogTags.NOTIFICATIONS} Scheduled notification after note edit")
                            }
                        }
                    },
                    themeColors = themeColors,
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(start = 10.dp, top = 10.dp, end = 0.dp, bottom = 10.dp),
                    text = stringResource(R.string.Save),
                    icon = painterResource(R.drawable.icon_add)
                )
                ThemedButton(
                    onClick = {
                        dayNoteState.value = DayNoteState.Reading
                        noteTextFieldState = TextFieldState(noteState.value?.text ?: "")
                        focusManager.clearFocus()
                    },
                    themeColors = themeColors,
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(start = 5.dp, top = 10.dp, end = 0.dp, bottom = 10.dp),
                    text = stringResource(R.string.Cancel),
                    icon = painterResource(R.drawable.icon_cancel)
                )
            } else {
                ThemedIconButton(
                    onClick = {
                        dayNoteState.value = DayNoteState.Editing
                        focusRequester.requestFocus()
                    },
                    themeColors = themeColors,
                    icon = painterResource(R.drawable.icon_edit),
                    contentDescription = stringResource(R.string.EditNote),
                    modifier = Modifier
                        .padding(start = 10.dp, top = 10.dp, end = 5.dp, bottom = 0.dp)
                        .size(48.dp)
                )
                ThemedIconButton(
                    onClick = {
                        val note = noteState.value

                        if (note != null) {
                            undoNoteState.value = note
                            noteRepository.delete(note)
                            coroutineScope.launch {
                                val isCancelled = notificationManager
                                    .tryCancelScheduledNotification(
                                        LocalDate.parse(note.date)
                                    )
                                if (isCancelled) {
                                    Timber.i("${LogTags.NOTIFICATIONS} Canceled notification after note deletion")
                                }
                            }
                        }
                        dayNoteState.value = DayNoteState.Empty
                        focusManager.clearFocus()
                    },
                    themeColors = themeColors,
                    icon = painterResource(R.drawable.icon_delete),
                    contentDescription = stringResource(R.string.DeleteNote),
                    modifier = Modifier
                        .padding(start = 5.dp, top = 10.dp, end = 5.dp, bottom = 0.dp)
                        .size(48.dp)
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 10.dp, top = 0.dp, end = 10.dp, bottom = 10.dp)
        ) {
            BasicTextField(
                state = noteTextFieldState,
                readOnly = dayNoteState.value == DayNoteState.Reading,
                lineLimits = TextFieldLineLimits.MultiLine(),
                textStyle = TextStyle(
                    color = themeColors.noteTextColor,
                    fontSize = 24.sp,
                    lineHeight = 26.sp,
                ),
                cursorBrush = SolidColor(themeColors.secondaryColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .focusRequester(focusRequester)
            )
        }
    }
    LaunchedEffect(dayNoteState.value) {
        if (dayNoteState.value == DayNoteState.Editing) {
            focusRequester.requestFocus()
        }
    }
}