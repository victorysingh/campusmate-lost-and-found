import 'package:flutter/material.dart';

import '../models/item.dart';
import '../theme.dart';
import '../utils/formatters.dart';
import 'item_image.dart';
import 'status_badge.dart';

/// A post in a list.
///  * normal:  big photo on top, details below (Home feed)
///  * compact: small photo on the left (match lists)
class ItemCard extends StatelessWidget {
  const ItemCard({
    super.key,
    required this.item,
    required this.onTap,
    this.compact = false,
    this.extraLine,
  });

  final Item item;
  final VoidCallback onTap;
  final bool compact;

  /// Optional bold line, e.g. "Matched on: wallet, black".
  final String? extraLine;

  @override
  Widget build(BuildContext context) {
    final image = Hero(tag: heroTagFor(item), child: ItemImage(item: item));
    return Card(
      clipBehavior: Clip.antiAlias,
      child: InkWell(
        onTap: onTap,
        child: compact ? _compactLayout(context, image) : _largeLayout(context, image),
      ),
    );
  }

  Widget _largeLayout(BuildContext context, Widget image) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Stack(
          children: [
            AspectRatio(aspectRatio: 16 / 9, child: image),
            Positioned(top: Space.s, right: Space.s, child: StatusBadge(item: item)),
          ],
        ),
        Padding(
          padding: const EdgeInsets.all(Space.m),
          child: _details(context),
        ),
      ],
    );
  }

  Widget _compactLayout(BuildContext context, Widget image) {
    return Padding(
      padding: const EdgeInsets.all(Space.s),
      child: Row(
        children: [
          ClipRRect(
            borderRadius: BorderRadius.circular(12),
            child: SizedBox.square(dimension: 80, child: image),
          ),
          const SizedBox(width: Space.m),
          Expanded(child: _details(context, showBadge: true)),
        ],
      ),
    );
  }

  Widget _details(BuildContext context, {bool showBadge = false}) {
    final text = Theme.of(context).textTheme;
    final muted = Theme.of(context).colorScheme.onSurfaceVariant;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        if (showBadge) ...[StatusBadge(item: item), const SizedBox(height: Space.xs)],
        Text(item.title,
            style: text.titleMedium?.copyWith(fontWeight: FontWeight.w600),
            maxLines: 1,
            overflow: TextOverflow.ellipsis),
        const SizedBox(height: Space.xs),
        _meta(Icons.place_outlined, item.location, muted, text),
        _meta(Icons.calendar_today_outlined, '${item.category} • ${formatDate(item.date)}',
            muted, text),
        if (extraLine != null)
          Padding(
            padding: const EdgeInsets.only(top: Space.xs),
            child: Text(extraLine!,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
                style: text.bodySmall?.copyWith(
                    fontWeight: FontWeight.w600, color: Theme.of(context).colorScheme.primary)),
          ),
      ],
    );
  }

  Widget _meta(IconData icon, String value, Color color, TextTheme text) {
    return Padding(
      padding: const EdgeInsets.only(top: 2),
      child: Row(
        children: [
          Icon(icon, size: 14, color: color),
          const SizedBox(width: Space.xs),
          Expanded(
            child: Text(value,
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
                style: text.bodySmall?.copyWith(color: color)),
          ),
        ],
      ),
    );
  }
}
