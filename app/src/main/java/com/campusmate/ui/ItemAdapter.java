package com.campusmate.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.campusmate.R;
import com.campusmate.data.Repositories;
import com.campusmate.databinding.ItemPostCardBinding;
import com.campusmate.databinding.ItemPostCompactBinding;
import com.campusmate.logic.Formatters;
import com.campusmate.model.AppUser;
import com.campusmate.model.Item;

import java.util.Objects;

/**
 * Shows posts in a RecyclerView. "Large" cards are for the Home feed;
 * "compact" cards (small thumbnail) are for matches and My posts.
 * ListAdapter + DiffUtil only redraws the cards that really changed.
 */
public class ItemAdapter extends ListAdapter<Item, ItemAdapter.Holder> {

    public interface OnItemClick {
        void onClick(Item item, View sharedImage);
    }

    public interface OnMoreClick {
        void onMore(Item item, View anchor);
    }

    /** Optional extra line under a compact card (for example "Matched on: ..."). */
    public interface ExtraText {
        @Nullable
        String of(Item item);
    }

    private final boolean compact;
    private final OnItemClick onClick;
    @Nullable
    private ExtraText extraText;
    @Nullable
    private OnMoreClick onMore;

    public ItemAdapter(boolean compact, OnItemClick onClick) {
        super(DIFF);
        this.compact = compact;
        this.onClick = onClick;
    }

    public void setExtraText(@Nullable ExtraText extraText) {
        this.extraText = extraText;
    }

    public void setOnMoreClick(@Nullable OnMoreClick onMore) {
        this.onMore = onMore;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (compact) {
            ItemPostCompactBinding b = ItemPostCompactBinding.inflate(inflater, parent, false);
            return new Holder(b.getRoot(), b.image, b.badge, b.title, b.meta, b.extra, b.more);
        }
        ItemPostCardBinding b = ItemPostCardBinding.inflate(inflater, parent, false);
        Holder h = new Holder(b.getRoot(), b.image, b.badge, b.title, b.meta, null, null);
        h.ownerAvatar = b.ownerAvatar;
        h.ownerName = b.ownerName;
        h.time = b.time;
        return h;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Item item = getItem(position);
        Ui.bindItemImage(h.image, item);
        h.image.setTransitionName(Ui.transitionName(item));
        Ui.bindBadge(h.badge, item);
        h.title.setText(item.title);
        if (h.time != null && h.ownerName != null && h.ownerAvatar != null) {
            // Big card: place on its own line, then who posted it and when.
            h.meta.setText(item.location);
            AppUser me = Repositories.auth().currentUser();
            boolean mine = me != null && me.uid.equals(item.ownerId);
            h.ownerName.setText(mine ? h.itemView.getContext().getString(R.string.you) : item.ownerName);
            h.ownerAvatar.setText(Ui.initials(item.ownerName));
            h.time.setText(Formatters.relativeDay(item.date, System.currentTimeMillis()));
        } else {
            h.meta.setText(Ui.itemMeta(item));
        }
        h.itemView.setOnClickListener(v -> onClick.onClick(item, h.image));

        if (h.extra != null) {
            String extra = extraText == null ? null : extraText.of(item);
            h.extra.setVisibility(extra == null ? View.GONE : View.VISIBLE);
            h.extra.setText(extra);
        }
        if (h.more != null) {
            h.more.setVisibility(onMore == null ? View.GONE : View.VISIBLE);
            h.more.setOnClickListener(v -> {
                if (onMore != null) onMore.onMore(item, v);
            });
        }
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView badge;
        final TextView title;
        final TextView meta;
        @Nullable
        final TextView extra;
        @Nullable
        final View more;
        // Footer of the big card only (null on compact cards).
        @Nullable
        TextView ownerAvatar;
        @Nullable
        TextView ownerName;
        @Nullable
        TextView time;

        Holder(View root, ImageView image, TextView badge, TextView title, TextView meta,
               @Nullable TextView extra, @Nullable View more) {
            super(root);
            this.image = image;
            this.badge = badge;
            this.title = title;
            this.meta = meta;
            this.extra = extra;
            this.more = more;
        }
    }

    private static final DiffUtil.ItemCallback<Item> DIFF = new DiffUtil.ItemCallback<Item>() {
        @Override
        public boolean areItemsTheSame(@NonNull Item a, @NonNull Item b) {
            return a.id.equals(b.id);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Item a, @NonNull Item b) {
            return a.title.equals(b.title) && a.status.equals(b.status) && a.type.equals(b.type)
                    && a.category.equals(b.category) && a.location.equals(b.location) && a.date == b.date
                    && Objects.equals(a.imageBase64, b.imageBase64);
        }
    };
}
