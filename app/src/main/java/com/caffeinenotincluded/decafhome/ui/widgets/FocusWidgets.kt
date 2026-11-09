package com.caffeinenotincluded.decafhome.ui.widgets

import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caffeinenotincluded.decafhome.data.DayPlan
import com.caffeinenotincluded.decafhome.data.DecafState
import com.caffeinenotincluded.decafhome.data.TimerKind
import com.caffeinenotincluded.decafhome.ui.clockText
import com.caffeinenotincluded.decafhome.ui.components.BrandCard
import com.caffeinenotincluded.decafhome.ui.components.ChoiceChip
import com.caffeinenotincluded.decafhome.ui.components.PillButton
import com.caffeinenotincluded.decafhome.ui.components.ProgressBar
import com.caffeinenotincluded.decafhome.ui.components.ProgressRing
import com.caffeinenotincluded.decafhome.ui.components.TextEntryDialog
import com.caffeinenotincluded.decafhome.ui.durationText
import com.caffeinenotincluded.decafhome.ui.minuteOfDay
import com.caffeinenotincluded.decafhome.ui.rememberNow
import com.caffeinenotincluded.decafhome.ui.screens.HomeActions
import com.caffeinenotincluded.decafhome.ui.screens.Overlay
import com.caffeinenotincluded.decafhome.ui.theme.Brand
import com.caffeinenotincluded.decafhome.ui.theme.Fredoka
import com.caffeinenotincluded.decafhome.ui.theme.LocalDecafColors
import com.caffeinenotincluded.decafhome.ui.theme.Nunito
import com.caffeinenotincluded.decafhome.ui.timerText
import kotlinx.coroutines.delay

// ---- Shared text styles ---------------------------------------------------------

@Composable
internal fun BigText(text: String, size: Int = 22, modifier: Modifier = Modifier) {
    Text(
        text,
        fontFamily = Fredoka,
        fontWeight = FontWeight.SemiBold,
        fontSize = size.sp,
        lineHeight = (size + 4).sp,
        color = LocalDecafColors.current.onCard,
        modifier = modifier,
    )
}

@Composable
internal fun BodyText(text: String, muted: Boolean = false, modifier: Modifier = Modifier) {
    val colors = LocalDecafColors.current
    Text(
        text,
        fontFamily = Nunito,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 21.sp,
        color = if (muted) colors.mutedOnCard else colors.onCard,
        modifier = modifier,
    )
}

@Composable
internal fun SmallLabel(text: String) {
    Text(
        text.uppercase(),
        fontFamily = Nunito,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 11.sp,
        letterSpacing = 1.sp,
        color = LocalDecafColors.current.mutedOnCard,
    )
}

@Composable
internal fun HeaderAction(text: String, onClick: () -> Unit) {
    Text(
        text,
        fontFamily = Nunito,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 13.sp,
        color = LocalDecafColors.current.onCard,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

@Composable
internal fun HeaderAdd(contentDescription: String, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(28.dp)) {
        Icon(Icons.Filled.Add, contentDescription = contentDescription, tint = LocalDecafColors.current.onCard)
    }
}

// ---- Where was I? ------------------------------------------------------------------

/** Shown after a focus sprint: one line for future you. */
@Composable
fun WhereWasICard(state: DecafState, actions: HomeActions) {
    val project = state.activeProject ?: return
    var text by remember { mutableStateOf("") }
    BrandCard("Before you switch off", Brand.Sunshine) {
        BigText("Where were you in ${project.name}?", size = 20)
        Spacer(Modifier.size(4.dp))
        BodyText("One line for future you. It's the first thing you'll see next time.", muted = true)
        Spacer(Modifier.size(10.dp))
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("e.g. halfway through the jump animation") },
            minLines = 2,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.size(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            PillButton("Save (+5 XP)", onClick = { actions.store.saveWhereWasI(text) }, enabled = text.isNotBlank())
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { actions.store.skipWhereWasI() }) { Text("Skip") }
        }
    }
}

// ---- Now / Next --------------------------------------------------------------------

@Composable
fun NowNextCard(state: DecafState, actions: HomeActions) {
    val context = LocalContext.current
    val is24 = DateFormat.is24HourFormat(context)
    val now by rememberNow(30_000)

    if (state.timeline.isEmpty()) {
        BrandCard("Now / Next", Brand.Sky) {
            BigText("What's now, what's next?", size = 20)
            Spacer(Modifier.size(4.dp))
            BodyText("Plan your day in a few blocks, and this strip shows where you are and how long is left.", muted = true)
            Spacer(Modifier.size(12.dp))
            PillButton("Plan my day", onClick = { actions.nav.open(Overlay.DayPlan) }, color = Brand.Sky)
        }
        return
    }

    val minute = minuteOfDay(now)
    val plan = DayPlan.at(state.timeline, minute)
    BrandCard(
        "Now / Next",
        Brand.Sky,
        action = { HeaderAction("Edit") { actions.nav.open(Overlay.DayPlan) } },
    ) {
        SmallLabel("Now")
        BigText(plan.now?.label ?: "Free time", size = 24)
        Spacer(Modifier.size(8.dp))
        ProgressBar(plan.fraction, Brand.Sky, Modifier.fillMaxWidth())
        Spacer(Modifier.size(8.dp))
        val next = plan.next
        if (next != null) {
            BodyText("${durationText(plan.minutesLeft)} left. Next: ${next.label} at ${clockText(next.minuteOfDay, is24)}")
        } else {
            BodyText("Nothing else planned today.", muted = true)
        }
    }
}

// ---- One thing now -----------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OneThingCard(state: DecafState, actions: HomeActions) {
    val colors = LocalDecafColors.current
    var adding by remember { mutableStateOf(false) }
    var splitting by remember { mutableStateOf(false) }
    var showQueue by remember { mutableStateOf(false) }
    val task = state.tasks.firstOrNull()

    BrandCard(
        "One thing now",
        Brand.Coral,
        action = { HeaderAdd("Add a task") { adding = true } },
    ) {
        if (task == null) {
            BigText("What's the one thing?", size = 22)
            Spacer(Modifier.size(4.dp))
            BodyText("Add a task and it sits here on its own. No list to get lost in.", muted = true)
            Spacer(Modifier.size(12.dp))
            PillButton("Add a task", onClick = { adding = true }, color = Brand.Coral)
        } else {
            BigText(task.text, size = 26)
            if (state.tasks.size > 1) {
                Spacer(Modifier.size(4.dp))
                Text(
                    if (showQueue) "Hide the queue" else "${state.tasks.size - 1} more waiting",
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = colors.mutedOnCard,
                    modifier = Modifier.clickable { showQueue = !showQueue },
                )
            }
            Spacer(Modifier.size(12.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PillButton("Done ✓", onClick = { actions.store.completeTask(task.id) }, color = Brand.Mint)
                PillButton(
                    "Sprint on it",
                    onClick = { actions.startTimer(TimerKind.FOCUS, state.focusMinutes, task.text) },
                    color = Brand.Sunshine,
                    enabled = state.timer == null,
                )
                PillButton("Later", onClick = { actions.store.laterTask(task.id) }, color = Brand.Cream, enabled = state.tasks.size > 1)
                PillButton("Too big? Split it", onClick = { splitting = true }, color = Brand.Lilac)
            }
            if (showQueue) {
                Spacer(Modifier.size(10.dp))
                state.tasks.drop(1).forEach { queued ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BodyText(
                            queued.text,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { actions.store.moveTaskToFront(queued.id) }
                                .padding(vertical = 6.dp),
                        )
                        IconButton(onClick = { actions.store.deleteTask(queued.id) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Filled.Close, contentDescription = "Delete", tint = colors.mutedOnCard)
                        }
                    }
                }
                BodyText("Tap a task to make it the one thing.", muted = true)
            }
        }
    }

    if (adding) {
        TextEntryDialog(
            title = "Add a task",
            confirmLabel = "Add",
            placeholder = "Something small enough to start now",
            onConfirm = { actions.store.addTask(it); adding = false },
            onDismiss = { adding = false },
        )
    }
    if (splitting && task != null) {
        TextEntryDialog(
            title = "Split it into small steps",
            confirmLabel = "Split",
            supporting = "One step per line. The first step becomes your one thing.",
            placeholder = "Open the project\nFind the jump code\nTweak the height",
            multiLine = true,
            onConfirm = { actions.store.splitTask(task.id, it.lines()); splitting = false },
            onDismiss = { splitting = false },
        )
    }
}

// ---- Focus timer ---------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FocusTimerCard(state: DecafState, actions: HomeActions) {
    val timer = state.timer
    if (timer == null) {
        val label = state.tasks.firstOrNull()?.text ?: state.activeProject?.name ?: ""
        BrandCard("Focus timer", Brand.Mint) {
            BigText("Focus sprint", size = 22)
            Spacer(Modifier.size(4.dp))
            BodyText(if (label.isNotEmpty()) "On: $label" else "Pick a length and go. The ring shows time left at a glance.", muted = true)
            Spacer(Modifier.size(10.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(10, 15, 25, 45).forEach { minutes ->
                    ChoiceChip("$minutes min", state.focusMinutes == minutes, onClick = { actions.store.setFocusMinutes(minutes) }, accent = Brand.Mint)
                }
            }
            Spacer(Modifier.size(12.dp))
            PillButton(
                "Start ${state.focusMinutes}-minute sprint",
                onClick = { actions.startTimer(TimerKind.FOCUS, state.focusMinutes, label) },
                color = Brand.Mint,
            )
        }
        return
    }

    val now by rememberNow(1_000)
    val total = timer.minutes * 60_000L
    val left = (timer.endsAt - now).coerceAtLeast(0)
    LaunchedEffect(timer) {
        val wait = timer.endsAt - System.currentTimeMillis()
        if (wait > 0) delay(wait)
        actions.finishTimerIfDue()
    }
    val focus = timer.kind == TimerKind.FOCUS
    val accent = if (focus) Brand.Mint else Brand.Sky

    BrandCard(if (focus) "Focus timer" else "Break timer", accent) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(left.toFloat() / total, accent, Modifier.size(132.dp), stroke = 14.dp) {
                Text(
                    timerText(left),
                    fontFamily = Fredoka,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 28.sp,
                    color = LocalDecafColors.current.onCard,
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                BigText(if (focus) "Focusing" else "On a break", size = 22)
                if (timer.label.isNotBlank()) {
                    Spacer(Modifier.size(2.dp))
                    BodyText(timer.label, muted = true)
                }
                Spacer(Modifier.size(10.dp))
                PillButton("Stop", onClick = { actions.cancelTimer() }, color = Brand.Cream, small = true)
            }
        }
    }
}

// ---- Break menu ----------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BreakMenuCard(state: DecafState, actions: HomeActions) {
    val busy = state.timer != null
    BrandCard(
        "Break menu",
        Brand.Mint,
        action = { HeaderAction("Edit") { actions.nav.open(Overlay.Breaks) } },
    ) {
        BodyText(
            if (busy) "Finish or stop the timer first, then pick a break."
            else "Breaks you picked in advance, so a break doesn't turn into scrolling. Tap one for 5 minutes.",
            muted = true,
        )
        Spacer(Modifier.size(10.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.breaks.forEach { item ->
                ChoiceChip(item.text, selected = false, accent = Brand.Mint, onClick = {
                    if (!busy) actions.startTimer(TimerKind.BREAK, 5, item.text)
                })
            }
        }
    }
}
