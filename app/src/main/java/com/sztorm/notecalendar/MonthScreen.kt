package com.sztorm.notecalendar

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sztorm.notecalendar.components.DayOfWeekBar
import com.sztorm.notecalendar.components.InfiniteHorizontalPager
import com.sztorm.notecalendar.components.MonthPage
import com.sztorm.notecalendar.repositories.NoteRepository
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

data class MonthViewDay(
    val date: LocalDate,
    val isSelected: Boolean,
    val isToday: Boolean,
    val isInCurrentMonth: Boolean,
    val hasNote: Boolean,
)

@Composable
fun MonthLayout(
    navController: NavController,
    mainActivity: MainActivity,
    noteRepository: NoteRepository
) {
    val themeColors = mainActivity.themeColors
    val selectedDateYearMonth = mainActivity.sharedData.viewedDate.yearMonth
    val today = LocalDate.now()
    var firstDayOfWeek by remember {
        mutableStateOf(WeekFields.of(Locale.getDefault()).firstDayOfWeek)
    }
    var currentYearMonth by remember {
        mutableStateOf(selectedDateYearMonth)
    }
    var notesCache by remember {
        mutableStateOf(MonthNotesCache(noteRepository, selectedDateYearMonth))
    }
    LaunchedEffect(Unit) {
        firstDayOfWeek = mainActivity.settings.getFirstDayOfWeek()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = currentYearMonth.getLocalizedName(),
            fontSize = 38.sp,
            fontWeight = FontWeight.Light,
            color = Color(themeColors.textColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp)
        )
        DayOfWeekBar(
            modifier = Modifier.padding(vertical = 8.dp),
            firstDayOfWeek = firstDayOfWeek,
            dayOfWeekText = { it.getLocalizedShortName() },
            backgroundColor = Color(themeColors.secondaryColor),
            textColor = Color(themeColors.buttonTextColor),
            fontSize = 16.sp,
        )
        InfiniteHorizontalPager(
            verticalAlignment = Alignment.Top,
            key = { selectedDateYearMonth.plusMonths(it.toLong()) },
            onPageChange = { page ->
                val prevYearMonth = currentYearMonth
                currentYearMonth = selectedDateYearMonth.plusMonths(page.toLong())

                notesCache = when {
                    currentYearMonth > prevYearMonth -> notesCache.nextMonth()
                    currentYearMonth < prevYearMonth -> notesCache.prevMonth()
                    else -> notesCache
                }
            }
        ) {
            val yearMonth = selectedDateYearMonth.plusMonths(it.toLong())

            MonthPage(
                modifier = Modifier.fillMaxSize(),
                yearMonth = yearMonth,
                firstDayOfWeek = firstDayOfWeek
            ) { date, modifier ->
                DayLayout(
                    modifier,
                    navController,
                    mainActivity,
                    dayData = MonthViewDay(
                        date = date,
                        isSelected = mainActivity.sharedData.viewedDate == date,
                        isToday = date == today,
                        isInCurrentMonth = date.month == yearMonth.month,
                        hasNote = notesCache.getBy(date) != null
                    )
                )
            }
        }
    }
}

@Composable
private fun DayLayout(
    modifier: Modifier,
    navController: NavController,
    mainActivity: MainActivity,
    dayData: MonthViewDay
) {
    val themeColors = mainActivity.themeColors

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(1f)
            .combinedClickable(
                onClick = {
                    mainActivity.sharedData.viewedDate = dayData.date
                    navController.navigate(Screen.Day())
                },
                onLongClick = {
                    mainActivity.sharedData.viewedDate = dayData.date
                    navController.navigate(Screen.Day(isCreateOrEditRequested = true))
                }
            )
            .drawWithCache {
                val stroke = Stroke(width = 3.dp.toPx())
                val height = size.height
                val radius = height * 0.36f
                val radiusWithStroke = radius + stroke.width * 0.5f
                val secondRadius = radius * 0.8f

                onDrawBehind {
                    when {
                        dayData.isSelected && dayData.hasNote -> {
                            drawCircle(
                                color = Color(themeColors.secondaryColor),
                                radius = secondRadius
                            )
                            drawCircle(
                                color = Color(themeColors.primaryColor),
                                radius = radius,
                                style = stroke
                            )
                        }

                        dayData.isSelected -> {
                            drawCircle(
                                color = Color(themeColors.secondaryColor),
                                radius = radiusWithStroke
                            )
                        }

                        dayData.hasNote && dayData.isInCurrentMonth -> {
                            drawCircle(
                                color = Color(themeColors.primaryColor),
                                radius = radius,
                                style = stroke
                            )
                        }

                        dayData.hasNote -> {
                            drawCircle(
                                color = Color(themeColors.primaryColor)
                                    .copy(alpha = 0.3333333f),
                                radius = radius,
                                style = stroke
                            )
                        }
                    }
                }
            }
    ) {
        Text(
            text = dayData.date.dayOfMonth.toString(),
            color = when {
                dayData.isSelected -> Color(themeColors.buttonTextColor)
                dayData.isToday -> Color(themeColors.secondaryColor)
                dayData.isInCurrentMonth -> Color(themeColors.textColor)
                else -> Color(themeColors.inactiveTextColor)
            }
        )
    }
}