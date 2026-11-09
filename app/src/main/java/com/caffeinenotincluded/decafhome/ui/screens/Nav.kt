package com.caffeinenotincluded.decafhome.ui.screens

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.caffeinenotincluded.decafhome.data.AppCatalog
import com.caffeinenotincluded.decafhome.data.AppEntry
import com.caffeinenotincluded.decafhome.data.Store
import com.caffeinenotincluded.decafhome.data.TimerKind
import com.caffeinenotincluded.decafhome.data.WidgetId
import com.caffeinenotincluded.decafhome.timer.TimerAlarm

/** Screens that open on top of the home screen. */
sealed interface Overlay {
    data object Drawer : Overlay
    data object Settings : Overlay
    data object Ideas : Overlay
    data object DayPlan : Overlay
    data object Breaks : Overlay
    data object SpeedBumpApps : Overlay
    data class SpeedBump(val app: AppEntry) : Overlay
    data class EditProject(val projectId: Long) : Overlay
}

/** Bottom sheets that open from the widget pills. */
sealed interface HomeSheet {
    data class Widget(val id: WidgetId) : HomeSheet
    data object WhereWasI : HomeSheet
    data object AllWidgets : HomeSheet
}

/** What's open. Pressing Home closes everything and goes back to favourites. */
class HomeNav {
    var overlay by mutableStateOf<Overlay?>(null)
    var sheet by mutableStateOf<HomeSheet?>(null)

    /** Browsing all apps from the alphabet rail, and the letter it's on. */
    var browsing by mutableStateOf(false)
    var browseLetter by mutableStateOf<Char?>(null)

    var homePresses by mutableIntStateOf(0)
        private set

    /** Pages opened from Settings go back to Settings when closed. */
    private var fromSettings = false

    fun open(page: Overlay) {
        fromSettings = overlay == Overlay.Settings
        sheet = null
        overlay = page
    }

    /** Back: close the top-most thing that's open. */
    fun close() {
        when {
            sheet != null -> sheet = null
            overlay != null -> {
                overlay = if (fromSettings && overlay != Overlay.Settings) Overlay.Settings else null
                fromSettings = false
            }
            browsing -> stopBrowsing()
        }
    }

    fun stopBrowsing() {
        browsing = false
        browseLetter = null
    }

    fun goHome() {
        overlay = null
        sheet = null
        fromSettings = false
        stopBrowsing()
        homePresses++
    }
}

/** Things the widgets do that reach outside the store: launching apps, alarms. */
class HomeActions(
    private val context: Context,
    val store: Store,
    val catalog: AppCatalog,
    val nav: HomeNav,
    private val requestNotifications: () -> Unit,
) {
    /** Opens an app, stopping at the speed bump first if it's marked as distracting. */
    fun launch(app: AppEntry) {
        if (app.packageName in store.state.value.distracting) {
            nav.open(Overlay.SpeedBump(app))
        } else {
            catalog.launch(app)
            if (nav.overlay == Overlay.Drawer) nav.overlay = null
        }
    }

    /** Opens an app straight away (after the speed bump). */
    fun launchNow(app: AppEntry) {
        catalog.launch(app)
        nav.overlay = null
    }

    fun startTimer(kind: TimerKind, minutes: Int, label: String) {
        val timer = store.startTimer(kind, minutes, label, System.currentTimeMillis())
        TimerAlarm.schedule(context, timer)
        requestNotifications()
    }

    fun cancelTimer() {
        TimerAlarm.cancel(context)
        store.cancelTimer()
    }

    /** Called by the timer widget when the countdown reaches zero on screen. */
    fun finishTimerIfDue() {
        if (store.finishTimer(System.currentTimeMillis()) != null) {
            TimerAlarm.cancel(context)
        }
    }

    /** Pulls down the notification shade (phones that allow it). */
    fun expandNotifications(): Boolean = try {
        val service = context.getSystemService("statusbar")
        Class.forName("android.app.StatusBarManager").getMethod("expandNotificationsPanel").invoke(service)
        true
    } catch (e: Throwable) {
        false
    }
}
