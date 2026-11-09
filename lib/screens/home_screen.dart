import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../config.dart';
import '../models/item.dart';
import '../providers/auth_provider.dart';
import '../providers/item_provider.dart';
import '../routes.dart';
import '../widgets/demo_mode_banner.dart';
import '../widgets/empty_state.dart';
import '../widgets/item_card.dart';

/// Main screen: search bar, category filter and two tabs (Lost / Found).
class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  Future<void> _addSamples(BuildContext context) async {
    final user = context.read<AuthProvider>().user!;
    final messenger = ScaffoldMessenger.of(context);
    try {
      await context.read<ItemProvider>().addSampleItems(user);
      messenger.showSnackBar(const SnackBar(content: Text('Added 10 sample items')));
    } catch (e) {
      messenger.showSnackBar(SnackBar(content: Text('Could not add samples: $e')));
    }
  }

  @override
  Widget build(BuildContext context) {
    final isDemo = context.watch<AuthProvider>().isDemoMode;

    return DefaultTabController(
      length: 2,
      child: Scaffold(
        appBar: AppBar(
          title: const Text(appName),
          actions: [
            PopupMenuButton<String>(
              onSelected: (choice) {
                if (choice == 'samples') _addSamples(context);
                if (choice == 'logout') context.read<AuthProvider>().signOut();
              },
              itemBuilder: (context) => const [
                PopupMenuItem(value: 'samples', child: Text('Add 10 sample items')),
                PopupMenuItem(value: 'logout', child: Text('Log out')),
              ],
            ),
          ],
          bottom: const TabBar(
            tabs: [
              Tab(icon: Icon(Icons.search_off), text: 'Lost'),
              Tab(icon: Icon(Icons.inventory), text: 'Found'),
            ],
          ),
        ),
        body: Column(
          children: [
            if (isDemo) const DemoModeBanner(),
            const _SearchAndFilterBar(),
            const Expanded(
              child: TabBarView(
                children: [
                  _ItemList(type: ItemType.lost),
                  _ItemList(type: ItemType.found),
                ],
              ),
            ),
          ],
        ),
        // Builder gives a context *inside* DefaultTabController, so we can
        // read which tab is open and pre-select Lost or Found on the form.
        floatingActionButton: Builder(
          builder: (context) => FloatingActionButton.extended(
            icon: const Icon(Icons.add),
            label: const Text('Post item'),
            onPressed: () {
              final tab = DefaultTabController.of(context).index;
              Navigator.pushNamed(context, AppRoutes.addItem,
                  arguments: tab == 0 ? ItemType.lost : ItemType.found);
            },
          ),
        ),
      ),
    );
  }
}

/// Keyword search box + category dropdown. Filters both tabs.
class _SearchAndFilterBar extends StatelessWidget {
  const _SearchAndFilterBar();

  @override
  Widget build(BuildContext context) {
    final items = context.watch<ItemProvider>();
    return Padding(
      padding: const EdgeInsets.fromLTRB(12, 12, 12, 4),
      child: Row(
        children: [
          Expanded(
            child: TextField(
              decoration: const InputDecoration(
                hintText: 'Search title, description, place',
                prefixIcon: Icon(Icons.search),
                isDense: true,
              ),
              onChanged: items.setQuery,
            ),
          ),
          const SizedBox(width: 8),
          DropdownButton<String?>(
            value: items.category,
            onChanged: items.setCategory,
            items: [
              const DropdownMenuItem(value: null, child: Text('All')),
              for (final c in itemCategories) DropdownMenuItem(value: c, child: Text(c)),
            ],
          ),
        ],
      ),
    );
  }
}

/// The list inside one tab, with loading / error / empty states.
class _ItemList extends StatelessWidget {
  const _ItemList({required this.type});

  final ItemType type;

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<ItemProvider>();

    if (provider.isLoading) {
      return const Center(child: CircularProgressIndicator());
    }
    if (provider.error != null) {
      return EmptyState(icon: Icons.cloud_off, message: provider.error!);
    }

    final items = provider.itemsOfType(type);
    if (items.isEmpty) {
      final word = type == ItemType.lost ? 'lost' : 'found';
      return EmptyState(
        icon: Icons.inbox_outlined,
        message: provider.hasFilters
            ? 'No $word items match your search.'
            : 'No $word items yet.\nTap "Post item" to add one.',
      );
    }

    return ListView.builder(
      padding: const EdgeInsets.only(bottom: 88), // room for the FAB
      itemCount: items.length,
      itemBuilder: (context, index) {
        final item = items[index];
        return ItemCard(
          item: item,
          onTap: () => Navigator.pushNamed(context, AppRoutes.itemDetail, arguments: item),
        );
      },
    );
  }
}
