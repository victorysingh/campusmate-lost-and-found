import 'package:flutter/material.dart';

import '../models/item.dart';
import '../theme.dart';

/// Small coloured "LOST" / "FOUND" label.
class TypeChip extends StatelessWidget {
  const TypeChip({super.key, required this.type});

  final ItemType type;

  @override
  Widget build(BuildContext context) {
    final isLost = type == ItemType.lost;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
      decoration: BoxDecoration(
        color: isLost ? lostColor : foundColor,
        borderRadius: BorderRadius.circular(12),
      ),
      child: Text(
        isLost ? 'LOST' : 'FOUND',
        style: const TextStyle(color: Colors.white, fontSize: 11, fontWeight: FontWeight.bold),
      ),
    );
  }
}
