package com.caffeinenotincluded.decafhome

import android.app.Application
import android.content.Context
import com.caffeinenotincluded.decafhome.data.AppCatalog
import com.caffeinenotincluded.decafhome.data.Store
import com.caffeinenotincluded.decafhome.timer.Notifications
import java.io.File

class DecafApp : Application() {
    lateinit var store: Store
        private set
    lateinit var catalog: AppCatalog
        private set

    override fun onCreate() {
        super.onCreate()
        store = Store(File(filesDir, "decaf-state.json"))
        catalog = AppCatalog(this)
        Notifications.createChannels(this)
    }
}

val Context.decaf: DecafApp get() = applicationContext as DecafApp
