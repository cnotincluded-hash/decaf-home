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
        put("dock", JSONArray(s.dock))
        put("distracting", JSONArray(s.distracting.sorted()))
        put("promptDay", s.promptDay)
        put("promptOffset", s.promptOffset)
        put("themeMode", s.themeMode.name)
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
            dock = o.optArray("dock").strings(),
            distracting = o.optArray("distracting").strings().toSet(),
            promptDay = o.optLong("promptDay", -1),
            promptOffset = o.optInt("promptOffset", 0),
            themeMode = enumOr(o.optString("themeMode"), ThemeMode.SYSTEM),
            nextId = o.optLong("nextId", defaults.nextId),
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
