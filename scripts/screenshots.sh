#!/usr/bin/env bash
#
# Runs inside CI on an Android 15 emulator: installs the debug APK, makes it the
# home app, fills it with sample data and saves screenshots to screenshots/.

set -uo pipefail

PKG=com.caffeinenotincluded.decafhome
APK=app/build/outputs/apk/debug/app-debug.apk
OUT=screenshots
mkdir -p "$OUT"

shot() {
    sleep "${2:-3}"
    adb exec-out screencap -p > "$OUT/$1.png"
    echo "saved $OUT/$1.png"
}

home() {
    adb shell input keyevent KEYCODE_HOME
}

adb wait-for-device
adb shell settings put system screen_off_timeout 1800000
adb install -r "$APK"
adb shell cmd package set-home-activity "$PKG/.MainActivity"

# 1. First run, before any data.
home
shot 01-first-run 8
adb shell input swipe 540 1800 540 700 400
shot 02-first-run-scrolled

# 2. Sample data, like a few days of use.
NOW=$(( $(date +%s) * 1000 ))
H=3600000
D=$(( 24 * H ))
DAY() { date -u -d "$1 day" +%F; }
cat > /tmp/decaf-state.json <<EOF
{
  "version": 1,
  "tasks": [
    {"id": 1, "text": "Fix the double-jump timing"},
    {"id": 2, "text": "Export the slime sprites"},
    {"id": 3, "text": "Write devlog #4"}
  ],
  "projects": [
    {"id": 10, "name": "Kettle Quest", "color": 2,
     "nextStep": "Tune jump gravity in player.gd",
     "whereWasI": "Halfway through the coyote-time check",
     "whereWasIAt": $(( NOW - 3 * H )),
     "apps": ["com.android.chrome/com.google.android.apps.chrome.Main", "com.android.settings/.Settings"]}
  ],
  "activeProjectId": 10,
  "ideas": [
    {"id": 20, "text": "A boss that steals your hat", "kind": "IDEA", "projectId": 10, "createdAt": $(( NOW - 2 * H ))},
    {"id": 21, "text": "Player clips through ledge corners", "kind": "BUG", "projectId": 10, "createdAt": $(( NOW - 5 * H ))}
  ],
  "deadlines": [
    {"id": 30, "title": "Game jam ends", "dueAt": $(( NOW + 2 * D + 5 * H ))},
    {"id": 31, "title": "Demo build", "dueAt": $(( NOW + 9 * D ))}
  ],
  "devDays": ["$(DAY -1)", "$(DAY -2)", "$(DAY -4)", "$(DAY -5)", "$(DAY -6)", "$(DAY -7)", "$(DAY -8)"],
  "restDaysPerWeek": 2,
  "timeline": [
    {"id": 40, "minuteOfDay": 540, "label": "Art"},
    {"id": 41, "minuteOfDay": 720, "label": "Lunch"},
    {"id": 42, "minuteOfDay": 780, "label": "Code"},
    {"id": 43, "minuteOfDay": 1080, "label": "Dinner"}
  ],
  "xp": 185,
  "timer": {"kind": "FOCUS", "minutes": 25, "startedAt": $(( NOW - 9 * 60000 )),
            "endsAt": $(( NOW + 16 * 60000 )), "label": "Fix the double-jump timing"},
  "focusMinutes": 25,
  "askWhereWasI": false,
  "dock": ["com.android.chrome/com.google.android.apps.chrome.Main",
           "com.android.settings/.Settings",
           "com.android.camera2/com.android.camera.CameraLauncher"],
  "distracting": ["com.android.chrome"],
  "themeMode": "LIGHT",
  "nextId": 100
}
EOF

adb shell am force-stop "$PKG"
adb shell "run-as $PKG sh -c 'mkdir -p files && cat > files/decaf-state.json'" < /tmp/decaf-state.json
adb shell "run-as $PKG ls -l files"
home
shot 03-home-top 6
adb shell input swipe 540 1800 540 700 400
shot 04-home-2
adb shell input swipe 540 1800 540 700 400
shot 05-home-3
adb shell input swipe 540 1800 540 700 400
shot 06-home-4
adb shell input swipe 540 1800 540 700 400
shot 07-home-5

# 3. All apps (tap the yellow button at the right end of the dock).
home
sleep 2
adb shell input swipe 540 2250 540 1200 250
shot 08-drawer

# 4. Speed bump: Chrome is marked as distracting in the sample data.
adb shell input tap 540 385   # the search box
sleep 1
adb shell input text Chrome
sleep 1
adb shell input keyevent KEYCODE_ENTER
shot 09-speed-bump 2

# 5. Dark mode.
adb shell input keyevent KEYCODE_BACK
adb shell am force-stop "$PKG"
sed -i 's/"themeMode": "LIGHT"/"themeMode": "DARK"/' /tmp/decaf-state.json
adb shell "run-as $PKG sh -c 'cat > files/decaf-state.json'" < /tmp/decaf-state.json
home
shot 10-dark-top 6
adb shell input swipe 540 1800 540 700 400
shot 11-dark-2

adb logcat -d -s AndroidRuntime:E > "$OUT/crashes.txt" || true
echo "done"
