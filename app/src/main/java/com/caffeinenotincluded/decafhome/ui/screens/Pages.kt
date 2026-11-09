package com.caffeinenotincluded.decafhome.ui.screens

import android.app.TimePickerDialog
import android.content.Intent
import android.provider.Settings
import android.text.format.DateFormat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
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
import com.caffeinenotincluded.decafhome.data.AppEntry
import com.caffeinenotincluded.decafhome.data.DecafState
import com.caffeinenotincluded.decafhome.data.IdeaKind
import com.caffeinenotincluded.decafhome.data.Store
import com.caffeinenotincluded.decafhome.data.ThemeMode
import com.caffeinenotincluded.decafhome.ui.agoText
import com.caffeinenotincluded.decafhome.ui.clockText
import com.caffeinenotincluded.decafhome.ui.components.BrandCard
import com.caffeinenotincluded.decafhome.ui.components.ChoiceChip
import com.caffeinenotincluded.decafhome.ui.components.PillButton
import com.caffeinenotincluded.decafhome.ui.components.TextEntryDialog
import com.caffeinenotincluded.decafhome.ui.theme.Brand
import com.caffeinenotincluded.decafhome.ui.theme.Fredoka
import com.caffeinenotincluded.decafhome.ui.theme.LocalDecafColors
import com.caffeinenotincluded.decafhome.ui.theme.Nunito
import com.caffeinenotincluded.decafhome.ui.widgets.BigText
import com.caffeinenotincluded.decafhome.ui.widgets.BodyText

/** A full-screen page over the home screen, with a back arrow and a title. */
@Composable
private fun Page(title: String, onBack: () -> Unit, content: LazyListScope.() -> Unit) {
    val colors = LocalDecafColors.current
    Column(
        Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.onBackground)
            }
            Text(title, fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, color = colors.onBackground)
        }
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            content = content,
        )
    }
}

// ---- Settings -----------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsPage(state: DecafState, actions: HomeActions) {
    val context = LocalContext.current
    Page("Settings", onBack = { actions.nav.close() }) {
        item {
            BrandCard("Home app", Brand.Sunshine) {
                BodyText("To use Decaf Home every time you press Home, choose it as your home app.")
                Spacer(Modifier.size(10.dp))
                PillButton("Choose home app", onClick = {
                    runCatching {
                        context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                })
                Spacer(Modifier.size(6.dp))
                BodyText("You can switch back to your phone's launcher in the same place.", muted = true)
            }
        }
        item {
            BrandCard("Look", Brand.Lilac) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(ThemeMode.SYSTEM to "Match phone", ThemeMode.LIGHT to "Light", ThemeMode.DARK to "Dark").forEach { (mode, label) ->
                        ChoiceChip(label, state.themeMode == mode, onClick = { actions.store.setThemeMode(mode) }, accent = Brand.Lilac)
                    }
                }
            }
        }
        item {
            BrandCard("Dev streak", Brand.Coral) {
                BodyText("Rest days per week. Missing this many days in a week won't break your streak.")
                Spacer(Modifier.size(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (0..3).forEach { days ->
                        ChoiceChip("$days", state.restDaysPerWeek == days, onClick = { actions.store.setRestDays(days) }, accent = Brand.Coral)
                    }
                }
            }
        }
        item {
            BrandCard("Speed bumps", Brand.Coral) {
                BodyText(
                    when (state.distracting.size) {
                        0 -> "No apps have a speed bump yet."
                        1 -> "1 app opens after a 10-second pause."
                        else -> "${state.distracting.size} apps open after a 10-second pause."
                    },
                )
                Spacer(Modifier.size(4.dp))
                BodyText(
                    "Only when opened from Decaf Home. Notifications and recent apps skip it.",
                    muted = true,
                )
                Spacer(Modifier.size(10.dp))
                PillButton("Choose apps", onClick = { actions.nav.open(Overlay.SpeedBumpApps) }, color = Brand.Coral)
            }
        }
        item {
            BrandCard("Plans and breaks", Brand.Sky) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PillButton("Day plan", onClick = { actions.nav.open(Overlay.DayPlan) }, color = Brand.Sky, small = true)
                    PillButton("Break menu", onClick = { actions.nav.open(Overlay.Breaks) }, color = Brand.Mint, small = true)
                }
            }
        }
        if (state.projects.isNotEmpty()) {
            item {
                BrandCard("Projects", Brand.Lilac) {
                    state.projects.forEach { project ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { actions.nav.open(Overlay.EditProject(project.id)) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                Modifier
                                    .size(14.dp)
                                    .background(Brand.Accents[project.color.mod(Brand.Accents.size)], CircleShape)
                                    .border(1.5.dp, LocalDecafColors.current.outline, CircleShape),
                            )
                            Spacer(Modifier.width(10.dp))
                            BodyText(project.name, modifier = Modifier.weight(1f))
                            BodyText("Edit", muted = true)
                        }
                    }
                }
            }
        }
        item {
            BrandCard("About", Brand.Mint) {
                BodyText("Decaf Home 0.1.0, by Caffeine Not Included.")
                Spacer(Modifier.size(4.dp))
                BodyText(
                    "Everything stays on this phone. If Android backup is on, it's included in your phone's backup.",
                    muted = true,
                )
            }
        }
    }
}

// ---- Idea parking lot: everything parked ----------------------------------------------------

@Composable
fun IdeasPage(state: DecafState, actions: HomeActions) {
    var filter by remember { mutableStateOf<IdeaKind?>(null) }
    val shown = state.ideas.filter { filter == null || it.kind == filter }
    val now = System.currentTimeMillis()

    Page("Parked ideas", onBack = { actions.nav.close() }) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChoiceChip("All", filter == null, onClick = { filter = null })
                IdeaKind.entries.forEach { kind ->
                    ChoiceChip(kind.label + "s", filter == kind, onClick = { filter = kind })
                }
            }
        }
        if (shown.isEmpty()) {
            item { BodyTextOnBackground("Nothing parked here.") }
        }
        items(shown, key = { it.id }) { idea ->
            val project = state.projects.firstOrNull { it.id == idea.projectId }
            BrandCard(idea.kind.label, if (idea.kind == IdeaKind.BUG) Brand.Coral else Brand.Sunshine) {
                BodyText(idea.text)
                Spacer(Modifier.size(4.dp))
                BodyText(
                    listOfNotNull(project?.name, agoText(idea.createdAt, now)).joinToString(" · "),
                    muted = true,
                )
                Spacer(Modifier.size(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PillButton("Make it a task", onClick = { actions.store.ideaToTask(idea.id) }, color = Brand.Coral, small = true)
                    PillButton("Delete", onClick = { actions.store.deleteIdea(idea.id) }, color = Brand.Cream, small = true)
                }
            }
        }
    }
}

@Composable
private fun BodyTextOnBackground(text: String) {
    Text(
        text,
        fontFamily = Nunito,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        color = LocalDecafColors.current.mutedOnBackground,
    )
}

// ---- Day plan (Now / Next) --------------------------------------------------------------------

@Composable
fun DayPlanPage(state: DecafState, actions: HomeActions) {
    val context = LocalContext.current
    val is24 = DateFormat.is24HourFormat(context)
    var pendingMinute by remember { mutableStateOf<Int?>(null) }
    val pickTime = {
        TimePickerDialog(context, { _, hour, minute -> pendingMinute = hour * 60 + minute }, 9, 0, is24).show()
    }

    Page("Day plan", onBack = { actions.nav.close() }) {
        item {
            BrandCard("How it works", Brand.Sky) {
                BodyText("Add the start time of each block in a normal day. The Now / Next strip shows the block you're in, how long is left, and what's next.")
                Spacer(Modifier.size(10.dp))
                PillButton("Add a block", onClick = { pickTime() }, color = Brand.Sky)
            }
        }
        items(state.timeline.sortedBy { it.minuteOfDay }, key = { it.id }) { block ->
            ListRow(
                title = block.label,
                detail = clockText(block.minuteOfDay, is24),
                onDelete = { actions.store.deleteTimeBlock(block.id) },
            )
        }
    }

    pendingMinute?.let { minute ->
        TextEntryDialog(
            title = "Block at ${clockText(minute, is24)}",
            confirmLabel = "Add",
            placeholder = "e.g. Art, Code, Lunch, Play-test",
            onConfirm = { actions.store.addTimeBlock(minute, it); pendingMinute = null },
            onDismiss = { pendingMinute = null },
        )
    }
}

// ---- Break menu -----------------------------------------------------------------------------------

@Composable
fun BreaksPage(state: DecafState, actions: HomeActions) {
    var adding by remember { mutableStateOf(false) }
    Page("Break menu", onBack = { actions.nav.close() }) {
        item {
            BrandCard("Your breaks", Brand.Mint) {
                BodyText("Breaks that actually recharge you. Pick from these when you need one, so it doesn't turn into scrolling.")
                Spacer(Modifier.size(10.dp))
                PillButton("Add a break", onClick = { adding = true }, color = Brand.Mint)
            }
        }
        items(state.breaks, key = { it.id }) { item ->
            ListRow(title = item.text, detail = null, onDelete = { actions.store.deleteBreak(item.id) })
        }
    }
    if (adding) {
        TextEntryDialog(
            title = "Add a break",
            confirmLabel = "Add",
            placeholder = "e.g. Make tea",
            onConfirm = { actions.store.addBreak(it); adding = false },
            onDismiss = { adding = false },
        )
    }
}

@Composable
private fun ListRow(title: String, detail: String?, onDelete: () -> Unit) {
    val colors = LocalDecafColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .background(colors.card, androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
            .border(2.dp, colors.outline, androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
            .padding(start = 16.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (detail != null) {
            Text(detail, fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = colors.onCard)
            Spacer(Modifier.width(12.dp))
        }
        BodyText(title, modifier = Modifier.weight(1f))
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Close, contentDescription = "Delete", tint = colors.mutedOnCard)
        }
    }
}

// ---- Speed bump apps -------------------------------------------------------------------------------

@Composable
fun SpeedBumpAppsPage(state: DecafState, apps: List<AppEntry>, actions: HomeActions) {
    var query by remember { mutableStateOf("") }
    // One row per package (some apps have more than one icon).
    val packages = remember(apps) { apps.distinctBy { it.packageName } }
    val shown = packages.filter { query.isBlank() || it.label.contains(query.trim(), ignoreCase = true) }

    Page("Speed bumps", onBack = { actions.nav.close() }) {
        item {
            Column {
                BodyTextOnBackground("Ticked apps open after a 10-second pause that asks what you came to do.")
                Spacer(Modifier.size(10.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search apps") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        items(shown, key = { it.packageName }) { app ->
            AppCheckRow(app, checked = app.packageName in state.distracting) {
                actions.store.toggleDistracting(app.packageName)
            }
        }
    }
}

@Composable
private fun AppCheckRow(app: AppEntry, checked: Boolean, enabled: Boolean = true, onToggle: () -> Unit) {
    val colors = LocalDecafColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled || checked, onClick = onToggle)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(app.icon, contentDescription = null, modifier = Modifier.size(40.dp))
        Spacer(Modifier.width(12.dp))
        Text(
            app.label,
            fontFamily = Nunito,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = colors.onBackground,
            modifier = Modifier.weight(1f),
        )
        Checkbox(checked = checked, onCheckedChange = { onToggle() }, enabled = enabled || checked)
    }
}

// ---- Edit project -----------------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditProjectPage(projectId: Long, state: DecafState, apps: List<AppEntry>, actions: HomeActions) {
    val project = state.projects.firstOrNull { it.id == projectId }
    if (project == null) {
        LaunchedEffect(projectId) { actions.nav.close() }
        return
    }
    var name by remember(projectId) { mutableStateOf(project.name) }
    var nextStep by remember(projectId) { mutableStateOf(project.nextStep) }
    var whereWasI by remember(projectId) { mutableStateOf(project.whereWasI) }
    var color by remember(projectId) { mutableStateOf(project.color) }
    var tools by remember(projectId) { mutableStateOf(project.apps) }
    var pickingApps by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    val byKey = remember(apps) { apps.associateBy { it.key } }

    val save = {
        actions.store.saveProject(
            project.copy(
                name = name.trim().ifEmpty { project.name },
                nextStep = nextStep.trim(),
                whereWasI = whereWasI.trim(),
                whereWasIAt = if (whereWasI.trim() != project.whereWasI) System.currentTimeMillis() else project.whereWasIAt,
                color = color,
                apps = tools,
            ),
        )
        actions.nav.close()
    }

    Page("Edit project", onBack = { actions.nav.close() }) {
        item {
            BrandCard("Project", Brand.Accents[color.mod(Brand.Accents.size)]) {
                Field("Name", name, { name = it })
                Spacer(Modifier.size(10.dp))
                Field("Next small step", nextStep, { nextStep = it })
                Spacer(Modifier.size(10.dp))
                Field("Where you left off", whereWasI, { whereWasI = it }, multiLine = true)
                Spacer(Modifier.size(12.dp))
                Text("Colour", fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = LocalDecafColors.current.mutedOnCard)
                Spacer(Modifier.size(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Brand.Accents.forEachIndexed { index, accent ->
                        Box(
                            Modifier
                                .size(if (index == color) 36.dp else 30.dp)
                                .background(accent, CircleShape)
                                .border(if (index == color) 3.dp else 1.5.dp, Brand.Ink, CircleShape)
                                .clickable { color = index },
                        )
                    }
                }
            }
        }
        item {
            BrandCard("Tools for this project", Brand.Sky) {
                BodyText("Up to ${Store.MAX_PROJECT_APPS} apps to launch from the spotlight, like your engine, art app and notes.", muted = true)
                Spacer(Modifier.size(8.dp))
                tools.mapNotNull { byKey[it] }.forEach { app ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        Image(app.icon, contentDescription = null, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.width(10.dp))
                        BodyText(app.label, modifier = Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.size(8.dp))
                PillButton("Choose apps", onClick = { pickingApps = true }, color = Brand.Sky, small = true)
            }
        }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PillButton("Save", onClick = save, color = Brand.Mint)
                PillButton("Delete project", onClick = { confirmDelete = true }, color = Brand.Coral)
            }
        }
    }

    if (pickingApps) {
        AppPickerDialog(
            apps = apps,
            selected = tools,
            max = Store.MAX_PROJECT_APPS,
            onDone = { tools = it; pickingApps = false },
            onDismiss = { pickingApps = false },
        )
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete ${project.name}?") },
            text = { Text("Its parked ideas stay, just without the project tag.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    actions.nav.close()
                    actions.store.deleteProject(project.id)
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Keep it") } },
        )
    }
}

@Composable
private fun Field(label: String, value: String, onChange: (String) -> Unit, multiLine: Boolean = false) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = !multiLine,
        minLines = if (multiLine) 2 else 1,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun AppPickerDialog(
    apps: List<AppEntry>,
    selected: List<String>,
    max: Int,
    onDone: (List<String>) -> Unit,
    onDismiss: () -> Unit,
) {
    var picked by remember { mutableStateOf(selected) }
    var query by remember { mutableStateOf("") }
    val shown = apps.filter { query.isBlank() || it.label.contains(query.trim(), ignoreCase = true) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { BigText("Choose up to $max apps", size = 20) },
        text = {
            Column {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search apps") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.size(8.dp))
                LazyColumn(Modifier.heightIn(max = 380.dp)) {
                    items(shown, key = { it.key }) { app ->
                        val checked = app.key in picked
                        AppCheckRow(app, checked = checked, enabled = picked.size < max) {
                            picked = if (checked) picked - app.key else picked + app.key
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onDone(picked) }) { Text("Done") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
