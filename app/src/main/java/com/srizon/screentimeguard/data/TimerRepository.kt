package com.srizon.screentimeguard.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId

/**
 * Single source of truth for app timers. Every read path runs the usage through
 * [resetIfNewDay] first, so a block set yesterday always clears itself the moment
 * the calendar date changes -- no separate "unlock" step needed.
 */
class TimerRepository(context: Context) {
    private val dao = AppDatabase.getInstance(context).appTimerDao()

    fun observeAll(): Flow<List<AppTimerEntity>> = dao.observeAll()

    private fun todayEpochDay(): Long = LocalDate.now(ZoneId.systemDefault()).toEpochDay()

    private fun resetIfNewDay(entity: AppTimerEntity): AppTimerEntity {
        val today = todayEpochDay()
        return if (entity.lastResetEpochDay != today) {
            entity.copy(usedTodayMillis = 0L, isBlocked = false, lastResetEpochDay = today)
        } else entity
    }

    suspend fun addOrUpdateApp(packageName: String, appName: String, dailyLimitMillis: Long) {
        val existing = dao.getByPackage(packageName)?.let { resetIfNewDay(it) }
        val entity = existing?.copy(
            appName = appName,
            dailyLimitMillis = dailyLimitMillis,
            isBlocked = existing.usedTodayMillis >= dailyLimitMillis
        ) ?: AppTimerEntity(
            packageName = packageName,
            appName = appName,
            dailyLimitMillis = dailyLimitMillis,
            lastResetEpochDay = todayEpochDay()
        )
        dao.upsert(entity)
    }

    suspend fun removeApp(entity: AppTimerEntity) = dao.delete(entity)

    /** The monitored entity for [packageName] after applying the daily reset, or null if untracked. */
    suspend fun getMonitored(packageName: String): AppTimerEntity? {
        val raw = dao.getByPackage(packageName) ?: return null
        val fresh = resetIfNewDay(raw)
        if (fresh != raw) dao.upsert(fresh)
        return fresh
    }

    /** Adds [deltaMillis] of usage for [packageName]. Returns the updated entity, or null if not monitored. */
    suspend fun addUsage(packageName: String, deltaMillis: Long): AppTimerEntity? {
        val raw = dao.getByPackage(packageName) ?: return null
        val fresh = resetIfNewDay(raw)
        val newUsed = fresh.usedTodayMillis + deltaMillis
        val updated = fresh.copy(
            usedTodayMillis = newUsed.coerceAtMost(fresh.dailyLimitMillis),
            isBlocked = fresh.isBlocked || newUsed >= fresh.dailyLimitMillis
        )
        dao.upsert(updated)
        return updated
    }

    suspend fun resetAllForNewDay() {
        dao.getAllOnce().forEach { entity ->
            val fresh = resetIfNewDay(entity)
            if (fresh != entity) dao.upsert(fresh)
        }
    }
}
