import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import 'config.dart';
import 'providers/auth_provider.dart';
import 'providers/item_provider.dart';
import 'providers/theme_provider.dart';
import 'routes.dart';
import 'screens/auth_gate.dart';
import 'services/auth_service.dart';
import 'services/item_service.dart';
import 'theme.dart';

/// Root widget: providers, theme, routes and the first screen.
///
/// The services are passed in (instead of created here) so main.dart can
/// choose Firebase or mock mode, and tests can pass the in-memory versions.
class CampusmateApp extends StatelessWidget {
  const CampusmateApp({super.key, required this.authService, required this.itemService});

  final AuthService authService;
  final ItemService itemService;

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => ThemeProvider()),
        ChangeNotifierProvider(create: (_) => AuthProvider(authService)),
        // ItemProvider depends on AuthProvider: whenever the logged-in user
        // changes, `update` runs and tells ItemProvider who it is.
        ChangeNotifierProxyProvider<AuthProvider, ItemProvider>(
          create: (_) => ItemProvider(itemService),
          update: (_, auth, items) => items!..setUser(auth.user),
        ),
      ],
      // Consumer rebuilds MaterialApp when the theme switch changes.
      child: Consumer<ThemeProvider>(
        builder: (context, theme, _) => MaterialApp(
          title: appName,
          debugShowCheckedModeBanner: false,
          theme: AppTheme.light,
          darkTheme: AppTheme.dark,
          themeMode: theme.mode,
          home: const AuthGate(),
          routes: appRoutes,
        ),
      ),
    );
  }
}
