package com.sztorm.notecalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.sztorm.notecalendar.NoteCalendarApplication.Companion.BUNDLE_KEY_NOTIFICATION_LAUNCH_DAY_SCREEN
import com.sztorm.notecalendar.repositories.FileRepositoryImpl
import com.sztorm.notecalendar.repositories.NoteRepositoryImpl
import com.sztorm.notecalendar.repositories.UserPreferencesRepository
import com.sztorm.notecalendar.screens.AppScreen
import com.sztorm.notecalendar.ui.AppTheme
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

data class BundleResult(val isLaunchedFromNotification: Boolean)

class MainActivity : ComponentActivity() {
    private var _permissionManager: AppPermissionManager? = null
    private var _notificationManager: AppNotificationManager? = null
    val permissionManager: AppPermissionManager
        get() = _permissionManager!!
    val notificationManager: AppNotificationManager
        get() = _notificationManager!!

    private fun readBundle(): BundleResult? {
        val bundle: Bundle = intent.extras ?: return null

        val isLaunchedFromNotification = bundle.getBoolean(
            BUNDLE_KEY_NOTIFICATION_LAUNCH_DAY_SCREEN, false
        )
        return BundleResult(isLaunchedFromNotification = isLaunchedFromNotification)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundleResult = readBundle()
        val viewModel: MainViewModel
        val startingView: StartingScreenType
        val noteRepository = NoteRepositoryImpl
        val fileRepository = FileRepositoryImpl(this)
        val preferencesRepository = UserPreferencesRepository(this)
        _permissionManager = _permissionManager ?: AppPermissionManager(this)
        _notificationManager = _notificationManager ?: AppNotificationManager(
            this, preferencesRepository
        )

        runBlocking {
            viewModel = MainViewModel(
                initialState = MainState(
                    themeColors = preferencesRepository.getThemeColors(),
                    dayScreenDate = LocalDate.now()
                )
            )
            startingView = if (bundleResult != null && bundleResult.isLaunchedFromNotification) {
                StartingScreenType.DayScreen
            } else preferencesRepository.getStartingScreen(StartingScreenType.DayScreen)
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
                        fileRepository = fileRepository,
                        preferencesRepository = preferencesRepository
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