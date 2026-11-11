import 'package:flutter/material.dart';

import '../theme.dart';

/// Friendly "nothing here" / "something went wrong" message:
/// a big icon in a soft circle, a title, a hint and an optional button.
class EmptyState extends StatelessWidget {
  const EmptyState({
    super.key,
    required this.icon,
    required this.title,
    required this.message,
    this.actionLabel,
    this.onAction,
  });

  final IconData icon;
  final String title;
  final String message;
  final String? actionLabel;
  final VoidCallback? onAction;

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(Space.xl),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              width: 112,
              height: 112,
              decoration: BoxDecoration(color: colors.primaryContainer, shape: BoxShape.circle),
              child: Icon(icon, size: 56, color: colors.onPrimaryContainer),
            ),
            const SizedBox(height: Space.m),
            Text(title, style: text.titleMedium, textAlign: TextAlign.center),
            const SizedBox(height: Space.s),
            Text(message,
                textAlign: TextAlign.center,
                style: text.bodyMedium?.copyWith(color: colors.onSurfaceVariant)),
            if (actionLabel != null) ...[
              const SizedBox(height: Space.m),
              FilledButton.tonal(onPressed: onAction, child: Text(actionLabel!)),
            ],
          ],
        ),
      ),
    );
  }
}
