package com.caffeinenotincluded.decafhome.ui.widgets

import androidx.compose.runtime.Composable
import com.caffeinenotincluded.decafhome.data.AppEntry
import com.caffeinenotincluded.decafhome.data.DecafState
import com.caffeinenotincluded.decafhome.data.WidgetId
import com.caffeinenotincluded.decafhome.ui.screens.HomeActions
import com.caffeinenotincluded.decafhome.ui.theme.Brand

/** The full card for a widget, shown in the sheet that opens from its pill. */
@Composable
fun WidgetCard(id: WidgetId, state: DecafState, apps: List<AppEntry>, actions: HomeActions) {
    when (id) {
        WidgetId.ONE_THING -> OneThingCard(state, actions)
        WidgetId.TIMER -> FocusTimerCard(state, actions)
        WidgetId.NOW_NEXT -> NowNextCard(state, actions)
        WidgetId.PROJECT -> ProjectSpotlightCard(state, apps, actions)
        WidgetId.IDEAS -> IdeaParkingCard(state, actions)
        WidgetId.COUNTDOWNS -> CountdownsCard(state, actions)
        WidgetId.STREAK -> StreakCard(state, actions)
        WidgetId.BREAKS -> BreakMenuCard(state, actions)
        WidgetId.PROMPT -> PromptCard(state, actions)
    }
}

/** Each widget's colour, used for its pill and card. */
fun WidgetId.accent() = when (this) {
    WidgetId.ONE_THING, WidgetId.STREAK -> Brand.Coral
    WidgetId.TIMER, WidgetId.BREAKS -> Brand.Mint
    WidgetId.NOW_NEXT, WidgetId.COUNTDOWNS -> Brand.Sky
    WidgetId.IDEAS -> Brand.Sunshine
    WidgetId.PROJECT, WidgetId.PROMPT -> Brand.Lilac
}

/** Emoji shown at the start of each pill, so they're easy to tell apart at a glance. */
fun WidgetId.emoji() = when (this) {
    WidgetId.ONE_THING -> "🎯"
    WidgetId.TIMER -> "⏱"
    WidgetId.NOW_NEXT -> "🗓"
    WidgetId.PROJECT -> "🎮"
    WidgetId.IDEAS -> "💡"
    WidgetId.COUNTDOWNS -> "⏳"
    WidgetId.STREAK -> "🔥"
    WidgetId.BREAKS -> "☕"
    WidgetId.PROMPT -> "✏️"
}
