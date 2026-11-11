import 'package:campusmate/app.dart';
import 'package:campusmate/services/mock_auth_service.dart';
import 'package:campusmate/services/mock_item_service.dart';
import 'package:campusmate/widgets/app_logo.dart';
import 'package:campusmate/widgets/item_card.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

/// Starts the app with the in-memory services (no Firebase in tests) on a
/// phone-sized screen. [width] x [height] are logical pixels.
Future<void> startApp(WidgetTester tester, {double width = 390, double height = 844}) async {
  tester.view.physicalSize = Size(width * 3, height * 3);
  tester.view.devicePixelRatio = 3;
  addTearDown(tester.view.reset);

  await tester.pumpWidget(CampusmateApp(
    authService: MockAuthService(),
    itemService: MockItemService(loadDelay: Duration.zero),
  ));
  await tester.pumpAndSettle();
}

Future<void> signUp(WidgetTester tester) async {
  await tester.tap(find.text('New here? Create an account'));
  await tester.pumpAndSettle();
  await tester.enterText(find.widgetWithText(TextFormField, 'Full name'), 'Asha Rao');
  await tester.enterText(
      find.widgetWithText(TextFormField, 'College email'), 'asha@college.edu');
  await tester.enterText(
      find.widgetWithText(TextFormField, 'Password (min 6 characters)'), 'secret1');
  await tester.enterText(find.widgetWithText(TextFormField, 'Confirm password'), 'secret1');
  await tester.ensureVisible(find.text('Sign up'));
  await tester.tap(find.text('Sign up'));
  await tester.pumpAndSettle();
}

/// Long-press the logo → hidden demo menu → Load sample data.
Future<void> loadSamples(WidgetTester tester) async {
  await tester.longPress(find.byType(AppLogo));
  await tester.pumpAndSettle();
  await tester.tap(find.text('Load sample data'));
  await tester.pumpAndSettle();
  // Wait for the "Added 12 sample posts" snackbar to go, so it covers nothing.
  await tester.pump(const Duration(seconds: 5));
  await tester.pumpAndSettle();
}

/// Picks [value] in the dropdown whose label is [label].
Future<void> chooseDropdown(WidgetTester tester, String label, String value) async {
  await tester.tap(find.text(label));
  await tester.pumpAndSettle();
  await tester.tap(find.text(value).last);
  await tester.pumpAndSettle();
}

/// Goes through the 3-step form and posts a found water bottle.
Future<void> postFoundBottle(WidgetTester tester) async {
  await tester.tap(find.byTooltip('Post an item'));
  await tester.pumpAndSettle();
  await tester.tap(find.text('Next')); // step 1: photo is optional
  await tester.pumpAndSettle();

  await tester.tap(find.text('I found it')); // step 2: details
  await tester.enterText(find.widgetWithText(TextFormField, 'Title'), 'Steel bottle');
  await chooseDropdown(tester, 'Category', 'Water Bottle');
  await tester.enterText(
      find.widgetWithText(TextFormField, 'Description'), 'Silver steel bottle with stickers');
  await scrollAndTap(tester, 'Next');

  await chooseDropdown(tester, 'Where on campus?', 'Basketball court'); // step 3
  await scrollAndTap(tester, 'Post');
}

/// Scrolls the form until the button with [label] is on screen, then taps it.
/// (On small phones the form is longer than the screen.)
Future<void> scrollAndTap(WidgetTester tester, String label) async {
  // scrollUntilVisible stops once the button is *built*; ensureVisible then
  // brings it fully on screen.
  await tester.scrollUntilVisible(find.text(label), 100,
      scrollable: find.byType(Scrollable).first);
  await tester.ensureVisible(find.text(label));
  await tester.pumpAndSettle();
  await tester.tap(find.text(label));
  await tester.pumpAndSettle();
}

void main() {
  testWidgets('App starts on the login screen in mock mode', (tester) async {
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

  testWidgets('Sign up leads to a greeting and an empty feed', (tester) async {
    await startApp(tester);
    await signUp(tester);

    expect(find.textContaining(', Asha'), findsOneWidget); // "Good morning, Asha"
    expect(find.text('No lost items yet'), findsOneWidget);
  });

  testWidgets('Sample data: search, category chip and matches on detail', (tester) async {
    await startApp(tester);
    await signUp(tester);
    await loadSamples(tester);

    expect(find.text('Lost (6)'), findsOneWidget);
    expect(find.text('Found (6)'), findsOneWidget);

    // Category chip filters the list.
    await tester.ensureVisible(find.widgetWithText(ChoiceChip, 'Wallet')); // chips scroll sideways
    await tester.pumpAndSettle();
    await tester.tap(find.widgetWithText(ChoiceChip, 'Wallet'));
    await tester.pumpAndSettle();
    expect(find.text('Lost (1)'), findsOneWidget);
    // Tapping the selected chip again clears the filter.
    await tester.tap(find.widgetWithText(ChoiceChip, 'Wallet'));
    await tester.pumpAndSettle();
    expect(find.text('Lost (6)'), findsOneWidget);

    // Search filters the list.
    await tester.enterText(find.byType(TextField).first, 'airpods');
    await tester.pumpAndSettle();
    expect(find.text('Apple AirPods Pro'), findsOneWidget);

    // Detail page lists the found AirPods as a match.
    await tester.tap(find.text('Apple AirPods Pro'));
    await tester.pumpAndSettle();
    expect(find.text('Possible matches (1)'), findsOneWidget);
    expect(find.text('AirPods case near reading tables'), findsOneWidget);
    expect(find.text('I found this'), findsOneWidget); // someone else's lost post
  });

  testWidgets('Post a found item with the stepper and see its match', (tester) async {
    await startApp(tester);
    await signUp(tester);
    await loadSamples(tester);
    await postFoundBottle(tester);

    // We land on the new post's detail page, with the lost bottle as a match.
    expect(find.text('Steel bottle'), findsOneWidget);
    expect(find.text('Possible matches (1)'), findsOneWidget);
    expect(find.text('Steel water bottle'), findsOneWidget);
  });

  testWidgets('Stepper blocks "Next" until details are filled', (tester) async {
    await startApp(tester);
    await signUp(tester);
    await tester.tap(find.byTooltip('Post an item'));
    await tester.pumpAndSettle();
    await tester.tap(find.text('Next'));
    await tester.pumpAndSettle();
    await scrollAndTap(tester, 'Next');

    expect(find.text('Enter a title'), findsOneWidget);
    expect(find.text('Choose a category'), findsOneWidget);
  });

  testWidgets('Profile: dark mode switch and log out', (tester) async {
    await startApp(tester);
    await signUp(tester);
    await tester.tap(find.text('Profile'));
    await tester.pumpAndSettle();

    await tester.tap(find.byType(Switch));
    await tester.pumpAndSettle();
    expect(Theme.of(tester.element(find.text('Dark mode'))).brightness, Brightness.dark);

    await tester.tap(find.text('Log out'));
    await tester.pumpAndSettle();
    expect(find.text('Log in'), findsOneWidget);
  });

  // Flutter fails a test automatically if any layout overflows, so simply
  // visiting every screen on a small phone (iPhone SE size) checks for it.
  testWidgets('Every screen fits a small 320 x 568 phone', (tester) async {
    await startApp(tester, width: 320, height: 568);
    await signUp(tester);
    await loadSamples(tester);
    await tester.enterText(find.byType(TextField).first, 'airpods');
    await tester.pumpAndSettle();
    // On this short screen the + button covers the card's centre,
    // so tap near the card's top-left corner (its photo).
    await tester.tapAt(tester.getTopLeft(find.byType(ItemCard)) + const Offset(24, 24));
    await tester.pumpAndSettle();
    await tester.pageBack();
    await tester.pumpAndSettle();
    await postFoundBottle(tester);
    await tester.pageBack();
    await tester.pumpAndSettle();
    await tester.tap(find.text('Profile'));
    await tester.pumpAndSettle();

    expect(find.text('Dark mode'), findsOneWidget);
  });
}
