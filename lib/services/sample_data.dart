import '../config.dart';
import '../models/app_user.dart';
import '../models/item.dart';

/// 12 realistic example posts for demos (debug menu → Load sample data).
///
/// Five lost/found pairs are written to match each other, and two posts
/// have no match, so every part of the matching can be shown.
///
/// In mock mode the posts belong to made-up students, which looks real and
/// lets you open someone else's post. With Firebase the security rules only
/// allow posting as yourself, so pass [useFakeOwners] = false there.
List<Item> buildSampleItems(AppUser me, {required bool useFakeOwners}) {
  final now = DateTime.now();
  final domain = ALLOWED_EMAIL_DOMAIN.isEmpty ? 'college.edu' : ALLOWED_EMAIL_DOMAIN;

  // Each row: type, title, category, description, location, posted by.
  final samples = <(ItemType, String, String, String, String, String)>[
    (ItemType.lost, 'Apple AirPods Pro', 'Electronics',
        'White AirPods Pro in the charging case. Small scratch on the lid and a blue sticker inside.',
        'Library · 1st floor reading area', 'Priya Sharma'),
    (ItemType.found, 'AirPods case near reading tables', 'Electronics',
        'White AirPods case with both pods, has a blue sticker. Collected at the library desk.',
        'Library', 'Rahul Kumar'),
    (ItemType.lost, 'Casio fx-991ES calculator', 'Electronics',
        'Scientific calculator, grey, name "ANANYA" scratched on the back cover.',
        'Seminar hall', 'Ananya Rao'),
    (ItemType.found, 'Scientific calculator', 'Electronics',
        'Casio calculator left on a canteen table after lunch. Has a name scratched on the back.',
        'Block A canteen', 'Kiran M'),
    (ItemType.lost, 'Student ID card', 'ID Card',
        'College ID card of a 2nd year ECE student, on a red lanyard.',
        'Main gate', 'Sneha Patil'),
    (ItemType.found, 'ID card with red lanyard', 'ID Card',
        'Found a college ID card on a red lanyard near the security cabin.',
        'Main gate', 'Arjun N'),
    (ItemType.lost, 'Steel water bottle', 'Water Bottle',
        'Blue Milton steel bottle, 1 litre, with a dent near the bottom and stickers.',
        'Basketball court', 'Rahul Kumar'),
    (ItemType.found, 'Blue steel bottle', 'Water Bottle',
        'Blue steel bottle with stickers, left on the bench after the match.',
        'Basketball court', 'Priya Sharma'),
    (ItemType.lost, 'Black leather wallet', 'Wallet',
        'Black leather wallet with a metro card and some cash. No ID inside.',
        'Block A canteen', 'Kiran M'),
    (ItemType.found, 'Wallet found in seminar hall', 'Wallet',
        'Black leather wallet under a seat in row 4 after the guest lecture.',
        'Seminar hall', 'Sneha Patil'),
    (ItemType.lost, 'Data Structures notes', 'Books & Notes',
        'Blue spiral notebook with handwritten DSA notes for the internals.',
        'CSE block · Room 204', 'Arjun N'),
    (ItemType.found, 'Grey hoodie', 'Clothing',
        'Grey hoodie, size M, left in the auditorium after the fest rehearsal.',
        'Auditorium', 'Ananya Rao'),
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
        date: now.subtract(Duration(days: i ~/ 3)),
        ownerId: useFakeOwners ? 'sample-${samples[i].$6}' : me.uid,
        ownerName: useFakeOwners ? samples[i].$6 : me.name,
        ownerEmail: useFakeOwners
            ? '${samples[i].$6.toLowerCase().replaceAll(' ', '.')}@$domain'
            : me.email,
        // Spread out by a few minutes so the feed order is stable.
        createdAt: now.subtract(Duration(minutes: 5 * (samples.length - i))),
      ),
  ];
}
