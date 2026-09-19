package com.srizon.screentimeguard.util

fun formatMillisAsClock(millis: Long): String {
    val totalSeconds = millis / 1000
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) String.format("%dh %02dm", h, m) else String.format("%dm %02ds", m, s)
}

fun millisUntilMidnight(): Long {
    val now = java.time.LocalDateTime.now()
    val midnight = now.toLocalDate().plusDays(1).atStartOfDay()
    return java.time.Duration.between(now, midnight).toMillis()
}
