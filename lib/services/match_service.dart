import '../models/item.dart';

// Simple matching between Lost and Found posts.
//
// Two posts are a "possible match" when:
//   1. one is LOST and the other is FOUND,
//   2. they are in the same category, and
//   3. their title + description share at least one keyword.
// More shared keywords = better match, so results are sorted by that count.

/// Common words that say nothing about the item itself.
const Set<String> _stopWords = {
  'the', 'and', 'with', 'for', 'near', 'lost', 'found', 'from', 'this',
  'that', 'was', 'were', 'have', 'has', 'had', 'its', 'are', 'not', 'but',
  'you', 'your', 'our', 'his', 'her', 'their', 'someone', 'please', 'contact',
  'item', 'left', 'today', 'yesterday', 'very', 'some', 'one', 'got', 'into',
  'inside', 'outside', 'around', 'kept', 'there', 'here', 'about', 'any',
};

/// Splits text into lowercase keywords: words of 3+ letters, stop words
/// removed, and a trailing plural "s" dropped (so "keys" matches "key").
Set<String> extractKeywords(String text) {
  final words = text.toLowerCase().split(RegExp(r'[^a-z0-9]+'));
  final keywords = <String>{};
  for (var word in words) {
    if (word.length < 3 || _stopWords.contains(word)) continue;
    if (word.length > 3 && word.endsWith('s') && !word.endsWith('ss')) {
      word = word.substring(0, word.length - 1);
    }
    keywords.add(word);
  }
  return keywords;
}

/// One suggested match and the words both posts have in common.
class MatchResult {
  const MatchResult(this.item, this.commonWords);

  final Item item;
  final Set<String> commonWords;

  int get score => commonWords.length;
}

/// Finds posts in [candidates] that may match [item], best match first.
List<MatchResult> findMatches(Item item, List<Item> candidates) {
  final myWords = extractKeywords('${item.title} ${item.description}');
  final results = <MatchResult>[];

  for (final other in candidates) {
    if (other.id == item.id) continue; // the post itself
    if (other.type == item.type) continue; // lost vs found only
    if (other.category != item.category) continue;

    final common =
        myWords.intersection(extractKeywords('${other.title} ${other.description}'));
    if (common.isNotEmpty) results.add(MatchResult(other, common));
  }

  results.sort((a, b) => b.score.compareTo(a.score));
  return results;
}
