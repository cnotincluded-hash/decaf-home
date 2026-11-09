# Decaf Home uses no reflection; the default Android rules are enough.
# Keep the timer receiver's class name stable for the alarm's PendingIntent.
-keep class com.caffeinenotincluded.decafhome.timer.TimerDoneReceiver
