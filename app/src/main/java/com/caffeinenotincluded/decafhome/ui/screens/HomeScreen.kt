package com.caffeinenotincluded.decafhome.ui.screens

import android.text.format.DateFormat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caffeinenotincluded.decafhome.data.AppEntry
import com.caffeinenotincluded.decafhome.data.DecafState
import com.caffeinenotincluded.decafhome.data.Levels
import com.caffeinenotincluded.decafhome.ui.components.AppTile
import com.caffeinenotincluded.decafhome.ui.components.ProgressBar
import com.caffeinenotincluded.decafhome.ui.localDateTime
import com.caffeinenotincluded.decafhome.ui.minuteOfDay
import com.caffeinenotincluded.decafhome.ui.clockText
import com.caffeinenotincluded.decafhome.ui.rememberNow
import com.caffeinenotincluded.decafhome.ui.theme.Brand
import com.caffeinenotincluded.decafhome.ui.theme.Fredoka
import com.caffeinenotincluded.decafhome.ui.theme.LocalDecafColors
import com.caffeinenotincluded.decafhome.ui.theme.Nunito
import com.caffeinenotincluded.decafhome.ui.widgets.BreakMenuCard
import com.caffeinenotincluded.decafhome.ui.widgets.CountdownsCard
import com.caffeinenotincluded.decafhome.ui.widgets.FocusTimerCard
import com.caffeinenotincluded.decafhome.ui.widgets.IdeaParkingCard
import com.caffeinenotincluded.decafhome.ui.widgets.NowNextCard
import com.caffeinenotincluded.decafhome.ui.widgets.OneThingCard
import com.caffeinenotincluded.decafhome.ui.widgets.ProjectSpotlightCard
import com.caffeinenotincluded.decafhome.ui.widgets.PromptCard
import com.caffeinenotincluded.decafhome.ui.widgets.StreakCard
import com.caffeinenotincluded.decafhome.ui.widgets.WhereWasICard
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(state: DecafState, apps: List<AppEntry>, actions: HomeActions) {
    val listState = rememberLazyListState()
    LaunchedEffect(actions.nav.homePresses) {
        if (actions.nav.homePresses > 0) listState.animateScrollToItem(0)
    }

    Column(Modifier.fillMaxSize().imePadding()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .statusBarsPadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item(key = "clock") { ClockHeader(state, onSettings = { actions.nav.open(Overlay.Settings) }) }
            if (state.askWhereWasI && state.activeProject != null) {
                item(key = "where") { WhereWasICard(state, actions) }
            }
            item(key = "nownext") { NowNextCard(state, actions) }
            item(key = "onething") { OneThingCard(state, actions) }
            item(key = "timer") { FocusTimerCard(state, actions) }
            item(key = "project") { ProjectSpotlightCard(state, apps, actions) }
            item(key = "ideas") { IdeaParkingCard(state, actions) }
            item(key = "countdowns") { CountdownsCard(state, actions) }
            item(key = "streak") { StreakCard(state, actions) }
            item(key = "breaks") { BreakMenuCard(state, actions) }
            item(key = "prompt") { PromptCard(state, actions) }
        }
        Dock(state, apps, actions)
    }
}

private val DayFormatter = DateTimeFormatter.ofPattern("EEEE d MMMM")

@Composable
private fun ClockHeader(state: DecafState, onSettings: () -> Unit) {
    val colors = LocalDecafColors.current
    val context = LocalContext.current
    val now by rememberNow(60_000)
    var showXp by remember { mutableStateOf(false) }

    Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f)) {
            Text(
                clockText(minuteOfDay(now), DateFormat.is24HourFormat(context)),
                fontFamily = Fredoka,
                fontWeight = FontWeight.SemiBold,
                fontSize = 56.sp,
                lineHeight = 60.sp,
                color = colors.onBackground,
            )
            Text(
                localDateTime(now).format(DayFormatter),
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = colors.mutedOnBackground,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            XpBadge(state.xp, onClick = { showXp = true })
            IconButton(onClick = onSettings) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = colors.onBackground)
            }
        }
    }

    if (showXp) XpDialog(state.xp, onDismiss = { showXp = false })
}

@Composable
private fun XpBadge(xp: Int, onClick: () -> Unit) {
    val colors = LocalDecafColors.current
    val level = Levels.levelFor(xp)
    val (into, size) = Levels.progress(xp)
    val shape = RoundedCornerShape(50)
    Row(
        Modifier
            .clip(shape)
            .background(colors.card)
            .border(2.dp, colors.outline, shape)
            .clickable(onClick = onClick)
            .padding(start = 4.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(30.dp)
                .background(Brand.Sunshine, CircleShape)
                .border(2.dp, colors.outline, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("$level", fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Brand.Ink)
        }
        Spacer(Modifier.width(8.dp))
        Column {
            Text("LEVEL $level", fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = colors.mutedOnCard)
            ProgressBar(into.toFloat() / size, Brand.Coral, Modifier.width(72.dp))
        }
    }
}

@Composable
private fun XpDialog(xp: Int, onDismiss: () -> Unit) {
    val level = Levels.levelFor(xp)
    val (into, size) = Levels.progress(xp)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Level $level", fontFamily = Fredoka, fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("$into / $size XP to level ${level + 1}. $xp XP in total.", fontFamily = Nunito, fontWeight = FontWeight.Bold)
                Spacer(Modifier.size(4.dp))
                listOf(
                    "Finish your one thing" to "+10",
                    "Focus sprint" to "+1 per minute",
                    "Note where you were" to "+5",
                    "Stop at a speed bump" to "+5",
                    "Log a dev day" to "+5",
                    "Take a proper break" to "+3",
                    "Park an idea" to "+2",
                ).forEach { (what, points) ->
                    Row {
                        Text(what, fontFamily = Nunito, modifier = Modifier.weight(1f))
                        Text(points, fontFamily = Fredoka, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Nice") } },
    )
}

@Composable
private fun Dock(state: DecafState, apps: List<AppEntry>, actions: HomeActions) {
    val colors = LocalDecafColors.current
    val shape = RoundedCornerShape(24.dp)
    val byKey = remember(apps) { apps.associateBy { it.key } }
    val dockApps = state.dock.mapNotNull { byKey[it] }

    Box(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 12.dp, end = 12.dp, bottom = 10.dp, top = 4.dp)
            .pointerInput(Unit) {
                // Swipe up on the dock to open all apps.
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -12f) actions.nav.overlay = Overlay.Drawer
                }
            },
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(colors.card)
                .border(2.dp, colors.outline, shape)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            if (dockApps.isEmpty()) {
                Text(
                    "Long-press an app in the drawer to pin it here",
                    fontFamily = Nunito,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = colors.mutedOnCard,
                    modifier = Modifier.weight(1f).padding(start = 8.dp),
                )
            }
            dockApps.forEach { app ->
                var menu by remember(app.key) { mutableStateOf(false) }
                Box {
                    AppTile(
                        app = app,
                        onClick = { actions.launch(app) },
                        onLongClick = { menu = true },
                        showLabel = false,
                        iconSize = 46.dp,
                    )
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(
                            text = { Text("Unpin from dock") },
                            onClick = { actions.store.toggleDock(app.key); menu = false },
                        )
                        DropdownMenuItem(
                            text = { Text("App info") },
                            onClick = { actions.catalog.openAppInfo(app); menu = false },
                        )
                    }
                }
            }
            AllAppsButton(onClick = { actions.nav.overlay = Overlay.Drawer })
        }
    }
}

/** A 3×3 grid of dots: "all apps". */
@Composable
private fun AllAppsButton(onClick: () -> Unit) {
    Box(
        Modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(Brand.Sunshine)
            .border(2.dp, Brand.Ink, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(20.dp)) {
            val step = size.width / 3
            for (row in 0..2) for (col in 0..2) {
                drawCircle(Brand.Ink, radius = step * 0.28f, center = Offset(step * col + step / 2, step * row + step / 2))
            }
        }
    }
}
