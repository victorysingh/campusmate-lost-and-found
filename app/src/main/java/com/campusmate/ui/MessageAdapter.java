package com.campusmate.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.campusmate.databinding.ItemMessageMineBinding;
import com.campusmate.databinding.ItemMessageTheirsBinding;
import com.campusmate.logic.Formatters;
import com.campusmate.model.ChatMessage;

/** Chat bubbles: my messages on the right, the other person's on the left (two view types). */
public class MessageAdapter extends ListAdapter<ChatMessage, MessageAdapter.Holder> {

    private static final int MINE = 1;
    private static final int THEIRS = 2;

    private final String myUid;

    public MessageAdapter(String myUid) {
        super(DIFF);
        this.myUid = myUid;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).senderId.equals(myUid) ? MINE : THEIRS;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == MINE) {
            ItemMessageMineBinding b = ItemMessageMineBinding.inflate(inflater, parent, false);
            return new Holder(b.getRoot(), b.text, b.time);
        }
        ItemMessageTheirsBinding b = ItemMessageTheirsBinding.inflate(inflater, parent, false);
        return new Holder(b.getRoot(), b.text, b.time);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        ChatMessage m = getItem(position);
        h.text.setText(m.text);
        h.time.setText(Formatters.time(m.createdAt));
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final TextView text;
        final TextView time;

        Holder(View root, TextView text, TextView time) {
            super(root);
            this.text = text;
            this.time = time;
        }
    }

    private static final DiffUtil.ItemCallback<ChatMessage> DIFF = new DiffUtil.ItemCallback<ChatMessage>() {
        @Override
        public boolean areItemsTheSame(@NonNull ChatMessage a, @NonNull ChatMessage b) {
            return a.id.equals(b.id);
        }

        @Override
        public boolean areContentsTheSame(@NonNull ChatMessage a, @NonNull ChatMessage b) {
            return a.text.equals(b.text);
        }
    };
}
