import 'package:flutter/material.dart';

import 'app.dart';
import 'config.dart';
import 'services/firebase_auth_service.dart';
import 'services/firebase_init.dart';
import 'services/firestore_item_service.dart';
import 'services/mock_auth_service.dart';
import 'services/mock_item_service.dart';

/// Entry point. Chooses Firebase or mock mode (USE_MOCK_DATA in
/// config.dart), then shows the app.
Future<void> main() async {
  // Needed before calling plugins (Firebase) ahead of runApp.
  WidgetsFlutterBinding.ensureInitialized();

  // Even with USE_MOCK_DATA = false we fall back to mock mode when
  // Firebase is not configured, so the app always starts.
  final useFirebase = !USE_MOCK_DATA && await initFirebase();

  runApp(CampusmateApp(
    authService: useFirebase ? FirebaseAuthService() : MockAuthService(),
    itemService: useFirebase ? FirestoreItemService() : MockItemService(),
  ));
}
