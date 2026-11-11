import 'package:flutter/material.dart';

import '../routes.dart';
import 'home_screen.dart';
import 'profile_screen.dart';

/// The logged-in app: bottom navigation bar with a centred "Post" button.
/// IndexedStack keeps each tab alive, so switching tabs keeps the scroll
/// position and the search text.
class MainShell extends StatefulWidget {
  const MainShell({super.key});

  @override
  State<MainShell> createState() => _MainShellState();
}

class _MainShellState extends State<MainShell> {
  int _tab = 0; // 0 = Home, 1 = Profile

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: IndexedStack(
        index: _tab,
        children: const [HomeScreen(), ProfileScreen()],
      ),
      floatingActionButton: FloatingActionButton(
        tooltip: 'Post an item',
        shape: const CircleBorder(),
        onPressed: () => Navigator.pushNamed(context, AppRoutes.addItem),
        child: const Icon(Icons.add_rounded, size: 32),
      ),
      floatingActionButtonLocation: FloatingActionButtonLocation.centerDocked,
      bottomNavigationBar: BottomAppBar(
        shape: const CircularNotchedRectangle(), // the cut-out for the + button
        notchMargin: 8,
        height: 68,
        padding: EdgeInsets.zero,
        child: Row(
          children: [
            _navButton(0, Icons.home_outlined, Icons.home_rounded, 'Home'),
            const SizedBox(width: 72), // space for the centred button
            _navButton(1, Icons.person_outline_rounded, Icons.person_rounded, 'Profile'),
          ],
        ),
      ),
    );
  }

  Widget _navButton(int index, IconData icon, IconData selectedIcon, String label) {
    final selected = _tab == index;
    final color = selected
        ? Theme.of(context).colorScheme.primary
        : Theme.of(context).colorScheme.onSurfaceVariant;
    return Expanded(
      child: InkResponse(
        onTap: () => setState(() => _tab = index),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(selected ? selectedIcon : icon, color: color),
            Text(label,
                style: TextStyle(
                    color: color,
                    fontSize: 12,
                    fontWeight: selected ? FontWeight.w600 : FontWeight.w400)),
          ],
        ),
      ),
    );
  }
}
