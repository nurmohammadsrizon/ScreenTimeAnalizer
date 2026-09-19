package com.srizon.screentimeguard.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AppTimerDao {
    @Query("SELECT * FROM app_timers ORDER BY appName ASC")
    fun observeAll(): Flow<List<AppTimerEntity>>

    @Query("SELECT * FROM app_timers WHERE packageName = :pkg LIMIT 1")
    suspend fun getByPackage(pkg: String): AppTimerEntity?

    @Query("SELECT * FROM app_timers")
    suspend fun getAllOnce(): List<AppTimerEntity>

    @Upsert
    suspend fun upsert(entity: AppTimerEntity)

    @Delete
    suspend fun delete(entity: AppTimerEntity)
}
