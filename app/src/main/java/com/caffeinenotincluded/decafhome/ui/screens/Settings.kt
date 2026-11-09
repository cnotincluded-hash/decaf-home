package com.caffeinenotincluded.decafhome.ui.screens

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.caffeinenotincluded.decafhome.data.ClockStyle
import com.caffeinenotincluded.decafhome.data.DecafState
import com.caffeinenotincluded.decafhome.data.GestureAction
import com.caffeinenotincluded.decafhome.data.Hand
import com.caffeinenotincluded.decafhome.data.HomeStyle
import com.caffeinenotincluded.decafhome.data.IconSize
import com.caffeinenotincluded.decafhome.data.LabelFont
import com.caffeinenotincluded.decafhome.data.LabelSize
import com.caffeinenotincluded.decafhome.data.ThemeMode
import com.caffeinenotincluded.decafhome.data.WidgetId
import com.caffeinenotincluded.decafhome.ui.components.BrandCard
import com.caffeinenotincluded.decafhome.ui.components.ChoiceChip
import com.caffeinenotincluded.decafhome.ui.components.PillButton
import com.caffeinenotincluded.decafhome.ui.theme.Brand
import com.caffeinenotincluded.decafhome.ui.theme.LocalDecafColors
import com.caffeinenotincluded.decafhome.ui.widgets.BodyText
import com.caffeinenotincluded.decafhome.ui.widgets.SmallLabel
import com.caffeinenotincluded.decafhome.ui.widgets.emoji

@Composable
fun SettingsPage(state: DecafState, actions: HomeActions) {
    val context = LocalContext.current
    val store = actions.store
    val layout = state.layout

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
            }
        }

        item {
            BrandCard("Layout", Brand.Sky) {
                OptionRow("Hand", Hand.entries, layout.hand, { it.label }, Brand.Sky) { v ->
                    store.updateLayout { it.copy(hand = v) }
                }
                OptionRow("Favourites sit", listOf(true, false), layout.favouritesAtBottom, { if (it) "Near your thumb" else "At the top" }, Brand.Sky) { v ->
                    store.updateLayout { it.copy(favouritesAtBottom = v) }
                }
                OptionRow("App icons", IconSize.entries, layout.iconSize, { it.label }, Brand.Sky) { v ->
                    store.updateLayout { it.copy(iconSize = v) }
                }
                OptionRow("App name size", LabelSize.entries, layout.labelSize, { it.label }, Brand.Sky) { v ->
                    store.updateLayout { it.copy(labelSize = v) }
                }
                OptionRow("App name font", LabelFont.entries, layout.font, { it.label }, Brand.Sky, last = true) { v ->
                    store.updateLayout { it.copy(font = v) }
                }
            }
        }

        item {
            BrandCard("Look", Brand.Lilac) {
                OptionRow("Background", HomeStyle.entries, layout.style, { it.label }, Brand.Lilac) { v ->
                    store.updateLayout { it.copy(style = v) }
                }
                OptionRow(
                    "Light or dark",
                    ThemeMode.entries,
                    state.themeMode,
                    { when (it) { ThemeMode.SYSTEM -> "Match phone"; ThemeMode.LIGHT -> "Light"; ThemeMode.DARK -> "Dark" } },
                    Brand.Lilac,
                ) { v -> store.setThemeMode(v) }
                SmallLabel("Accent colour")
                Spacer(Modifier.size(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Brand.Accents.forEachIndexed { index, accent ->
                        val selected = index == layout.accent
                        Box(
                            Modifier
                                .size(if (selected) 36.dp else 30.dp)
                                .background(accent, CircleShape)
                                .border(if (selected) 3.dp else 1.5.dp, Brand.Ink, CircleShape)
                                .clickable { store.updateLayout { it.copy(accent = index) } },
                        )
                    }
                }
                Spacer(Modifier.size(4.dp))
                BodyText("Used for the alphabet rail's highlight.", muted = true)
            }
        }

        item {
            BrandCard("Top of the screen", Brand.Coral) {
                OptionRow("Clock", ClockStyle.entries, layout.clock, { it.label }, Brand.Coral) { v ->
                    store.updateLayout { it.copy(clock = v) }
                }
                ToggleRow("Show the date", layout.showDate) { v -> store.updateLayout { it.copy(showDate = v) } }
                ToggleRow("Show your level", layout.showXp) { v -> store.updateLayout { it.copy(showXp = v) } }
            }
        }

        item {
            BrandCard("Home widgets", Brand.Mint) {
                BodyText("Ticked widgets sit on the home screen as pills, in this order. The rest are under ＋.", muted = true)
                Spacer(Modifier.size(8.dp))
                val ordered = layout.pills + WidgetId.entries.filterNot { it in layout.pills }
                ordered.forEach { id ->
                    val on = id in layout.pills
                    val position = layout.pills.indexOf(id)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = on, onCheckedChange = { store.togglePill(id) })
                        BodyText("${id.emoji()}  ${id.label}", muted = !on, modifier = Modifier.weight(1f))
                        if (on) {
                            IconButton(onClick = { store.movePill(id, -1) }, enabled = position > 0) {
                                Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Move up")
                            }
                            IconButton(onClick = { store.movePill(id, 1) }, enabled = position < layout.pills.size - 1) {
                                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Move down")
                            }
                        }
                    }
                }
            }
        }

        item {
            BrandCard("Gestures", Brand.Sunshine) {
                OptionRow("Swipe up", GestureAction.entries, layout.swipeUp, { it.label }, Brand.Sunshine) { v ->
                    store.updateLayout { it.copy(swipeUp = v) }
                }
                OptionRow("Swipe down", GestureAction.entries, layout.swipeDown, { it.label }, Brand.Sunshine) { v ->
                    store.updateLayout { it.copy(swipeDown = v) }
                }
                ToggleRow("Little buzzes (haptics)", layout.haptics) { v -> store.updateLayout { it.copy(haptics = v) } }
                Spacer(Modifier.size(4.dp))
                BodyText("Opening notifications by swiping works on most phones, but a few block it.", muted = true)
            }
        }

        item {
            BrandCard("Dev streak", Brand.Coral) {
                BodyText("Rest days per week. Missing this many days in a week won't break your streak.")
                Spacer(Modifier.size(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (0..3).forEach { days ->
                        ChoiceChip("$days", state.restDaysPerWeek == days, onClick = { store.setRestDays(days) }, accent = Brand.Coral)
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
                BodyText("Only when opened from Decaf Home. Notifications and recent apps skip it.", muted = true)
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
                BodyText("Decaf Home, by Caffeine Not Included.")
                Spacer(Modifier.size(4.dp))
                BodyText(
                    "Everything stays on this phone. If Android backup is on, it's included in your phone's backup.",
                    muted = true,
                )
            }
        }
    }
}

/** A titled row of choice chips. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> OptionRow(
    title: String,
    options: List<T>,
    selected: T,
    label: (T) -> String,
    accent: Color,
    last: Boolean = false,
    onSelect: (T) -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        SmallLabel(title)
        Spacer(Modifier.size(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                ChoiceChip(label(option), option == selected, onClick = { onSelect(option) }, accent = accent)
            }
        }
        if (!last) Spacer(Modifier.size(14.dp))
    }
}

@Composable
private fun ToggleRow(title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BodyText(title, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
