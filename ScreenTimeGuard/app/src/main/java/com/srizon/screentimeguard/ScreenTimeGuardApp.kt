package com.srizon.screentimeguard

import android.app.Application
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.srizon.screentimeguard.service.MidnightResetWorker
import java.util.concurrent.TimeUnit

/** Schedules a periodic safety-net job that resets all timers at day rollover
 *  even if the user never reopens a monitored app right after midnight. */
class ScreenTimeGuardApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val request = PeriodicWorkRequestBuilder<MidnightResetWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueue(request)
    }
}
