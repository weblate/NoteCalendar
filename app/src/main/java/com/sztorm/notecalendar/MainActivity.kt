package com.sztorm.notecalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.sztorm.notecalendar.repositories.FileRepositoryImpl
import com.sztorm.notecalendar.repositories.NoteRepositoryImpl
import com.sztorm.notecalendar.repositories.UserPreferencesRepository
import com.sztorm.notecalendar.screens.AppScreen
import com.sztorm.notecalendar.ui.AppTheme
import com.sztorm.notecalendar.viewmodels.MainState
import com.sztorm.notecalendar.viewmodels.MainViewFactory
import com.sztorm.notecalendar.viewmodels.MainViewModel
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

data class BundleResult(val isLaunchedFromNotification: Boolean)

class MainActivity : ComponentActivity() {
    private fun readBundle(): BundleResult? {
        val bundle: Bundle = intent.extras ?: return null
        val isLaunchedFromNotification = bundle.getBoolean(
            IntentKeys.NOTIFICATION_LAUNCH_DAY_SCREEN, false
        )
        return BundleResult(isLaunchedFromNotification = isLaunchedFromNotification)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val logger = TimberLogger
        val bundleResult = readBundle()
        val viewModel: MainViewModel
        val startingView: StartingScreenType
        val noteRepository = NoteRepositoryImpl(logger)
        val fileRepository = FileRepositoryImpl(this)
        val preferencesRepository = UserPreferencesRepository(this)
        val permissionManager = AppPermissionManager(this)
        val notificationManager = AppNotificationManager(this, preferencesRepository)

        runBlocking {
            startingView =
                if (bundleResult != null && bundleResult.isLaunchedFromNotification)
                    StartingScreenType.DayScreen
                else preferencesRepository.getStartingScreen(StartingScreenType.DayScreen)
            themeColors = preferencesRepository.getThemeColors()
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContent {
            val viewModel = viewModel<MainViewModel>(
                factory = MainViewFactory(
                    initialState = MainState(
                        themeColors = themeColors,
                        dayScreenDate = dayScreenDate
                    )
                )
            )
            AppTheme(viewModel.state.themeColors) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppScreen(
                        logger = logger,
                        viewModel = viewModel,
                        startingView = startingView,
                        permissionManager = permissionManager,
                        notificationManager = notificationManager,
                        noteRepository = noteRepository,
                        fileRepository = fileRepository,
                        preferencesRepository = preferencesRepository
                    )
                }
            }
        }
    }
}