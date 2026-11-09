package com.caffeinenotincluded.decafhome

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.caffeinenotincluded.decafhome.ui.screens.DecafRoot
import com.caffeinenotincluded.decafhome.ui.screens.HomeNav

class MainActivity : ComponentActivity() {

    private val nav = HomeNav()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { DecafRoot(nav) }
    }

    /** Pressing Home while already home closes whatever is open. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        nav.goHome()
    }

    override fun onResume() {
        super.onResume()
        // Catch a timer that ended while the phone was asleep.
        decaf.store.finishTimer(System.currentTimeMillis())
    }
}
