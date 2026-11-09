import 'package:flutter/material.dart';

import 'app.dart';
import 'services/firebase_init.dart';

/// Entry point. Starts Firebase (or falls back to demo mode) and then
/// shows the app.
Future<void> main() async {
  // Needed before calling plugins (Firebase) ahead of runApp.
  WidgetsFlutterBinding.ensureInitialized();
  final firebaseReady = await initFirebase();
  runApp(CampusmateApp(demoMode: !firebaseReady));
}
