package com.srizon.screentimeguard.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.srizon.screentimeguard.data.AppTimerEntity
import com.srizon.screentimeguard.data.TimerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TimerRepository(application)

    val monitoredApps = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addApp(packageName: String, appName: String, dailyLimitMinutes: Int) {
        viewModelScope.launch {
            repository.addOrUpdateApp(packageName, appName, dailyLimitMinutes * 60_000L)
        }
    }

    fun updateLimit(entity: AppTimerEntity, dailyLimitMinutes: Int) {
        viewModelScope.launch {
            repository.addOrUpdateApp(entity.packageName, entity.appName, dailyLimitMinutes * 60_000L)
        }
    }

    fun removeApp(entity: AppTimerEntity) {
        viewModelScope.launch { repository.removeApp(entity) }
    }
}
