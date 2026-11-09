package com.caffeinenotincluded.decafhome.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

/** The current time, refreshed on each [periodMs] boundary (so the clock flips on the minute). */
@Composable
fun rememberNow(periodMs: Long): State<Long> = produceState(System.currentTimeMillis(), periodMs) {
    while (true) {
        val now = System.currentTimeMillis()
        value = now
        delay(periodMs - now % periodMs + 5)
    }
}

fun localDateTime(epochMs: Long): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMs), ZoneId.systemDefault())

fun minuteOfDay(epochMs: Long): Int = localDateTime(epochMs).let { it.hour * 60 + it.minute }

fun clockText(minuteOfDay: Int, is24Hour: Boolean): String {
    val h = minuteOfDay / 60
    val m = minuteOfDay % 60
    return if (is24Hour) {
        "%02d:%02d".format(h, m)
    } else {
        val h12 = if (h % 12 == 0) 12 else h % 12
        "%d:%02d %s".format(h12, m, if (h < 12) "am" else "pm")
    }
}

/** "1h 20m", "45m". */
fun durationText(minutes: Int): String = when {
    minutes >= 60 && minutes % 60 == 0 -> "${minutes / 60}h"
    minutes >= 60 -> "${minutes / 60}h ${minutes % 60}m"
    else -> "${minutes}m"
}

/** Countdown to a deadline: "3d 4h", "5h 20m", "12m", or "Time's up". */
fun countdownText(msLeft: Long): String {
    if (msLeft <= 0) return "Time's up"
    val minutes = msLeft / 60_000
    val days = minutes / (24 * 60)
    val hours = (minutes / 60) % 24
    val mins = minutes % 60
    return when {
        days >= 1 -> "${days}d ${hours}h"
        hours >= 1 -> "${hours}h ${mins}m"
        else -> "${mins.coerceAtLeast(1)}m"
    }
}

/** "just now", "20 min ago", "3 h ago", "2 days ago". */
fun agoText(thenMs: Long, nowMs: Long): String {
    val minutes = (nowMs - thenMs) / 60_000
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "$minutes min ago"
        minutes < 24 * 60 -> "${minutes / 60} h ago"
        minutes < 48 * 60 -> "yesterday"
        else -> "${minutes / (24 * 60)} days ago"
    }
}

fun timerText(msLeft: Long): String {
    val total = (msLeft + 999) / 1000
    return "%d:%02d".format(total / 60, total % 60)
}

private val DateFormatter = DateTimeFormatter.ofPattern("EEE d MMM")

fun shortDate(epochMs: Long): String = localDateTime(epochMs).format(DateFormatter)
