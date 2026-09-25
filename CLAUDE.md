# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Язык общения

Всегда отвечай пользователю на русском языке. Комментарии, названия викторин, категорий и весь UI в проекте — на русском.

## Commands

Build and test are driven by the Gradle wrapper (`./gradlew`):

- Build debug APK: `./gradlew assembleDebug`
- Full build: `./gradlew build`
- Lint: `./gradlew lint` (report in `app/build/reports/lint-results-*.html`)
- Unit tests (JVM): `./gradlew test`
- A single unit test class: `./gradlew testDebugUnitTest --tests "ru.plumsoftware.game.ExampleUnitTest"`
- Instrumented tests (need a device/emulator): `./gradlew connectedAndroidTest`
- Install on a connected device: `./gradlew installDebug`

Note: `app/google-services.json` is committed and required for the Firebase Gradle plugin to build.

## Big-picture architecture

Single-module Android app (`:app`), 100% Jetpack Compose, package `ru.plumsoftware.game`. There is **no Jetpack Navigation graph and no per-screen ViewModels** — the whole app is one screen switcher driven by a single ViewModel.

### Navigation is an enum + AnimatedContent (not NavHost)
- `GameScreen` enum (in `GameViewModel.kt`) lists every screen. `GameViewModel.currentScreen: StateFlow<GameScreen>` is the current route.
- `MainActivity.GameApp()` renders the current screen via a `when (screen)` inside `AnimatedContent`. Slide direction is derived from `GameScreen.ordinal`, so **the order of enum entries determines transition direction** — keep that in mind when reordering.
- Back navigation is manual: `MainActivity.onBackPressed()` → `GameViewModel.navigateUp()`, a hardcoded `when` mapping each screen to its parent. Add new screens to *both* the `when` in `GameApp` and the `when` in `navigateUp`.
- The quiz *result* is not a `GameScreen`; it's an overlay gated by `showQuizResult: StateFlow<Boolean>`.
- Shop has special back behavior: `openShopFromQuiz()` / `closeShop()` remember whether the shop was entered from the quiz to return there.

### Single source of truth: GameViewModel + GameManager + DataStore
- `GameManager` (`data/GameManager.kt`) owns all persistence via a Preferences **DataStore** named `game_preferences`. All mutations are `suspend` and go through `dataStore.edit { }`; reads are `Flow`s mapped from `dataStore.data`. `GameState` (in `GameData.kt`) is the deserialized snapshot.
- `GameViewModel` (`AndroidViewModel`) collects `GameManager.gameState` and exposes many derived `StateFlow`s (available/completed/finished quizzes, tier totals, etc.). UI is stateless and reads these flows. When adding persisted fields: add a `PreferencesKeys` entry, map it in `GameManager.gameState`, add it to `GameState`, and expose/derive it in the ViewModel.
- Progression rules live in `GameData`: leveling is 100 XP per level (`addExperience`), and player level maps to unlocked quiz *tiers* via `unlockedQuizTiersForPlayerLevel` (tiers at player levels 1/3/5/7/10/15). A quiz is only "completed" when the player answers **all** questions correctly (`onQuizComplete`).

### Quiz content
- All quiz/question data is **hardcoded in `GameData.kt`** (~2200 lines): `Question`, `Quiz`, and the master `quizzes` list, accessed through `getQuiz`, `getAllQuizzes`, `getQuestionsForQuiz`. This is the file to edit to add/change quizzes.
- `QuizCategories.kt` defines the display-only category catalog (`ALL_CATEGORIES`); `findQuizIdForCategory` fuzzily maps a category to a quiz by name/emoji matching.
- **Remote quizzes**: Firebase Remote Config key `time_quiz` is parsed (`kotlinx.serialization`) into `RemoteConfigQuizModel` in `MainActivity`. If a remote quiz is active its questions override the local ones (`getQuestionsForCurrentQuiz`); `setEmptyRemoteQuiz()` clears it.

### Achievements
- `getAchievements(gameState)` (in `ui/screens/`) computes achievement progress from `GameState`. `GameViewModel.checkNewAchievements()` runs after each quiz, unlocks newly-earned ones in DataStore, grants coin rewards, and queues an `AchievementToast` shown via `AchievementToastOverlay` (queue drained one-at-a-time).

### Ads (Yandex Mobile Ads)
- Uses `com.yandex.android:mobileads`, not Google AdMob. `AdsBase` (sealed class) holds ad unit IDs per distribution target (RuStore / Huawei / Google Play); `App.adsBase` selects the active one. `AdsManager` (builder-based) loads/shows interstitial, rewarded, and app-open ads. Rewarded ads grant coins via `GameViewModel.onAdsRewarded`.

### Notifications
- `NotificationScheduler` schedules periodic WorkManager jobs (`DailyNotificationWorker`) for daily and quiz-reminder notifications; `NotificationManager` builds/shows them. Scheduling is (re)triggered from `GameApp` once notification permission is granted.

### UI conventions
- Theme lives in `ui/theme/` — use `ExtendedTheme { }` (an extended Material 3 theme with `GameColors`), not the raw `MaterialTheme`. `MainBack` wraps screens with the app background.
- Screens are in `ui/screens/`, reusable pieces in `ui/components/` (game-specific ones under `ui/components/game/`). Version catalog is `gradle/libs.versions.toml` — add dependencies there, not inline (the one exception already inlined is `kotlin("plugin.serialization")` in `app/build.gradle.kts`).
