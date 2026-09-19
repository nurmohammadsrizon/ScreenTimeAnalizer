package com.srizon.screentimeguard.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import com.srizon.screentimeguard.data.TimerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Watches which app is in the foreground. While a *monitored* app is on screen it
 * ticks its usage counter every second; once the daily limit is hit it sends the
 * user home and shows the glass block-overlay until midnight resets things.
 */
class AppMonitorAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var repository: TimerRepository
    private val handler = Handler(Looper.getMainLooper())

    private var trackedPackage: String? = null
    private var tickRunnable: Runnable? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        repository = TimerRepository(applicationContext)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == trackedPackage) return

        stopTicking()
        trackedPackage = pkg

        scope.launch {
            val entity = repository.getMonitored(pkg) ?: return@launch
            if (entity.isBlocked) {
                showBlockScreen(pkg, entity.appName)
            } else {
                handler.post { startTicking(pkg) }
            }
        }
    }

    private fun startTicking(pkg: String) {
        lateinit var runnable: Runnable
        runnable = Runnable {
            if (trackedPackage != pkg) return@Runnable
            scope.launch {
                val updated = repository.addUsage(pkg, TICK_MILLIS)
                when {
                    updated == null -> Unit
                    updated.isBlocked -> showBlockScreen(pkg, updated.appName)
                    else -> handler.postDelayed(runnable, TICK_MILLIS)
                }
            }
        }
        tickRunnable = runnable
        handler.postDelayed(runnable, TICK_MILLIS)
    }

    private fun stopTicking() {
        tickRunnable?.let { handler.removeCallbacks(it) }
        tickRunnable = null
    }

    private fun showBlockScreen(pkg: String, appName: String) {
        stopTicking()
        performGlobalAction(GLOBAL_ACTION_HOME)
        startService(
            Intent(this, BlockOverlayService::class.java)
                .putExtra(BlockOverlayService.EXTRA_APP_NAME, appName)
        )
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        stopTicking()
    }

    companion object {
        private const val TICK_MILLIS = 1000L
    }
}
