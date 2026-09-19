package com.srizon.screentimeguard.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.srizon.screentimeguard.data.TimerRepository

class MidnightResetWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        TimerRepository(applicationContext).resetAllForNewDay()
        return Result.success()
    }
}
