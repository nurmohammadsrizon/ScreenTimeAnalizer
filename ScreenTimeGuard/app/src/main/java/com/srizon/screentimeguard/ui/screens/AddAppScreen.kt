package com.srizon.screentimeguard.ui.screens

import android.widget.ImageView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.srizon.screentimeguard.data.AppTimerEntity
import com.srizon.screentimeguard.ui.MainViewModel
import com.srizon.screentimeguard.ui.theme.GlassBackground
import com.srizon.screentimeguard.ui.theme.TextPrimary
import com.srizon.screentimeguard.ui.theme.glassCard
import com.srizon.screentimeguard.util.AppInfoProvider
import com.srizon.screentimeguard.util.InstalledAppInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppScreen(viewModel: MainViewModel, onDone: () -> Unit) {
    val context = LocalContext.current
    val allApps = remember { AppInfoProvider.getLaunchableApps(context) }
    var query by remember { mutableStateOf("") }
    var pendingApp by remember { mutableStateOf<InstalledAppInfo?>(null) }

    val filtered = remember(query, allApps) {
        if (query.isBlank()) allApps else allApps.filter { it.appName.contains(query, ignoreCase = true) }
    }

    GlassBackground {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDone) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text(
                    "Choose an app",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search apps") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(cornerRadius = 20),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                )
            )
            Spacer(Modifier.height(12.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filtered, key = { it.packageName }) { info ->
                    InstalledAppRow(info) { pendingApp = info }
                }
            }
        }
    }

    pendingApp?.let { info ->
        SetTimerSheet(
            app = AppTimerEntity(info.packageName, info.appName, dailyLimitMillis = 60 * 60_000L),
            onDismiss = { pendingApp = null },
            onConfirm = { minutes ->
                viewModel.addApp(info.packageName, info.appName, minutes)
                pendingApp = null
                onDone()
            }
        )
    }
}

@Composable
private fun InstalledAppRow(info: InstalledAppInfo, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(cornerRadius = 20)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AndroidView(
            factory = { ImageView(it) },
            update = { it.setImageDrawable(info.icon) },
            modifier = Modifier.size(36.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(info.appName, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Spacer(Modifier.weight(1f))
        TextButton(onClick = onClick) { Text("Add") }
    }
}
