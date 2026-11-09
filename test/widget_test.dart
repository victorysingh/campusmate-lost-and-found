import 'package:campusmate/app.dart';
import 'package:campusmate/services/mock_auth_service.dart';
import 'package:campusmate/services/mock_item_service.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

/// Starts the app with the in-memory services (no Firebase in tests).
Future<void> startApp(WidgetTester tester) async {
  // Phone-sized screen (360 x 800 logical pixels) instead of the default 800 x 600.
  tester.view.physicalSize = const Size(1080, 2400);
  tester.view.devicePixelRatio = 3;
  addTearDown(tester.view.reset);

  await tester.pumpWidget(CampusmateApp(
    authService: MockAuthService(),
    itemService: MockItemService(),
  ));
  await tester.pumpAndSettle();
}

Future<void> signUp(WidgetTester tester) async {
  await tester.tap(find.text('New here? Create an account'));
  await tester.pumpAndSettle();
  await tester.enterText(find.widgetWithText(TextFormField, 'Full name'), 'Asha');
  await tester.enterText(
      find.widgetWithText(TextFormField, 'College email'), 'asha@college.edu');
  await tester.enterText(
      find.widgetWithText(TextFormField, 'Password (min 6 characters)'), 'secret1');
  await tester.enterText(
      find.widgetWithText(TextFormField, 'Confirm password'), 'secret1');
  await tester.tap(find.text('Sign up'));
  await tester.pumpAndSettle();
}

void main() {
  testWidgets('App starts on the login screen in demo mode', (tester) async {
    await startApp(tester);

    expect(find.text('Campusmate'), findsOneWidget);
    expect(find.text('Log in'), findsOneWidget);
    expect(find.textContaining('DEMO MODE'), findsOneWidget);
  });

  testWidgets('Login form shows errors for empty fields', (tester) async {
    await startApp(tester);

    await tester.tap(find.text('Log in'));
    await tester.pumpAndSettle();

    expect(find.text('Enter your email'), findsOneWidget);
    expect(find.text('Password must be at least 6 characters'), findsOneWidget);
  });

  testWidgets('Sign up leads to an empty feed', (tester) async {
    await startApp(tester);
    await signUp(tester);

    expect(find.text('Post item'), findsOneWidget);
    expect(find.textContaining('No lost items yet'), findsOneWidget);
  });

  testWidgets('Sample items appear in the feed and detail shows a match',
      (tester) async {
    await startApp(tester);
    await signUp(tester);

    // Menu → Add 10 sample items
    await tester.tap(find.byType(PopupMenuButton<String>));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Add 10 sample items'));
    await tester.pumpAndSettle();

    // Newest lost post is at the top of the Lost tab.
    expect(find.text('Steel water bottle'), findsOneWidget);

    // Search filters the list.
    await tester.enterText(find.byType(TextField).first, 'charger');
    await tester.pumpAndSettle();
    expect(find.text('Steel water bottle'), findsNothing);
    expect(find.text('HP laptop charger'), findsOneWidget);

    // Open the post: its found counterpart is listed as a match.
    await tester.tap(find.text('HP laptop charger'));
    await tester.pumpAndSettle();
    expect(find.text('Possible matches (1)'), findsOneWidget);
    expect(find.text('Laptop charger in library'), findsOneWidget);
  });

  testWidgets('Post a new item and see its match', (tester) async {
    await startApp(tester);
    await signUp(tester);
    await tester.tap(find.byType(PopupMenuButton<String>));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Add 10 sample items'));
    await tester.pumpAndSettle();

    // Switch to the Found tab, so the form opens with "found" selected.
    await tester.tap(find.text('Found'));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Post item'));
    await tester.pumpAndSettle();

    await tester.enterText(find.widgetWithText(TextFormField, 'Title'), 'Steel bottle');
    await tester.tap(find.text('Category'));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Water Bottle').last);
    await tester.pumpAndSettle();
    await tester.enterText(
        find.widgetWithText(TextFormField, 'Description'), 'Silver bottle with stickers');
    await tester.enterText(find.widgetWithText(TextFormField, 'Location'), 'Gym');
    // The form is longer than the screen: scroll the form's list down to the button.
    await tester.scrollUntilVisible(find.text('Post'), 200,
        scrollable: find.byType(Scrollable).first);
    await tester.tap(find.text('Post'));
    await tester.pumpAndSettle();

    // We land on the new post's detail page with the sample lost bottle as a match.
    expect(find.text('Found item'), findsOneWidget);
    expect(find.text('Possible matches (1)'), findsOneWidget);
    expect(find.text('Steel water bottle'), findsOneWidget);
  });
}
