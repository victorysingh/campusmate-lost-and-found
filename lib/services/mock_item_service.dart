import 'dart:async';

import '../models/item.dart';
import 'item_service.dart';

/// In-memory posts for demo mode and tests. Behaves like Firestore's
/// realtime listener: every change is pushed to watchItems() listeners.
class MockItemService implements ItemService {
  final List<Item> _items = [];
  final _changes = StreamController<List<Item>>.broadcast();
  int _nextId = 1;

  @override
  Stream<List<Item>> watchItems() async* {
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
}
