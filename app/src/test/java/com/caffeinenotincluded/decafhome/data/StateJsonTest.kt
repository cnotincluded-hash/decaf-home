package com.caffeinenotincluded.decafhome.data

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

class StateJsonTest {

    @Test
    fun roundTrip_keepsEverything() {
        val state = DecafState(
            tasks = listOf(Task(1, "Fix the jump"), Task(2, "Export sprites")),
            projects = listOf(
                Project(3, "Kettle Quest", color = 2, nextStep = "Tune gravity", whereWasI = "Jump arc", whereWasIAt = 123L, apps = listOf("a/b", "c/d")),
            ),
            activeProjectId = 3,
            ideas = listOf(Idea(4, "Double jump?", IdeaKind.IDEA, 3, 99L), Idea(5, "Clipping bug", IdeaKind.BUG, null, 100L)),
            deadlines = listOf(Deadline(6, "Game jam", 1_800_000_000_000L)),
            devDays = setOf("2026-11-08", "2026-11-09"),
            restDaysPerWeek = 1,
            timeline = listOf(TimeBlock(7, 540, "Art")),
            breaks = listOf(BreakItem(8, "Tea")),
            xp = 420,
            timer = Timer(TimerKind.FOCUS, 25, 1_000L, 1_501_000L, "Fix the jump"),
            focusMinutes = 45,
            askWhereWasI = true,
            dock = listOf("x/y"),
            distracting = setOf("com.example.social"),
            promptDay = 20_000,
            promptOffset = 2,
            themeMode = ThemeMode.DARK,
            nextId = 9,
        )
        val decoded = StateJson.decode(JSONObject(StateJson.encode(state).toString()))
        assertEquals(state, decoded)
    }

    @Test
    fun emptyObject_givesDefaults() {
        assertEquals(DecafState(), StateJson.decode(JSONObject()))
    }
}
