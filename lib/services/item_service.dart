import '../models/item.dart';

/// What the app needs from the database for Lost & Found posts.
///  * FirestoreItemService – the real one (Cloud Firestore)
///  * MockItemService      – in-memory, used in demo mode and in tests
abstract class ItemService {
  /// Live list of posts, newest first. Emits again whenever anything changes.
  Stream<List<Item>> watchItems();

  /// Saves a new post and returns its id.
  Future<String> addItem(Item item);
}
