package com.campusmate.logic;

import com.campusmate.model.Item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Simple matching between Lost and Found posts.
 *
 * Two posts are a "possible match" when:
 *   1. one is LOST and the other is FOUND,
 *   2. they are in the same category, and
 *   3. their title + description share at least one keyword.
 * More shared keywords = better match, so results are sorted by that count.
 */
public final class MatchService {

    private MatchService() {
    }

    /** Common words that say nothing about the item itself. */
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "the", "and", "with", "for", "near", "lost", "found", "from", "this",
            "that", "was", "were", "have", "has", "had", "its", "are", "not", "but",
            "you", "your", "our", "his", "her", "their", "someone", "please", "contact",
            "item", "left", "today", "yesterday", "very", "some", "one", "got", "into",
            "inside", "outside", "around", "kept", "there", "here", "about", "any"));

    /** One suggested match and the words both posts have in common. */
    public static final class MatchResult {
        public final Item item;
        public final Set<String> commonWords;

        MatchResult(Item item, Set<String> commonWords) {
            this.item = item;
            this.commonWords = commonWords;
        }

        public int score() {
            return commonWords.size();
        }
    }

    /**
     * Splits text into lowercase keywords: words of 3+ letters, stop words
     * removed, and a trailing plural "s" dropped (so "keys" matches "key").
     */
    public static Set<String> extractKeywords(String text) {
        Set<String> keywords = new LinkedHashSet<>();
        for (String word : text.toLowerCase(Locale.ROOT).split("[^a-z0-9]+")) {
            if (word.length() < 3 || STOP_WORDS.contains(word)) continue;
            if (word.length() > 3 && word.endsWith("s") && !word.endsWith("ss")) {
                word = word.substring(0, word.length() - 1);
            }
            keywords.add(word);
        }
        return keywords;
    }

    /** Finds posts in {@code candidates} that may match {@code item}, best match first. */
    public static List<MatchResult> findMatches(Item item, List<Item> candidates) {
        Set<String> myWords = extractKeywords(item.title + " " + item.description);
        List<MatchResult> results = new ArrayList<>();

        for (Item other : candidates) {
            if (other.id.equals(item.id)) continue;           // the post itself
            if (other.type.equals(item.type)) continue;       // lost vs found only
            if (!other.category.equals(item.category)) continue;
            if (other.isReturned()) continue;                 // already back with its owner

            Set<String> common = new LinkedHashSet<>(myWords);
            common.retainAll(extractKeywords(other.title + " " + other.description));
            if (!common.isEmpty()) results.add(new MatchResult(other, common));
        }

        // Stable sort: equal scores keep the feed order (newest first).
        results.sort((a, b) -> Integer.compare(b.score(), a.score()));
        return results;
    }
}
