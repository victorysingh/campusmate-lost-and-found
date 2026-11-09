# Campusmate – Campus Lost & Found

A Flutter app for the MAD lab mini project. Students post **lost** or
**found** items with a photo, category, description and location. The app
suggests possible matches between lost and found posts. Only college email
addresses can log in.

**Stack:** Flutter (Material 3) · Firebase Auth (email/password) · Cloud
Firestore · Provider. No Firebase Storage or Cloud Functions (both need
the paid plan). Photos are compressed to under 300 KB and stored as base64
text inside the Firestore document.

---

## 1. Run it right now (demo mode, no Firebase needed)

```bash
flutter pub get
flutter run              # on an Android phone/emulator
flutter run -d chrome    # or quickly in Chrome
```

Until Firebase is configured the app shows a yellow **DEMO MODE** banner.
Accounts and posts are then kept in memory only and disappear when the app
closes. That is expected.

## 2. Connect Firebase (one-time, team lead)

1. Go to <https://console.firebase.google.com> and click **Create a project**.
   Name it `campusmate`. Google Analytics is not needed.
2. **Build → Authentication → Get started → Sign-in method → Email/Password →
   Enable → Save.**
3. **Build → Firestore Database → Create database.** Pick location
   `asia-south1 (Mumbai)` and start in **production mode**. Then open the
   **Rules** tab, paste the rules below and click **Publish**. They allow
   only logged-in users. Phase 3 replaces them with stricter rules.

   ```
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /{document=**} {
         allow read, write: if request.auth != null;
       }
     }
   }
   ```

4. Install the command-line tools once (Node.js is required):

   ```bash
   npm install -g firebase-tools
   dart pub global activate flutterfire_cli
   firebase login
   ```

5. Inside this project folder run:

   ```bash
   flutterfire configure --project=<your-firebase-project-id> --platforms=android
   ```

   This creates `android/app/google-services.json` (plus
   `lib/firebase_options.dart` and `firebase.json`) and adds the Google
   Services Gradle plugin. The three generated files are **git-ignored**.
   Never commit them.
6. Run the app on Android. The DEMO MODE banner should be gone.

**Other teammates:** clone the repo and get `google-services.json` from the
team lead privately (WhatsApp/Drive). Put it at
`android/app/google-services.json`. You can also run step 5 yourself if you
were added to the Firebase project.

## 3. Restrict login to the college domain

Edit `lib/config.dart`:

```dart
const String ALLOWED_EMAIL_DOMAIN = 'yourcollege.edu.in';
```

Empty (`''`) means any email is allowed. That is the default, for testing.

## 4. Checks

```bash
flutter analyze
flutter test
```

## 5. Folder structure

```
lib/
  main.dart            entry point: start Firebase or demo mode
  app.dart             MaterialApp: theme, routes, providers
  config.dart          values the team may change (email domain, categories)
  theme.dart           Material 3 light/dark theme
  routes.dart          named routes
  models/              plain data classes (Item, AppUser)
  services/            talks to Firebase (or the in-memory mock)
  providers/           app state (Provider / ChangeNotifier)
  screens/             one file per screen
  widgets/             small reusable widgets
  utils/               validators, date formatting
test/                  unit + widget tests
docs/                  DECISIONS.md, later VIVA_NOTES.md and DEMO_SCRIPT.md
```

See `docs/DECISIONS.md` for why things are done the way they are.
