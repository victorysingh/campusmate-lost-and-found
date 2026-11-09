import 'dart:async';

import 'package:flutter/foundation.dart';

import '../config.dart';
import '../models/app_user.dart';
import '../services/auth_service.dart';
import '../services/mock_auth_service.dart';
import '../utils/validators.dart';

/// Holds "who is logged in" for the whole app. Screens read it with
/// `context.watch<AuthProvider>()` and rebuild when it changes.
class AuthProvider extends ChangeNotifier {
  AuthProvider(this._service) {
    _subscription = _service.authStateChanges().listen((user) {
      _user = user;
      _ready = true;
      notifyListeners();
    });
  }

  final AuthService _service;
  late final StreamSubscription<AppUser?> _subscription;

  AppUser? _user;
  bool _ready = false; // false until Firebase tells us the first login state
  bool _busy = false; // true while a login / sign-up request is running

  AppUser? get user => _user;
  bool get isReady => _ready;
  bool get isBusy => _busy;
  bool get isDemoMode => _service is MockAuthService;

  /// Returns null on success, or an error message to show the user.
  Future<String?> signIn(String email, String password) {
    return _run(email, () => _service.signIn(email: email.trim(), password: password));
  }

  /// Returns null on success, or an error message to show the user.
  Future<String?> signUp(String name, String email, String password) {
    return _run(email, () => _service.signUp(
          name: name.trim(),
          email: email.trim(),
          password: password,
        ));
  }

  Future<void> signOut() => _service.signOut();

  /// Shared code for sign-in and sign-up: domain check, busy flag, errors.
  Future<String?> _run(String email, Future<void> Function() action) async {
    // The form already checks the domain; checking again here means no
    // screen can skip it by mistake.
    if (!isAllowedEmail(email)) {
      return 'Only @$ALLOWED_EMAIL_DOMAIN emails can use $appName.';
    }
    _busy = true;
    notifyListeners();
    try {
      await action();
      _user = _service.currentUser; // picks up the display name after sign-up
      return null;
    } on AuthException catch (e) {
      return e.message;
    } catch (e) {
      return 'Something went wrong: $e';
    } finally {
      _busy = false;
      notifyListeners();
    }
  }

  @override
  void dispose() {
    _subscription.cancel();
    super.dispose();
  }
}
