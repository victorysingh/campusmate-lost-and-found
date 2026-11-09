import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../providers/auth_provider.dart';
import 'home_screen.dart';
import 'login_screen.dart';

/// First widget of the app. Shows Home when someone is logged in, else
/// Login. Because it watches AuthProvider, logging in or out switches the
/// screen automatically; no Navigator call is needed.
class AuthGate extends StatelessWidget {
  const AuthGate({super.key});

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthProvider>();
    if (!auth.isReady) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }
    return auth.user == null ? const LoginScreen() : const HomeScreen();
  }
}
