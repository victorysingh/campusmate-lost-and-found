package com.campusmate.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import com.campusmate.R;
import com.campusmate.data.Repositories;
import com.campusmate.logic.Formatters;
import com.campusmate.logic.ImageUtils;
import com.campusmate.model.Claim;
import com.campusmate.model.Item;

/** Small helpers shared by many screens, so each screen stays short. */
public final class Ui {

    private Ui() {
    }

    /** Icon for each category (chips, placeholders, info rows). */
    @DrawableRes
    public static int categoryIcon(String category) {
        switch (category) {
            case "Electronics":
                return R.drawable.ic_cat_electronics;
            case "ID Card":
                return R.drawable.ic_cat_id_card;
            case "Wallet":
                return R.drawable.ic_cat_wallet;
            case "Keys":
                return R.drawable.ic_cat_keys;
            case "Books & Notes":
                return R.drawable.ic_cat_books;
            case "Bags":
                return R.drawable.ic_cat_bags;
            case "Clothing":
                return R.drawable.ic_cat_clothing;
            case "Water Bottle":
                return R.drawable.ic_cat_bottle;
            default:
                return R.drawable.ic_cat_other;
        }
    }

    /** Two colours per category for the placeholder gradient when a post has no photo. */
    private static int[] categoryGradient(String category) {
        switch (category) {
            case "Electronics":
                return new int[]{0xFF3B5BDB, 0xFF15AABF};
            case "ID Card":
                return new int[]{0xFFE8590C, 0xFFF59F00};
            case "Wallet":
                return new int[]{0xFF5F3DC4, 0xFF9C36B5};
            case "Keys":
                return new int[]{0xFFC2255C, 0xFFF06595};
            case "Books & Notes":
                return new int[]{0xFF2B8A3E, 0xFF74B816};
            case "Bags":
                return new int[]{0xFF862E9C, 0xFFDA77F2};
            case "Clothing":
                return new int[]{0xFF495057, 0xFF868E96};
            case "Water Bottle":
                return new int[]{0xFF1971C2, 0xFF4DABF7};
            default:
                return new int[]{0xFF0A5E60, 0xFF14A3A5};
        }
    }

    /** Shows the photo, or a coloured gradient with the category icon. */
    public static void bindItemImage(ImageView view, Item item) {
        Bitmap bitmap = item.hasImage() ? ImageUtils.fromBase64(item.imageBase64) : null;
        if (bitmap != null) {
            view.setBackground(null);
            ImageViewCompat.setImageTintList(view, null);
            view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            view.setImageBitmap(bitmap);
        } else {
            GradientDrawable gradient = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                    categoryGradient(item.category));
            view.setBackground(gradient);
            view.setScaleType(ImageView.ScaleType.CENTER);
            view.setImageResource(categoryIcon(item.category));
            ImageViewCompat.setImageTintList(view, ColorStateList.valueOf(Color.argb(230, 255, 255, 255)));
        }
    }

    /** Same transition name on the card and on the detail screen = shared element animation. */
    public static String transitionName(Item item) {
        return "item_image_" + item.id;
    }

    /** LOST / FOUND / RETURNED pill. */
    public static void bindBadge(TextView badge, Item item) {
        int color;
        int text;
        if (item.isReturned()) {
            color = R.color.status_returned;
            text = R.string.badge_returned;
        } else if (item.isLost()) {
            color = R.color.status_lost;
            text = R.string.badge_lost;
        } else {
            color = R.color.status_found;
            text = R.string.badge_found;
        }
        badge.setText(text);
        badge.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(badge.getContext(), color)));
    }

    /** Pending / Approved / Rejected pill. */
    public static void bindClaimStatus(TextView badge, String status) {
        int color;
        int text;
        if (Claim.APPROVED.equals(status)) {
            color = R.color.status_approved;
            text = R.string.status_approved;
        } else if (Claim.REJECTED.equals(status)) {
            color = R.color.status_rejected;
            text = R.string.status_rejected;
        } else {
            color = R.color.status_pending;
            text = R.string.status_pending;
        }
        badge.setText(text);
        badge.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(badge.getContext(), color)));
    }

    /** "Library · Today" */
    public static String itemMeta(Item item) {
        return item.location + " · " + Formatters.relativeDay(item.date, System.currentTimeMillis());
    }

    public static void showDemoBanner(View banner) {
        banner.setVisibility(Repositories.isDemoMode() ? View.VISIBLE : View.GONE);
    }

    /** True when the phone has a working internet connection. */
    public static boolean isOnline(Context context) {
        ConnectivityManager cm = ContextCompat.getSystemService(context, ConnectivityManager.class);
        if (cm == null) return true;
        NetworkCapabilities caps = cm.getNetworkCapabilities(cm.getActiveNetwork());
        return caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
    }

    public static void hideKeyboard(View view) {
        InputMethodManager imm = ContextCompat.getSystemService(view.getContext(), InputMethodManager.class);
        if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static String text(TextView view) {
        return view.getText() == null ? "" : view.getText().toString();
    }
}
