import 'package:firebase_auth/firebase_auth.dart';

import '../models/app_user.dart';
import 'auth_service.dart';

/// Real login using Firebase Authentication (email + password).
class FirebaseAuthService implements AuthService {
  final FirebaseAuth _auth = FirebaseAuth.instance;

  AppUser? _toAppUser(User? user) {
    if (user == null) return null;
    final email = user.email ?? '';
    return AppUser(
      uid: user.uid,
      email: email,
      // displayName is set at sign-up; fall back to the part before '@'.
      name: user.displayName ?? email.split('@').first,
    );
  }

  @override
  Stream<AppUser?> authStateChanges() => _auth.authStateChanges().map(_toAppUser);

  @override
  AppUser? get currentUser => _toAppUser(_auth.currentUser);

  @override
  Future<void> signUp({
    required String name,
    required String email,
    required String password,
  }) async {
    try {
      final result = await _auth.createUserWithEmailAndPassword(
          email: email, password: password);
      await result.user!.updateDisplayName(name);
      await result.user!.reload(); // so currentUser has the new name
    } on FirebaseAuthException catch (e) {
      throw AuthException(_friendlyMessage(e));
    }
  }

  @override
  Future<void> signIn({required String email, required String password}) async {
    try {
      await _auth.signInWithEmailAndPassword(email: email, password: password);
    } on FirebaseAuthException catch (e) {
      throw AuthException(_friendlyMessage(e));
    }
  }

  @override
  Future<void> signOut() => _auth.signOut();

  /// Turns Firebase error codes into short messages for the user.
  String _friendlyMessage(FirebaseAuthException e) {
    switch (e.code) {
      case 'email-already-in-use':
        return 'An account with this email already exists. Please log in.';
      case 'invalid-email':
        return 'That email address is not valid.';
      case 'weak-password':
        return 'Password is too weak (use at least 6 characters).';
      case 'user-not-found':
      case 'wrong-password':
      case 'invalid-credential':
        return 'Wrong email or password.';
      case 'too-many-requests':
        return 'Too many attempts. Please wait a minute and try again.';
      case 'network-request-failed':
        return 'No internet connection.';
      default:
        return e.message ?? 'Login failed (${e.code}).';
    }
  }
}
