package com.srizon.screentimeguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.srizon.screentimeguard.ui.MainViewModel
import com.srizon.screentimeguard.ui.navigation.AppNavGraph
import com.srizon.screentimeguard.ui.navigation.Routes
import com.srizon.screentimeguard.ui.theme.ScreenTimeGuardTheme
import com.srizon.screentimeguard.util.PermissionUtils

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScreenTimeGuardTheme {
                val start = if (PermissionUtils.allGranted(this)) Routes.DASHBOARD else Routes.ONBOARDING
                AppNavGraph(viewModel = viewModel, startDestination = start)
            }
        }
    }
}
