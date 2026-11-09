import '../config.dart';

// Form validators. Each returns null when the value is OK, or an error
// message that the TextFormField shows under the box.

/// True if [email] is allowed to use the app.
/// An empty [domain] means every email is allowed.
bool isAllowedEmail(String email, {String domain = ALLOWED_EMAIL_DOMAIN}) {
  if (domain.isEmpty) return true;
  return email.trim().toLowerCase().endsWith('@${domain.toLowerCase()}');
}

String? validateEmail(String? value) {
  final email = (value ?? '').trim();
  if (email.isEmpty) return 'Enter your email';
  if (!RegExp(r'^[^@\s]+@[^@\s]+\.[^@\s]+$').hasMatch(email)) {
    return 'Enter a valid email';
  }
  if (!isAllowedEmail(email)) return 'Use your college email (@$ALLOWED_EMAIL_DOMAIN)';
  return null;
}

String? validatePassword(String? value) {
  if ((value ?? '').length < 6) return 'Password must be at least 6 characters';
  return null;
}

String? validateRequired(String? value, String fieldName) {
  if ((value ?? '').trim().isEmpty) return 'Enter $fieldName';
  return null;
}
