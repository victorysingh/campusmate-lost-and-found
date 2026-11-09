import '../models/app_user.dart';
import '../models/item.dart';

/// 10 example posts for demos (menu → "Add sample items").
/// Four lost/found pairs are written so they match each other, and two
/// posts have no match, so every part of the matching can be shown.
/// The posts belong to the logged-in user (allowed by the security rules).
List<Item> buildSampleItems(AppUser owner) {
  final now = DateTime.now();
  final samples = <(ItemType, String, String, String, String)>[
    // (type, title, category, description, location)
    (ItemType.lost, 'Black leather wallet', 'Wallet',
        'Black leather wallet with college ID and some cash inside.', 'Main canteen'),
    (ItemType.found, 'Wallet found on canteen table', 'Wallet',
        'Black wallet, leather, has an ID card inside. Handing it to whoever proves it is theirs.', 'Main canteen'),
    (ItemType.lost, 'HP laptop charger', 'Electronics',
        '65W HP charger with a blue sticker on the adapter.', 'Library, 2nd floor'),
    (ItemType.found, 'Laptop charger in library', 'Electronics',
        'HP charger left plugged in near the reading tables.', 'Library'),
    (ItemType.lost, 'Bike keys with red keychain', 'Keys',
        'Two Honda keys on a red keychain.', 'Parking lot B'),
    (ItemType.found, 'Keys with red keychain', 'Keys',
        'Found a bunch of keys, red keychain, near the bike stand.', 'Parking lot B'),
    (ItemType.lost, 'Student ID card', 'ID Card',
        'ID card of a 3rd year CSE student, blue lanyard.', 'Block A corridor'),
    (ItemType.found, 'ID card with blue lanyard', 'ID Card',
        'College ID card on a blue lanyard, found on the stairs.', 'Block A stairs'),
    (ItemType.lost, 'Steel water bottle', 'Water Bottle',
        'Silver Milton bottle with stickers.', 'Basketball court'),
    (ItemType.found, 'Grey hoodie', 'Clothing',
        'Grey hoodie, size M, left in the seminar hall.', 'Seminar hall'),
  ];

  return [
    for (var i = 0; i < samples.length; i++)
      Item(
        id: '', // the database assigns the real id
        type: samples[i].$1,
        title: samples[i].$2,
        category: samples[i].$3,
        description: samples[i].$4,
        location: samples[i].$5,
        date: now.subtract(Duration(days: 1 + i ~/ 2)),
        ownerId: owner.uid,
        ownerName: owner.name,
        ownerEmail: owner.email,
        // Spread out by a minute each so the feed order is stable.
        createdAt: now.subtract(Duration(minutes: samples.length - i)),
      ),
  ];
}
