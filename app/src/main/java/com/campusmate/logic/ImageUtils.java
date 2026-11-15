package com.campusmate.logic;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.util.Base64;
import android.util.LruCache;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Photo helpers. We have no Firebase Storage (paid plan), so each photo is
 * shrunk to under 300 KB and saved inside the Firestore document as base64 text.
 */
public final class ImageUtils {

    private ImageUtils() {
    }

    /** Longest side of a saved photo, in pixels. */
    public static final int MAX_SIDE = 1024;

    /** Decoded photos, so scrolling the feed does not decode the same text again. */
    private static final LruCache<String, Bitmap> CACHE = new LruCache<String, Bitmap>(16 * 1024 * 1024) {
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getByteCount();
        }
    };

    /** Reads a picked or camera photo, already turned the right way up and scaled down. */
    public static Bitmap decodeUri(ContentResolver resolver, Uri uri) throws IOException {
        if (Build.VERSION.SDK_INT >= 28) {
            ImageDecoder.Source source = ImageDecoder.createSource(resolver, uri);
            return ImageDecoder.decodeBitmap(source, (decoder, info, src) -> {
                int w = info.getSize().getWidth();
                int h = info.getSize().getHeight();
                float scale = Math.min(1f, (float) MAX_SIDE / Math.max(w, h));
                decoder.setTargetSize(Math.max(1, Math.round(w * scale)), Math.max(1, Math.round(h * scale)));
                decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
            });
        }
        // Android 7-8: read the size first, then load a smaller copy.
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream in = resolver.openInputStream(uri)) {
            BitmapFactory.decodeStream(in, null, bounds);
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        while (Math.max(bounds.outWidth, bounds.outHeight) / (options.inSampleSize * 2) >= MAX_SIDE) {
            options.inSampleSize *= 2;
        }
        try (InputStream in = resolver.openInputStream(uri)) {
            Bitmap bitmap = BitmapFactory.decodeStream(in, null, options);
            if (bitmap == null) throw new IOException("Not an image");
            return scaleDown(bitmap, MAX_SIDE);
        }
    }

    /**
     * JPEG-compresses {@code bitmap} until it is smaller than {@code maxBytes}:
     * first by lowering quality, then by shrinking the picture.
     */
    public static byte[] compressToLimit(Bitmap bitmap, int maxBytes) {
        Bitmap current = scaleDown(bitmap, MAX_SIDE);
        while (true) {
            for (int quality = 85; quality >= 40; quality -= 15) {
                byte[] bytes = toJpeg(current, quality);
                if (bytes.length <= maxBytes) return bytes;
            }
            int side = Math.max(current.getWidth(), current.getHeight());
            if (side <= 64) return toJpeg(current, 40); // tiny already; cannot shrink further
            current = scaleDown(current, (int) (side * 0.75f));
        }
    }

    /** Compresses and returns base64 text ready to store in Firestore. */
    public static String toBase64(Bitmap bitmap, int maxBytes) {
        return Base64.encodeToString(compressToLimit(bitmap, maxBytes), Base64.NO_WRAP);
    }

    /** Turns stored base64 text back into a picture (cached). Returns null for bad data. */
    public static Bitmap fromBase64(String base64) {
        if (base64 == null || base64.isEmpty()) return null;
        String key = base64.length() + ":" + base64.hashCode();
        Bitmap cached = CACHE.get(key);
        if (cached != null) return cached;
        try {
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bitmap != null) CACHE.put(key, bitmap);
            return bitmap;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    static Bitmap scaleDown(Bitmap bitmap, int maxSide) {
        int side = Math.max(bitmap.getWidth(), bitmap.getHeight());
        if (side <= maxSide) return bitmap;
        float scale = (float) maxSide / side;
        return Bitmap.createScaledBitmap(bitmap,
                Math.max(1, Math.round(bitmap.getWidth() * scale)),
                Math.max(1, Math.round(bitmap.getHeight() * scale)), true);
    }

    private static byte[] toJpeg(Bitmap bitmap, int quality) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
        return out.toByteArray();
    }
}
