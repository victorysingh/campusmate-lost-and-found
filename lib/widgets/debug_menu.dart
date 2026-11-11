import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../providers/auth_provider.dart';
import '../providers/item_provider.dart';
import '../providers/theme_provider.dart';
import '../routes.dart';

/// Hidden demo menu, opened by LONG-PRESSING the logo on the Home screen.
/// Lets the presenter prepare the app live without needing the internet.
void showDebugMenu(BuildContext context) {
  showModalBottomSheet<void>(
    context: context,
    showDragHandle: true,
    isScrollControlled: true, // may be taller than the default half-screen limit
    builder: (sheetContext) {
      final items = context.read<ItemProvider>();
      final messenger = ScaffoldMessenger.of(context);

      void done(String message) {
        Navigator.pop(sheetContext);
        messenger.showSnackBar(SnackBar(content: Text(message)));
      }

      // Scrolls on very small phones instead of overflowing.
      return SafeArea(
        child: SingleChildScrollView(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const ListTile(
                title: Text('Demo menu', style: TextStyle(fontWeight: FontWeight.w600)),
                subtitle: Text('For live presentations'),
              ),
              ListTile(
                leading: const Icon(Icons.playlist_add_rounded),
                title: const Text('Load sample data'),
                subtitle: const Text('Adds 12 realistic posts'),
                onTap: () async {
                  await items.addSampleItems(context.read<AuthProvider>().user!);
                  done('Added 12 sample posts');
                },
              ),
              ListTile(
                leading: const Icon(Icons.delete_sweep_rounded),
                title: const Text('Reset data'),
                subtitle: Text(
                  items.isMock
                      ? 'Removes every post (mock mode only)'
                      : 'Disabled: never wipes the real database',
                ),
                enabled: items.isMock,
                onTap: () {
                  items.resetData();
                  done('All posts removed');
                },
              ),
              ListTile(
                leading: const Icon(Icons.dark_mode_rounded),
                title: const Text('Toggle theme'),
                onTap: () {
                  context.read<ThemeProvider>().toggle();
                  Navigator.pop(sheetContext);
                },
              ),
              ListTile(
                leading: const Icon(Icons.notifications_active_rounded),
                title: const Text('Simulate match notification'),
                onTap: () {
                  Navigator.pop(sheetContext);
                  _showFakeMatch(context);
                },
              ),
            ],
          ),
        ),
      );
    },
  );
}

/// Shows an in-app banner as if a new match had just arrived.
/// (Phase 2 adds real system notifications via flutter_local_notifications.)
void _showFakeMatch(BuildContext context) {
  final messenger = ScaffoldMessenger.of(context);
  final pair = context.read<ItemProvider>().anyMatchPair();
  if (pair == null) {
    messenger.showSnackBar(
      const SnackBar(content: Text('No matching posts yet. Load sample data first.')),
    );
    return;
  }
  final (lost, found) = pair;
  messenger.showMaterialBanner(
    MaterialBanner(
      leading: const Icon(Icons.notifications_active_rounded),
      content: Text('Possible match! "${found.title}" may be your "${lost.title}".'),
      actions: [
        TextButton(onPressed: messenger.hideCurrentMaterialBanner, child: const Text('Dismiss')),
        TextButton(
          onPressed: () {
            messenger.hideCurrentMaterialBanner();
            Navigator.pushNamed(context, AppRoutes.itemDetail, arguments: lost);
          },
          child: const Text('View'),
        ),
      ],
    ),
  );
}
