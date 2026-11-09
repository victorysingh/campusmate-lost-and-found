import 'dart:async';

import '../models/app_user.dart';
import 'auth_service.dart';

/// In-memory login used in demo mode and in tests. Accounts disappear
/// when the app closes. It mimics Firebase's error messages.
class MockAuthService implements AuthService {
  final Map<String, _MockAccount> _accounts = {}; // key = email
  final _changes = StreamController<AppUser?>.broadcast();
  AppUser? _current;
  int _nextId = 1;

  @override
  Stream<AppUser?> authStateChanges() async* {
    yield _current; // the state right now...
    yield* _changes.stream; // ...then every later change
  }

  @override
  AppUser? get currentUser => _current;

  @override
  Future<void> signUp({
    required String name,
    required String email,
    required String password,
  }) async {
    final key = email.trim().toLowerCase();
    if (_accounts.containsKey(key)) {
      throw const AuthException('An account with this email already exists. Please log in.');
    }
    if (password.length < 6) {
      throw const AuthException('Password is too weak (use at least 6 characters).');
    }
    final user = AppUser(uid: 'demo-user-${_nextId++}', email: key, name: name);
    _accounts[key] = _MockAccount(user, password);
    _setUser(user);
  }

  @override
  Future<void> signIn({required String email, required String password}) async {
    final account = _accounts[email.trim().toLowerCase()];
    if (account == null || account.password != password) {
      throw const AuthException('Wrong email or password.');
    }
    _setUser(account.user);
  }

  @override
  Future<void> signOut() async => _setUser(null);

  void _setUser(AppUser? user) {
    _current = user;
    _changes.add(user);
  }
}

class _MockAccount {
  _MockAccount(this.user, this.password);
  final AppUser user;
  final String password;
}
