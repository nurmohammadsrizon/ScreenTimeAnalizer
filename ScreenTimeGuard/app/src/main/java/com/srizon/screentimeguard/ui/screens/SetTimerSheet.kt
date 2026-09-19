package com.srizon.screentimeguard.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.srizon.screentimeguard.data.AppTimerEntity
import com.srizon.screentimeguard.ui.theme.AccentBlue
import com.srizon.screentimeguard.ui.theme.TextPrimary
import com.srizon.screentimeguard.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetTimerSheet(app: AppTimerEntity, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var minutes by remember {
        mutableFloatStateOf((app.dailyLimitMillis / 60000L).coerceAtLeast(5L).toFloat())
    }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color(0xFFFDFBFF)) {
        Column(Modifier.fillMaxWidth().padding(24.dp)) {
            Text(
                "Daily limit for ${app.appName}",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            androidx.compose.foundation.layout.Spacer(Modifier.height(20.dp))
            Text(
                "${minutes.toInt()} minutes / day",
                style = MaterialTheme.typography.headlineMedium,
                color = AccentBlue,
                fontWeight = FontWeight.Bold
            )
            androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))
            Slider(
                value = minutes,
                onValueChange = { minutes = it },
                valueRange = 5f..480f,
                steps = 94,
                colors = SliderDefaults.colors(thumbColor = AccentBlue, activeTrackColor = AccentBlue)
            )
            Text("From 5 minutes up to 8 hours.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            androidx.compose.foundation.layout.Spacer(Modifier.height(20.dp))
            Button(
                onClick = { onConfirm(minutes.toInt()) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("Save limit")
            }
            androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
        }
    }
}
