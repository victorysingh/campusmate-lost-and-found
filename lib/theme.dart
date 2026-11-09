import 'package:flutter/material.dart';

/// Material 3 themes for the whole app. Every screen takes its colours
/// from here, so the look stays consistent.
class AppTheme {
  static const Color _seed = Colors.indigo;

  static ThemeData light = _build(Brightness.light);
  static ThemeData dark = _build(Brightness.dark);

  static ThemeData _build(Brightness brightness) {
    final colors = ColorScheme.fromSeed(seedColor: _seed, brightness: brightness);
    return ThemeData(
      useMaterial3: true,
      colorScheme: colors,
      inputDecorationTheme: const InputDecorationTheme(
        border: OutlineInputBorder(),
      ),
      cardTheme: const CardThemeData(
        margin: EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      ),
    );
  }
}

/// Colour used for "LOST" labels.
const Color lostColor = Color(0xFFE65100); // deep orange

/// Colour used for "FOUND" labels.
const Color foundColor = Color(0xFF2E7D32); // green
