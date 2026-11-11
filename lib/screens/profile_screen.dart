import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../providers/auth_provider.dart';
import '../providers/theme_provider.dart';
import '../theme.dart';
import '../widgets/primary_button.dart';

/// Profile tab: who is logged in, dark-mode switch and log out.
class ProfileScreen extends StatelessWidget {
  const ProfileScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthProvider>();
    final theme = context.watch<ThemeProvider>();
    final user = auth.user;
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    if (user == null) return const SizedBox.shrink();

    final initials = user.name
        .split(' ')
        .where((part) => part.isNotEmpty)
        .take(2)
        .map((part) => part[0].toUpperCase())
        .join();

    return Scaffold(
      appBar: AppBar(title: const Text('Profile')),
      body: ListView(
        padding: const EdgeInsets.all(Space.m),
        children: [
          Center(
            child: CircleAvatar(
              radius: 44,
              backgroundColor: colors.primaryContainer,
              child: Text(initials,
                  style: text.headlineMedium?.copyWith(color: colors.onPrimaryContainer)),
            ),
          ),
          const SizedBox(height: Space.m),
          Text(user.name, textAlign: TextAlign.center, style: text.titleLarge),
          Text(user.email,
              textAlign: TextAlign.center,
              style: text.bodyMedium?.copyWith(color: colors.onSurfaceVariant)),
          const SizedBox(height: Space.l),
          Card(
            child: Column(
              children: [
                SwitchListTile(
                  secondary: const Icon(Icons.dark_mode_rounded),
                  title: const Text('Dark mode'),
                  value: theme.isDark,
                  onChanged: theme.setDark,
                ),
                const Divider(height: 1),
                ListTile(
                  leading: const Icon(Icons.storage_rounded),
                  title: const Text('Data source'),
                  subtitle: Text(auth.isDemoMode
                      ? 'Mock mode: kept in memory on this device'
                      : 'Firebase (Cloud Firestore)'),
                ),
              ],
            ),
          ),
          const SizedBox(height: Space.l),
          PrimaryButton(
            label: 'Log out',
            icon: Icons.logout_rounded,
            outlined: true,
            onPressed: () => context.read<AuthProvider>().signOut(),
          ),
        ],
      ),
    );
  }
}
