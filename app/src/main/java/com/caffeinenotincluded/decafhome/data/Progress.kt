package com.caffeinenotincluded.decafhome.data

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/** XP levels: each level needs 50 more XP than the one before (50, 100, 150...). */
object Levels {
    /** Total XP needed to reach [level]. Level 1 starts at 0. */
    fun threshold(level: Int): Int = 25 * level * (level - 1)

    fun levelFor(xp: Int): Int {
        var level = 1
        while (xp >= threshold(level + 1)) level++
        return level
    }

    /** XP earned inside the current level, and the size of that level. */
    fun progress(xp: Int): Pair<Int, Int> {
        val level = levelFor(xp)
        val start = threshold(level)
        return (xp - start) to (threshold(level + 1) - start)
    }
}

enum class DayMark { DEV, REST, TODAY, FUTURE }

data class StreakInfo(
    val days: Int,
    val restLeftThisWeek: Int,
    /** Monday to Sunday of the current week. */
    val week: List<DayMark>,
    val workedToday: Boolean,
)

/**
 * Dev streak with built-in rest days: up to [restPerWeek] missed days in each
 * Monday-to-Sunday week don't break the streak. Today never breaks it, because
 * today isn't over yet.
 */
object Streaks {

    fun compute(devDays: Set<String>, today: LocalDate, restPerWeek: Int): StreakInfo {
        val days = devDays.mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }.toSet()
        val workedToday = today in days
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        var count = 0
        val earliest = days.minOrNull()
        if (earliest != null) {
            if (workedToday) count++
            val missesByWeek = HashMap<LocalDate, Int>()
            var day = today.minusDays(1)
            while (!day.isBefore(earliest)) {
                if (day in days) {
                    count++
                } else {
                    val week = day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    val misses = (missesByWeek[week] ?: 0) + 1
                    missesByWeek[week] = misses
                    if (misses > restPerWeek) break
                }
                day = day.minusDays(1)
            }
        }

        // This week's dots and rest days used so far (days before today only).
        val week = (0..6).map { offset ->
            val day = weekStart.plusDays(offset.toLong())
            when {
                day.isAfter(today) -> DayMark.FUTURE
                day == today -> if (workedToday) DayMark.DEV else DayMark.TODAY
                day in days -> DayMark.DEV
                else -> DayMark.REST
            }
        }
        val restUsed = week.count { it == DayMark.REST }
        return StreakInfo(
            days = count,
            restLeftThisWeek = (restPerWeek - restUsed).coerceAtLeast(0),
            week = week,
            workedToday = workedToday,
        )
    }
}

/** Where the Now / Next strip is in the day. */
data class NowNext(
    val now: TimeBlock?,
    val next: TimeBlock?,
    val minutesLeft: Int,
    val fraction: Float,
)

object DayPlan {
    fun at(blocks: List<TimeBlock>, minuteOfDay: Int): NowNext {
        val sorted = blocks.sortedBy { it.minuteOfDay }
        val now = sorted.lastOrNull { it.minuteOfDay <= minuteOfDay }
        val next = sorted.firstOrNull { it.minuteOfDay > minuteOfDay }
        val end = next?.minuteOfDay ?: (24 * 60)
        val start = now?.minuteOfDay ?: 0
        val length = (end - start).coerceAtLeast(1)
        return NowNext(
            now = now,
            next = next,
            minutesLeft = (end - minuteOfDay).coerceAtLeast(0),
            fraction = ((minuteOfDay - start).toFloat() / length).coerceIn(0f, 1f),
        )
    }
}
