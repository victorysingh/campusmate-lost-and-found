import 'package:flutter/material.dart';

import 'app.dart';
import 'services/firebase_auth_service.dart';
import 'services/firebase_init.dart';
import 'services/firestore_item_service.dart';
import 'services/mock_auth_service.dart';
import 'services/mock_item_service.dart';

/// Entry point. Starts Firebase, or falls back to in-memory demo mode,
/// and then shows the app.
Future<void> main() async {
  // Needed before calling plugins (Firebase) ahead of runApp.
  WidgetsFlutterBinding.ensureInitialized();
  final firebaseReady = await initFirebase();

  runApp(CampusmateApp(
    authService: firebaseReady ? FirebaseAuthService() : MockAuthService(),
    itemService: firebaseReady ? FirestoreItemService() : MockItemService(),
  ));
}
