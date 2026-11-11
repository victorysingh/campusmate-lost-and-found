import 'dart:convert';
import 'dart:typed_data';

import 'package:cloud_firestore/cloud_firestore.dart';

/// Whether a post is about something lost or something found.
enum ItemType { lost, found }

/// One Lost or Found post. Stored as a document in the Firestore
/// collection "items" (see toMap / fromMap for the exact fields).
class Item {
  final String id; // Firestore document id
  final ItemType type;
  final String title;
  final String category;
  final String description;
  final String location;
  final DateTime date; // the day the item was lost or found
  final String? imageBase64; // compressed JPEG as text (we have no Firebase Storage)
  final String ownerId; // uid of the student who posted it
  final String ownerName;
  final String ownerEmail;
  final DateTime createdAt; // when the post was made; the feed sorts by this
  final String status; // 'open', or 'returned' once the owner has it back

  Item({
    required this.id,
    required this.type,
    required this.title,
    required this.category,
    required this.description,
    required this.location,
    required this.date,
    required this.ownerId,
    required this.ownerName,
    required this.ownerEmail,
    required this.createdAt,
    this.imageBase64,
    this.status = 'open',
  });

  /// The photo as raw bytes. `late final` means it is decoded only once,
  /// the first time a widget asks for it, and then reused.
  late final Uint8List? imageBytes =
      imageBase64 == null ? null : base64Decode(imageBase64!);

  bool get isLost => type == ItemType.lost;
  bool get isReturned => status == 'returned';

  /// Converts this item into a map that Firestore can store.
  Map<String, dynamic> toMap() {
    return {
      'type': type.name, // 'lost' or 'found'
      'title': title,
      'category': category,
      'description': description,
      'location': location,
      'date': Timestamp.fromDate(date),
      'imageBase64': imageBase64,
      'ownerId': ownerId,
      'ownerName': ownerName,
      'ownerEmail': ownerEmail,
      'createdAt': Timestamp.fromDate(createdAt),
      'status': status,
    };
  }

  /// Builds an item from a Firestore document. Missing fields get safe
  /// defaults so one bad document cannot crash the feed.
  factory Item.fromMap(String id, Map<String, dynamic> map) {
    return Item(
      id: id,
      type: map['type'] == 'found' ? ItemType.found : ItemType.lost,
      title: map['title'] ?? '',
      category: map['category'] ?? 'Other',
      description: map['description'] ?? '',
      location: map['location'] ?? '',
      date: _toDateTime(map['date']),
      imageBase64: map['imageBase64'],
      ownerId: map['ownerId'] ?? '',
      ownerName: map['ownerName'] ?? '',
      ownerEmail: map['ownerEmail'] ?? '',
      createdAt: _toDateTime(map['createdAt']),
      status: map['status'] ?? 'open',
    );
  }

  /// Returns a copy with a new id. Used after saving, when the id is known.
  Item copyWithId(String newId) {
    return Item(
      id: newId,
      type: type,
      title: title,
      category: category,
      description: description,
      location: location,
      date: date,
      imageBase64: imageBase64,
      ownerId: ownerId,
      ownerName: ownerName,
      ownerEmail: ownerEmail,
      createdAt: createdAt,
      status: status,
    );
  }
}

DateTime _toDateTime(Object? value) {
  if (value is Timestamp) return value.toDate();
  return DateTime.now();
}
