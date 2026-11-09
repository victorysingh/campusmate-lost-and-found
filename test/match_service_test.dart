import 'package:campusmate/models/item.dart';
import 'package:campusmate/services/match_service.dart';
import 'package:flutter_test/flutter_test.dart';

/// Small helper so each test only states the fields it cares about.
Item makeItem(String id, ItemType type, String title,
    {String category = 'Wallet', String description = ''}) {
  return Item(
    id: id,
    type: type,
    title: title,
    category: category,
    description: description,
    location: 'Canteen',
    date: DateTime(2026, 11, 10),
    ownerId: 'u1',
    ownerName: 'Test',
    ownerEmail: 'test@college.edu',
    createdAt: DateTime(2026, 11, 10),
  );
}

void main() {
  group('extractKeywords', () {
    test('lowercases, drops short words and stop words', () {
      expect(extractKeywords('Lost my BLACK wallet near the canteen'),
          {'black', 'wallet', 'canteen'});
    });

    test('treats plurals as the same word', () {
      expect(extractKeywords('keys'), {'key'});
      expect(extractKeywords('glass'), {'glass'}); // "ss" endings are kept
    });
  });

  group('findMatches', () {
    final lostWallet = makeItem('1', ItemType.lost, 'Black leather wallet');

    test('matches opposite type, same category, shared keyword', () {
      final found = makeItem('2', ItemType.found, 'Found a black wallet');
      final result = findMatches(lostWallet, [lostWallet, found]);
      expect(result.length, 1);
      expect(result.first.item.id, '2');
      expect(result.first.commonWords, {'black', 'wallet'});
    });

    test('ignores posts of the same type', () {
      final otherLost = makeItem('2', ItemType.lost, 'Black wallet');
      expect(findMatches(lostWallet, [otherLost]), isEmpty);
    });

    test('ignores other categories even with shared words', () {
      final found = makeItem('2', ItemType.found, 'Black wallet', category: 'Bags');
      expect(findMatches(lostWallet, [found]), isEmpty);
    });

    test('needs at least one shared keyword', () {
      final found = makeItem('2', ItemType.found, 'Brown purse');
      expect(findMatches(lostWallet, [found]), isEmpty);
    });

    test('uses the description too', () {
      final found = makeItem('2', ItemType.found, 'Purse on bench',
          description: 'Made of leather');
      expect(findMatches(lostWallet, [found]).single.commonWords, {'leather'});
    });

    test('best match (most shared words) comes first', () {
      final weak = makeItem('2', ItemType.found, 'Wallet');
      final strong = makeItem('3', ItemType.found, 'Black leather wallet');
      final result = findMatches(lostWallet, [weak, strong]);
      expect(result.map((m) => m.item.id), ['3', '2']);
    });
  });
}
