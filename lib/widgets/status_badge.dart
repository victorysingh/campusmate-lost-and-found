import 'package:flutter/material.dart';

import '../models/item.dart';
import '../theme.dart';

/// Coloured pill showing LOST, FOUND or RETURNED for a post.
class StatusBadge extends StatelessWidget {
  const StatusBadge({super.key, required this.item});

  final Item item;

  @override
  Widget build(BuildContext context) {
    final (String label, Color color, IconData icon) = item.isReturned
        ? ('RETURNED', returnedColor, Icons.check_circle_rounded)
        : item.isLost
            ? ('LOST', lostColor, Icons.search_rounded)
            : ('FOUND', foundColor, Icons.volunteer_activism_rounded);

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(color: color, borderRadius: BorderRadius.circular(20)),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 14, color: Colors.white),
          const SizedBox(width: Space.xs),
          Text(label,
              style: const TextStyle(
                  color: Colors.white, fontSize: 11, fontWeight: FontWeight.w700, letterSpacing: 0.5)),
        ],
      ),
    );
  }
}
