package com.srizon.screentimeguard.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.srizon.screentimeguard.ui.screens.BlockedGlassScreen
import com.srizon.screentimeguard.ui.theme.ScreenTimeGuardTheme

/** Draws the frosted "time's up" screen as a system overlay window, independent of
 *  any activity -- this is what makes the block visible no matter what app is open. */
class BlockOverlayService : Service(), LifecycleOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private var windowManager: WindowManager? = null
    private var overlayView: ComposeView? = null

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }
        val appName = intent?.getStringExtra(EXTRA_APP_NAME) ?: "This app"
        showOverlay(appName)
        return START_NOT_STICKY
    }

    private fun showOverlay(appName: String) {
        removeOverlay()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.CENTER }

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@BlockOverlayService)
            setViewTreeSavedStateRegistryOwner(this@BlockOverlayService)
            setContent {
                ScreenTimeGuardTheme {
                    BlockedGlassScreen(
                        appName = appName,
                        onDismiss = { removeOverlay(); stopSelf() }
                    )
                }
            }
        }
        overlayView = composeView
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        windowManager?.addView(composeView, params)
    }

    private fun removeOverlay() {
        overlayView?.let { view -> runCatching { windowManager?.removeView(view) } }
        overlayView = null
    }

    override fun onDestroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        removeOverlay()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val EXTRA_APP_NAME = "extra_app_name"
    }
}
