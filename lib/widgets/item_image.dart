import 'package:flutter/material.dart';

import '../models/item.dart';
import 'category_chip.dart';

/// An item's photo filling its box, or (no photo) a soft gradient with
/// the category icon, so cards always look "image-led".
class ItemImage extends StatelessWidget {
  const ItemImage({super.key, required this.item, this.iconSize = 48});

  final Item item;
  final double iconSize;

  @override
  Widget build(BuildContext context) {
    final bytes = item.imageBytes;
    if (bytes != null) {
      return Image.memory(
        bytes,
        fit: BoxFit.cover,
        width: double.infinity,
        height: double.infinity,
        gaplessPlayback: true, // no flicker when lists rebuild
        errorBuilder: (context, error, stack) => _placeholder(context),
      );
    }
    return _placeholder(context);
  }

  Widget _placeholder(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    return Container(
      width: double.infinity,
      height: double.infinity,
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [colors.primaryContainer, colors.tertiaryContainer],
        ),
      ),
      child: Icon(iconForCategory(item.category),
          size: iconSize, color: colors.onPrimaryContainer.withValues(alpha: 0.7)),
    );
  }
}

/// Hero tag shared by the feed card and the detail page image, so the
/// picture "flies" from the list into the detail screen.
String heroTagFor(Item item) => 'item-image-${item.id}';
