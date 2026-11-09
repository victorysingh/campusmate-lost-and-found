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
