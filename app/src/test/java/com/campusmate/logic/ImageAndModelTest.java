package com.campusmate.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.campusmate.Config;
import com.campusmate.model.Claim;
import com.campusmate.model.Item;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Random;

@RunWith(RobolectricTestRunner.class)
public class ImageAndModelTest {

    /** Random noise compresses badly: the worst case for the 300 KB limit. */
    private static Bitmap noisyPhoto(int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Random random = new Random(42);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                bitmap.setPixel(x, y, Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
            }
        }
        return bitmap;
    }

    @Test
    public void photosAreCompressedUnder300KbAndShrunkTo1024px() {
        byte[] jpeg = ImageUtils.compressToLimit(noisyPhoto(2000, 1500), Config.MAX_IMAGE_BYTES);
        assertTrue("size was " + jpeg.length, jpeg.length <= Config.MAX_IMAGE_BYTES);

        Bitmap back = ImageUtils.fromBase64(ImageUtils.toBase64(noisyPhoto(2000, 1500), Config.MAX_IMAGE_BYTES));
        assertNotNull(back);
        assertTrue(Math.max(back.getWidth(), back.getHeight()) <= ImageUtils.MAX_SIDE);
    }

    @Test
    public void itemAndClaimSurviveAFirestoreRoundTrip() {
        Item item = new Item();
        item.type = Item.FOUND;
        item.title = "Blue steel bottle";
        item.date = 1_700_000_000_000L;
        item.createdAt = 1_700_000_100_000L;
        item.proofQuestion = "What brand is it?";
        Item back = Item.fromMap("abc", item.toMap());
        assertEquals("abc", back.id);
        assertEquals(Item.FOUND, back.type);
        assertEquals(item.date, back.date);
        assertEquals("What brand is it?", back.proofQuestion);

        Claim claim = new Claim();
        claim.ownerId = "rahul";
        claim.claimantId = "asha";
        claim.status = Claim.APPROVED;
        Claim claimBack = Claim.fromMap("x", claim.toMap());
        assertTrue(claimBack.isApproved());
        assertEquals("asha", claimBack.claimantId);
        assertEquals(java.util.Arrays.asList("rahul", "asha"), claim.toMap().get("participants"));
    }
}
