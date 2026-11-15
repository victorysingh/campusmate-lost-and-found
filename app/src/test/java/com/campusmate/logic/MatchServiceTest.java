package com.campusmate.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.campusmate.model.Item;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class MatchServiceTest {

    private static Item item(String id, String type, String category, String title, String description) {
        Item item = new Item();
        item.id = id;
        item.type = type;
        item.category = category;
        item.title = title;
        item.description = description;
        return item;
    }

    @Test
    public void keywordsAreLowercaseWithoutStopWordsOrPlurals() {
        Set<String> words = MatchService.extractKeywords("Lost my KEYS near the Library with a red tag");
        assertTrue(words.contains("key"));      // plural "s" removed
        assertTrue(words.contains("library"));
        assertTrue(words.contains("red"));
        assertTrue(words.contains("tag"));
        assertFalse(words.contains("lost"));    // stop word
        assertFalse(words.contains("the"));
        assertFalse(words.contains("my"));      // shorter than 3 letters
    }

    @Test
    public void wordsEndingInDoubleSKeepTheirS() {
        assertTrue(MatchService.extractKeywords("glass").contains("glass"));
    }

    @Test
    public void lostMatchesFoundInSameCategoryWithSharedWords() {
        Item lost = item("1", Item.LOST, "Electronics", "Apple AirPods", "white case blue sticker");
        Item found = item("2", Item.FOUND, "Electronics", "AirPods case", "white with sticker");
        List<MatchService.MatchResult> results = MatchService.findMatches(lost, Arrays.asList(lost, found));
        assertEquals(1, results.size());
        assertEquals("2", results.get(0).item.id);
        assertTrue(results.get(0).commonWords.contains("airpod"));
    }

    @Test
    public void sameTypeOrOtherCategoryNeverMatches() {
        Item lost = item("1", Item.LOST, "Wallet", "Black wallet", "leather");
        Item otherLost = item("2", Item.LOST, "Wallet", "Black wallet", "leather");
        Item foundOtherCategory = item("3", Item.FOUND, "Bags", "Black wallet", "leather");
        assertTrue(MatchService.findMatches(lost, Arrays.asList(otherLost, foundOtherCategory)).isEmpty());
    }

    @Test
    public void returnedPostsAreNotSuggested() {
        Item lost = item("1", Item.LOST, "Keys", "Bike keys", "red keychain");
        Item found = item("2", Item.FOUND, "Keys", "Keys with red keychain", "");
        found.status = Item.RETURNED;
        assertTrue(MatchService.findMatches(lost, Arrays.asList(found)).isEmpty());
    }

    @Test
    public void bestMatchComesFirst() {
        Item lost = item("1", Item.LOST, "Water Bottle", "Blue steel bottle", "dent stickers milton");
        Item weak = item("2", Item.FOUND, "Water Bottle", "Bottle", "plastic");
        Item strong = item("3", Item.FOUND, "Water Bottle", "Blue steel bottle", "with stickers and a dent");
        List<MatchService.MatchResult> results = MatchService.findMatches(lost, Arrays.asList(weak, strong));
        assertEquals("3", results.get(0).item.id);
        assertTrue(results.get(0).score() > results.get(1).score());
    }
}
