# Decisions and assumptions

Short log of choices made so the team can explain them in the viva.
Newest phase at the bottom.

> **D1 to D34 are about the first Flutter version** (kept in git history, tag
> `flutter-phase1`). From D35 on, the app is native Android in Java, and those
> decisions replace the Flutter ones wherever they disagree.

## Phase 0 (10 Nov 2026)

| # | Decision | Why |
|---|----------|-----|
| D1 | **Target platform is Android.** A web build is also generated, but only for quick UI checks in Chrome. | MAD lab = Android. Web lets us test without an emulator. |
| D2 | **Demo mode**: if `Firebase.initializeApp()` fails, the app uses in-memory "mock" services and shows a yellow DEMO MODE banner. | The brief asks for the UI to run before Firebase is set up. The banner stops anyone thinking demo data is saved. |
| D3 | **We do not import `lib/firebase_options.dart`.** On Android, `Firebase.initializeApp()` with no options reads `android/app/google-services.json`, which `flutterfire configure` creates. Both files are git-ignored. | The brief says never commit Firebase config files. If the code imported `firebase_options.dart`, a fresh clone would not compile without it. With this setup a fresh clone always compiles and falls back to demo mode. |
| D4 | **The web build always runs in demo mode.** | Firebase on web needs `firebase_options.dart` (see D3). Web is not a target. |
| D5 | `ALLOWED_EMAIL_DOMAIN` keeps the exact UPPER_CASE name from the brief, with an `ignore` comment for the Dart naming lint. | Easy to find for the team. |
| D6 | Navigation uses plain `Navigator` + named routes (`routes.dart`), not go_router. | Fewer packages, and it is the classic "screens + navigation" MAD concept. |
| D7 | Dark theme is defined from day one (`ThemeMode.system`). Phase 3 will review it. | It is a one-line addition in `theme.dart`. |
| D8 | Flutter SDK was installed at `C:\src\flutter` on the dev laptop (no spaces in the path). | Flutter's docs warn against SDK paths with spaces. |
| D9 | `flutter_web_plugins` (an SDK package) is listed in `pubspec.yaml` even though our code never imports it. | With Flutter 3.47.9 + firebase_core_web 4.1.0, `flutter build web` fails without it. The generated plugin registrant imports it, but no package depends on it any more. |
| D10 | `cupertino_icons` was removed from the template. | We only use Material icons (brief: no unused packages). |
| D11 | `minSdk = 23` and the `INTERNET` permission are set in the Android project. | Firebase needs API 23+. Flutter's template only adds INTERNET to debug builds, so without it the release APK could not reach Firebase. |

## Phase 1 (10 Nov 2026)

| # | Decision | Why |
|---|----------|-----|
| D12 | **Matches are worked out live in the app** (`match_service.dart`), not saved in Firestore. The detail screen runs `findMatches(item, allLoadedPosts)` every time it builds. After posting, a snackbar shows how many matches were found. | Always up to date (a new found post shows up as a match at once). Nothing to keep in sync. No Cloud Functions needed. |
| D13 | Match rule: opposite type + same category + at least 1 shared keyword (3+ letters, stop words removed, plural "s" dropped) from title + description. Sorted by number of shared words. | Easy to explain and test. Location is shown but not used for matching. |
| D14 | **Search and category filter run in the app** on the newest 100 posts (`feedLimit`). Firestore only does `orderBy('createdAt')`. | No composite indexes to create. Fine for one campus. If posts grow far beyond 100, add paging later. |
| D15 | **The photo is optional.** | Some found items are easier to describe than to photograph. The sample items have no photo. |
| D16 | Photos: image_picker resizes to at most 1024 px at JPEG quality 70. Then `compressToLimit()` (package `image`) lowers quality and size until the file is ≤ 300 KB. It is stored as base64 (about 33% larger, so ≤ ~400 KB). | Well under Firestore's 1 MiB-per-document limit. Compression runs in `compute()` so the UI does not freeze. |
| D17 | Sign-up collects a name, saved as the Firebase Auth `displayName`. There is no `users` collection yet. Each post copies the poster's name and email. | One less collection. The feed does not need extra reads. |
| D18 | Sample items ("Add 10 sample items" in the menu) are posted as the logged-in user. | Phase 3 security rules will only allow `ownerId == your uid`, so this keeps working then. |
| D19 | **Known gap:** the college-domain check is only in the app (form + AuthProvider). There is no email verification, so someone could sign up with a college-looking address they do not own. Phase 3 rules will also check `request.auth.token.email` on the server. Email verification is not in the brief, so it was not added. | No scope creep. Noted so the team can answer it in the viva. |
| D20 | **Known gap:** posting waits for Firestore to confirm the write. Offline, the Post button keeps spinning until the network is back. | Phase 2 covers "offline-safe errors". |
| D21 | The detail route is given the whole `Item` but shows the live copy from ItemProvider when there is one. | Shows at once right after saving, and still updates in realtime. |

## Phase 1 UI polish (11 Nov 2026)

| # | Decision | Why |
|---|----------|-----|
| D22 | **`USE_MOCK_DATA` in `config.dart`** now picks mock or Firebase. It replaces the automatic detection from D2. With `false`, the app still falls back to mock mode (with the banner) if Firebase is not configured. | The brief asks for one flag. The fallback means a missing config file can never crash the demo. |
| D23 | Poppins TTF files are **bundled** in `assets/google_fonts/` and `GoogleFonts.config.allowRuntimeFetching = false`. The greeting has no emoji. | The demo must work offline. google_fonts would otherwise download the font, and an emoji on web needs a font download too. |
| D24 | The dark-mode switch is not saved; the app starts in light mode. | Saving it needs `shared_preferences`, an extra package the brief does not ask for. |
| D25 | `google_fonts` is pinned to **^8.0.0**. | Version 9.0 is built on the new `package:material_ui`. Its `TextTheme` is a different class from Flutter's, so the theme does not compile. |
| D26 | The shimmer skeleton is hand-written (`widgets/shimmer_list.dart`, about 60 lines), not the `shimmer` package. | One small widget, no extra dependency, and easy to explain (AnimationController + ShaderMask). |
| D27 | In mock mode the 12 sample posts belong to made-up students (Priya Sharma, Rahul Kumar…). With Firebase they belong to the logged-in user. This replaces D18. | It looks real, and the "This is mine / I found this" button shows on someone else's post. Firestore rules (Phase 3) will only allow posting as yourself. |
| D28 | Mock mode waits 700 ms before the first list loads. | Otherwise the skeleton is never visible in a demo. Tests pass `Duration.zero`. |
| D29 | Demo menu: "Simulate match notification" shows an in-app MaterialBanner for now. "Reset data" works in mock mode only. | Phone notifications (flutter_local_notifications) are Phase 2. We never wipe the real database from a hidden menu. |
| D30 | **Android SDK not installed.** On 11 Nov, C: had 6.5 GB free, below the 8 GB limit set by the team lead, so JDK 17 + Android tools were not installed. Screenshots were taken in Chrome at 390 × 844, not on an emulator or phone. | Rule from the team lead. Free some disk space, then the install can go ahead. |
| D31 | Post form: Material `Stepper` (horizontal). Step names sit **under** the circles (`Step.label`). | With names beside the circles the header overflowed by 14 px on a 320 px-wide phone (caught by the small-phone test). |
| D32 | Location step = campus place dropdown (`campusLocations` in config) + optional detail text, saved as one string, e.g. "Library · 2nd floor". No map. | Simple, works offline, no API key. A map pin stays optional for Phase 2. |
| D33 | Bottom bar has Home + Profile around the centred **+** button. My Posts / Alerts tabs arrive in Phase 2. | No empty placeholder tabs in Review 1. |
| D34 | The detail screen's sticky button: on your own post it scrolls to the matches. On someone else's post it says "I found this" / "This is mine" and, for now, shows "Claiming arrives in the next update." | Phase 2 replaces the placeholder with the claim flow. |

## Switch to Java (16 Nov 2026)

| # | Decision | Why |
|---|----------|-----|
| D35 | **The app was rewritten as a native Android app in Java** (XML layouts, Material 3, AndroidX). The Flutter code was removed from the branch but stays in history (tag `flutter-phase1`). | The faculty requires Java for the MAD lab. |
| D36 | Toolchain: JDK 17, Android SDK 35 (compile + target), minSdk 24 (Android 7.0), Gradle 8.10.2, Android Gradle Plugin 8.7.3. No Android Studio and no emulator on the dev laptop. | Disk space was tight. Everything builds from the command line with `./gradlew`. |
| D37 | **Same Firebase plan as before:** Firebase Auth + Cloud Firestore only. No Storage, no Cloud Functions. Photos are compressed to at most 300 KB and saved as base64 text inside the post. | Works on the free Spark plan, nothing to pay or set up beyond Firestore. |
| D38 | `Config.USE_MOCK_DATA` (in `Config.java`) picks mock or Firebase. The google-services Gradle plugin is applied only when `app/google-services.json` exists, so a fresh clone builds without any Firebase file. | Never commit Firebase config. The demo must work offline. |
| D39 | Data goes through **repository interfaces** (`AuthRepository`, `ItemRepository`, `ClaimRepository`), each with a mock and a Firebase version. Screens never call Firebase directly. | The UI does not care where data comes from, and tests run on the mock version. |
| D40 | Mock mode has **6 sample students** (password `demo123`) and the in-memory data is shared between logins. | You can log in as Asha, post a claim, log out, log in as the post owner and approve it, all on one phone. |
| D41 | The four tabs are Fragments that are shown and hidden, not replaced. | Each tab keeps its scroll position and search text when you switch. |
| D42 | Matching is unchanged (D13): opposite type, same category, at least one shared keyword. Returned posts are not matched. | Still easy to explain and test. |

## Phase 2 (16 Nov 2026)

| # | Decision | Why |
|---|----------|-----|
| D43 | **Claim flow:** a found post has a proof question set by the finder. The claimant answers it; the post owner approves or rejects. One claim per student per post (claim id = `itemId_claimantId`). | Stops strangers taking items. The fixed id makes duplicates impossible, even on the server. |
| D44 | **Chat opens only after approval**, as a sub-collection `claims/{id}/messages`. | Students only talk once the owner is happy the claim is real. |
| D45 | **Notifications are local** (NotificationCompat), not push. A watcher compares new data with what the phone saw last time (kept in SharedPreferences) and notifies about: a new claim on my post, my claim approved or rejected, and a new post that matches mine. If notifications are not allowed, the same message shows as a snackbar. | Push needs Cloud Functions or a server (not allowed, D37). Local notifications work while the app is open or still running in the background. |
| D46 | **Known gap:** with the app fully closed, no notification arrives. | Needs Firebase Cloud Messaging + a server. Listed as future scope. |
| D47 | **Offline:** Firestore keeps writes on the phone and syncs them later. If a write has not been confirmed after 8 seconds, the app treats it as saved. When posting or editing with no internet, the form says the post is saved on the phone and will sync. | Otherwise a button would spin forever with no signal. |
| D48 | Location picker = campus place dropdown + optional detail (as D32). No map. | No API key, works offline. |
| D49 | My Posts: edit, mark as returned (and undo), delete with a confirmation dialog. Returned posts drop out of matching and cannot be claimed. | Owners need to close a post once the item is back. |

## Phase 3 (16 Nov 2026)

| # | Decision | Why |
|---|----------|-----|
| D50 | **`firestore.rules`**: posts readable by any signed-in student; only the owner may create (as themselves), edit or delete. Claims readable only by the two participants; created only by the claimant, as pending, on someone else's open post; only the post owner may change the status (and nothing else). Chat messages: participants only, and only after approval. | The app checks all of this too, but an APK can be modified, so the server must enforce it. |
| D51 | **Known gap:** the rules have not been run against the Firestore emulator or a real project yet (no Firebase project exists). | Firebase setup is a human step. Test them once the project exists. |
| D52 | Tests run on the laptop with **Robolectric** (Android framework on the JVM) and Espresso, so no phone or emulator is needed: 41 tests. A small-phone test (320 × 568 dp) fails if any view is cut off. | No emulator (D36). |
| D53 | Screenshots are drawn by a test (`ScreenshotTest`) at 390 × 844 dp. They are only rewritten when you ask: `./gradlew testDebugUnitTest -Pscreenshots`. | Same screenshots on every machine, and normal test runs do not touch the docs. |
| D54 | The dark-mode switch is saved (SharedPreferences) and applied when the app starts. This replaces D24. | SharedPreferences is built into Android, no extra library. |
