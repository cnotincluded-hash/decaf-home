# Decaf Home

*Ship faster. Stay sane.*

An Android home screen for indie game devs, in the Caffeine Not Included style,
with widgets built to help an ADHD brain start, stay on track and come back.
It's the phone sibling of the Decaf Linux remix. Runs on Android 8 and up,
tuned for Android 15. Everything stays on the phone.

## What's on the home screen

**Focus**
- **One thing now.** Only your current task, big. *Done* brings up the next one.
  *Too big? Split it* turns it into small steps. *Later* sends it to the back.
- **Focus timer.** 10, 15, 25 or 45-minute sprints, shown as a ring that drains,
  so time left is visible at a glance. A notification tells you when it ends,
  even if you're in Godot.
- **Where was I?** After a sprint, one line for future you. It's the first
  thing the project spotlight shows next time.
- **Speed bump.** Apps you choose open after a 10-second pause that asks what
  you came to do. *Not now* earns XP; you can also park the thought as a note.
- **Now / Next.** Your day in blocks: what you're doing now, time left, and
  what's next.
- **Break menu.** Breaks you picked in advance (5 minutes each), so a break
  doesn't turn into scrolling.

**Game dev**
- **Project spotlight.** One active game, its next step and quick-launch
  buttons for its tools. *Make it my one thing* sends the next step to the top.
- **Idea parking lot.** One tap to save an idea, bug or note, so it's kept
  without pulling you off the current project.
- **Countdowns.** Game jams, devlog days, milestones.
- **Dev streak.** Days you worked on games, with rest days (2 a week by
  default) so one missed day doesn't reset everything.
- **Design prompt of the day.** A small mechanic, art or level prompt.
- **XP and levels.** Finishing things earns XP, with confetti and a level badge.

**Launcher**
- Dock for up to 5 apps, and all apps behind the yellow button (or swipe up on the dock).
- Search in the app drawer. Press Enter to open the first result.
- Long-press any app to pin it, give it a speed bump, or open its app info.
- Light, dark, or match the phone.

## How it's built

- Kotlin and Jetpack Compose, one activity registered as a HOME app.
- `data/Store.kt` holds all state and saves it as JSON in the app's private
  storage. `data/Progress.kt` has the streak, XP and day-plan rules.
- `timer/TimerAlarm.kt` uses an exact alarm so sprints end on time.
- `ui/widgets/` has the home-screen widgets, `ui/screens/` the drawer, speed
  bump and settings pages.

## What a launcher can't do

Android doesn't let a launcher restyle the status bar, quick settings,
notification shade or Settings app, or replace the recent-apps screen and
gesture animations. Speed bumps only apply to apps opened from Decaf Home, not
from notifications or recent apps.

Fonts: Fredoka and Nunito, SIL Open Font License (see `OFL-*.txt`).
