package com.srizon.screentimeguard.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_timers")
data class AppTimerEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val dailyLimitMillis: Long,
    val usedTodayMillis: Long = 0L,
    val lastResetEpochDay: Long = 0L,
    val isBlocked: Boolean = false
)
