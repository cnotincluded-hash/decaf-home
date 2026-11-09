package com.caffeinenotincluded.decafhome.data

import org.json.JSONArray
import org.json.JSONObject

/**
 * DecafState <-> JSON. Every field has a default, so older saves keep loading
 * after new fields are added.
 */
object StateJson {

    fun encode(s: DecafState): JSONObject = JSONObject().apply {
        put("version", 1)
        put("tasks", JSONArray(s.tasks.map { JSONObject().put("id", it.id).put("text", it.text) }))
        put("projects", JSONArray(s.projects.map { p ->
            JSONObject()
                .put("id", p.id)
                .put("name", p.name)
                .put("color", p.color)
                .put("nextStep", p.nextStep)
                .put("whereWasI", p.whereWasI)
                .put("whereWasIAt", p.whereWasIAt)
                .put("apps", JSONArray(p.apps))
        }))
        s.activeProjectId?.let { put("activeProjectId", it) }
        put("ideas", JSONArray(s.ideas.map { i ->
            JSONObject()
                .put("id", i.id)
                .put("text", i.text)
                .put("kind", i.kind.name)
                .put("createdAt", i.createdAt)
                .apply { i.projectId?.let { put("projectId", it) } }
        }))
        put("deadlines", JSONArray(s.deadlines.map {
            JSONObject().put("id", it.id).put("title", it.title).put("dueAt", it.dueAt)
        }))
        put("devDays", JSONArray(s.devDays.sorted()))
        put("restDaysPerWeek", s.restDaysPerWeek)
        put("timeline", JSONArray(s.timeline.map {
            JSONObject().put("id", it.id).put("minuteOfDay", it.minuteOfDay).put("label", it.label)
        }))
        put("breaks", JSONArray(s.breaks.map { JSONObject().put("id", it.id).put("text", it.text) }))
        put("xp", s.xp)
        s.timer?.let { t ->
            put("timer", JSONObject()
                .put("kind", t.kind.name)
                .put("minutes", t.minutes)
                .put("startedAt", t.startedAt)
                .put("endsAt", t.endsAt)
                .put("label", t.label))
        }
        put("focusMinutes", s.focusMinutes)
        put("askWhereWasI", s.askWhereWasI)
        put("favourites", JSONArray(s.favourites))
        put("distracting", JSONArray(s.distracting.sorted()))
        put("promptDay", s.promptDay)
        put("promptOffset", s.promptOffset)
        put("themeMode", s.themeMode.name)
        put("layout", encodeLayout(s.layout))
        put("nextId", s.nextId)
    }

    fun decode(o: JSONObject): DecafState {
        val defaults = DecafState()
        return DecafState(
            tasks = o.optArray("tasks").objects().map { Task(it.getLong("id"), it.getString("text")) },
            projects = o.optArray("projects").objects().map { p ->
                Project(
                    id = p.getLong("id"),
                    name = p.optString("name", "Untitled"),
                    color = p.optInt("color", 0),
                    nextStep = p.optString("nextStep", ""),
                    whereWasI = p.optString("whereWasI", ""),
                    whereWasIAt = p.optLong("whereWasIAt", 0L),
                    apps = p.optArray("apps").strings(),
                )
            },
            activeProjectId = if (o.has("activeProjectId")) o.getLong("activeProjectId") else null,
            ideas = o.optArray("ideas").objects().map { i ->
                Idea(
                    id = i.getLong("id"),
                    text = i.optString("text", ""),
                    kind = enumOr(i.optString("kind"), IdeaKind.IDEA),
                    projectId = if (i.has("projectId")) i.getLong("projectId") else null,
                    createdAt = i.optLong("createdAt", 0L),
                )
            },
            deadlines = o.optArray("deadlines").objects().map {
                Deadline(it.getLong("id"), it.optString("title", ""), it.getLong("dueAt"))
            },
            devDays = o.optArray("devDays").strings().toSet(),
            restDaysPerWeek = o.optInt("restDaysPerWeek", defaults.restDaysPerWeek),
            timeline = o.optArray("timeline").objects().map {
                TimeBlock(it.getLong("id"), it.getInt("minuteOfDay"), it.optString("label", ""))
            },
            breaks = if (o.has("breaks")) {
                o.optArray("breaks").objects().map { BreakItem(it.getLong("id"), it.optString("text", "")) }
            } else {
                defaults.breaks
            },
            xp = o.optInt("xp", 0),
            timer = o.optJSONObject("timer")?.let { t ->
                Timer(
                    kind = enumOr(t.optString("kind"), TimerKind.FOCUS),
                    minutes = t.optInt("minutes", 25),
                    startedAt = t.optLong("startedAt", 0L),
                    endsAt = t.getLong("endsAt"),
                    label = t.optString("label", ""),
                )
            },
            focusMinutes = o.optInt("focusMinutes", defaults.focusMinutes),
            askWhereWasI = o.optBoolean("askWhereWasI", false),
            // "dock" is the name used by version 0.1.0
            favourites = o.optArray(if (o.has("favourites")) "favourites" else "dock").strings(),
            distracting = o.optArray("distracting").strings().toSet(),
            promptDay = o.optLong("promptDay", -1),
            promptOffset = o.optInt("promptOffset", 0),
            themeMode = enumOr(o.optString("themeMode"), ThemeMode.SYSTEM),
            layout = o.optJSONObject("layout")?.let { decodeLayout(it) } ?: defaults.layout,
            nextId = o.optLong("nextId", defaults.nextId),
        )
    }

    private fun encodeLayout(l: LayoutPrefs): JSONObject = JSONObject()
        .put("style", l.style.name)
        .put("hand", l.hand.name)
        .put("iconSize", l.iconSize.name)
        .put("labelSize", l.labelSize.name)
        .put("font", l.font.name)
        .put("clock", l.clock.name)
        .put("showDate", l.showDate)
        .put("showXp", l.showXp)
        .put("favouritesAtBottom", l.favouritesAtBottom)
        .put("accent", l.accent)
        .put("pills", JSONArray(l.pills.map { it.name }))
        .put("swipeUp", l.swipeUp.name)
        .put("swipeDown", l.swipeDown.name)
        .put("haptics", l.haptics)

    private fun decodeLayout(o: JSONObject): LayoutPrefs {
        val d = LayoutPrefs()
        return LayoutPrefs(
            style = enumOr(o.optString("style"), d.style),
            hand = enumOr(o.optString("hand"), d.hand),
            iconSize = enumOr(o.optString("iconSize"), d.iconSize),
            labelSize = enumOr(o.optString("labelSize"), d.labelSize),
            font = enumOr(o.optString("font"), d.font),
            clock = enumOr(o.optString("clock"), d.clock),
            showDate = o.optBoolean("showDate", d.showDate),
            showXp = o.optBoolean("showXp", d.showXp),
            favouritesAtBottom = o.optBoolean("favouritesAtBottom", d.favouritesAtBottom),
            accent = o.optInt("accent", d.accent),
            pills = if (o.has("pills")) {
                o.optArray("pills").strings().mapNotNull { name -> WidgetId.entries.firstOrNull { it.name == name } }.distinct()
            } else {
                d.pills
            },
            swipeUp = enumOr(o.optString("swipeUp"), d.swipeUp),
            swipeDown = enumOr(o.optString("swipeDown"), d.swipeDown),
            haptics = o.optBoolean("haptics", d.haptics),
        )
    }

    private fun JSONObject.optArray(name: String): JSONArray = optJSONArray(name) ?: JSONArray()

    private fun JSONArray.objects(): List<JSONObject> =
        (0 until length()).mapNotNull { optJSONObject(it) }

    private fun JSONArray.strings(): List<String> =
        (0 until length()).mapNotNull { i -> optString(i).takeIf { it.isNotEmpty() } }

    private inline fun <reified E : Enum<E>> enumOr(name: String?, fallback: E): E =
        enumValues<E>().firstOrNull { it.name == name } ?: fallback
}
