package com.srizon.screentimeguard.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.srizon.screentimeguard.ui.theme.AccentBlue
import com.srizon.screentimeguard.ui.theme.DangerRed
import com.srizon.screentimeguard.ui.theme.TextPrimary
import com.srizon.screentimeguard.ui.theme.TextSecondary
import com.srizon.screentimeguard.ui.theme.glassCard
import com.srizon.screentimeguard.util.formatMillisAsClock
import com.srizon.screentimeguard.util.millisUntilMidnight
import kotlinx.coroutines.delay

/** Full-screen frosted overlay shown when a monitored app's daily limit is hit.
 *  Ticks down to midnight so the person can see exactly when the app frees up. */
@Composable
fun BlockedGlassScreen(appName: String, onDismiss: () -> Unit) {
    var remaining by remember { mutableStateOf(millisUntilMidnight()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            remaining = millisUntilMidnight()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .glassCard(cornerRadius = 32)
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size((72 * scale).dp)
                    .clip(CircleShape)
                    .background(DangerRed.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = DangerRed, modifier = Modifier.size(34.dp))
            }
            Spacer(Modifier.height(18.dp))
            Text(
                "Time's up",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "$appName is locked for today. It unlocks in ${formatMillisAsClock(remaining)}.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onDismiss,
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("Got it")
            }
        }
    }
}
