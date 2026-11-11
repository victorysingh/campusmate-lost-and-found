import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../models/item.dart';
import '../providers/auth_provider.dart';
import '../providers/item_provider.dart';
import '../routes.dart';
import '../theme.dart';
import '../utils/formatters.dart';
import '../widgets/category_chip.dart';
import '../widgets/item_card.dart';
import '../widgets/item_image.dart';
import '../widgets/primary_button.dart';
import '../widgets/status_badge.dart';

/// Full details of one post, plus "Possible matches" from the other side
/// (lost ↔ found). Route argument: the Item to show.
class ItemDetailScreen extends StatefulWidget {
  const ItemDetailScreen({super.key});

  @override
  State<ItemDetailScreen> createState() => _ItemDetailScreenState();
}

class _ItemDetailScreenState extends State<ItemDetailScreen> {
  final _matchesKey = GlobalKey(); // lets the button scroll to the matches

  void _scrollToMatches() {
    final target = _matchesKey.currentContext;
    if (target != null) {
      Scrollable.ensureVisible(target,
          duration: const Duration(milliseconds: 400), curve: Curves.easeOut);
    }
  }

  @override
  Widget build(BuildContext context) {
    final passed = ModalRoute.of(context)!.settings.arguments as Item;
    final provider = context.watch<ItemProvider>();
    // Prefer the live copy from the provider (it updates in realtime);
    // fall back to the passed copy, e.g. just after saving.
    final item = provider.findById(passed.id) ?? passed;
    final matches = provider.matchesFor(item);
    final isMine = item.ownerId == context.watch<AuthProvider>().user?.uid;
    final text = Theme.of(context).textTheme;
    final colors = Theme.of(context).colorScheme;

    return Scaffold(
      body: CustomScrollView(
        slivers: [
          SliverAppBar(
            pinned: true,
            expandedHeight: 300,
            // Round button so "back" stays visible on top of any photo.
            leading: Padding(
              padding: const EdgeInsets.all(Space.s),
              child: CircleAvatar(
                backgroundColor: colors.surface.withValues(alpha: 0.85),
                child: BackButton(color: colors.onSurface),
              ),
            ),
            flexibleSpace: FlexibleSpaceBar(
              // Same Hero tag as the card in the feed → the image "flies" in.
              background: Hero(tag: heroTagFor(item), child: ItemImage(item: item, iconSize: 96)),
            ),
          ),
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.all(Space.m),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  StatusBadge(item: item),
                  const SizedBox(height: Space.s),
                  Text(item.title,
                      style: text.headlineSmall?.copyWith(fontWeight: FontWeight.w600)),
                  const SizedBox(height: Space.m),
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.symmetric(vertical: Space.s),
                      child: Column(
                        children: [
                          _InfoRow(iconForCategory(item.category), 'Category', item.category),
                          _InfoRow(Icons.place_rounded, 'Location', item.location),
                          _InfoRow(Icons.event_rounded, item.isLost ? 'Lost on' : 'Found on',
                              formatDate(item.date)),
                          _InfoRow(Icons.person_rounded, 'Posted by',
                              '${item.ownerName}\n${item.ownerEmail}'),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: Space.m),
                  Text('Description', style: text.titleMedium),
                  const SizedBox(height: Space.xs),
                  Text(item.description, style: text.bodyLarge),
                  const SizedBox(height: Space.l),
                  Text('Possible matches (${matches.length})',
                      key: _matchesKey, style: text.titleMedium),
                  const SizedBox(height: Space.s),
                  if (matches.isEmpty)
                    Text(
                      'No matching ${item.isLost ? 'found' : 'lost'} posts yet. '
                      'New posts are checked automatically.',
                      style: text.bodyMedium?.copyWith(color: colors.onSurfaceVariant),
                    ),
                  for (final match in matches)
                    Padding(
                      padding: const EdgeInsets.only(bottom: Space.s),
                      child: ItemCard(
                        item: match.item,
                        compact: true,
                        extraLine: 'Matched on: ${match.commonWords.join(', ')}',
                        // push (not replace) so Back returns to this post.
                        onTap: () => Navigator.pushNamed(context, AppRoutes.itemDetail,
                            arguments: match.item),
                      ),
                    ),
                ],
              ),
            ),
          ),
        ],
      ),
      // "Sticky" action: always visible at the bottom while the page scrolls.
      bottomNavigationBar: SafeArea(
        child: Padding(
          padding: const EdgeInsets.fromLTRB(Space.m, Space.s, Space.m, Space.m),
          child: isMine
              ? PrimaryButton(
                  label: matches.isEmpty
                      ? 'No matches yet'
                      : 'View ${matches.length} possible match${matches.length == 1 ? '' : 'es'}',
                  icon: Icons.manage_search_rounded,
                  onPressed: matches.isEmpty ? null : _scrollToMatches,
                )
              : PrimaryButton(
                  label: item.isLost ? 'I found this' : 'This is mine',
                  icon: Icons.handshake_rounded,
                  // TODO(phase 2): open the claim flow here.
                  onPressed: () => ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(content: Text('Claiming arrives in the next update.'))),
                ),
        ),
      ),
    );
  }
}

/// One "icon + label + value" row in the details card.
class _InfoRow extends StatelessWidget {
  const _InfoRow(this.icon, this.label, this.value);

  final IconData icon;
  final String label;
  final String value;

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    return ListTile(
      dense: true,
      leading: CircleAvatar(
        radius: 18,
        backgroundColor: colors.primaryContainer,
        child: Icon(icon, size: 18, color: colors.onPrimaryContainer),
      ),
      title: Text(label, style: TextStyle(color: colors.onSurfaceVariant, fontSize: 12)),
      subtitle: Text(value,
          style: TextStyle(color: colors.onSurface, fontSize: 14, fontWeight: FontWeight.w500)),
    );
  }
}
