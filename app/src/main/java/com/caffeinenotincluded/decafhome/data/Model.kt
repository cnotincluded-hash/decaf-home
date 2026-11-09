package com.caffeinenotincluded.decafhome.data

/** Everything Decaf Home remembers. Saved as one JSON file on the phone. */
data class DecafState(
    // One thing now: the first task is the one on screen.
    val tasks: List<Task> = emptyList(),

    // Project spotlight
    val projects: List<Project> = emptyList(),
    val activeProjectId: Long? = null,

    // Idea parking lot
    val ideas: List<Idea> = emptyList(),

    // Jam and deadline countdown
    val deadlines: List<Deadline> = emptyList(),

    // Dev streak: ISO dates (yyyy-mm-dd) you worked on games
    val devDays: Set<String> = emptySet(),
    val restDaysPerWeek: Int = 2,

    // Now / Next: a daily plan of time blocks
    val timeline: List<TimeBlock> = emptyList(),

    // Break menu
    val breaks: List<BreakItem> = DefaultBreaks,

    // XP
    val xp: Int = 0,

    // Focus timer
    val timer: Timer? = null,
    val focusMinutes: Int = 25,
    val askWhereWasI: Boolean = false,

    // Launcher
    val dock: List<String> = emptyList(),          // app keys, max 5
    val distracting: Set<String> = emptySet(),     // package names behind the speed bump

    // Design prompt of the day
    val promptDay: Long = -1,
    val promptOffset: Int = 0,

    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val nextId: Long = 100,
) {
    val activeProject: Project? get() = projects.firstOrNull { it.id == activeProjectId }
}

data class Task(val id: Long, val text: String)

data class Project(
    val id: Long,
    val name: String,
    val color: Int = 0,                 // index into the accent colours
    val nextStep: String = "",
    val whereWasI: String = "",
    val whereWasIAt: Long = 0L,
    val apps: List<String> = emptyList(), // app keys for quick launch, max 4
)

enum class IdeaKind(val label: String) { IDEA("Idea"), BUG("Bug"), NOTE("Note") }

data class Idea(
    val id: Long,
    val text: String,
    val kind: IdeaKind = IdeaKind.IDEA,
    val projectId: Long? = null,
    val createdAt: Long = 0L,
)

data class Deadline(val id: Long, val title: String, val dueAt: Long)

data class TimeBlock(val id: Long, val minuteOfDay: Int, val label: String)

data class BreakItem(val id: Long, val text: String)

enum class TimerKind { FOCUS, BREAK }

data class Timer(
    val kind: TimerKind,
    val minutes: Int,
    val startedAt: Long,
    val endsAt: Long,
    val label: String,
)

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Something worth a little party on screen. */
data class Celebration(val message: String, val points: Int, val newLevel: Int? = null)

val DefaultBreaks = listOf(
    BreakItem(1, "Drink some water"),
    BreakItem(2, "Stretch for 2 minutes"),
    BreakItem(3, "Step outside"),
    BreakItem(4, "Make a snack"),
    BreakItem(5, "Doodle something silly"),
    BreakItem(6, "Tidy one small thing"),
    BreakItem(7, "Dance to one song"),
)
