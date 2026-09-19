# Screen Time Guard

Native Android app (Kotlin + Jetpack Compose) that lets you pick apps, set a daily
time limit, and locks each app out once its timer runs out -- until the next day.

## Why this had to be a real app, not a web page
Blocking another app requires OS-level access (seeing what's in the foreground,
drawing over other apps, forcing the user home). That only exists through native
Android APIs, so this ships as a full Android Studio project rather than a browser
artifact.

## How it works
- **Accessibility Service** (`AppMonitorAccessibilityService`) detects which app is
  in the foreground and ticks a per-second usage counter for any app you're monitoring.
- **Room database** stores each app's daily limit, minutes used today, and whether
  it's currently blocked.
- Once usage hits the limit: the service sends the user to the home screen and shows
  a **system overlay** (`BlockOverlayService`) -- a frosted glass "Time's up" card
  with a live countdown to midnight.
- **Daily reset** happens automatically: every read compares the stored date to
  today, so the moment the calendar flips, the block clears itself. A `WorkManager`
  job also runs every 15 minutes as a safety net.
- UI is Jetpack Compose, Material 3, with a white-glass aesthetic (translucent
  gradient cards, soft borders, spring-animated progress rings, animated screen
  transitions, a pulsing lock icon on the block screen).

## Project structure
```
app/src/main/java/com/srizon/screentimeguard/
  data/       Room entity, DAO, database, TimerRepository (all timer logic + daily reset)
  service/    AppMonitorAccessibilityService, BlockOverlayService, MidnightResetWorker
  ui/
    theme/    Color, Type, Theme, glassCard() modifier + gradient background
    screens/  Onboarding, Dashboard, AddApp, SetTimerSheet, BlockedGlassScreen
    navigation/  Compose Navigation graph with slide/fade transitions
  util/       AppInfoProvider (installed apps), PermissionUtils, time formatting
```

## Getting a built APK
This was written outside Android Studio (no SDK/network in the environment that
built it), so no compiled APK is included. Two ways to get one:

**Option A -- GitHub Actions (no local install needed).** Push this folder to a
GitHub repo. The included `.github/workflows/build-apk.yml` builds a debug APK on
every push and on manual trigger (Actions tab -> "Build APK" -> "Run workflow").
Download it from the run's Artifacts section, then install it on your phone
(you'll need "install from unknown sources" allowed, or `adb install app-debug.apk`).

**Option B -- Android Studio**, see Setup below; use *Build > Build App Bundle(s) /
APK(s) > Build APK(s)*, or run `./gradlew assembleDebug` in its terminal. Output
lands at `app/build/outputs/apk/debug/app-debug.apk`.

One APK covers every supported Android version (8.0 through the latest) -- no
separate builds per OS version needed. A signed release build needs your own
keystore; ask if you want that wired up.

## Setup
1. Open the `ScreenTimeGuard` folder in **Android Studio** (Hedgehog or newer).
   Let it sync -- Android Studio will generate the Gradle wrapper jar for you if it's
   missing (this project ships without the binary wrapper jar since it can't be
   produced offline).
2. Run on a **physical device** rather than an emulator -- app blocking needs real
   foreground-app switching, which is flaky on emulators.
3. On first launch you'll see a 3-step permission screen:
   - **Usage Access** -- lets the app see which app is active
   - **Display over other apps** -- needed for the block overlay
   - **Accessibility Service** -- needed to detect app switches in real time
   Each button opens the right system settings page. The app moves on automatically
   once all three are granted.
4. Tap **+**, pick an app, drag the slider to set its daily limit, save.
5. Use that app normally -- once the timer runs out you're sent home and see the
   block screen. Try opening it again: it blocks instantly. It stays locked until
   the next calendar day.

## Known limitations / things to tune
- Some OEMs (Xiaomi, Oppo, Vivo, etc.) aggressively kill background accessibility
  services. If enforcement stops working after a while, exempt the app from battery
  optimization in system settings.
- Gradle/AGP/Compose versions are pinned to what was current as of writing
  (AGP 8.2.2, Kotlin 1.9.22, Compose BOM 2024.02.00). If Android Studio prompts an
  upgrade, accepting it is safe.
- This was written and reviewed outside of Android Studio (no SDK/network access in
  the environment that built it), so give it a first build/sync pass before relying
  on it -- flag anything that doesn't compile and it can be fixed directly.
