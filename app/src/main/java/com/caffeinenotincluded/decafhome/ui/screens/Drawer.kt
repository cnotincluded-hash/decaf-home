package com.caffeinenotincluded.decafhome.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.caffeinenotincluded.decafhome.data.AppEntry
import com.caffeinenotincluded.decafhome.data.DecafState
import com.caffeinenotincluded.decafhome.data.IdeaKind
import com.caffeinenotincluded.decafhome.data.Store
import com.caffeinenotincluded.decafhome.ui.components.AppTile
import com.caffeinenotincluded.decafhome.ui.components.PillButton
import com.caffeinenotincluded.decafhome.ui.components.ProgressRing
import com.caffeinenotincluded.decafhome.ui.theme.Brand
import com.caffeinenotincluded.decafhome.ui.theme.Fredoka
import com.caffeinenotincluded.decafhome.ui.theme.LocalDecafColors
import com.caffeinenotincluded.decafhome.ui.theme.Nunito
import kotlinx.coroutines.delay

/** All apps, with search. Long-press an app to pin it, add a speed bump or see its info. */
@Composable
fun AppDrawer(state: DecafState, apps: List<AppEntry>, actions: HomeActions) {
    val colors = LocalDecafColors.current
    var query by remember { mutableStateOf("") }
    val results = remember(apps, query) {
        val q = query.trim()
        if (q.isEmpty()) apps else apps.filter { it.label.contains(q, ignoreCase = true) }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 16.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "All apps",
                fontFamily = Fredoka,
                fontWeight = FontWeight.SemiBold,
                fontSize = 30.sp,
                color = colors.onBackground,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { actions.nav.close() }) {
                Icon(Icons.Filled.Close, contentDescription = "Close", tint = colors.onBackground)
            }
        }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search apps") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = { results.firstOrNull()?.let { actions.launch(it) } }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.card,
                unfocusedContainerColor = colors.card,
                focusedTextColor = colors.onCard,
                unfocusedTextColor = colors.onCard,
                focusedBorderColor = colors.outline,
                unfocusedBorderColor = colors.outline,
                focusedLeadingIconColor = colors.onCard,
                unfocusedLeadingIconColor = colors.mutedOnCard,
                focusedPlaceholderColor = colors.mutedOnCard,
                unfocusedPlaceholderColor = colors.mutedOnCard,
                cursorColor = colors.onCard,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
        )
        Text(
            "Long-press an app to pin it to the dock or give it a speed bump.",
            fontFamily = Nunito,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = colors.mutedOnBackground,
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 78.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f),
        ) {
            items(results, key = { it.key }) { app ->
                DrawerTile(app, state, actions)
            }
        }
    }
}

@Composable
private fun DrawerTile(app: AppEntry, state: DecafState, actions: HomeActions) {
    var menu by remember { mutableStateOf(false) }
    val pinned = app.key in state.dock
    val bumped = app.packageName in state.distracting
    Box {
        AppTile(app = app, onClick = { actions.launch(app) }, onLongClick = { menu = true })
        if (bumped) {
            // A little coral dot marks apps behind a speed bump.
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = 10.dp)
                    .size(12.dp)
                    .background(Brand.Coral, CircleShape)
                    .border(1.5.dp, Brand.Ink, CircleShape),
            )
        }
        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
            DropdownMenuItem(
                text = {
                    Text(
                        when {
                            pinned -> "Unpin from dock"
                            state.dock.size >= Store.MAX_DOCK -> "Dock is full (${Store.MAX_DOCK})"
                            else -> "Pin to dock"
                        },
                    )
                },
                enabled = pinned || state.dock.size < Store.MAX_DOCK,
                onClick = { actions.store.toggleDock(app.key); menu = false },
            )
            DropdownMenuItem(
                text = { Text(if (bumped) "Remove speed bump" else "Add speed bump") },
                onClick = { actions.store.toggleDistracting(app.packageName); menu = false },
            )
            DropdownMenuItem(
                text = { Text("App info") },
                onClick = { actions.catalog.openAppInfo(app); menu = false },
            )
        }
    }
}

/**
 * The speed bump: a 10-second pause before a distracting app opens, with a
 * question about why you're opening it.
 */
@Composable
fun SpeedBumpScreen(app: AppEntry, actions: HomeActions) {
    var seconds by remember(app.key) { mutableIntStateOf(10) }
    var intention by remember(app.key) { mutableStateOf("") }
    LaunchedEffect(app.key) {
        while (seconds > 0) {
            delay(1_000)
            seconds--
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Brand.Ink)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(app.icon, contentDescription = app.label, modifier = Modifier.size(64.dp))
        Spacer(Modifier.size(16.dp))
        Text("Pause for a sec.", fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, color = Brand.Cream)
        Spacer(Modifier.size(6.dp))
        Text(
            "What did you open ${app.label} for?",
            fontFamily = Nunito,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            color = Brand.Cream.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.size(16.dp))
        OutlinedTextField(
            value = intention,
            onValueChange = { intention = it },
            placeholder = { Text("I'm opening it to…") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Brand.Cream,
                unfocusedTextColor = Brand.Cream,
                focusedBorderColor = Brand.Sunshine,
                unfocusedBorderColor = Brand.Cream.copy(alpha = 0.5f),
                cursorColor = Brand.Sunshine,
                focusedPlaceholderColor = Brand.Cream.copy(alpha = 0.5f),
                unfocusedPlaceholderColor = Brand.Cream.copy(alpha = 0.5f),
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.size(24.dp))
        ProgressRing(
            seconds / 10f,
            Brand.Coral,
            Modifier.size(110.dp),
            stroke = 10.dp,
            track = Brand.Cream.copy(alpha = 0.15f),
        ) {
            Text(
                if (seconds > 0) "$seconds" else "✓",
                fontFamily = Fredoka,
                fontWeight = FontWeight.SemiBold,
                fontSize = 34.sp,
                color = Brand.Cream,
            )
        }
        Spacer(Modifier.size(24.dp))
        PillButton(
            "Not now (+5 XP)",
            onClick = {
                actions.store.speedBumpSave()
                actions.nav.close()
            },
            color = Brand.Mint,
        )
        if (intention.isNotBlank()) {
            Spacer(Modifier.size(10.dp))
            PillButton(
                "Park that as a note instead",
                onClick = {
                    actions.store.parkIdea(intention, IdeaKind.NOTE)
                    actions.nav.close()
                },
                color = Brand.Sunshine,
            )
        }
        Spacer(Modifier.size(10.dp))
        PillButton(
            if (seconds > 0) "Open in ${seconds}s" else "Open ${app.label}",
            onClick = { actions.launchNow(app) },
            color = Brand.Cream,
            enabled = seconds == 0,
        )
    }
}
