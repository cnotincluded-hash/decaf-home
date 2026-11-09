package com.caffeinenotincluded.decafhome.data

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherApps
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.os.UserHandle
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** One app the launcher can open. [key] is "package/activity". */
data class AppEntry(
    val key: String,
    val label: String,
    val packageName: String,
    val component: ComponentName,
    val icon: ImageBitmap,
)

/** All apps with an icon, kept up to date as apps are installed and removed. */
class AppCatalog(private val context: Context) {

    private val launcherApps = context.getSystemService(LauncherApps::class.java)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _apps = MutableStateFlow<List<AppEntry>>(emptyList())
    val apps: StateFlow<List<AppEntry>> = _apps.asStateFlow()

    private val callback = object : LauncherApps.Callback() {
        override fun onPackageRemoved(packageName: String, user: UserHandle) = refresh()
        override fun onPackageAdded(packageName: String, user: UserHandle) = refresh()
        override fun onPackageChanged(packageName: String, user: UserHandle) = refresh()
        override fun onPackagesAvailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) = refresh()
        override fun onPackagesUnavailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) = refresh()
    }

    init {
        launcherApps.registerCallback(callback, Handler(Looper.getMainLooper()))
        refresh()
    }

    fun refresh() {
        scope.launch {
            _apps.value = runCatching { load() }.getOrDefault(_apps.value)
        }
    }

    private fun load(): List<AppEntry> {
        val user = Process.myUserHandle()
        val size = (56 * context.resources.displayMetrics.density).toInt()
        return launcherApps.getActivityList(null, user)
            .filter { it.componentName.packageName != context.packageName }
            .map { info ->
                AppEntry(
                    key = info.componentName.flattenToShortString(),
                    label = info.label.toString(),
                    packageName = info.componentName.packageName,
                    component = info.componentName,
                    icon = info.getBadgedIcon(0).toBitmap(size, size).asImageBitmap(),
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    fun launch(app: AppEntry): Boolean = try {
        launcherApps.startMainActivity(app.component, Process.myUserHandle(), null, null)
        true
    } catch (e: Exception) {
        refresh()
        false
    }

    fun openAppInfo(app: AppEntry) {
        runCatching {
            launcherApps.startAppDetailsActivity(app.component, Process.myUserHandle(), null, null)
        }
    }
}
