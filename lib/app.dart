import 'package:flutter/material.dart';

import 'config.dart';
import 'routes.dart';
import 'screens/login_screen.dart';
import 'theme.dart';

/// Root widget: theme, routes and the first screen.
class CampusmateApp extends StatelessWidget {
  const CampusmateApp({super.key, required this.demoMode});

  /// True when Firebase is not configured (in-memory data only).
  final bool demoMode;

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: appName,
      debugShowCheckedModeBanner: false,
      theme: AppTheme.light,
      darkTheme: AppTheme.dark,
      themeMode: ThemeMode.system,
      home: LoginScreen(demoMode: demoMode),
      routes: appRoutes,
    );
  }
}
