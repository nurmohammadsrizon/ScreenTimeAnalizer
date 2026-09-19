package com.srizon.screentimeguard.ui.screens

import android.widget.ImageView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.srizon.screentimeguard.data.AppTimerEntity
import com.srizon.screentimeguard.ui.MainViewModel
import com.srizon.screentimeguard.ui.theme.AccentBlue
import com.srizon.screentimeguard.ui.theme.DangerRed
import com.srizon.screentimeguard.ui.theme.GlassBackground
import com.srizon.screentimeguard.ui.theme.TextPrimary
import com.srizon.screentimeguard.ui.theme.TextSecondary
import com.srizon.screentimeguard.ui.theme.glassCard
import com.srizon.screentimeguard.util.AppInfoProvider
import com.srizon.screentimeguard.util.formatMillisAsClock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: MainViewModel, onAddApp: () -> Unit) {
    val apps by viewModel.monitoredApps.collectAsState()
    var editingApp by remember { mutableStateOf<AppTimerEntity?>(null) }

    GlassBackground {
        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                FloatingActionButton(onClick = onAddApp, containerColor = AccentBlue) {
                    Icon(Icons.Filled.Add, contentDescription = "Add app", tint = Color.White)
                }
            }
        ) { padding ->
            Column(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Screen Time Guard",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    "Set a daily limit, we take it from there.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(Modifier.height(20.dp))

                if (apps.isEmpty()) {
                    EmptyState()
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        items(apps, key = { it.packageName }) { app ->
                            AppTimerCard(
                                app = app,
                                onClick = { editingApp = app },
                                onDelete = { viewModel.removeApp(app) }
                            )
                        }
                    }
                }
            }
        }
    }

    editingApp?.let { app ->
        SetTimerSheet(
            app = app,
            onDismiss = { editingApp = null },
            onConfirm = { minutes ->
                viewModel.updateLimit(app, minutes)
                editingApp = null
            }
        )
    }
}

@Composable
private fun EmptyState() {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 60.dp)
            .glassCard()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "No apps yet",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Tap the + button to pick an app and set its daily limit.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
private fun AppTimerCard(app: AppTimerEntity, onClick: () -> Unit, onDelete: () -> Unit) {
    val context = LocalContext.current
    val progress = if (app.dailyLimitMillis == 0L) 0f else
        (app.usedTodayMillis.toFloat() / app.dailyLimitMillis.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 90f),
        label = "progress"
    )
    val ringColor = if (app.isBlocked) DangerRed else AccentBlue

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(56.dp)) {
            Canvas(modifier = Modifier.size(56.dp)) {
                val stroke = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                drawArc(
                    color = Color.White.copy(alpha = 0.5f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = stroke,
                    size = Size(size.width, size.height)
                )
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = stroke,
                    size = Size(size.width, size.height)
                )
            }
            AndroidView(
                factory = { ctx -> ImageView(ctx) },
                update = { it.setImageDrawable(AppInfoProvider.getAppIcon(context, app.packageName)) },
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(
            Modifier
                .weight(1f)
                .clickable(onClick = onClick)
        ) {
            Text(
                app.appName,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(2.dp))
            val statusText = if (app.isBlocked) "Blocked until tomorrow"
                else "${formatMillisAsClock(app.usedTodayMillis)} / ${formatMillisAsClock(app.dailyLimitMillis)}"
            Text(
                statusText,
                style = MaterialTheme.typography.bodyMedium,
                color = if (app.isBlocked) DangerRed else TextSecondary
            )
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Remove", tint = TextSecondary)
        }
    }
}
