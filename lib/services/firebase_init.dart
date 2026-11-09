import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/foundation.dart';

/// Tries to start Firebase. Returns true if it worked.
///
/// If it returns false the app runs in DEMO MODE: login and posts are kept
/// in memory only (see the mock_* services). This lets the team run the UI
/// before `flutterfire configure` has been done.
///
/// On Android, Firebase.initializeApp() with no options reads
/// android/app/google-services.json (added by `flutterfire configure`).
/// The web build always uses demo mode (see docs/DECISIONS.md).
Future<bool> initFirebase() async {
  if (kIsWeb) return false;
  try {
    await Firebase.initializeApp().timeout(const Duration(seconds: 10));
    return true;
  } catch (e) {
    debugPrint('Firebase is not configured, starting in demo mode. ($e)');
    return false;
  }
}
