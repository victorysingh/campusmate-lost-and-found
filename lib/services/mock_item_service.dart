import 'dart:async';

import '../models/item.dart';
import 'item_service.dart';

/// In-memory posts for mock mode and tests. Behaves like Firestore's
/// realtime listener: every change is pushed to watchItems() listeners.
class MockItemService implements ItemService {
  MockItemService({this.loadDelay = const Duration(milliseconds: 700)});

  /// Pretend network delay on first load, so the loading skeleton is
  /// visible in demos. Tests pass Duration.zero.
  final Duration loadDelay;

  final List<Item> _items = [];
  final _changes = StreamController<List<Item>>.broadcast();
  int _nextId = 1;

  @override
  Stream<List<Item>> watchItems() async* {
    if (loadDelay > Duration.zero) await Future.delayed(loadDelay);
    yield List.of(_items);
    yield* _changes.stream;
  }

  @override
  Future<String> addItem(Item item) async {
    final saved = item.copyWithId('demo-item-${_nextId++}');
    _items.add(saved);
    _items.sort((a, b) => b.createdAt.compareTo(a.createdAt)); // newest first
    _changes.add(List.of(_items));
    return saved.id;
  }

  /// Deletes every post (debug menu → Reset data). Mock mode only.
  void clearAll() {
    _items.clear();
    _changes.add([]);
  }
}
