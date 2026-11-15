# Viva notes

What each part of Campusmate does, and which MAD (Mobile Application Development)
concept it shows. Questions examiners are likely to ask are at the end.

All code is in `app/src/main/java/com/campusmate/`. Layouts are in `app/src/main/res/layout/`.

## The big picture in one minute

- **Android app in Java**, with screens built from **XML layouts** and **Material Design 3** components.
- **Activities** for full screens (login, detail, post form, chat) and **Fragments** for the four tabs.
- Data goes through **repository interfaces**. Each has two versions: **mock** (in memory, for the demo
  and tests) and **Firebase** (Authentication + Cloud Firestore). One flag picks which:
  `Config.USE_MOCK_DATA`.
- Screens **observe LiveData**, so they redraw by themselves when data changes.
- **No Firebase Storage, no Cloud Functions**: photos are compressed and stored as text in Firestore;
  matching and notifications run inside the app.

```
Activity / Fragment  ──observes──▶  LiveData  ◀──updates──  Repository (interface)
        │                                                      ├─ Mock…Repository   (in memory)
        └──calls──▶  repository methods  ─────────────────────▶└─ Firestore…Repository (Firebase)
```

## Screens

| Screen | Files | What it does | MAD concepts it shows |
|--------|-------|--------------|------------------------|
| Splash | `MainActivity` (`SplashScreen.installSplashScreen`), `Theme.Campusmate.Starting` in `values/themes.xml` | Teal splash with the logo while the app starts | SplashScreen API, themes |
| Login | `ui/LoginActivity`, `activity_login.xml` | Email + password, error under each field, link to sign up, demo accounts hint | Activity, explicit Intent, `TextInputLayout` errors, input validation, `Intent.FLAG_ACTIVITY_CLEAR_TASK` (so Back does not return to login) |
| Sign up | `ui/SignupActivity`, `activity_signup.xml` | Name, college email, password, confirm password | Form validation, Firebase Auth `createUserWithEmailAndPassword` |
| Main (tabs) | `ui/MainActivity`, `activity_main.xml`, `menu/bottom_nav.xml` | Bottom bar with Home, My posts, Claims, Profile and a centred **+** button; badge with waiting claims; asks for notification permission once | `BottomAppBar` + `FloatingActionButton`, `BottomNavigationView`, **Fragments** with `FragmentManager` show/hide, `BadgeDrawable`, **runtime permission** (`POST_NOTIFICATIONS`), `onNewIntent` (opened from a notification) |
| Home | `ui/HomeFragment`, `fragment_home.xml`, `ui/ItemAdapter`, `item_post_card.xml` | Greeting, search, category chips, Lost/Found switch with counts, cards, pull to refresh, skeleton while loading, empty/error states | **RecyclerView + ListAdapter + DiffUtil**, `SwipeRefreshLayout`, `ChipGroup`, `MaterialButtonToggleGroup`, `TextWatcher`, `CoordinatorLayout` + `AppBarLayout` (header scrolls away), LiveData observer, layout animation |
| Item detail | `ui/ItemDetailActivity`, `activity_item_detail.xml` | Big image, badge, info rows, description, possible matches, sticky button whose text depends on who you are and the claim state | **Shared element transition**, `CollapsingToolbarLayout`, `NestedScrollView`, `MaterialAlertDialogBuilder` with a custom view (claim dialog), Snackbar |
| Post an item | `ui/AddItemActivity`, `ui/AddItemViewModel`, `activity_add_item.xml` | 3 steps: Photo → Details → Location. Camera or gallery, compress, validate each step, date picker, campus place dropdown. Also used to **edit** a post | **ViewModel** (keeps the form through screen rotation), `ViewFlipper`, **Activity Result APIs** (`TakePicture`, `PickVisualMedia`), **FileProvider** for the camera, background thread (`ExecutorService` + `Handler`), `MaterialDatePicker`, `OnBackPressedCallback` (Back goes to the previous step) |
| My posts | `ui/MyPostsFragment`, `fragment_my_posts.xml`, `item_post_compact.xml`, `menu/menu_my_post.xml` | Your posts with waiting claims; ⋮ menu to edit, mark returned / not returned, delete (with confirm) | `PopupMenu`, confirm dialog, update/delete in Firestore, plurals resource |
| Claims | `ui/ClaimsFragment`, `ui/ClaimAdapter`, `fragment_claims.xml`, `item_claim.xml` | Two tabs: claims on my posts (approve / reject) and claims I sent (status) | `TabLayout`, RecyclerView with button callbacks, status chips |
| Chat | `ui/ChatActivity`, `ui/MessageAdapter`, `activity_chat.xml`, `item_message_mine/theirs.xml` | Messages after approval; locked notice before; owner can mark the item returned from the bar | RecyclerView with **two view types**, `stackFromEnd`, `adjustResize` for the keyboard, toolbar menu, realtime listener |
| Profile | `ui/ProfileFragment`, `fragment_profile.xml` | Initials avatar, stats, dark mode switch, notifications row, data source, log out, version | **SharedPreferences**, `AppCompatDelegate.setDefaultNightMode` (dark mode), **implicit Intent** to the system notification settings, `BuildConfig.VERSION_NAME` |
| Demo menu | `ui/DemoMenu`, `sheet_demo_menu.xml` | Long-press the logo: load samples, reset, toggle theme, simulate a match | `BottomSheetDialog`, long-click listener |

## Other files

| File | What it does | Concept |
|------|--------------|---------|
| `CampusmateApp` | Runs once when the app process starts: applies the saved theme, creates the notification channel, picks mock or Firebase | Custom `Application` class (named in the manifest) |
| `Config` | `USE_MOCK_DATA`, college email domain, categories, campus places, 300 KB photo limit | Constants in one place |
| `AndroidManifest.xml` | Activities, permissions (`INTERNET`, `ACCESS_NETWORK_STATE`, `POST_NOTIFICATIONS`), launcher activity, FileProvider | Manifest, intent filters, permissions |
| `model/Item`, `Claim`, `ChatMessage`, `AppUser` | Plain Java objects with `toMap()` / `fromMap()` for Firestore | Data model, Firestore documents |
| `data/*Repository` (interfaces) | What the app can do with users, posts, claims, without saying how | Abstraction / interface, dependency inversion |
| `data/Mock…Repository` | In-memory versions with sample students; small delay so loading is visible | Test doubles, `Handler.postDelayed` |
| `data/Firebase…`, `Firestore…Repository` | Firebase Auth and Firestore versions; realtime `addSnapshotListener` | Cloud backend, realtime updates, offline cache |
| `data/FirebaseErrors` | Turns Firebase error codes into friendly messages; treats a write as saved after 8 s offline | Error handling, offline handling |
| `data/Repositories` | Creates and hands out the chosen repositories | Service locator |
| `data/Session` | Starts and stops listeners and the notification watcher on login / logout | Lifecycle of shared state |
| `data/SampleData` | 6 demo students and 12 campus posts | Test data |
| `data/FeedState` | Loading / ready / failed, plus the list | UI state object for LiveData |
| `logic/MatchService` | Finds lost ↔ found matches by category and shared keywords | Plain Java logic, unit-tested |
| `logic/Validators` | Rules for every form field | Input validation, unit-tested |
| `logic/Formatters` | "Today", "Yesterday", "16 Nov 2026" | Date formatting |
| `logic/ImageUtils` | Decode, shrink to 1024 px, JPEG-compress below 300 KB, base64, small image cache | `Bitmap`, `ImageDecoder`, `LruCache` |
| `logic/EventDetector` | Decides which notices to show (new claim, decision, new match) | Plain Java logic, unit-tested |
| `logic/NotificationWatcher` | Watches the data, remembers what was already seen (SharedPreferences), sends notices | `LiveData.observeForever` |
| `logic/NotificationHelper` | Creates the channel, checks permission, shows a notification that opens the right screen | `NotificationChannel`, `NotificationCompat`, `PendingIntent` |
| `logic/ThemePrefs` | Saves and applies dark mode | SharedPreferences |
| `ui/Ui` | Shared helpers: category icons, placeholder colours, badges, online check | `ConnectivityManager`, drawables |
| `ui/SkeletonView` | Grey cards that gently pulse while loading | Custom view, `ObjectAnimator` |
| `res/values/` + `values-night/` | Colours, sizes (8 dp grid), strings, themes; night versions for dark mode | **Resource qualifiers**, Material 3 theme |
| `res/drawable/` | All icons as vector drawables | Vector drawables (sharp at any size, tiny APK) |
| `res/anim/` | Slide / fade between screens, fall-down for list items | View animations, `layoutAnimation` |
| `res/mipmap-anydpi-v26/ic_launcher.xml` | Adaptive app icon | Adaptive icons |
| `firestore.rules` | Server-side security for posts, claims, chat | Firebase Security Rules |
| `app/build.gradle` | SDK versions, dependencies, signing, test settings | Gradle build |

## Tests (41, all run on the laptop)

| File | Count | What it checks |
|------|-------|----------------|
| `logic/MatchServiceTest` | 6 | keywords, matching rules, returned posts skipped, best match first |
| `logic/ValidatorsTest` | 3 | email, password, title, description, proof question, chat message |
| `logic/EventDetectorTest` | 4 | new claim, approval only when the status changed, no old news on a new phone, matches from someone else |
| `logic/FormattersTest` | 1 | Today / Yesterday / dates |
| `logic/ImageAndModelTest` | 2 | photo below 300 KB and 1024 px; post and claim survive a Firestore round trip |
| `data/MockRepositoriesTest` | 4 | sample data, claim → approve → chat, edit / returned / delete, wrong password |
| `ui/ScreensTest` | 12 | real screens with Espresso: login, sign up, home filters, demo menu, matches, claim, approve, chat, post form, My posts, notification, dark mode |
| `ui/SmallScreenTest` | 1 | every screen on a 320 × 568 phone; fails if anything is cut off |
| `ui/ScreenshotTest` | 8 | draws the 25 screenshots (only with `-Pscreenshots`) |

Run: `./gradlew testDebugUnitTest`. **Robolectric** runs the Android framework on the laptop's JVM, so
no phone or emulator is needed. **Espresso** clicks and types like a user.

## Likely viva questions

**1. Why Java and not Kotlin or Flutter?**
The faculty asked for Java. (Our first version was Flutter; it is kept in git history as tag
`flutter-phase1`.) Java with XML layouts is also the classic way the Android framework is taught.

**2. What is the difference between an Activity and a Fragment? Where do you use each?**
An Activity is a full screen with its own lifecycle and entry in the back stack. A Fragment is a
reusable part of a screen that lives inside an Activity. Our full screens (login, detail, post form,
chat) are Activities. The four tabs are Fragments inside `MainActivity`, shown and hidden so each tab
keeps its scroll position.

**3. Explain the Activity lifecycle with an example from your app.**
`onCreate` → `onStart` → `onResume` (visible and interactive) → `onPause` → `onStop` →
`onDestroy`. Example: the chat's messages LiveData starts its Firestore listener when the screen
becomes active and removes it when it stops, so a closed chat uses no data or battery. When the
phone rotates, the Activity is destroyed and recreated; `AddItemViewModel` keeps the half-filled form.

**4. What is a ViewModel and why use it?**
An object that survives configuration changes like rotation. Without it, the photo and step of the
post form would be lost when you turn the phone.

**5. What is LiveData?**
An observable value that knows the lifecycle of whoever is watching. Screens observe the feed and
claims; when data changes they redraw. It never calls a screen that has been stopped, which avoids crashes.

**6. How does RecyclerView work, and why ListAdapter / DiffUtil?**
RecyclerView only creates enough views to fill the screen and reuses them while scrolling
(ViewHolder pattern). `ListAdapter` uses `DiffUtil` to compare the old and new lists and update only
the cards that changed, with animations, instead of redrawing everything.

**7. How are explicit and implicit Intents used?**
Explicit: `ItemDetailActivity.newIntent(context, itemId)` opens our own screen with the post id as an
extra. Implicit: opening the system notification settings, and the camera (`TakePicture`), where
Android picks the app that can handle the action.

**8. How do you take a photo and where is it stored?**
Activity Result APIs: `TakePicture` writes the photo to a file shared through a **FileProvider**
(safe `content://` URI, not a raw file path), or `PickVisualMedia` opens the photo picker. On a
background thread we shrink it to 1024 px and JPEG-compress below 300 KB, then save it as **base64
text inside the Firestore post**. That avoids Firebase Storage, which needs the paid plan.
A Firestore document can be up to 1 MiB, so ~400 KB of text fits.

**9. How does matching work?**
For a post we look at posts of the **opposite type** (lost ↔ found) in the **same category** that are
not returned. We take keywords from title + description (3+ letters, common words removed, plural
"s" dropped). If they share at least one keyword it is a match; more shared words = higher. It runs
in the app (`MatchService`), so no server is needed, and it is unit-tested.

**10. How do you stop someone claiming an item that is not theirs?**
Found posts have a **proof question** set by the finder (for example "What colour is the sticker
inside the case?"). The claimant must answer it, and only the finder can approve. Chat opens only
after approval. The claim id is `itemId_claimantId`, so each student can claim a post only once.

**11. How do notifications work without a server?**
They are **local notifications**. A watcher observes the data the app already receives from
Firestore, compares it with what it saw last time (saved in SharedPreferences) and shows a
notification for a new claim, a decision, or a new match. Android 8+ needs a **notification
channel**; Android 13+ needs the `POST_NOTIFICATIONS` **runtime permission**. If permission is denied
we show an in-app banner instead. Limitation: nothing arrives if the app is fully closed; that would
need Firebase Cloud Messaging and a server.

**12. Is the data secure? Couldn't someone edit another person's post?**
The app hides those buttons, but an APK can be modified, so the real protection is
**Firestore Security Rules** (`firestore.rules`), checked on Google's servers: only the owner can edit
or delete a post; only the two people in a claim can read it; only the post owner can approve;
chat only after approval. Passwords are handled by Firebase Authentication; we never store them.

**13. What happens when there is no internet?**
Firestore keeps a cache on the phone: posts already loaded still show, and new writes are saved
locally and sent when the network returns. When you post or edit with no internet, the app says it is saved on the phone and will sync,
instead of spinning forever.

**14. How does dark mode work?**
Colours are defined twice: `res/values/colors.xml` and `res/values-night/colors.xml`. Android picks
the right file (**resource qualifiers**). The switch calls `AppCompatDelegate.setDefaultNightMode`
and saves the choice in SharedPreferences, so it is applied again on the next start.

**15. How did you test it without a phone?**
41 automated tests run on the laptop. Pure logic (matching, validation) uses JUnit. Screens use
**Robolectric**, which runs the Android framework on the JVM, and **Espresso**, which taps and types
like a user. One test opens every screen on a small 320 × 568 phone and fails if anything is cut off.
Another draws the screenshots in `docs/screenshots/`.

**16. What is demo / mock mode?**
The same screens with in-memory repositories instead of Firebase, so the app runs with no internet
and no Firebase project. It is switched by `Config.USE_MOCK_DATA`. A yellow strip makes it obvious.
Because screens only talk to interfaces, nothing else changes.

**17. Why minSdk 24 and targetSdk 35?**
minSdk 24 (Android 7.0) covers almost all phones students use. targetSdk 35 (Android 15) is the
current level required by Google Play, and means we follow the newest behaviour rules (for example
the notification permission).

## Honest limitations (say them before you are asked)

- Not yet tested on a real phone or a real Firebase project (only on the laptop with automated tests).
- Notifications need the app open or running in the background (no push server).
- The college-domain check is in the app; there is no email verification.
- Search and matching run on the newest 100 posts, which is enough for one campus.
