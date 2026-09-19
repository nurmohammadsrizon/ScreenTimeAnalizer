package com.srizon.screentimeguard.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** The core "frosted glass" look: translucent gradient fill + soft white border + a
 *  gentle shadow. No blur API needed, so it renders identically on every API 26+ device. */
fun Modifier.glassCard(cornerRadius: Int = 28): Modifier = this
    .shadow(elevation = 18.dp, shape = RoundedCornerShape(cornerRadius.dp), ambientColor = Color(0x33000000))
    .clip(RoundedCornerShape(cornerRadius.dp))
    .background(
        Brush.linearGradient(
            colors = listOf(Color.White.copy(alpha = 0.55f), Color.White.copy(alpha = 0.25f))
        )
    )
    .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(cornerRadius.dp))

@Composable
fun GlassBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(colors = listOf(SkyMist, LavenderMist, PeachMist))
            )
    ) { content() }
}
