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
    val favourites: List<String> = emptyList(),    // app keys, in order, max 10
    val distracting: Set<String> = emptySet(),     // package names behind the speed bump

    // Design prompt of the day
    val promptDay: Long = -1,
    val promptOffset: Int = 0,

    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val layout: LayoutPrefs = LayoutPrefs(),
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

// ---- Customisation -------------------------------------------------------------

/** The widgets, each of which can sit on the home screen as a pill. */
enum class WidgetId(val label: String) {
    ONE_THING("One thing now"),
    TIMER("Focus timer"),
    NOW_NEXT("Now / Next"),
    PROJECT("Project spotlight"),
    IDEAS("Idea parking lot"),
    COUNTDOWNS("Countdowns"),
    STREAK("Dev streak"),
    BREAKS("Break menu"),
    PROMPT("Design prompt"),
}

enum class HomeStyle(val label: String) { CNI("Full CNI"), MINIMAL("Wallpaper") }
enum class Hand(val label: String) { RIGHT("Right-handed"), LEFT("Left-handed") }
enum class IconSize(val label: String, val size: Int) { HIDDEN("Hidden", 0), SMALL("Small", 28), MEDIUM("Medium", 36), LARGE("Large", 46) }
enum class LabelSize(val label: String, val size: Int) { SMALL("Small", 18), MEDIUM("Medium", 22), LARGE("Large", 28) }
enum class LabelFont(val label: String) { FREDOKA("Fredoka"), NUNITO("Nunito"), SYSTEM("Phone font") }
enum class ClockStyle(val label: String) { BIG("Big"), SMALL("Small"), HIDDEN("Hidden") }

enum class GestureAction(val label: String) {
    SEARCH("Search apps"),
    NOTIFICATIONS("Notifications"),
    TIMER("Focus timer"),
    ONE_THING("One thing now"),
    NOTHING("Nothing"),
}

/** How the home screen looks and behaves. All of it is in Settings. */
data class LayoutPrefs(
    val style: HomeStyle = HomeStyle.CNI,
    val hand: Hand = Hand.RIGHT,
    val iconSize: IconSize = IconSize.MEDIUM,
    val labelSize: LabelSize = LabelSize.MEDIUM,
    val font: LabelFont = LabelFont.FREDOKA,
    val clock: ClockStyle = ClockStyle.BIG,
    val showDate: Boolean = true,
    val showXp: Boolean = true,
    val favouritesAtBottom: Boolean = true,
    val accent: Int = 0,                         // index into the brand accents
    val pills: List<WidgetId> = DefaultPills,    // shown on home, in this order
    val swipeUp: GestureAction = GestureAction.SEARCH,
    val swipeDown: GestureAction = GestureAction.NOTIFICATIONS,
    val haptics: Boolean = true,
)

val DefaultPills = listOf(WidgetId.ONE_THING, WidgetId.TIMER, WidgetId.PROJECT, WidgetId.STREAK, WidgetId.IDEAS)

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
