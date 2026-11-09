import 'package:flutter/material.dart';

import '../models/item.dart';

/// Shows an item's photo, or a category icon if it has no photo.
/// [size] is the width and height of the box.
class ItemImage extends StatelessWidget {
  const ItemImage({super.key, required this.item, required this.size});

  final Item item;
  final double size;

  @override
  Widget build(BuildContext context) {
    final bytes = item.imageBytes;
    final colors = Theme.of(context).colorScheme;

    return ClipRRect(
      borderRadius: BorderRadius.circular(8),
      child: SizedBox(
        width: size,
        height: size,
        child: bytes == null
            ? ColoredBox(
                color: colors.surfaceContainerHighest,
                child: Icon(iconForCategory(item.category),
                    size: size / 2, color: colors.onSurfaceVariant),
              )
            : Image.memory(
                bytes,
                fit: BoxFit.cover,
                gaplessPlayback: true, // no flicker when the list rebuilds
                // Decode a smaller copy for thumbnails to save memory.
                cacheWidth: (size * MediaQuery.devicePixelRatioOf(context)).round(),
                errorBuilder: (context, error, stack) =>
                    const Center(child: Icon(Icons.broken_image)),
              ),
      ),
    );
  }
}

IconData iconForCategory(String category) {
  switch (category) {
    case 'Electronics':
      return Icons.devices;
    case 'ID Card':
      return Icons.badge;
    case 'Wallet':
      return Icons.account_balance_wallet;
    case 'Keys':
      return Icons.key;
    case 'Books & Notes':
      return Icons.menu_book;
    case 'Bags':
      return Icons.backpack;
    case 'Clothing':
      return Icons.checkroom;
    case 'Water Bottle':
      return Icons.water_drop;
    default:
      return Icons.inventory_2;
  }
}
