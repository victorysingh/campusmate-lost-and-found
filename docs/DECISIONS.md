# Decisions and assumptions

Short log of choices made so the team can explain them in the viva.
Newest phase at the bottom.

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
