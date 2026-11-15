# Demo script (about 6 minutes, demo mode)

Demo mode needs **no internet and no Firebase**. Every step below is covered by
the automated screen tests (`ScreensTest`, `ScreenshotTest`).
**It has not been run on a real phone yet**: do one full practice run on the
demo phone before the review.

## Before the review (10 minutes earlier)

1. Install [`releases/campusmate-1.0.0.apk`](../releases/) on the demo phone
   (copy it over and tap it), or connect by USB and run `./gradlew installDebug`.
2. Open the app once. On Android 13+ tap **Allow** when it asks about notifications.
3. Keep a photo in the gallery to pick (for example a water bottle).
4. Close the app fully (swipe it away in recent apps), so the demo starts clean on the
   login screen.
5. Optional: mirror the phone on the projector (e.g. `scrcpy`, or the phone's cast feature).

> Demo data lives in memory. Swiping the app away from recent apps clears everything.
> That is the quickest reset between practice runs.

## Accounts used

| Who | Email | Password |
|-----|-------|----------|
| Asha (new student, signs up live) | `asha.rao@college.edu` | `secret1` |
| Rahul (posted the found AirPods case) | `rahul.kumar@college.edu` | `demo123` |

## The flow

| Time | Say | Tap / do |
|------|-----|----------|
| 0:00 | "Campusmate is a lost-and-found app for our campus, written in Java for Android." | Show the **login** screen. Point at the DEMO MODE strip: "this build runs offline for the demo." |
| 0:15 | "New students sign up in one step. The form checks every field." | **New here? Create an account** → name `Asha Rao`, email `asha.rao@college.edu`, password `secret1` twice → **Sign up**. |
| 0:35 | "While posts load you see skeleton cards, then a friendly empty state." | Watch the skeleton, then **No lost items yet**. |
| 0:45 | "For the demo I'll load 12 realistic campus posts." | **Long-press the teal logo** (top right) → **Load sample data**. |
| 0:55 | "Lost and Found are separate, newest first. Filter by category or search any word." | Tap **Found (6)**, then **Lost (6)**. Tap **Electronics**, tap it again to clear. |
| 1:10 | "Each post opens a detail page, and the app has already found a possible match." | Tap **Apple AirPods Pro** → scroll to **Possible matches (1)** → point at **Matched on: …**. |
| 1:30 | "Matching is opposite type, same category, and shared keywords. Say these are Asha's AirPods; she opens the found post." | Tap the matched card **AirPods case near reading tables**. |
| 1:40 | "To stop strangers taking items, the finder set a question only the owner can answer." | Tap **This is mine** → read the question → answer `Blue, with a small star` → **Send claim**. Snackbar: **Claim sent…** ← back to Home. |
| 2:00 | "Asha also found a bottle on the basketball court. Posting is 3 steps: photo, details, location." | Tap the round **+**. **Gallery** → pick the bottle photo. "Compressed below 300 KB and stored as text in Firestore, so no paid storage." **Next**. |
| 2:25 | "Validation stops empty or half-filled posts." | Tap **Next** with empty fields → red messages. Then **I found it**, Title `Steel bottle`, Category **Water Bottle**, Description `Blue steel bottle with stickers`, proof question `What brand is the bottle?` → **Next**. |
| 3:00 | "Location is picked from campus places." | **Where on campus?** → **Basketball court**, detail `near the bench` → **Post**. |
| 3:15 | "Saved, and it already matches Rahul's lost bottle." | Snackbar **Posted. 1 possible match found!** → tap **View 1 possible match**. |
| 3:30 | "Now I'm Rahul, who found the AirPods case and lost a bottle." | **Profile** → **Log out** → log in as `rahul.kumar@college.edu` / `demo123`. |
| 3:45 | "Rahul is told straight away: a notification, and a badge on Claims." | Show the **New claim** notification (or the banner at the bottom). Tap **Claims**. |
| 4:00 | "He sees Asha's answer and decides. Approve opens a chat between just the two of them." | Read the answer → **Approve** → **Open chat**. |
| 4:15 | "They agree where to meet." | Type `Hi Asha, the sticker matches. Library desk at 4?` → send. |
| 4:30 | "Once it's handed over, the owner closes the post." | Tap the **✓** in the chat bar (Mark as returned) → **Marked as returned. Nice work!** ← back. |
| 4:40 | "My Posts lists everything Rahul posted. His lost bottle already shows Asha's bottle as a match." | **My posts** → tap **Steel water bottle** → **Possible matches (1)**. ← back, tap ⋮ on a post to show edit / returned / delete. |
| 5:05 | "Matches also trigger notifications." | **Home** → long-press the logo → **Simulate match notification** → tap the notification (or **View** on the banner). |
| 5:20 | "Dark mode is remembered." | **Profile** → turn on **Dark mode** → tap **Home**. |
| 5:35 | "With Firebase the same screens use Firebase Auth and Firestore, protected by security rules so a modified app still can't change other people's posts." | Optional: show `firestore.rules` on the laptop. |
| 5:50 | "That's Campusmate. Thank you." | Done. |

## If something goes wrong

- **Lost track of the data:** long-press the logo → **Reset data**, then **Load sample data**.
- **No notification appeared:** notifications may be off. The same message appears as a banner
  at the bottom. Profile → **Notifications** turns them on.
- **Forgot Asha's password:** log in as any demo account with `demo123` (see README).
- **Camera / gallery will not open:** skip the photo; it is optional, so tap **Next**.
- **Asked "is it saved online?":** "In this demo build it's in memory. Set `USE_MOCK_DATA`
  to `false` in `Config.java`, add `google-services.json`, and the same screens use Firebase."
