package com.caffeinenotincluded.decafhome.ui.screens

import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.caffeinenotincluded.decafhome.data.Celebration
import com.caffeinenotincluded.decafhome.decaf
import com.caffeinenotincluded.decafhome.timer.Notifications
import com.caffeinenotincluded.decafhome.ui.components.CelebrationLayer
import com.caffeinenotincluded.decafhome.ui.components.DotGrid
import com.caffeinenotincluded.decafhome.ui.theme.DecafTheme
import com.caffeinenotincluded.decafhome.ui.theme.LocalDecafColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DecafRoot(nav: HomeNav) {
    val context = LocalContext.current
    val app = context.decaf
    val state by app.store.state.collectAsState()
    val apps by app.catalog.apps.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val actions = remember {
        HomeActions(context, app.store, app.catalog, nav) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !Notifications.canPost(context)) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    DecafTheme(state.themeMode) {
        // Status and navigation bar icons follow the app's own light/dark choice
        // (the speed bump is always dark).
        val view = LocalView.current
        val darkBars = LocalDecafColors.current.dark || nav.overlay is Overlay.SpeedBump
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkBars
                isAppearanceLightNavigationBars = !darkBars
            }
        }

        // Celebrations: confetti, a banner and a little buzz.
        var celebration by remember { mutableStateOf<Celebration?>(null) }
        var showBanner by remember { mutableStateOf(false) }
        var serial by remember { mutableIntStateOf(0) }
        val haptics = LocalHapticFeedback.current
        LaunchedEffect(Unit) {
            app.store.celebrations.collectLatest { c ->
                celebration = c
                serial++
                showBanner = true
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                delay(2600)
                showBanner = false
            }
        }

        Box(Modifier.fillMaxSize()) {
            DotGrid(Modifier.fillMaxSize())
            HomeScreen(state, apps, actions)

            AnimatedVisibility(
                visible = nav.overlay == Overlay.Drawer,
                enter = slideInVertically { it / 3 } + fadeIn(),
                exit = slideOutVertically { it / 3 } + fadeOut(),
            ) {
                AppDrawer(state, apps, actions)
            }

            when (val overlay = nav.overlay) {
                is Overlay.SpeedBump -> SpeedBumpScreen(overlay.app, actions)
                Overlay.Settings -> SettingsPage(state, actions)
                Overlay.Ideas -> IdeasPage(state, actions)
                Overlay.DayPlan -> DayPlanPage(state, actions)
                Overlay.Breaks -> BreaksPage(state, actions)
                Overlay.SpeedBumpApps -> SpeedBumpAppsPage(state, apps, actions)
                is Overlay.EditProject -> EditProjectPage(overlay.projectId, state, apps, actions)
                Overlay.Drawer, null -> Unit
            }

            CelebrationLayer(celebration, showBanner, serial)
        }

        // Back closes what's open. On the home screen itself it does nothing.
        BackHandler(enabled = true) { nav.close() }
    }
}
