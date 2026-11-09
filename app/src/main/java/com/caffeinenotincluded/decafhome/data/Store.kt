package com.caffeinenotincluded.decafhome.data

import java.io.File
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

/**
 * The one place state changes. Every change is saved straight away to a JSON
 * file in the app's private storage (nothing leaves the phone).
 */
class Store(private val file: File) {

    private val _state = MutableStateFlow(load())
    val state: StateFlow<DecafState> = _state.asStateFlow()

    private val _celebrations = MutableSharedFlow<Celebration>(extraBufferCapacity = 8)
    val celebrations: SharedFlow<Celebration> = _celebrations.asSharedFlow()

    private fun load(): DecafState = try {
        if (file.exists()) StateJson.decode(JSONObject(file.readText())) else DecafState()
    } catch (e: Exception) {
        // A damaged save shouldn't stop the home screen from opening.
        runCatching { file.copyTo(File(file.parentFile, file.name + ".broken"), overwrite = true) }
        DecafState()
    }

    @Synchronized
    private fun update(transform: (DecafState) -> DecafState) {
        val old = _state.value
        val new = transform(old)
        if (new == old) return
        _state.value = new
        val tmp = File(file.parentFile, file.name + ".tmp")
        tmp.writeText(StateJson.encode(new).toString())
        tmp.renameTo(file)
    }

    /** Adds XP inside an update and queues the celebration for after it. */
    private fun award(points: Int, message: String) {
        var celebration: Celebration? = null
        update { s ->
            val before = Levels.levelFor(s.xp)
            val after = Levels.levelFor(s.xp + points)
            celebration = Celebration(message, points, after.takeIf { it > before })
            s.copy(xp = s.xp + points)
        }
        celebration?.let { _celebrations.tryEmit(it) }
    }

    private fun today(): String = LocalDate.now().toString()

    private fun DecafState.withDevDay(): DecafState = copy(devDays = devDays + today())

    // ---- One thing now -------------------------------------------------------

    fun addTask(text: String, atFront: Boolean = false) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        update { s ->
            val task = Task(s.nextId, clean)
            s.copy(tasks = if (atFront) listOf(task) + s.tasks else s.tasks + task, nextId = s.nextId + 1)
        }
    }

    /** Replaces a task with smaller steps, in the same place in the queue. */
    fun splitTask(id: Long, steps: List<String>) {
        val clean = steps.map { it.trim() }.filter { it.isNotEmpty() }
        if (clean.isEmpty()) return
        update { s ->
            val index = s.tasks.indexOfFirst { it.id == id }
            if (index < 0) return@update s
            val newTasks = clean.mapIndexed { i, text -> Task(s.nextId + i, text) }
            val tasks = s.tasks.toMutableList().apply {
                removeAt(index)
                addAll(index, newTasks)
            }
            s.copy(tasks = tasks, nextId = s.nextId + newTasks.size)
        }
    }

    fun completeTask(id: Long) {
        val task = _state.value.tasks.firstOrNull { it.id == id } ?: return
        update { s -> s.copy(tasks = s.tasks.filterNot { it.id == id }).withDevDay() }
        award(10, Cheers.taskDone(task.text))
    }

    fun laterTask(id: Long) = update { s ->
        val task = s.tasks.firstOrNull { it.id == id } ?: return@update s
        s.copy(tasks = s.tasks.filterNot { it.id == id } + task)
    }

    fun moveTaskToFront(id: Long) = update { s ->
        val task = s.tasks.firstOrNull { it.id == id } ?: return@update s
        s.copy(tasks = listOf(task) + s.tasks.filterNot { it.id == id })
    }

    fun deleteTask(id: Long) = update { s -> s.copy(tasks = s.tasks.filterNot { it.id == id }) }

    // ---- Focus timer -----------------------------------------------------------

    fun setFocusMinutes(minutes: Int) = update { it.copy(focusMinutes = minutes) }

    fun startTimer(kind: TimerKind, minutes: Int, label: String, now: Long): Timer {
        val timer = Timer(kind, minutes, now, now + minutes * 60_000L, label)
        update { it.copy(timer = timer, askWhereWasI = false) }
        return timer
    }

    fun cancelTimer() = update { it.copy(timer = null) }

    /**
     * Ends the timer if its time is up. Safe to call more than once (the alarm
     * and the screen may both try): only the first call counts.
     */
    fun finishTimer(now: Long): Timer? {
        val timer = _state.value.timer ?: return null
        if (now < timer.endsAt - 2_000) return null
        update { s ->
            when (timer.kind) {
                TimerKind.FOCUS -> s.copy(timer = null, askWhereWasI = s.activeProject != null).withDevDay()
                TimerKind.BREAK -> s.copy(timer = null)
            }
        }
        when (timer.kind) {
            TimerKind.FOCUS -> award(timer.minutes, "Sprint done! ${timer.minutes} minutes of real work.")
            TimerKind.BREAK -> award(3, "Proper break taken. Back to it?")
        }
        return timer
    }

    // ---- Where was I -----------------------------------------------------------

    fun saveWhereWasI(text: String) {
        val clean = text.trim()
        val project = _state.value.activeProject ?: return
        if (clean.isEmpty()) return
        update { s ->
            s.copy(
                projects = s.projects.map {
                    if (it.id == project.id) it.copy(whereWasI = clean, whereWasIAt = System.currentTimeMillis()) else it
                },
                askWhereWasI = false,
            )
        }
        award(5, "Future you says thanks.")
    }

    fun skipWhereWasI() = update { it.copy(askWhereWasI = false) }

    // ---- Projects --------------------------------------------------------------

    fun addProject(name: String): Long? {
        val clean = name.trim()
        if (clean.isEmpty()) return null
        var id: Long? = null
        update { s ->
            id = s.nextId
            val project = Project(s.nextId, clean, color = s.projects.size % 5)
            s.copy(projects = s.projects + project, activeProjectId = project.id, nextId = s.nextId + 1)
        }
        return id
    }

    fun saveProject(project: Project) = update { s ->
        s.copy(projects = s.projects.map { if (it.id == project.id) project else it })
    }

    fun deleteProject(id: Long) = update { s ->
        val remaining = s.projects.filterNot { it.id == id }
        s.copy(
            projects = remaining,
            activeProjectId = if (s.activeProjectId == id) remaining.firstOrNull()?.id else s.activeProjectId,
            ideas = s.ideas.map { if (it.projectId == id) it.copy(projectId = null) else it },
        )
    }

    fun setActiveProject(id: Long) = update { it.copy(activeProjectId = id) }

    // ---- Idea parking lot ------------------------------------------------------

    fun parkIdea(text: String, kind: IdeaKind) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        update { s ->
            val idea = Idea(s.nextId, clean, kind, s.activeProjectId, System.currentTimeMillis())
            s.copy(ideas = listOf(idea) + s.ideas, nextId = s.nextId + 1)
        }
        award(2, "Parked. It'll be here when you're ready.")
    }

    fun deleteIdea(id: Long) = update { s -> s.copy(ideas = s.ideas.filterNot { it.id == id }) }

    fun ideaToTask(id: Long) {
        val idea = _state.value.ideas.firstOrNull { it.id == id } ?: return
        update { s -> s.copy(ideas = s.ideas.filterNot { it.id == id }) }
        addTask(idea.text)
    }

    // ---- Deadlines -------------------------------------------------------------

    fun addDeadline(title: String, dueAt: Long) {
        val clean = title.trim()
        if (clean.isEmpty()) return
        update { s ->
            s.copy(deadlines = (s.deadlines + Deadline(s.nextId, clean, dueAt)).sortedBy { it.dueAt }, nextId = s.nextId + 1)
        }
    }

    fun deleteDeadline(id: Long) = update { s -> s.copy(deadlines = s.deadlines.filterNot { it.id == id }) }

    // ---- Dev streak ------------------------------------------------------------

    fun checkInToday() {
        if (today() in _state.value.devDays) return
        update { it.withDevDay() }
        award(5, "Logged! Every day counts.")
    }

    fun setRestDays(days: Int) = update { it.copy(restDaysPerWeek = days.coerceIn(0, 3)) }

    // ---- Now / Next ------------------------------------------------------------

    fun addTimeBlock(minuteOfDay: Int, label: String) {
        val clean = label.trim()
        if (clean.isEmpty()) return
        update { s ->
            val blocks = s.timeline.filterNot { it.minuteOfDay == minuteOfDay } + TimeBlock(s.nextId, minuteOfDay, clean)
            s.copy(timeline = blocks.sortedBy { it.minuteOfDay }, nextId = s.nextId + 1)
        }
    }

    fun deleteTimeBlock(id: Long) = update { s -> s.copy(timeline = s.timeline.filterNot { it.id == id }) }

    // ---- Break menu ------------------------------------------------------------

    fun addBreak(text: String) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        update { s -> s.copy(breaks = s.breaks + BreakItem(s.nextId, clean), nextId = s.nextId + 1) }
    }

    fun deleteBreak(id: Long) = update { s -> s.copy(breaks = s.breaks.filterNot { it.id == id }) }

    // ---- Launcher --------------------------------------------------------------

    fun toggleFavourite(key: String) = update { s ->
        when {
            key in s.favourites -> s.copy(favourites = s.favourites - key)
            s.favourites.size >= MAX_FAVOURITES -> s
            else -> s.copy(favourites = s.favourites + key)
        }
    }

    /** Moves a favourite up (-1) or down (+1) the list. */
    fun moveFavourite(key: String, delta: Int) = update { s ->
        s.copy(favourites = s.favourites.moved(key, delta))
    }

    fun toggleDistracting(packageName: String) = update { s ->
        s.copy(distracting = if (packageName in s.distracting) s.distracting - packageName else s.distracting + packageName)
    }

    /** You stopped at the speed bump and didn't open the app. */
    fun speedBumpSave() = award(5, "Nice save. That's a win.")

    // ---- Prompt of the day -----------------------------------------------------

    fun anotherPrompt(epochDay: Long) = update { s ->
        if (s.promptDay == epochDay) s.copy(promptOffset = s.promptOffset + 1)
        else s.copy(promptDay = epochDay, promptOffset = 1)
    }

    // ---- Settings --------------------------------------------------------------

    fun setThemeMode(mode: ThemeMode) = update { it.copy(themeMode = mode) }

    fun updateLayout(transform: (LayoutPrefs) -> LayoutPrefs) = update { it.copy(layout = transform(it.layout)) }

    /** Shows or hides a widget's pill on the home screen. */
    fun togglePill(id: WidgetId) = updateLayout { l ->
        l.copy(pills = if (id in l.pills) l.pills - id else l.pills + id)
    }

    fun movePill(id: WidgetId, delta: Int) = updateLayout { l -> l.copy(pills = l.pills.moved(id, delta)) }

    companion object {
        const val MAX_FAVOURITES = 10
        const val MAX_PROJECT_APPS = 4
    }
}

/** This list with [item] moved [delta] places (clamped to the ends). */
fun <T> List<T>.moved(item: T, delta: Int): List<T> {
    val from = indexOf(item)
    if (from < 0) return this
    val to = (from + delta).coerceIn(0, size - 1)
    if (to == from) return this
    return toMutableList().apply { add(to, removeAt(from)) }
}

/** Small, varied encouragement. */
object Cheers {
    private val done = listOf(
        "Done! One less thing.",
        "Shipped it.",
        "Look at you go.",
        "That's progress.",
        "Crossed off. Nice.",
        "Brain space freed up.",
    )

    fun taskDone(text: String): String = done[Math.floorMod(text.hashCode(), done.size)]
}
