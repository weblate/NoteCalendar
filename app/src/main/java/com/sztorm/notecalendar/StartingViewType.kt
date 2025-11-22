package com.sztorm.notecalendar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource

enum class StartingViewType {
    DAY_VIEW,
    WEEK_VIEW,
    MONTH_VIEW;
}

@Composable
@ReadOnlyComposable
fun StartingViewType.getLocalizedName() =
    when (this) {
        StartingViewType.DAY_VIEW -> stringResource(R.string.DayView)
        StartingViewType.WEEK_VIEW -> stringResource(R.string.WeekView)
        StartingViewType.MONTH_VIEW -> stringResource(R.string.MonthView)
    }