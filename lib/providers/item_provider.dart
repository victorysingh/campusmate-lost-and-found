import 'dart:async';

import 'package:flutter/foundation.dart';

import '../models/app_user.dart';
import '../models/item.dart';
import '../services/item_service.dart';
import '../services/match_service.dart';
import '../services/mock_item_service.dart';
import '../services/sample_data.dart';

/// Holds the live list of posts plus the search text and category filter.
/// The feed, detail and add screens all read from here.
class ItemProvider extends ChangeNotifier {
  ItemProvider(this._service);

  final ItemService _service;
  StreamSubscription<List<Item>>? _subscription;
  String? _listeningForUid;

  List<Item> _items = [];
  bool _loading = false;
  String? _error;
  String _query = '';
  String? _category; // null = all categories

  bool get isLoading => _loading;
  String? get error => _error;
  String get query => _query;
  String? get category => _category;
  bool get hasFilters => _query.isNotEmpty || _category != null;

  /// Called by the ProxyProvider in app.dart whenever login state changes.
  /// We only listen to Firestore while someone is logged in, because the
  /// security rules reject reads from logged-out users.
  void setUser(AppUser? user) {
    if (user?.uid == _listeningForUid) return; // nothing changed
    _listeningForUid = user?.uid;
    _listen(user != null);
  }

  /// Starts the realtime listener (or stops it when logged out).
  void _listen(bool loggedIn) {
    _subscription?.cancel();
    _items = [];
    _error = null;
    _loading = loggedIn;
    if (!loggedIn) return;
    _subscribe();
  }

  void _subscribe() {
    _subscription = _service.watchItems().listen(
      (items) {
        _items = items;
        _loading = false;
        _error = null;
        notifyListeners();
      },
      onError: (Object e) {
        _loading = false;
        _error = 'Could not load posts. Check your internet and pull down to retry.\n($e)';
        notifyListeners();
      },
    );
  }

  /// Pull-to-refresh. The feed is realtime anyway, so this just restarts
  /// the listener (useful after an error). Keeps the current list on screen.
  Future<void> refresh() async {
    await _subscription?.cancel();
    _error = null;
    _subscribe();
    await Future.delayed(const Duration(milliseconds: 600)); // let the spinner show
  }

  /// Number of Lost or Found posts after filtering (shown on the tabs).
  int countOfType(ItemType type) => itemsOfType(type).length;

  /// Posts of one type (Lost or Found) that pass the search and category filters.
  List<Item> itemsOfType(ItemType type) {
    return _items.where((item) => item.type == type && _passesFilters(item)).toList();
  }

  bool _passesFilters(Item item) {
    if (_category != null && item.category != _category) return false;
    if (_query.isEmpty) return true;
    final text = '${item.title} ${item.description} ${item.location}'.toLowerCase();
    return text.contains(_query);
  }

  void setQuery(String value) {
    _query = value.trim().toLowerCase();
    notifyListeners();
  }

  void setCategory(String? value) {
    _category = value;
    notifyListeners();
  }

  Item? findById(String id) {
    for (final item in _items) {
      if (item.id == id) return item;
    }
    return null;
  }

  /// Possible matches for [item] among all loaded posts (see match_service.dart).
  List<MatchResult> matchesFor(Item item) => findMatches(item, _items);

  /// Saves a post and returns it with its new id.
  Future<Item> addItem(Item item) async {
    final id = await _service.addItem(item);
    return item.copyWithId(id);
  }

  /// True in mock mode; some debug-menu actions only make sense there.
  bool get isMock => _service is MockItemService;

  /// Adds the 12 sample posts (debug menu).
  Future<void> addSampleItems(AppUser me) async {
    for (final item in buildSampleItems(me, useFakeOwners: isMock)) {
      await _service.addItem(item);
    }
  }

  /// Deletes all posts. Mock mode only (we never wipe the real database).
  void resetData() {
    final service = _service;
    if (service is MockItemService) service.clearAll();
  }

  /// A lost post and its best "found" match, for the debug menu's
  /// "Simulate match notification". Null if no pair exists yet.
  (Item lost, Item found)? anyMatchPair() {
    for (final item in _items.where((i) => i.isLost)) {
      final matches = matchesFor(item);
      if (matches.isNotEmpty) return (item, matches.first.item);
    }
    return null;
  }

  @override
  void dispose() {
    _subscription?.cancel();
    super.dispose();
  }
}
