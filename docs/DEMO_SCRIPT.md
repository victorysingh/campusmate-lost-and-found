# Review 1 demo script (4 minutes, mock mode)

Mock mode needs **no internet and no Firebase**. Everything below was run
in Chrome on 11 Nov 2026 (see `docs/screenshots/`).

## Before the review (5 minutes earlier)

1. `lib/config.dart` → `USE_MOCK_DATA = true` (this is the default).
2. Start the app:
   - **Chrome (laptop):** `flutter run -d chrome`. Press F12 → device toolbar
     (Ctrl+Shift+M) → pick a phone size such as 390 × 844, so it looks like a phone.
   - **Android phone (USB):** `flutter run` (see README → "Run on a phone").
3. Keep a photo ready to pick from the gallery (for example a water bottle).
4. Do **not** sign up yet; the demo starts on the login screen.

> Mock data lives in memory. Closing the app or pressing `R` (hot restart)
> clears it, which is a quick way to reset between run-throughs.

## The flow

| Time | Say | Tap / do |
|------|-----|----------|
| 0:00 | "Campusmate is a lost-and-found app for our campus. Only college emails can join." | Show the **login** screen. Point at the DEMO MODE strip: "this build runs offline for the demo." |
| 0:15 | "New students sign up in one step." | Tap **New here? Create an account** → type name `Asha Rao`, email `asha.rao@college.edu`, password `secret1` twice → tap **Sign up**. |
| 0:35 | "While posts load you see skeleton cards, not a blank screen. Then a friendly empty state." | Watch the shimmer; the Home screen shows **No lost items yet**. |
| 0:45 | "For the demo I'll load 12 realistic campus posts." | **Long-press the teal logo** (top right) → **Load sample data**. |
| 1:00 | "The feed is split into Lost and Found, newest first, with photos or category art." | Scroll a little. Tap **Found (6)**, then **Lost (6)**. |
| 1:15 | "Filter by category with these chips, or search by any word." | Tap the **Electronics** chip, then tap it again to clear. Type `airpods` in the search bar. |
| 1:30 | "Each post opens a detail page. The picture animates in (Hero animation)." | Tap **Apple AirPods Pro**. |
| 1:40 | "The app has already found a possible match: a FOUND post with the same category and shared keywords." | Scroll down to **Possible matches (1)**. Point at **Matched on: airpod, white, case, blue, sticker**. |
| 2:00 | "The finder or owner will claim it from this button; that's our next phase." | Point at **I found this**. Tap ← back. Clear the search (×). |
| 2:10 | "Posting is a 3-step form: photo, details, location." | Tap the round **+** button. Tap **Gallery** → pick the bottle photo. "Photos are compressed below 300 KB and stored in Firestore as text: no paid storage needed." Tap **Next**. |
| 2:35 | "Validation stops empty posts." | Tap **Next** with empty fields → red messages. Then tap **I found it**, Title `Steel bottle`, Category **Water Bottle**, Description `Blue steel bottle with stickers` → **Next**. |
| 3:00 | "Location is picked from campus places." | **Where on campus?** → **Basketball court**, More detail `near the bench` → **Post**. |
| 3:10 | "Saved, and it already matches someone's lost bottle." | The snackbar says **1 possible match found**. Tap **View 1 possible match**. |
| 3:25 | "When a match appears the owner gets alerted." | ← back to Home. Long-press the logo → **Simulate match notification** → tap **View** on the banner. ("Real phone notifications come in Phase 2.") |
| 3:40 | "Profile shows who is logged in and has dark mode." | Bottom bar → **Profile** → turn on **Dark mode** → tap **Home** to show the dark feed. |
| 3:55 | "That's Review 1. Phase 2 adds claims with proof questions, chat, notifications and My Posts." | Done. |

## If something goes wrong

- **Lost track of the data:** long-press the logo → **Reset data**, then **Load sample data**.
- **Stuck in dark mode:** Profile → switch off, or long-press the logo → **Toggle theme**.
- **Photo picker will not open in Chrome:** skip the photo; it is optional, so tap **Next**.
- **Asked "is it saved online?":** "In this demo build it's in memory. Flip `USE_MOCK_DATA`
  to `false` and the same screens use Firebase Auth and Cloud Firestore."
