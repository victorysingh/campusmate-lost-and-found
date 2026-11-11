import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../config.dart';
import '../models/item.dart';
import '../providers/auth_provider.dart';
import '../providers/item_provider.dart';
import '../routes.dart';
import '../theme.dart';
import '../widgets/app_logo.dart';
import '../widgets/category_chip.dart';
import '../widgets/debug_menu.dart';
import '../widgets/demo_mode_banner.dart';
import '../widgets/empty_state.dart';
import '../widgets/item_card.dart';
import '../widgets/shimmer_list.dart';

/// Home tab: greeting, search, category chips, Lost/Found switch and the feed.
class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final _searchController = TextEditingController();
  ItemType _type = ItemType.lost; // which list is showing

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthProvider>();
    final provider = context.watch<ItemProvider>();

    return SafeArea(
      bottom: false,
      child: RefreshIndicator(
        onRefresh: provider.refresh,
        // CustomScrollView = one scrolling page made of "slivers", so the
        // header scrolls away together with the list.
        child: CustomScrollView(
          physics: const AlwaysScrollableScrollPhysics(), // pull works even when empty
          slivers: [
            if (auth.isDemoMode) const SliverToBoxAdapter(child: DemoModeBanner()),
            SliverToBoxAdapter(child: _header(context, auth.user?.name ?? '')),
            SliverToBoxAdapter(child: _searchBar(provider)),
            SliverToBoxAdapter(child: _categoryChips(provider)),
            SliverToBoxAdapter(child: _typeSwitch(provider)),
            ..._content(context, provider),
          ],
        ),
      ),
    );
  }

  Widget _header(BuildContext context, String name) {
    final text = Theme.of(context).textTheme;
    final hour = DateTime.now().hour;
    final greeting = hour < 12
        ? 'Good morning'
        : hour < 17
            ? 'Good afternoon'
            : 'Good evening';
    final firstName = name.split(' ').first;

    return Padding(
      padding: const EdgeInsets.fromLTRB(Space.m, Space.m, Space.m, Space.s),
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // (No emoji: on web it needs a font download, and the demo is offline.)
                Text('$greeting, $firstName',
                    style: text.headlineSmall?.copyWith(fontWeight: FontWeight.w600),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis),
                Text('Lost something? Let’s find it together.',
                    style: text.bodyMedium
                        ?.copyWith(color: Theme.of(context).colorScheme.onSurfaceVariant)),
              ],
            ),
          ),
          // Long-press the logo to open the hidden demo menu.
          GestureDetector(
            onLongPress: () => showDebugMenu(context),
            child: const AppLogo(size: 48),
          ),
        ],
      ),
    );
  }

  Widget _searchBar(ItemProvider provider) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: Space.m, vertical: Space.s),
      child: TextField(
        controller: _searchController,
        textInputAction: TextInputAction.search,
        decoration: InputDecoration(
          hintText: 'Search AirPods, ID card, library…',
          prefixIcon: const Icon(Icons.search_rounded),
          suffixIcon: provider.query.isEmpty
              ? null
              : IconButton(
                  tooltip: 'Clear search',
                  icon: const Icon(Icons.close_rounded),
                  onPressed: () {
                    _searchController.clear();
                    provider.setQuery('');
                  },
                ),
        ),
        onChanged: provider.setQuery,
      ),
    );
  }

  Widget _categoryChips(ItemProvider provider) {
    return SizedBox(
      height: 56,
      child: ListView(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: Space.m, vertical: Space.s),
        children: [
          CategoryChip(
            label: 'All',
            icon: Icons.apps_rounded,
            selected: provider.category == null,
            onTap: () => provider.setCategory(null),
          ),
          for (final c in itemCategories)
            Padding(
              padding: const EdgeInsets.only(left: Space.s),
              child: CategoryChip(
                label: c,
                icon: iconForCategory(c),
                selected: provider.category == c,
                // Tapping the selected chip again clears the filter.
                onTap: () => provider.setCategory(provider.category == c ? null : c),
              ),
            ),
        ],
      ),
    );
  }

  Widget _typeSwitch(ItemProvider provider) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(Space.m, Space.s, Space.m, Space.m),
      child: SizedBox(
        width: double.infinity,
        child: SegmentedButton<ItemType>(
          segments: [
            ButtonSegment(
              value: ItemType.lost,
              icon: const Icon(Icons.search_rounded),
              label: Text('Lost (${provider.countOfType(ItemType.lost)})'),
            ),
            ButtonSegment(
              value: ItemType.found,
              icon: const Icon(Icons.volunteer_activism_rounded),
              label: Text('Found (${provider.countOfType(ItemType.found)})'),
            ),
          ],
          selected: {_type},
          onSelectionChanged: (s) => setState(() => _type = s.first),
        ),
      ),
    );
  }

  /// The part under the filters: skeleton, error, empty message or list.
  List<Widget> _content(BuildContext context, ItemProvider provider) {
    if (provider.isLoading) {
      return const [
        SliverPadding(
          padding: EdgeInsets.symmetric(horizontal: Space.m),
          sliver: SliverToBoxAdapter(child: ShimmerList()),
        ),
      ];
    }
    if (provider.error != null) {
      return [
        SliverFillRemaining(
          hasScrollBody: false,
          child: EmptyState(
            icon: Icons.cloud_off_rounded,
            title: 'Couldn’t load posts',
            message: provider.error!,
            actionLabel: 'Try again',
            onAction: provider.refresh,
          ),
        ),
      ];
    }

    final items = provider.itemsOfType(_type);
    if (items.isEmpty) {
      final word = _type == ItemType.lost ? 'lost' : 'found';
      return [
        SliverFillRemaining(
          hasScrollBody: false,
          child: provider.hasFilters
              ? const EmptyState(
                  icon: Icons.search_off_rounded,
                  title: 'No results',
                  message: 'Try another word or pick a different category.',
                )
              : EmptyState(
                  icon: Icons.inventory_2_outlined,
                  title: 'No $word items yet',
                  message: 'When someone posts a $word item it shows up here.\n'
                      'Tap the + button to post one.',
                ),
        ),
      ];
    }

    return [
      SliverPadding(
        // Extra bottom space so the last card is not hidden by the Post button.
        padding: const EdgeInsets.fromLTRB(Space.m, 0, Space.m, 96),
        sliver: SliverList.separated(
          itemCount: items.length,
          separatorBuilder: (context, index) => const SizedBox(height: Space.m),
          itemBuilder: (context, index) => ItemCard(
            item: items[index],
            onTap: () =>
                Navigator.pushNamed(context, AppRoutes.itemDetail, arguments: items[index]),
          ),
        ),
      ),
    ];
  }
}
