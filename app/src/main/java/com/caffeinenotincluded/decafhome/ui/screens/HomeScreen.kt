package com.caffeinenotincluded.decafhome.ui.screens

import android.content.Intent
import android.provider.AlarmClock
import android.text.format.DateFormat
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caffeinenotincluded.decafhome.data.AppEntry
import com.caffeinenotincluded.decafhome.data.ClockStyle
import com.caffeinenotincluded.decafhome.data.DayPlan
import com.caffeinenotincluded.decafhome.data.DecafState
import com.caffeinenotincluded.decafhome.data.GestureAction
import com.caffeinenotincluded.decafhome.data.Hand
import com.caffeinenotincluded.decafhome.data.LabelFont
import com.caffeinenotincluded.decafhome.data.LayoutPrefs
import com.caffeinenotincluded.decafhome.data.Levels
import com.caffeinenotincluded.decafhome.data.Store
import com.caffeinenotincluded.decafhome.data.Streaks
import com.caffeinenotincluded.decafhome.data.TimerKind
import com.caffeinenotincluded.decafhome.data.WidgetId
import com.caffeinenotincluded.decafhome.ui.clockText
import com.caffeinenotincluded.decafhome.ui.components.ProgressBar
import com.caffeinenotincluded.decafhome.ui.countdownText
import com.caffeinenotincluded.decafhome.ui.durationText
import com.caffeinenotincluded.decafhome.ui.localDateTime
import com.caffeinenotincluded.decafhome.ui.minuteOfDay
import com.caffeinenotincluded.decafhome.ui.rememberNow
import com.caffeinenotincluded.decafhome.ui.theme.Brand
import com.caffeinenotincluded.decafhome.ui.theme.Fredoka
import com.caffeinenotincluded.decafhome.ui.theme.LocalDecafColors
import com.caffeinenotincluded.decafhome.ui.theme.Nunito
import com.caffeinenotincluded.decafhome.ui.timerText
import com.caffeinenotincluded.decafhome.ui.widgets.WhereWasICard
import com.caffeinenotincluded.decafhome.ui.widgets.WidgetCard
import com.caffeinenotincluded.decafhome.ui.widgets.accent
import com.caffeinenotincluded.decafhome.ui.widgets.emoji
import java.text.Normalizer
import java.time.format.DateTimeFormatter
import kotlin.math.abs

/** The rail's letters, top to bottom. */
private val RailLetters: List<Char> = ('A'..'Z').toList() + '#'

/** The letter an app files under: A–Z (accents removed), or # for anything else. */
fun letterOf(label: String): Char {
    val first = label.trim().firstOrNull() ?: return '#'
    val plain = Normalizer.normalize(first.toString(), Normalizer.Form.NFD).first().uppercaseChar()
    return if (plain in 'A'..'Z') plain else '#'
}

fun labelFamily(font: LabelFont): FontFamily = when (font) {
    LabelFont.FREDOKA -> Fredoka
    LabelFont.NUNITO -> Nunito
    LabelFont.SYSTEM -> FontFamily.Default
}

/** Soft shadow behind light text on a wallpaper, so it stays readable. */
@Composable
private fun homeTextStyle(): TextStyle =
    if (LocalDecafColors.current.minimal) {
        TextStyle(shadow = Shadow(Color.Black.copy(alpha = 0.5f), Offset(0f, 2f), 8f))
    } else {
        TextStyle.Default
    }

/**
 * Niagara-style home: clock, a strip of widget pills, and your favourite apps as
 * a big text list near your thumb. Slide along the alphabet rail to reach any app.
 */
@Composable
fun HomeScreen(state: DecafState, apps: List<AppEntry>, actions: HomeActions) {
    val nav = actions.nav
    val layout = state.layout
    val rightHanded = layout.hand == Hand.RIGHT

    fun runGesture(action: GestureAction) {
        when (action) {
            GestureAction.SEARCH -> nav.open(Overlay.Drawer)
            GestureAction.NOTIFICATIONS -> actions.expandNotifications()
            GestureAction.TIMER -> nav.sheet = HomeSheet.Widget(WidgetId.TIMER)
            GestureAction.ONE_THING -> nav.sheet = HomeSheet.Widget(WidgetId.ONE_THING)
            GestureAction.NOTHING -> Unit
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .pointerInput(nav.browsing, layout.swipeUp, layout.swipeDown) {
                if (nav.browsing) return@pointerInput
                var total = 0f
                detectVerticalDragGestures(
                    onDragStart = { total = 0f },
                    onDragEnd = {
                        val threshold = 56.dp.toPx()
                        when {
                            total < -threshold -> runGesture(layout.swipeUp)
                            total > threshold -> runGesture(layout.swipeDown)
                        }
                    },
                ) { _, dragAmount -> total += dragAmount }
            },
    ) {
        HomeHeader(state, onSettings = { nav.open(Overlay.Settings) })
        PillStrip(state, actions)
        Row(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            if (!rightHanded) AlphabetRail(apps, actions, layout, onLeft = true)
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clipToBounds(),
            ) {
                if (nav.browsing) {
                    AllAppsList(state, apps, actions, alignEnd = !rightHanded)
                } else {
                    FavouritesList(
                        state,
                        apps,
                        actions,
                        alignEnd = !rightHanded,
                        modifier = Modifier.align(
                            when {
                                layout.favouritesAtBottom && rightHanded -> Alignment.BottomStart
                                layout.favouritesAtBottom -> Alignment.BottomEnd
                                rightHanded -> Alignment.TopStart
                                else -> Alignment.TopEnd
                            },
                        ),
                    )
                }
            }
            if (rightHanded) AlphabetRail(apps, actions, layout, onLeft = false)
        }
    }

    WidgetSheets(state, apps, actions)
}

// ---- Clock, date, level ------------------------------------------------------------------

private val DayFormatter = DateTimeFormatter.ofPattern("EEEE d MMMM")

@Composable
private fun HomeHeader(state: DecafState, onSettings: () -> Unit) {
    val colors = LocalDecafColors.current
    val context = LocalContext.current
    val layout = state.layout
    val now by rememberNow(60_000)
    var showXp by remember { mutableStateOf(false) }
    val shadow = homeTextStyle()

    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 8.dp, top = 8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(Modifier.weight(1f)) {
            if (layout.clock != ClockStyle.HIDDEN) {
                val big = layout.clock == ClockStyle.BIG
                Text(
                    clockText(minuteOfDay(now), DateFormat.is24HourFormat(context)),
                    fontFamily = Fredoka,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = if (big) 56.sp else 32.sp,
                    lineHeight = if (big) 60.sp else 36.sp,
                    color = colors.homeText,
                    style = shadow,
                    modifier = Modifier.clickable {
                        runCatching {
                            context.startActivity(Intent(AlarmClock.ACTION_SHOW_ALARMS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        }
                    },
                )
            }
            if (layout.showDate) {
                Text(
                    localDateTime(now).format(DayFormatter),
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = colors.homeMuted,
                    style = shadow,
                    modifier = Modifier.clickable {
                        runCatching {
                            context.startActivity(
                                Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_CALENDAR)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                            )
                        }
                    },
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (layout.showXp) XpBadge(state.xp, onClick = { showXp = true })
            IconButton(onClick = onSettings) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = colors.homeText)
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
            .padding(start = 4.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(26.dp)
                .background(Brand.Sunshine, CircleShape)
                .border(2.dp, colors.outline, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("$level", fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Brand.Ink)
        }
        Spacer(Modifier.width(6.dp))
        ProgressBar(into.toFloat() / size, Brand.Coral, Modifier.width(48.dp))
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

// ---- Widget pills ------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PillStrip(state: DecafState, actions: HomeActions) {
    val nav = actions.nav
    val context = LocalContext.current
    val now by rememberNow(if (state.timer != null) 1_000L else 30_000L)
    val today = localDateTime(now).toLocalDate()
    val streak = remember(state.devDays, state.restDaysPerWeek, today) {
        Streaks.compute(state.devDays, today, state.restDaysPerWeek).days
    }
    val is24 = DateFormat.is24HourFormat(context)

    FlowRow(
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (state.askWhereWasI && state.activeProject != null) {
            WidgetPill("✍️ Where were you?", Brand.Sunshine, highlighted = true) { nav.sheet = HomeSheet.WhereWasI }
        }
        state.layout.pills.forEach { id ->
            val (text, highlighted) = pillText(id, state, now, streak, is24)
            WidgetPill("${id.emoji()} $text", id.accent(), highlighted) { nav.sheet = HomeSheet.Widget(id) }
        }
        WidgetPill("＋", Brand.Cream) { nav.sheet = HomeSheet.AllWidgets }
    }
}

/** The one line a pill shows, and whether it should stand out (e.g. a running timer). */
private fun pillText(id: WidgetId, state: DecafState, now: Long, streak: Int, is24: Boolean): Pair<String, Boolean> =
    when (id) {
        WidgetId.ONE_THING -> (state.tasks.firstOrNull()?.text ?: "Add your one thing") to false
        WidgetId.TIMER -> state.timer?.let { t ->
            val left = timerText((t.endsAt - now).coerceAtLeast(0))
            (if (t.kind == TimerKind.BREAK) "Break $left" else left) to true
        } ?: ("Focus" to false)
        WidgetId.NOW_NEXT -> if (state.timeline.isEmpty()) {
            "Plan my day" to false
        } else {
            val plan = DayPlan.at(state.timeline, minuteOfDay(now))
            val nowLabel = plan.now?.label ?: "Free"
            val next = plan.next
            (if (next != null) "$nowLabel · next ${clockText(next.minuteOfDay, is24)}" else nowLabel) to false
        }
        WidgetId.PROJECT -> (state.activeProject?.name ?: "Pick a project") to false
        WidgetId.IDEAS -> (if (state.ideas.isEmpty()) "Park an idea" else "${state.ideas.size} parked") to false
        WidgetId.COUNTDOWNS -> state.deadlines.filter { it.dueAt > now }.minByOrNull { it.dueAt }?.let {
            "${it.title} ${countdownText(it.dueAt - now)}" to false
        } ?: ("Countdowns" to false)
        WidgetId.STREAK -> (if (streak == 1) "1 day" else "$streak days") to false
        WidgetId.BREAKS -> "Take a break" to false
        WidgetId.PROMPT -> "Today's prompt" to false
    }

@Composable
private fun WidgetPill(text: String, accent: Color, highlighted: Boolean = false, onClick: () -> Unit) {
    val colors = LocalDecafColors.current
    val shape = RoundedCornerShape(50)
    val textStyle = Modifier.widthIn(max = 240.dp)
    if (colors.minimal) {
        Box(
            Modifier
                .clip(shape)
                .background(if (highlighted) accent else Color.White.copy(alpha = 0.2f))
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            Text(
                text,
                fontFamily = Nunito,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = if (highlighted) Brand.Ink else Brand.Cream,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = textStyle,
            )
        }
    } else {
        val shadow = if (colors.dark) accent else Brand.Ink
        Box(Modifier.padding(end = 3.dp, bottom = 3.dp)) {
            Box(
                Modifier
                    .matchParentSize()
                    .offset(3.dp, 3.dp)
                    .background(shadow, shape),
            )
            Box(
                Modifier
                    .clip(shape)
                    .background(if (highlighted) accent else colors.card)
                    .border(2.dp, Brand.Ink, shape)
                    .clickable(onClick = onClick)
                    .padding(horizontal = 13.dp, vertical = 7.dp),
            ) {
                Text(
                    text,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = Brand.Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = textStyle,
                )
            }
        }
    }
}

// ---- App lists -----------------------------------------------------------------------------

@Composable
private fun FavouritesList(
    state: DecafState,
    apps: List<AppEntry>,
    actions: HomeActions,
    alignEnd: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = LocalDecafColors.current
    val byKey = remember(apps) { apps.associateBy { it.key } }
    val favourites = state.favourites.mapNotNull { byKey[it] }
    Column(
        modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
    ) {
        if (favourites.isEmpty()) {
            Text(
                "Slide along the letters to find any app, then long-press it to add it to your favourites.",
                fontFamily = Nunito,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = colors.homeMuted,
                style = homeTextStyle(),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )
        }
        favourites.forEach { app -> AppRow(app, state, actions, alignEnd, inFavourites = true) }
    }
}

@Composable
private fun AllAppsList(state: DecafState, apps: List<AppEntry>, actions: HomeActions, alignEnd: Boolean) {
    val nav = actions.nav
    val colors = LocalDecafColors.current
    val accent = Brand.Accents[state.layout.accent.mod(Brand.Accents.size)]
    val sections = remember(apps) {
        apps.groupBy { letterOf(it.label) }
            .toSortedMap(compareBy { if (it == '#') Char.MAX_VALUE else it })
    }
    val headerIndex = remember(sections) {
        val map = HashMap<Char, Int>()
        var index = 0
        sections.forEach { (letter, list) ->
            map[letter] = index
            index += 1 + list.size
        }
        map
    }
    val listState = rememberLazyListState()
    LaunchedEffect(nav.browseLetter) {
        nav.browseLetter?.let { letter -> headerIndex[letter]?.let { listState.scrollToItem(it) } }
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "All apps",
                fontFamily = Fredoka,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = colors.homeText,
                style = homeTextStyle(),
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { nav.stopBrowsing() }) {
                Icon(Icons.Filled.Close, contentDescription = "Back to favourites", tint = colors.homeText)
            }
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            sections.forEach { (letter, list) ->
                item(key = "letter-$letter") {
                    Text(
                        letter.toString(),
                        fontFamily = Fredoka,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (letter == nav.browseLetter) accent else colors.homeMuted,
                        style = homeTextStyle(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        textAlign = if (alignEnd) androidx.compose.ui.text.style.TextAlign.End else null,
                    )
                }
                items(list, key = { "app-" + it.key }) { app ->
                    AppRow(app, state, actions, alignEnd, inFavourites = false)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppRow(app: AppEntry, state: DecafState, actions: HomeActions, alignEnd: Boolean, inFavourites: Boolean) {
    val colors = LocalDecafColors.current
    val layout = state.layout
    var menu by remember { mutableStateOf(false) }
    val isFavourite = app.key in state.favourites
    val bumped = app.packageName in state.distracting
    val iconSize = layout.iconSize.size

    val icon: @Composable () -> Unit = {
        if (iconSize > 0) Image(app.icon, contentDescription = null, modifier = Modifier.size(iconSize.dp))
    }
    val label: @Composable () -> Unit = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                app.label,
                fontFamily = labelFamily(layout.font),
                fontWeight = FontWeight.SemiBold,
                fontSize = layout.labelSize.size.sp,
                color = colors.homeText,
                style = homeTextStyle(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (bumped) {
                Spacer(Modifier.width(6.dp))
                Box(
                    Modifier
                        .size(8.dp)
                        .background(Brand.Coral, CircleShape),
                )
            }
        }
    }

    Box {
        Row(
            Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = { actions.launch(app) }, onLongClick = { menu = true })
                .padding(horizontal = 20.dp, vertical = 7.dp),
            horizontalArrangement = if (alignEnd) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (alignEnd) {
                label()
                if (iconSize > 0) Spacer(Modifier.width(14.dp))
                icon()
            } else {
                icon()
                if (iconSize > 0) Spacer(Modifier.width(14.dp))
                label()
            }
        }
        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
            if (inFavourites) {
                DropdownMenuItem(text = { Text("Move up") }, onClick = { actions.store.moveFavourite(app.key, -1); menu = false })
                DropdownMenuItem(text = { Text("Move down") }, onClick = { actions.store.moveFavourite(app.key, 1); menu = false })
            }
            DropdownMenuItem(
                text = {
                    Text(
                        when {
                            isFavourite -> "Remove from favourites"
                            state.favourites.size >= Store.MAX_FAVOURITES -> "Favourites are full (${Store.MAX_FAVOURITES})"
                            else -> "Add to favourites"
                        },
                    )
                },
                enabled = isFavourite || state.favourites.size < Store.MAX_FAVOURITES,
                onClick = { actions.store.toggleFavourite(app.key); menu = false },
            )
            DropdownMenuItem(
                text = { Text(if (bumped) "Remove speed bump" else "Add speed bump") },
                onClick = { actions.store.toggleDistracting(app.packageName); menu = false },
            )
            DropdownMenuItem(text = { Text("App info") }, onClick = { actions.catalog.openAppInfo(app); menu = false })
        }
    }
}

// ---- Alphabet rail -------------------------------------------------------------------------

/**
 * Letters down the edge of the screen. Touch and slide: the letters near your
 * finger swell out like a wave and the list jumps to that letter.
 */
@Composable
private fun AlphabetRail(apps: List<AppEntry>, actions: HomeActions, layout: LayoutPrefs, onLeft: Boolean) {
    val nav = actions.nav
    val colors = LocalDecafColors.current
    val haptics = LocalHapticFeedback.current
    val accent = Brand.Accents[layout.accent.mod(Brand.Accents.size)]
    val available = remember(apps) { apps.map { letterOf(it.label) }.toSet() }
    var touch by remember { mutableStateOf<Float?>(null) }

    val select by rememberUpdatedState { index: Int ->
        val letter = nearestLetter(index, available)
        if (letter != null && letter != nav.browseLetter) {
            if (layout.haptics) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            nav.browseLetter = letter
        }
        nav.browsing = true
    }

    Column(
        Modifier
            .width(36.dp)
            .fillMaxHeight()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconButton(onClick = { nav.open(Overlay.Drawer) }, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Filled.Search, contentDescription = "Search apps", tint = colors.homeText)
        }
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        down.consume()
                        val height = size.height.toFloat().coerceAtLeast(1f)
                        fun at(y: Float) = (y / height * RailLetters.size).coerceIn(0f, RailLetters.size - 0.01f)
                        touch = at(down.position.y)
                        select(at(down.position.y).toInt())
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break
                            change.consume()
                            val position = at(change.position.y)
                            touch = position
                            select(position.toInt())
                        }
                        touch = null
                    }
                },
        ) {
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                RailLetters.forEachIndexed { index, letter ->
                    val t = touch
                    val pull = if (t == null) 0f else (1f - abs(index + 0.5f - t) / 4f).coerceIn(0f, 1f)
                    val active = nav.browsing && nav.browseLetter == letter
                    Text(
                        letter.toString(),
                        fontFamily = Nunito,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        color = when {
                            active -> accent
                            letter in available -> colors.homeText
                            else -> colors.homeMuted.copy(alpha = 0.35f)
                        },
                        style = homeTextStyle(),
                        modifier = Modifier.graphicsLayer {
                            val scale = 1f + 1.4f * pull
                            scaleX = scale
                            scaleY = scale
                            translationX = (if (onLeft) 1f else -1f) * 34.dp.toPx() * pull
                        },
                    )
                }
            }
        }
    }
}

/** The letter at [index], or the next one down that has apps (then the next one up). */
private fun nearestLetter(index: Int, available: Set<Char>): Char? {
    val start = index.coerceIn(0, RailLetters.size - 1)
    for (i in start until RailLetters.size) if (RailLetters[i] in available) return RailLetters[i]
    for (i in start downTo 0) if (RailLetters[i] in available) return RailLetters[i]
    return null
}

// ---- Sheets ----------------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WidgetSheets(state: DecafState, apps: List<AppEntry>, actions: HomeActions) {
    val nav = actions.nav
    val sheet = nav.sheet ?: return
    val colors = LocalDecafColors.current

    if (sheet == HomeSheet.WhereWasI && (!state.askWhereWasI || state.activeProject == null)) {
        LaunchedEffect(Unit) { nav.sheet = null }
        return
    }

    ModalBottomSheet(
        onDismissRequest = { nav.sheet = null },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.background,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 32.dp),
        ) {
            when (sheet) {
                is HomeSheet.Widget -> WidgetCard(sheet.id, state, apps, actions)
                HomeSheet.WhereWasI -> WhereWasICard(state, actions)
                HomeSheet.AllWidgets -> AllWidgetsList(state, actions)
            }
        }
    }
}

@Composable
private fun AllWidgetsList(state: DecafState, actions: HomeActions) {
    val colors = LocalDecafColors.current
    Text(
        "All widgets",
        fontFamily = Fredoka,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        color = colors.onBackground,
        modifier = Modifier.padding(bottom = 4.dp),
    )
    Text(
        "Tap one to open it. Choose which sit on your home screen, and their order, in Settings.",
        fontFamily = Nunito,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        color = colors.mutedOnBackground,
        modifier = Modifier.padding(bottom = 12.dp),
    )
    WidgetId.entries.forEach { id ->
        val onHome = id in state.layout.pills
        val shape = RoundedCornerShape(14.dp)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(shape)
                .background(colors.card)
                .border(2.dp, colors.outline, shape)
                .clickable { actions.nav.sheet = HomeSheet.Widget(id) }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("${id.emoji()}  ${id.label}", fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = colors.onCard, modifier = Modifier.weight(1f))
            Text(
                if (onHome) "On home ✓" else "Add to home",
                fontFamily = Nunito,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = colors.mutedOnCard,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable { actions.store.togglePill(id) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}
