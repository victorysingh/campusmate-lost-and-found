import 'package:flutter/material.dart';

/// Light / dark mode switch (Profile screen and the debug menu).
/// Starts in light mode. The choice is not saved, so it resets on restart
/// (see docs/DECISIONS.md).
class ThemeProvider extends ChangeNotifier {
  ThemeMode _mode = ThemeMode.light;

  ThemeMode get mode => _mode;
  bool get isDark => _mode == ThemeMode.dark;

  void setDark(bool dark) {
    _mode = dark ? ThemeMode.dark : ThemeMode.light;
    notifyListeners();
  }

  void toggle() => setDark(!isDark);
}
