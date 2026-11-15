package com.campusmate.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.campusmate.R;
import com.campusmate.data.Callback;
import com.campusmate.data.Repositories;
import com.campusmate.databinding.ActivityChatBinding;
import com.campusmate.logic.Validators;
import com.campusmate.model.AppUser;
import com.campusmate.model.ChatMessage;
import com.campusmate.model.Claim;
import com.campusmate.model.Item;
import com.google.android.material.snackbar.Snackbar;

/**
 * Chat between the post owner and the claimant. It opens only after the
 * owner approves the claim; then they agree where to hand the item over.
 */
public class ChatActivity extends AppCompatActivity {

    private static final String EXTRA_CLAIM_ID = "claim_id";

    private ActivityChatBinding b;
    private MessageAdapter adapter;
    private AppUser me;
    private String claimId;
    private Claim claim;

    public static Intent newIntent(Context context, String claimId) {
        return new Intent(context, ChatActivity.class).putExtra(EXTRA_CLAIM_ID, claimId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        me = Repositories.auth().currentUser();
        claimId = getIntent().getStringExtra(EXTRA_CLAIM_ID);
        if (me == null || claimId == null) {
            finish();
            return;
        }

        b.toolbar.setNavigationOnClickListener(v -> finish());
        b.toolbar.inflateMenu(R.menu.menu_chat);
        b.toolbar.setOnMenuItemClickListener(this::onMenuItem);

        adapter = new MessageAdapter(me.uid);
        LinearLayoutManager layout = new LinearLayoutManager(this);
        layout.setStackFromEnd(true); // newest message at the bottom, like every chat app
        b.messages.setLayoutManager(layout);
        b.messages.setAdapter(adapter);

        b.sendButton.setOnClickListener(v -> send());

        Repositories.claims().claims().observe(this, list -> {
            claim = Repositories.claims().findById(claimId);
            bindClaim();
        });
        Repositories.items().feed().observe(this, s -> updateMenu());
        Repositories.claims().messages(claimId).observe(this, messages -> {
            adapter.submitList(messages, () -> {
                if (!messages.isEmpty()) b.messages.scrollToPosition(messages.size() - 1);
            });
            b.empty.setVisibility(messages.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void bindClaim() {
        if (claim == null) return; // still loading
        b.toolbar.setTitle(getString(R.string.chat_with, claim.otherPersonName(me.uid)));
        b.toolbar.setSubtitle(claim.itemTitle);
        b.summaryText.setText(getString(R.string.question_label, claim.question) + "\n"
                + getString(R.string.answer_label, claim.answer));
        Ui.bindClaimStatus(b.status, claim.status);

        boolean open = claim.isApproved();
        b.inputRow.setVisibility(open ? View.VISIBLE : View.GONE);
        b.lockedNotice.setVisibility(open ? View.GONE : View.VISIBLE);
        updateMenu();
    }

    /** Only the post owner can mark the item as returned, and only once. */
    private void updateMenu() {
        MenuItem markReturned = b.toolbar.getMenu().findItem(R.id.action_mark_returned);
        if (markReturned == null) return;
        Item item = claim == null ? null : Repositories.items().findById(claim.itemId);
        markReturned.setVisible(claim != null && claim.isOwner(me.uid) && claim.isApproved()
                && item != null && !item.isReturned());
    }

    private boolean onMenuItem(MenuItem menuItem) {
        if (menuItem.getItemId() != R.id.action_mark_returned || claim == null) return false;
        Repositories.items().setStatus(claim.itemId, Item.RETURNED, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Snackbar.make(b.root, R.string.marked_returned, Snackbar.LENGTH_LONG).setAnchorView(b.inputRow).show();
            }

            @Override
            public void onError(String message) {
                Snackbar.make(b.root, message, Snackbar.LENGTH_LONG).show();
            }
        });
        return true;
    }

    private void send() {
        String text = Ui.text(b.input).trim();
        String error = Validators.chatMessage(text);
        if (error != null) {
            b.inputLayout.setError(error);
            return;
        }
        b.inputLayout.setError(null);
        b.sendButton.setEnabled(false);
        ChatMessage message = new ChatMessage(me.uid, me.name, text, System.currentTimeMillis());
        Repositories.claims().sendMessage(claimId, message, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                b.sendButton.setEnabled(true);
                b.input.setText("");
            }

            @Override
            public void onError(String msg) {
                b.sendButton.setEnabled(true);
                Snackbar.make(b.root, msg, Snackbar.LENGTH_LONG).setAnchorView(b.inputRow).show();
            }
        });
    }
}
