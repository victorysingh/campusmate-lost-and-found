import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

/// Spacing scale: everything is a multiple of 8 px, so gaps look even.
class Space {
  static const double xs = 4;
  static const double s = 8;
  static const double m = 16;
  static const double l = 24;
  static const double xl = 32;
}

/// Corner radius used by cards, buttons and inputs.
const double kRadius = 16;

/// Colours for the LOST / FOUND / RETURNED badges (same in light and dark).
const Color lostColor = Color(0xFFE8590C); // orange
const Color foundColor = Color(0xFF2B8A3E); // green
const Color returnedColor = Color(0xFF5C7CFA); // indigo-blue

/// Material 3 themes for the whole app, built from ONE seed colour.
/// Every screen takes its colours, font and shapes from here.
class AppTheme {
  static const Color seed = Color(0xFF0F8B8D); // teal

  static ThemeData light = _build(Brightness.light);
  static ThemeData dark = _build(Brightness.dark);

  static ThemeData _build(Brightness brightness) {
    // Poppins files are bundled in assets/google_fonts/, so never download.
    GoogleFonts.config.allowRuntimeFetching = false;

    final colors = ColorScheme.fromSeed(seedColor: seed, brightness: brightness);
    final base = ThemeData(useMaterial3: true, colorScheme: colors);
    final rounded = RoundedRectangleBorder(borderRadius: BorderRadius.circular(kRadius));

    return base.copyWith(
      textTheme: GoogleFonts.poppinsTextTheme(base.textTheme),
      scaffoldBackgroundColor: colors.surface,
      appBarTheme: AppBarTheme(
        backgroundColor: colors.surface,
        surfaceTintColor: Colors.transparent,
        centerTitle: false,
        titleTextStyle: GoogleFonts.poppins(
            fontSize: 20, fontWeight: FontWeight.w600, color: colors.onSurface),
      ),
      cardTheme: CardThemeData(
        elevation: 2,
        shadowColor: Colors.black.withValues(alpha: 0.25), // soft shadow
        surfaceTintColor: Colors.transparent,
        color: colors.surfaceContainerLow,
        shape: rounded,
        margin: EdgeInsets.zero,
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: colors.surfaceContainerHighest.withValues(alpha: 0.5),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(kRadius),
          borderSide: BorderSide.none,
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(kRadius),
          borderSide: BorderSide(color: colors.outlineVariant),
        ),
        contentPadding: const EdgeInsets.symmetric(horizontal: Space.m, vertical: Space.m),
      ),
      filledButtonTheme: FilledButtonThemeData(
        style: FilledButton.styleFrom(shape: rounded, minimumSize: const Size(0, 52)),
      ),
      outlinedButtonTheme: OutlinedButtonThemeData(
        style: OutlinedButton.styleFrom(shape: rounded, minimumSize: const Size(0, 52)),
      ),
      chipTheme: ChipThemeData(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(kRadius)),
      ),
      snackBarTheme: SnackBarThemeData(
        behavior: SnackBarBehavior.floating,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      ),
      // Same smooth zoom/fade page transition on every platform.
      pageTransitionsTheme: const PageTransitionsTheme(builders: {
        TargetPlatform.android: ZoomPageTransitionsBuilder(),
        TargetPlatform.iOS: ZoomPageTransitionsBuilder(),
        TargetPlatform.windows: ZoomPageTransitionsBuilder(),
        TargetPlatform.macOS: ZoomPageTransitionsBuilder(),
        TargetPlatform.linux: ZoomPageTransitionsBuilder(),
      }),
    );
  }
}
