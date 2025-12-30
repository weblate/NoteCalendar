package com.sztorm.notecalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.sztorm.notecalendar.NoteCalendarApplication.Companion.BUNDLE_KEY_NOTIFICATION_LAUNCH_DAY_SCREEN
import com.sztorm.notecalendar.repositories.FileRepository
import com.sztorm.notecalendar.repositories.FileRepositoryImpl
import com.sztorm.notecalendar.repositories.NoteRepository
import com.sztorm.notecalendar.repositories.NoteRepositoryImpl
import com.sztorm.notecalendar.repositories.UserPreferencesRepository
import com.sztorm.notecalendar.ui.AppTheme
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.time.LocalDate

data class BundleResult(val isLaunchedFromNotification: Boolean)

class MainActivity : ComponentActivity() {
    private var _settings: UserPreferencesRepository? = null
    private var _permissionManager: AppPermissionManager? = null
    private var _notificationManager: AppNotificationManager? = null
    val settings: UserPreferencesRepository
        get() = _settings!!
    val permissionManager: AppPermissionManager
        get() = _permissionManager!!
    val notificationManager: AppNotificationManager
        get() = _notificationManager!!

    fun initManagers() {
        _settings = _settings ?: UserPreferencesRepository(this)
        _permissionManager = _permissionManager ?: AppPermissionManager(this)
        _notificationManager = _notificationManager ?: AppNotificationManager(this)
    }

    private fun readBundle(): BundleResult? {
        val bundle: Bundle = intent.extras ?: return null

        val isLaunchedFromNotification = bundle.getBoolean(
            BUNDLE_KEY_NOTIFICATION_LAUNCH_DAY_SCREEN, false
        )
        return BundleResult(isLaunchedFromNotification = isLaunchedFromNotification)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initManagers()
        val bundleResult = readBundle()
        val viewModel: MainViewModel
        val startingView: StartingViewType
        val noteRepository = NoteRepositoryImpl
        val fileRepository = FileRepositoryImpl(this)

        runBlocking {
            viewModel = MainViewModel(
                initialState = MainState(
                    themeColors = settings.getThemeColors(),
                    dayScreenDate = LocalDate.now()
                )
            )
            startingView = if (bundleResult != null && bundleResult.isLaunchedFromNotification) {
                StartingViewType.DAY_VIEW
            } else settings.getStartingView(StartingViewType.DAY_VIEW)
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContent {
            AppTheme(viewModel.state.themeColors) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppScreen(
                        viewModel = viewModel,
                        startingView = startingView,
                        mainActivity = this,
                        noteRepository = noteRepository,
                        fileRepository = fileRepository
                    )
                }
            }
        }
    }

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}

data class MainTab(
    val screen: Screen,
    val icon: ImageVector,
    val description: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(
    viewModel: MainViewModel,
    startingView: StartingViewType,
    mainActivity: MainActivity,
    noteRepository: NoteRepository,
    fileRepository: FileRepository
) {
    val navController = rememberNavController()
    var selectedTabIndex by rememberSaveable {
        mutableIntStateOf(
            when (startingView) {
                StartingViewType.DAY_VIEW -> 2
                StartingViewType.WEEK_VIEW -> 1
                StartingViewType.MONTH_VIEW -> 0
            }
        )
    }
    LaunchedEffect(Unit) {
        if (mainActivity.notificationManager.tryScheduleNotification(
                args = ScheduleNoteNotificationArguments(),
                noteRepository = noteRepository
            )
        ) {
            Timber.i(
                "${LogTags.NOTIFICATIONS} Scheduled notification upon MainActivity creation"
            )
        }
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
                icon = ImageVector.vectorResource(R.drawable.icon_calendar_month),
                description = "Month" // TODO: Add to strings.xml
            ),
            MainTab(
                screen = Screen.Week,
                icon = ImageVector.vectorResource(R.drawable.icon_calendar_week),
                description = "Week" // TODO: Add to strings.xml
            ),
            MainTab(
                screen = Screen.Day(),
                icon = ImageVector.vectorResource(R.drawable.icon_calendar_day),
                description = "Day" // TODO: Add to strings.xml
            ),
            MainTab(
                screen = Screen.Settings,
                icon = ImageVector.vectorResource(R.drawable.icon_settings),
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
                        )
                    }
                )
            }
        }
        NavHost(
            navController = navController,
            startDestination = when (startingView) {
                StartingViewType.DAY_VIEW -> Screen.Day()
                StartingViewType.WEEK_VIEW -> Screen.Week
                StartingViewType.MONTH_VIEW -> Screen.Month
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
                MonthScreen(viewModel, navController, mainActivity, noteRepository)
            }
            composable<Screen.Week> {
                WeekScreen(viewModel, navController, mainActivity, noteRepository)
            }
            composable<Screen.Day> {
                val day = it.toRoute<Screen.Day>()

                DayScreen(viewModel, mainActivity, noteRepository, day.isCreateOrEditRequested)
            }
            composable<Screen.Settings> {
                SettingsScreen(
                    viewModel = viewModel,
                    preferencesRepository = mainActivity.settings,
                    noteRepository = noteRepository,
                    fileRepository = fileRepository,
                    notificationManager = mainActivity.notificationManager
                )
            }
        }
    }
}