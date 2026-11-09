import 'package:flutter/material.dart';

import '../models/item.dart';
import '../utils/formatters.dart';
import 'item_image.dart';
import 'type_chip.dart';

/// One row in a list of posts: photo, title, category, place and date.
class ItemCard extends StatelessWidget {
  const ItemCard({super.key, required this.item, required this.onTap, this.extraLine});

  final Item item;
  final VoidCallback onTap;

  /// Optional extra text, e.g. "Matched on: wallet, black".
  final String? extraLine;

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme;
    return Card(
      clipBehavior: Clip.antiAlias,
      child: InkWell(
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.all(10),
          child: Row(
            children: [
              ItemImage(item: item, size: 72),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Expanded(
                          child: Text(item.title,
                              style: textTheme.titleMedium,
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis),
                        ),
                        TypeChip(type: item.type),
                      ],
                    ),
                    const SizedBox(height: 4),
                    Text('${item.category} • ${formatDate(item.date)}',
                        style: textTheme.bodySmall),
                    Text(item.location,
                        style: textTheme.bodySmall,
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis),
                    if (extraLine != null)
                      Text(extraLine!,
                          style: textTheme.bodySmall
                              ?.copyWith(fontWeight: FontWeight.bold)),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
