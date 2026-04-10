@file:Suppress("SameParameterValue")

package com.sztorm.notecalendar

import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi

private class RequestExactAlarmSettingsCallback(
    val onResult: (isGranted: Boolean) -> Unit
) : DefaultActivityLifecycleCallbacks {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onActivityResumed(activity: Activity) {
        activity.unregisterActivityLifecycleCallbacks(this)
        val alarmManager = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        onResult(alarmManager.canScheduleExactAlarms())
    }
}

class AppPermissionManager(val activity: ComponentActivity) {
    private val grantsByPermission = BooleanArray(2) { i ->
        AppPermission.entries[i].isAlwaysGranted
    }
    private val requestsByPermission = mutableMapOf<AppPermission, (Boolean) -> Unit>()

    private val requestScheduleExactAlarmLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        grantsByPermission[AppPermission.ScheduleExactAlarm.ordinal] = isGranted
        requestsByPermission[AppPermission.ScheduleExactAlarm]?.invoke(isGranted)
    }
    private val requestPostNotificationsLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        grantsByPermission[AppPermission.PostNotifications.ordinal] = isGranted
        requestsByPermission[AppPermission.PostNotifications]?.invoke(isGranted)
    }

    fun isGranted(permission: AppPermission) = grantsByPermission[permission.ordinal]

    fun requestPermission(
        permission: AppPermission, onRequestDecision: (Boolean) -> Unit
    ) {
        if (!permission.isAlwaysGranted) {
            requestsByPermission[permission] = onRequestDecision

            when (permission) {
                AppPermission.ScheduleExactAlarm -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val alarmManager = activity
                            .getSystemService(Context.ALARM_SERVICE) as AlarmManager

                        if (!alarmManager.canScheduleExactAlarms()) {
                            val requestSettingCallback =
                                RequestExactAlarmSettingsCallback { isGranted ->
                                    grantsByPermission[AppPermission.ScheduleExactAlarm.ordinal] =
                                        isGranted
                                    requestsByPermission[AppPermission.ScheduleExactAlarm]
                                        ?.invoke(isGranted)
                                }
                            activity.registerActivityLifecycleCallbacks(requestSettingCallback)
                            requestSettingsActivity(
                                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                            )
                        } else {
                            requestScheduleExactAlarmLauncher
                                .launch(permission.permissionString)
                        }
                    }
                }

                AppPermission.PostNotifications ->
                    requestPostNotificationsLauncher.launch(permission.permissionString)
            }
        }
    }

    private fun requestSettingsActivity(actionRequest: String) {
        activity.startActivity(
            Intent().apply {
                action = actionRequest
                data = Uri
                    .fromParts("package", activity.packageName, null)
            }
        )
    }
}