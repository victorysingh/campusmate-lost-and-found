import '../models/app_user.dart';

/// What the app needs from a login system. There are two versions:
///  * FirebaseAuthService – the real one (Firebase Authentication)
///  * MockAuthService     – in-memory, used in demo mode and in tests
abstract class AuthService {
  /// Emits the current user now, and again on every login / logout.
  Stream<AppUser?> authStateChanges();

  AppUser? get currentUser;

  Future<void> signUp({
    required String name,
    required String email,
    required String password,
  });

  Future<void> signIn({required String email, required String password});

  Future<void> signOut();
}

/// Error with a message that is safe to show to the user.
class AuthException implements Exception {
  const AuthException(this.message);
  final String message;

  @override
  String toString() => message;
}
