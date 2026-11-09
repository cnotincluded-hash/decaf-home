package com.caffeinenotincluded.decafhome.timer

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.caffeinenotincluded.decafhome.MainActivity
import com.caffeinenotincluded.decafhome.R
import com.caffeinenotincluded.decafhome.data.Timer
import com.caffeinenotincluded.decafhome.data.TimerKind
import com.caffeinenotincluded.decafhome.decaf

/** Wakes the phone when a focus sprint or break ends, even if you're in another app. */
object TimerAlarm {

    fun schedule(context: Context, timer: Timer) {
        val alarms = context.getSystemService(AlarmManager::class.java) ?: return
        val pending = pendingIntent(context)
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarms.canScheduleExactAlarms()
        try {
            if (canExact) {
                alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timer.endsAt, pending)
            } else {
                alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timer.endsAt, pending)
            }
        } catch (e: SecurityException) {
            alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timer.endsAt, pending)
        }
    }

    fun cancel(context: Context) {
        context.getSystemService(AlarmManager::class.java)?.cancel(pendingIntent(context))
    }

    private fun pendingIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, TimerDoneReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
}

class TimerDoneReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val finished = context.decaf.store.finishTimer(System.currentTimeMillis()) ?: return
        Notifications.timerDone(context, finished)
    }
}

object Notifications {
    private const val CHANNEL_TIMER = "timer"
    private const val ID_TIMER = 1

    fun createChannels(context: Context) {
        val channel = NotificationChannel(CHANNEL_TIMER, "Focus timer", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Tells you when a focus sprint or a break ends"
        }
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    fun canPost(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun timerDone(context: Context, timer: Timer) {
        if (!canPost(context)) return
        val open = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val (title, text) = when (timer.kind) {
            TimerKind.FOCUS -> "Sprint done: +${timer.minutes} XP" to
                "Nice work${if (timer.label.isNotBlank()) " on ${timer.label}" else ""}. Note where you were before you switch."
            TimerKind.BREAK -> "Break's over" to "Ready for the next thing?"
        }
        val notification = Notification.Builder(context, CHANNEL_TIMER)
            .setSmallIcon(R.drawable.ic_stat_mug)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(Notification.BigTextStyle().bigText(text))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java)?.notify(ID_TIMER, notification)
    }
}
