import 'package:flutter/material.dart';

/// Yellow strip shown when Firebase is not configured, so nobody mistakes
/// the in-memory demo for the real app during a review.
class DemoModeBanner extends StatelessWidget {
  const DemoModeBanner({super.key});

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      color: Colors.amber.shade200,
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      child: const Text(
        'DEMO MODE: Firebase not configured. Data is kept in memory and '
        'is lost when the app closes.',
        style: TextStyle(color: Colors.black87, fontSize: 12),
      ),
    );
  }
}
