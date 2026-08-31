package com.sztorm.notecalendar.screens

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.sztorm.notecalendar.AppNotificationManager
import com.sztorm.notecalendar.AppPermissionManager
import com.sztorm.notecalendar.ILogger
import com.sztorm.notecalendar.R
import com.sztorm.notecalendar.StartingScreenType
import com.sztorm.notecalendar.repositories.FileRepository
import com.sztorm.notecalendar.repositories.NoteRepository
import com.sztorm.notecalendar.repositories.UserPreferencesRepository
import com.sztorm.notecalendar.viewmodels.MainViewModel

private data class MainTab(
    val screen: Screen,
    val icon: ImageVector,
    val description: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(
    logger: ILogger,
    viewModel: MainViewModel,
    startingView: StartingScreenType,
    permissionManager: AppPermissionManager,
    notificationManager: AppNotificationManager,
    noteRepository: NoteRepository,
    fileRepository: FileRepository,
    preferencesRepository: UserPreferencesRepository
) {
    val navController = rememberNavController()
    var selectedTabIndex by rememberSaveable {
        mutableIntStateOf(
            when (startingView) {
                StartingScreenType.DayScreen -> 2
                StartingScreenType.WeekScreen -> 1
                StartingScreenType.MonthScreen -> 0
            }
        )
    }
    DisposableEffect(Unit) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            selectedTabIndex = when (destination.route) {
                Screen.Month.route -> 0
                Screen.Week.route -> 1
                Screen.Day().route -> 2
                Screen.Settings.route -> 3
                else -> selectedTabIndex
            }
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }
    Column(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .fillMaxWidth()
    ) {
        val tabs = listOf(
            MainTab(
                screen = Screen.Month,
                icon = ImageVector.vectorResource(R.drawable.icon_outline_rounded_month),
                description = "Month" // TODO: Add to strings.xml
            ),
            MainTab(
                screen = Screen.Week,
                icon = ImageVector.vectorResource(R.drawable.icon_outline_rounded_week),
                description = "Week" // TODO: Add to strings.xml
            ),
            MainTab(
                screen = Screen.Day(),
                icon = ImageVector.vectorResource(R.drawable.icon_outline_rounded_day),
                description = "Day" // TODO: Add to strings.xml
            ),
            MainTab(
                screen = Screen.Settings,
                icon = ImageVector.vectorResource(R.drawable.icon_outline_rounded_settings),
                description = "Settings" // TODO: Add to strings.xml
            )
        )
        PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { i, tab ->
                Tab(
                    selected = selectedTabIndex == i,
                    onClick = {
                        navController.navigate(tab.screen)
                        selectedTabIndex = i
                    },
                    text = {
                        Text(
                            text = tab.description,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.description,
                            modifier = Modifier.defaultMinSize(36.dp, 36.dp)
                        )
                    }
                )
            }
        }
        NavHost(
            navController = navController,
            startDestination = when (startingView) {
                StartingScreenType.DayScreen -> Screen.Day()
                StartingScreenType.WeekScreen -> Screen.Week
                StartingScreenType.MonthScreen -> Screen.Month
            },
            enterTransition = {
                slideInHorizontally(animationSpec = tween(durationMillis = 400)) { -it }
            },
            exitTransition = {
                fadeOut(animationSpec = tween(durationMillis = 400)) +
                    slideOutVertically(animationSpec = tween(durationMillis = 400)) { it }
            }
        ) {
            composable<Screen.Month> {
                MonthScreen(
                    viewModel = viewModel,
                    navController = navController,
                    noteRepository = noteRepository,
                    preferencesRepository = preferencesRepository
                )
            }
            composable<Screen.Week> {
                WeekScreen(
                    viewModel = viewModel,
                    navController = navController,
                    noteRepository = noteRepository,
                    preferencesRepository = preferencesRepository
                )
            }
            composable<Screen.Day> {
                val day = it.toRoute<Screen.Day>()

                DayScreen(
                    logger = logger,
                    mainViewModel = viewModel,
                    permissionManager = permissionManager,
                    notificationManager = notificationManager,
                    noteRepository = noteRepository,
                    isCreateOrEditRequested = day.isCreateOrEditRequested
                )
            }
            composable<Screen.Settings> {
                SettingsScreen(
                    logger = logger,
                    viewModel = viewModel,
                    notificationManager = notificationManager,
                    fileRepository = fileRepository,
                    noteRepository = noteRepository,
                    preferencesRepository = preferencesRepository
                )
            }
        }
    }
}