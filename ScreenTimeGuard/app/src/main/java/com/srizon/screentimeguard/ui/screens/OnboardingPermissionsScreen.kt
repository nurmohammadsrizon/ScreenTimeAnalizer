package com.srizon.screentimeguard.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.srizon.screentimeguard.ui.theme.GlassBackground
import com.srizon.screentimeguard.ui.theme.TextPrimary
import com.srizon.screentimeguard.ui.theme.TextSecondary
import com.srizon.screentimeguard.ui.theme.glassCard
import com.srizon.screentimeguard.util.PermissionUtils

private data class PermStep(
    val title: String,
    val description: String,
    val isGranted: Boolean,
    val action: () -> Unit
)

@Composable
fun OnboardingPermissionsScreen(onAllGranted: () -> Unit) {
    val context = LocalContext.current
    var usageGranted by remember { mutableStateOf(PermissionUtils.hasUsageAccess(context)) }
    var overlayGranted by remember { mutableStateOf(PermissionUtils.hasOverlayPermission(context)) }
    var accessibilityGranted by remember { mutableStateOf(PermissionUtils.isAccessibilityServiceEnabled(context)) }

    // Permissions are granted in the system Settings app, so we re-check every time
    // this screen comes back into the foreground rather than polling.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                usageGranted = PermissionUtils.hasUsageAccess(context)
                overlayGranted = PermissionUtils.hasOverlayPermission(context)
                accessibilityGranted = PermissionUtils.isAccessibilityServiceEnabled(context)
                if (usageGranted && overlayGranted && accessibilityGranted) onAllGranted()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val steps = listOf(
        PermStep(
            "Usage Access",
            "Lets the app see which app is active so it can count screen time.",
            usageGranted
        ) { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) },
        PermStep(
            "Display Over Other Apps",
            "Needed to show the frosted block screen when time is up.",
            overlayGranted
        ) {
            context.startActivity(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
            )
        },
        PermStep(
            "Accessibility Service",
            "Lets the app detect app switches in real time to enforce limits.",
            accessibilityGranted
        ) { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
    )

    GlassBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "One-time setup",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Screen Time Guard needs three permissions to actually enforce limits.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(Modifier.height(24.dp))

            steps.forEach { step ->
                PermissionCard(step)
                Spacer(Modifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun PermissionCard(step: PermStep) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .glassCard()
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (step.isGranted) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (step.isGranted) Color(0xFF4CC38A) else TextSecondary
            )
            Spacer(Modifier.width(10.dp))
            Text(
                step.title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(step.description, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        if (!step.isGranted) {
            Spacer(Modifier.height(10.dp))
            Button(onClick = step.action, shape = MaterialTheme.shapes.large) {
                Text("Grant")
            }
        }
    }
}
