import 'package:cloud_firestore/cloud_firestore.dart';

import '../config.dart';
import '../models/item.dart';
import 'item_service.dart';

/// Stores posts in the Firestore collection "items".
class FirestoreItemService implements ItemService {
  final CollectionReference<Map<String, dynamic>> _items =
      FirebaseFirestore.instance.collection('items');

  @override
  Stream<List<Item>> watchItems() {
    // snapshots() is a realtime listener: Firestore pushes a new snapshot
    // whenever any post is added or changed, so the feed updates by itself.
    // We sort on a single field only, so no composite index is needed.
    // Type/category/keyword filters are applied in the app (ItemProvider).
    return _items
        .orderBy('createdAt', descending: true)
        .limit(feedLimit)
        .snapshots()
        .map((snapshot) => snapshot.docs
            .map((doc) => Item.fromMap(doc.id, doc.data()))
            .toList());
  }

  @override
  Future<String> addItem(Item item) async {
    final doc = await _items.add(item.toMap());
    return doc.id;
  }
}
