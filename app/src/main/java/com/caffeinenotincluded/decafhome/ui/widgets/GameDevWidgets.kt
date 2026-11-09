package com.caffeinenotincluded.decafhome.ui.widgets

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caffeinenotincluded.decafhome.data.AppEntry
import com.caffeinenotincluded.decafhome.data.DayMark
import com.caffeinenotincluded.decafhome.data.DecafState
import com.caffeinenotincluded.decafhome.data.IdeaKind
import com.caffeinenotincluded.decafhome.data.Prompts
import com.caffeinenotincluded.decafhome.data.Streaks
import com.caffeinenotincluded.decafhome.ui.agoText
import com.caffeinenotincluded.decafhome.ui.components.AppTile
import com.caffeinenotincluded.decafhome.ui.components.BrandCard
import com.caffeinenotincluded.decafhome.ui.components.ChoiceChip
import com.caffeinenotincluded.decafhome.ui.components.PillButton
import com.caffeinenotincluded.decafhome.ui.components.TextEntryDialog
import com.caffeinenotincluded.decafhome.ui.countdownText
import com.caffeinenotincluded.decafhome.ui.localDateTime
import com.caffeinenotincluded.decafhome.ui.rememberNow
import com.caffeinenotincluded.decafhome.ui.screens.HomeActions
import com.caffeinenotincluded.decafhome.ui.screens.Overlay
import com.caffeinenotincluded.decafhome.ui.shortDate
import com.caffeinenotincluded.decafhome.ui.theme.Brand
import com.caffeinenotincluded.decafhome.ui.theme.Fredoka
import com.caffeinenotincluded.decafhome.ui.theme.LocalDecafColors
import com.caffeinenotincluded.decafhome.ui.theme.Nunito
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

// ---- Project spotlight -----------------------------------------------------------------

@Composable
fun ProjectSpotlightCard(state: DecafState, apps: List<AppEntry>, actions: HomeActions) {
    val now by rememberNow(60_000)
    var adding by remember { mutableStateOf(false) }
    var editingNext by remember { mutableStateOf(false) }
    var switching by remember { mutableStateOf(false) }
    val project = state.activeProject
    val accent = project?.let { Brand.Accents[it.color.mod(Brand.Accents.size)] } ?: Brand.Lilac

    BrandCard(
        "Project spotlight",
        accent,
        action = {
            if (state.projects.isNotEmpty()) {
                Box {
                    HeaderAction("Switch") { switching = true }
                    DropdownMenu(expanded = switching, onDismissRequest = { switching = false }) {
                        state.projects.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(if (p.id == state.activeProjectId) "${p.name}  ✓" else p.name) },
                                onClick = { actions.store.setActiveProject(p.id); switching = false },
                            )
                        }
                        DropdownMenuItem(text = { Text("New project…") }, onClick = { switching = false; adding = true })
                    }
                }
            }
        },
    ) {
        if (project == null) {
            BigText("Pick one game to focus on", size = 22)
            Spacer(Modifier.size(4.dp))
            BodyText(
                "One project in the spotlight makes it easier to start, and easier to say \"later\" to shiny new ideas.",
                muted = true,
            )
            Spacer(Modifier.size(12.dp))
            PillButton("Add a project", onClick = { adding = true }, color = accent)
        } else {
            BigText(project.name, size = 28)
            Spacer(Modifier.size(10.dp))

            SmallLabel("Where you left off")
            if (project.whereWasI.isNotBlank()) {
                BodyText(project.whereWasI)
                BodyText(agoText(project.whereWasIAt, now), muted = true)
            } else {
                BodyText("Nothing yet. After a focus sprint you'll be asked.", muted = true)
            }
            Spacer(Modifier.size(10.dp))

            SmallLabel("Next step")
            BodyText(
                project.nextStep.ifBlank { "Tap to set the next small step" },
                muted = project.nextStep.isBlank(),
                modifier = Modifier.clickable { editingNext = true }.padding(vertical = 2.dp),
            )

            val byKey = remember(apps) { apps.associateBy { it.key } }
            val tools = project.apps.mapNotNull { byKey[it] }
            if (tools.isNotEmpty()) {
                Spacer(Modifier.size(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    tools.forEach { app ->
                        AppTile(
                            app = app,
                            onClick = { actions.launch(app) },
                            onLongClick = {},
                            iconSize = 44.dp,
                            labelColor = LocalDecafColors.current.onCard,
                            modifier = Modifier.width(68.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.size(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (project.nextStep.isNotBlank()) {
                    PillButton(
                        "Make it my one thing",
                        onClick = { actions.store.addTask(project.nextStep, atFront = true) },
                        color = Brand.Coral,
                        small = true,
                    )
                }
                PillButton(
                    "Edit project",
                    onClick = { actions.nav.open(Overlay.EditProject(project.id)) },
                    color = Brand.Cream,
                    small = true,
                )
            }
        }
    }

    if (adding) {
        TextEntryDialog(
            title = "New project",
            confirmLabel = "Add",
            placeholder = "Game name",
            onConfirm = { actions.store.addProject(it); adding = false },
            onDismiss = { adding = false },
        )
    }
    if (editingNext && project != null) {
        TextEntryDialog(
            title = "Next small step",
            confirmLabel = "Save",
            initial = project.nextStep,
            placeholder = "Small enough to start in 5 minutes",
            onConfirm = { actions.store.saveProject(project.copy(nextStep = it.trim())); editingNext = false },
            onDismiss = { editingNext = false },
        )
    }
}

// ---- Idea parking lot --------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IdeaParkingCard(state: DecafState, actions: HomeActions) {
    val colors = LocalDecafColors.current
    var text by remember { mutableStateOf("") }
    var kind by remember { mutableStateOf(IdeaKind.IDEA) }
    val park = {
        actions.store.parkIdea(text, kind)
        text = ""
    }

    BrandCard(
        "Idea parking lot",
        Brand.Sunshine,
        action = {
            if (state.ideas.isNotEmpty()) HeaderAction("See all (${state.ideas.size})") { actions.nav.open(Overlay.Ideas) }
        },
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("Park an idea, bug or note…") },
            maxLines = 3,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { if (text.isNotBlank()) park() }),
            trailingIcon = {
                IconButton(onClick = park, enabled = text.isNotBlank()) {
                    Icon(Icons.Filled.Add, contentDescription = "Park it")
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.size(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IdeaKind.entries.forEach { k ->
                ChoiceChip(k.label, kind == k, onClick = { kind = k }, accent = Brand.Sunshine)
            }
        }
        if (state.ideas.isNotEmpty()) {
            Spacer(Modifier.size(10.dp))
            state.ideas.take(2).forEach { idea ->
                Text(
                    "${idea.kind.label}: ${idea.text}",
                    fontFamily = Nunito,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = colors.mutedOnCard,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        } else {
            Spacer(Modifier.size(6.dp))
            BodyText("New ideas wait here, so they don't pull you off the project you're on.", muted = true)
        }
    }
}

// ---- Countdowns ----------------------------------------------------------------------------

@Composable
fun CountdownsCard(state: DecafState, actions: HomeActions) {
    val context = LocalContext.current
    val colors = LocalDecafColors.current
    val now by rememberNow(60_000)
    var pendingDue by remember { mutableStateOf<Long?>(null) }

    // Date, then time, then a name.
    val startAdding = {
        val today = LocalDate.now()
        DatePickerDialog(
            context,
            { _, year, month, day ->
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        pendingDue = LocalDateTime.of(year, month + 1, day, hour, minute)
                            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    },
                    18,
                    0,
                    DateFormat.is24HourFormat(context),
                ).show()
            },
            today.year,
            today.monthValue - 1,
            today.dayOfMonth,
        ).show()
    }

    // Keep passed deadlines for a day so you see "Time's up", then drop them from view.
    val visible = state.deadlines.filter { it.dueAt > now - 24 * 3_600_000L }.sortedBy { it.dueAt }

    BrandCard(
        "Countdowns",
        Brand.Sky,
        action = { HeaderAdd("Add a countdown") { startAdding() } },
    ) {
        if (visible.isEmpty()) {
            BigText("Nothing on the clock", size = 20)
            Spacer(Modifier.size(4.dp))
            BodyText("Add a game jam, a devlog day or your own milestone and watch it count down.", muted = true)
            Spacer(Modifier.size(12.dp))
            PillButton("Add a countdown", onClick = { startAdding() }, color = Brand.Sky)
        } else {
            visible.take(4).forEach { deadline ->
                val left = deadline.dueAt - now
                val urgent = left < 24 * 3_600_000L
                Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        BigText(deadline.title, size = 18)
                        BodyText(shortDate(deadline.dueAt), muted = true)
                    }
                    Text(
                        countdownText(left),
                        fontFamily = Fredoka,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = if (urgent) Brand.Coral else colors.onCard,
                    )
                    IconButton(onClick = { actions.store.deleteDeadline(deadline.id) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = "Remove", tint = colors.mutedOnCard)
                    }
                }
            }
        }
    }

    pendingDue?.let { due ->
        TextEntryDialog(
            title = "What's the countdown for?",
            confirmLabel = "Add",
            placeholder = "e.g. Ludum Dare, devlog #4, demo build",
            onConfirm = { actions.store.addDeadline(it, due); pendingDue = null },
            onDismiss = { pendingDue = null },
        )
    }
}

// ---- Dev streak --------------------------------------------------------------------------------

@Composable
fun StreakCard(state: DecafState, actions: HomeActions) {
    val colors = LocalDecafColors.current
    val now by rememberNow(60_000)
    val today = localDateTime(now).toLocalDate()
    val info = remember(state.devDays, state.restDaysPerWeek, today) {
        Streaks.compute(state.devDays, today, state.restDaysPerWeek)
    }

    BrandCard("Dev streak", Brand.Coral) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${info.days}",
                fontFamily = Fredoka,
                fontWeight = FontWeight.Bold,
                fontSize = 52.sp,
                lineHeight = 54.sp,
                color = colors.onCard,
            )
            Spacer(Modifier.width(12.dp))
            Column {
                BigText(if (info.days == 1) "dev day in a row" else "dev days in a row", size = 18)
                BodyText(
                    when (info.restLeftThisWeek) {
                        0 -> "No rest days left this week"
                        1 -> "1 rest day left this week"
                        else -> "${info.restLeftThisWeek} rest days left this week"
                    },
                    muted = true,
                )
            }
        }
        Spacer(Modifier.size(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("M", "T", "W", "T", "F", "S", "S").zip(info.week).forEach { (letter, mark) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    WeekDot(mark)
                    Spacer(Modifier.size(4.dp))
                    Text(letter, fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = colors.mutedOnCard)
                }
            }
        }
        Spacer(Modifier.size(12.dp))
        if (info.workedToday) {
            BodyText("Today counts. Nice.", muted = true)
        } else {
            PillButton("I worked on my game today", onClick = { actions.store.checkInToday() }, color = Brand.Coral, small = true)
        }
    }
}

@Composable
private fun WeekDot(mark: DayMark) {
    val colors = LocalDecafColors.current
    val (fill, border, label) = when (mark) {
        DayMark.DEV -> Triple(Brand.Coral, colors.outline, "✓")
        DayMark.REST -> Triple(Color.Transparent, colors.outline.copy(alpha = 0.35f), "z")
        DayMark.TODAY -> Triple(Brand.Sunshine.copy(alpha = 0.35f), colors.outline, "")
        DayMark.FUTURE -> Triple(Color.Transparent, colors.outline.copy(alpha = 0.2f), "")
    }
    Box(
        Modifier
            .size(30.dp)
            .background(fill, CircleShape)
            .border(2.dp, border, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (label.isNotEmpty()) {
            Text(
                label,
                fontFamily = Fredoka,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (mark == DayMark.DEV) Brand.Ink else colors.mutedOnCard,
            )
        }
    }
}

// ---- Design prompt of the day --------------------------------------------------------------

@Composable
fun PromptCard(state: DecafState, actions: HomeActions) {
    val now by rememberNow(60_000)
    val epochDay = localDateTime(now).toLocalDate().toEpochDay()
    val offset = if (state.promptDay == epochDay) state.promptOffset else 0
    val prompt = Prompts.forDay(epochDay, offset)

    BrandCard("Design prompt of the day", Brand.Lilac) {
        BigText(prompt, size = 20)
        Spacer(Modifier.size(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PillButton("Another one", onClick = { actions.store.anotherPrompt(epochDay) }, color = Brand.Cream, small = true)
            PillButton("Park it", onClick = { actions.store.parkIdea(prompt, IdeaKind.IDEA) }, color = Brand.Sunshine, small = true)
        }
    }
}
