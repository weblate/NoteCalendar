package com.sztorm.notecalendar

import android.app.ActivityOptions
import android.content.Intent
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.savedstate.SavedState
import com.sztorm.notecalendar.NoteCalendarApplication.Companion.BUNDLE_KEY_MAIN_FRAGMENT_TYPE
import com.sztorm.notecalendar.repositories.NoteRepository
import com.sztorm.notecalendar.repositories.NoteRepositoryImpl
import com.sztorm.notecalendar.repositories.UserPreferencesRepository
import com.sztorm.notecalendar.ui.AppTheme
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    private var _settings: UserPreferencesRepository? = null
    private var _permissionManager: AppPermissionManager? = null
    private var _notificationManager: AppNotificationManager? = null
    private var _themePainter: ThemePainter? = null
    val sharedData = AppSharedData(viewedDate = LocalDate.now())
    val settings: UserPreferencesRepository
        get() = _settings!!
    val permissionManager: AppPermissionManager
        get() = _permissionManager!!
    val notificationManager: AppNotificationManager
        get() = _notificationManager!!
    val themePainter: ThemePainter
        get() = _themePainter!!

    fun initManagers() {
        _settings = _settings ?: UserPreferencesRepository(this)
        _permissionManager = _permissionManager ?: AppPermissionManager(this)
        _notificationManager = _notificationManager ?: AppNotificationManager(this)

        runBlocking {
            _themePainter = _themePainter ?: ThemePainter(settings.getThemeColors())
        }
    }

    //private fun setMainFragmentOnCreate() {
    //    val bundle: Bundle? = intent.extras
    //
    //    if (bundle === null) {
    //        lifecycleScope.launch {
    //            setMainFragment(
    //                settings.getStartingView(StartingViewType.DAY_VIEW).toMainFragmentType()
    //            )
    //        }
    //        return
    //    }
    //    val mainFragmentTypeOrdinal: Int = bundle.getInt(
    //        BUNDLE_KEY_MAIN_FRAGMENT_TYPE, MainFragmentType.DAY.ordinal
    //    )
    //    setMainFragment(
    //        MainFragmentType.entries[mainFragmentTypeOrdinal],
    //        resAnimIn = R.anim.anim_immediate,
    //        resAnimOut = R.anim.anim_immediate
    //    )
    //}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        initManagers()
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                AppTheme(themePainter.values) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        AppLayout(this, NoteRepositoryImpl)
                    }
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

    fun restart(startingMainFragment: MainFragmentType) {
        val bundle = Bundle()
        bundle.putInt(BUNDLE_KEY_MAIN_FRAGMENT_TYPE, startingMainFragment.ordinal)

        val intent = Intent(applicationContext, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            .putExtras(bundle)
        val options = ActivityOptions.makeCustomAnimation(baseContext, 0, 0)

        startActivity(intent, options.toBundle())
        finish()
    }

}

data class MainTab(
    val screen: Screen,
    val icon: ImageVector,
    val description: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLayout(
    mainActivity: MainActivity,
    noteRepository: NoteRepository
) {
    val navController = rememberNavController()
    var initialScreen by remember { mutableStateOf<Screen?>(null) }
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val initialView = mainActivity.settings.getStartingView(StartingViewType.DAY_VIEW)

        when (initialView) {
            StartingViewType.DAY_VIEW -> {
                initialScreen = Screen.Day()
                selectedTabIndex = 2
            }

            StartingViewType.WEEK_VIEW -> {
                initialScreen = Screen.Week
                selectedTabIndex = 1
            }

            StartingViewType.MONTH_VIEW -> {
                initialScreen = Screen.Month
                selectedTabIndex = 0
            }
        }
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
        val listener = { _: NavController, destination: NavDestination, _: SavedState? ->
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
        initialScreen?.let { screen ->
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
                startDestination = screen,
                enterTransition = {
                    slideInHorizontally(animationSpec = tween(durationMillis = 400)) { -it }
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(durationMillis = 400)) +
                    slideOutVertically(animationSpec = tween(durationMillis = 400)) { it }
                }
            ) {
                composable<Screen.Month> {
                    MonthLayout(navController, mainActivity, noteRepository)
                }
                composable<Screen.Week> {
                    WeekLayout(navController, mainActivity, noteRepository)
                }
                composable<Screen.Day> {
                    val day = it.toRoute<Screen.Day>()

                    DayLayout(mainActivity, noteRepository, day.isCreateOrEditRequested)
                }
                composable<Screen.Settings> {
                    SettingsLayout(mainActivity, noteRepository)
                }
            }
        }
    }
}