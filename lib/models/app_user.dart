/// The logged-in student. Kept separate from Firebase's own User class so
/// screens do not depend on Firebase (and the demo mode can create one too).
class AppUser {
  final String uid;
  final String email;
  final String name;

  const AppUser({required this.uid, required this.email, required this.name});
}
