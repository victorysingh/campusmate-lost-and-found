import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../models/item.dart';
import '../providers/item_provider.dart';
import '../routes.dart';
import '../utils/formatters.dart';
import '../widgets/item_card.dart';
import '../widgets/item_image.dart';
import '../widgets/type_chip.dart';

/// Full details of one post, plus "Possible matches" from the other side
/// (lost ↔ found). Route argument: the Item to show.
class ItemDetailScreen extends StatelessWidget {
  const ItemDetailScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final passed = ModalRoute.of(context)!.settings.arguments as Item;
    final provider = context.watch<ItemProvider>();
    // Prefer the live copy from the provider (it updates in realtime);
    // fall back to the passed copy, e.g. just after saving.
    final item = provider.findById(passed.id) ?? passed;
    final matches = provider.matchesFor(item);
    final textTheme = Theme.of(context).textTheme;

    return Scaffold(
      appBar: AppBar(title: Text(item.isLost ? 'Lost item' : 'Found item')),
      body: ListView(
        padding: const EdgeInsets.only(bottom: 24),
        children: [
          _buildPhoto(context, item),
          Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Expanded(child: Text(item.title, style: textTheme.headlineSmall)),
                    TypeChip(type: item.type),
                  ],
                ),
                const SizedBox(height: 12),
                _infoRow(Icons.category_outlined, item.category),
                _infoRow(Icons.place_outlined, item.location),
                _infoRow(Icons.event,
                    '${item.isLost ? 'Lost' : 'Found'} on ${formatDate(item.date)}'),
                _infoRow(Icons.person_outline, 'Posted by ${item.ownerName} (${item.ownerEmail})'),
                const SizedBox(height: 12),
                Text(item.description, style: textTheme.bodyLarge),
                const Divider(height: 32),
                Text('Possible matches (${matches.length})', style: textTheme.titleMedium),
                if (matches.isEmpty)
                  Padding(
                    padding: const EdgeInsets.only(top: 8),
                    child: Text(
                      'No matching ${item.isLost ? 'found' : 'lost'} posts yet. '
                      'Check back later.',
                      style: textTheme.bodyMedium,
                    ),
                  ),
              ],
            ),
          ),
          for (final match in matches)
            ItemCard(
              item: match.item,
              extraLine: 'Matched on: ${match.commonWords.join(', ')}',
              // push (not replace) so Back returns to this post.
              onTap: () => Navigator.pushNamed(context, AppRoutes.itemDetail,
                  arguments: match.item),
            ),
        ],
      ),
    );
  }

  Widget _buildPhoto(BuildContext context, Item item) {
    final bytes = item.imageBytes;
    if (bytes == null) {
      return Container(
        height: 160,
        color: Theme.of(context).colorScheme.surfaceContainerHighest,
        child: Icon(iconForCategory(item.category), size: 72),
      );
    }
    return AspectRatio(
      aspectRatio: 4 / 3,
      child: Image.memory(bytes, fit: BoxFit.cover, gaplessPlayback: true),
    );
  }

  Widget _infoRow(IconData icon, String text) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 3),
      child: Row(
        children: [
          Icon(icon, size: 18),
          const SizedBox(width: 8),
          Expanded(child: Text(text)),
        ],
      ),
    );
  }
}
