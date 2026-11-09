import 'package:campusmate/app.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  testWidgets('App starts on the login screen', (tester) async {
    await tester.pumpWidget(const CampusmateApp(demoMode: true));

    expect(find.text('Campusmate'), findsOneWidget);
    expect(find.text('Log in'), findsOneWidget);
    expect(find.textContaining('DEMO MODE'), findsOneWidget);
  });
}
