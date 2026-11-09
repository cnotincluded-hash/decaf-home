package com.caffeinenotincluded.decafhome.data

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressTest {

    // Monday 9 November 2026 starts the week used below.
    private val monday = LocalDate.of(2026, 11, 9)

    private fun days(vararg dates: LocalDate) = dates.map { it.toString() }.toSet()

    @Test
    fun levels_growBy50EachTime() {
        assertEquals(0, Levels.threshold(1))
        assertEquals(50, Levels.threshold(2))
        assertEquals(150, Levels.threshold(3))
        assertEquals(300, Levels.threshold(4))
        assertEquals(1, Levels.levelFor(0))
        assertEquals(1, Levels.levelFor(49))
        assertEquals(2, Levels.levelFor(50))
        assertEquals(3, Levels.levelFor(150))
        assertEquals(10 to 100, Levels.progress(60))
    }

    @Test
    fun streak_isZeroWithNoDevDays() {
        val info = Streaks.compute(emptySet(), monday, 2)
        assertEquals(0, info.days)
        assertEquals(2, info.restLeftThisWeek)
        assertFalse(info.workedToday)
    }

    @Test
    fun streak_todayNotDoneYetDoesNotBreakIt() {
        val thursday = monday.plusDays(3)
        val info = Streaks.compute(days(monday, monday.plusDays(1), monday.plusDays(2)), thursday, 2)
        assertEquals(3, info.days)
        assertEquals(
            listOf(DayMark.DEV, DayMark.DEV, DayMark.DEV, DayMark.TODAY, DayMark.FUTURE, DayMark.FUTURE, DayMark.FUTURE),
            info.week,
        )
        assertEquals(2, info.restLeftThisWeek)
    }

    @Test
    fun streak_countsToday() {
        val info = Streaks.compute(days(monday.minusDays(1), monday), monday, 2)
        assertEquals(2, info.days)
        assertTrue(info.workedToday)
    }

    @Test
    fun streak_restDaysBridgeGapsUntilTheWeekRunsOut() {
        val wednesday = monday.plusDays(2)
        val dev = days(
            monday.minusDays(7),          // Mon 2 Nov
            monday.minusDays(4),          // Thu 5 Nov
            monday.minusDays(3),          // Fri 6 Nov
            monday,                       // Mon 9 Nov
            monday.plusDays(1),           // Tue 10 Nov
        )
        // Sat 7 and Sun 8 are rest days (2 allowed); Wed 4 is a third miss that week.
        assertEquals(4, Streaks.compute(dev, wednesday, 2).days)
        // With no rest days, Sunday's miss ends it.
        assertEquals(2, Streaks.compute(dev, wednesday, 0).days)
    }

    @Test
    fun dayPlan_findsNowAndNext() {
        val blocks = listOf(
            TimeBlock(1, 9 * 60, "Art"),
            TimeBlock(2, 13 * 60, "Code"),
            TimeBlock(3, 12 * 60, "Lunch"),
        )
        val lunch = DayPlan.at(blocks, 12 * 60 + 30)
        assertEquals("Lunch", lunch.now?.label)
        assertEquals("Code", lunch.next?.label)
        assertEquals(30, lunch.minutesLeft)
        assertEquals(0.5f, lunch.fraction, 0.001f)

        val early = DayPlan.at(blocks, 8 * 60)
        assertNull(early.now)
        assertEquals("Art", early.next?.label)
        assertEquals(60, early.minutesLeft)

        val late = DayPlan.at(blocks, 22 * 60)
        assertEquals("Code", late.now?.label)
        assertNull(late.next)
        assertEquals(120, late.minutesLeft)
    }

    @Test
    fun prompts_areStablePerDayAndWrap() {
        assertEquals(Prompts.forDay(20_000, 0), Prompts.forDay(20_000, 0))
        assertEquals(Prompts.forDay(20_000, Prompts.All.size), Prompts.forDay(20_000, 0))
        assertTrue(Prompts.All.size >= 50)
    }
}
