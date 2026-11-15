package com.campusmate.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.campusmate.R;
import com.campusmate.data.Callback;
import com.campusmate.data.FeedState;
import com.campusmate.data.Repositories;
import com.campusmate.databinding.ActivityItemDetailBinding;
import com.campusmate.databinding.DialogClaimBinding;
import com.campusmate.databinding.ItemInfoRowBinding;
import com.campusmate.logic.Formatters;
import com.campusmate.logic.MatchService;
import com.campusmate.logic.Validators;
import com.campusmate.model.AppUser;
import com.campusmate.model.Claim;
import com.campusmate.model.Item;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * One post: big photo (shared-element animation from the card), status,
 * details, possible matches, and a sticky button to claim, chat or edit.
 */
public class ItemDetailActivity extends AppCompatActivity {

    private static final String EXTRA_ITEM_ID = "item_id";
    /** Optional snackbar text, e.g. "Posted. 1 possible match found!" */
    public static final String EXTRA_MESSAGE = "message";

    private ActivityItemDetailBinding b;
    private ItemAdapter matchesAdapter;
    private AppUser me;
    private String itemId;
    private Item item;
    private List<Item> allItems = new ArrayList<>();
    private Claim myClaim;
    private final Map<String, String> matchedWords = new HashMap<>();

    public static Intent newIntent(Context context, String itemId) {
        return new Intent(context, ItemDetailActivity.class).putExtra(EXTRA_ITEM_ID, itemId);
    }

    /** Opens the post with the card's photo flying into place. */
    public static void open(Activity activity, Item item, @Nullable View sharedImage) {
        Intent intent = newIntent(activity, item.id);
        if (sharedImage == null) {
            activity.startActivity(intent);
            return;
        }
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity, sharedImage, Ui.transitionName(item));
        activity.startActivity(intent, options.toBundle());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityItemDetailBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        me = Repositories.auth().currentUser();
        itemId = getIntent().getStringExtra(EXTRA_ITEM_ID);
        if (me == null || itemId == null) {
            finish();
            return;
        }
        b.image.setTransitionName("item_image_" + itemId);
        if (savedInstanceState == null) {
            Motion.fadeUp(b.content, 120);
            Motion.slideUp(b.actionButton, 220);
        }
        b.backButton.setOnClickListener(v -> supportFinishAfterTransition());

        matchesAdapter = new ItemAdapter(true, (match, image) -> ItemDetailActivity.open(this, match, image));
        matchesAdapter.setExtraText(match -> {
            String words = matchedWords.get(match.id);
            return words == null ? null : getString(R.string.matched_on, words);
        });
        b.matches.setLayoutManager(new LinearLayoutManager(this));
        b.matches.setAdapter(matchesAdapter);

        Repositories.items().feed().observe(this, this::onFeed);
        Repositories.claims().claims().observe(this, claims -> {
            myClaim = Repositories.claims().findById(Claim.idFor(itemId, me.uid));
            bind();
        });

        String message = getIntent().getStringExtra(EXTRA_MESSAGE);
        if (message != null && savedInstanceState == null) {
            Snackbar.make(b.root, message, Snackbar.LENGTH_LONG).setAnchorView(b.actionButton).show();
        }
    }

    private void onFeed(FeedState state) {
        if (state == null || state.loading) return;
        allItems = state.items;
        item = Repositories.items().findById(itemId);
        if (item == null) {
            Toast.makeText(this, R.string.deleted, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        bind();
    }

    private void bind() {
        if (item == null) return;
        boolean mine = item.ownerId.equals(me.uid);

        Ui.bindItemImage(b.image, item);
        Ui.bindBadge(b.badge, item);
        b.title.setText(item.title);
        b.description.setText(item.description);

        b.infoRows.removeAllViews();
        addInfoRow(Ui.categoryIcon(item.category), getString(R.string.detail_category), item.category);
        addInfoRow(R.drawable.ic_place, getString(R.string.detail_location), item.location);
        addInfoRow(R.drawable.ic_event, getString(item.isLost() ? R.string.detail_date_lost : R.string.detail_date_found),
                Formatters.date(item.date));
        addInfoRow(R.drawable.ic_person, getString(R.string.detail_posted_by),
                mine ? item.ownerName + " (you)" : item.ownerName);

        b.proofNote.setVisibility(!mine && !item.isLost() && !item.isReturned() ? View.VISIBLE : View.GONE);
        b.proofNote.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_verified, 0, 0, 0);

        b.myClaimCard.setVisibility(myClaim == null ? View.GONE : View.VISIBLE);
        if (myClaim != null) {
            Ui.bindClaimStatus(b.myClaimStatus, myClaim.status);
            b.myClaimText.setText(getString(R.string.answer_label, myClaim.answer));
        }

        List<MatchService.MatchResult> matches = MatchService.findMatches(item, allItems);
        matchedWords.clear();
        List<Item> matchItems = new ArrayList<>();
        for (MatchService.MatchResult m : matches) {
            matchItems.add(m.item);
            matchedWords.put(m.item.id, String.join(", ", m.commonWords));
        }
        b.matchesTitle.setText(getString(R.string.possible_matches, matches.size()));
        b.noMatches.setVisibility(matches.isEmpty() ? View.VISIBLE : View.GONE);
        matchesAdapter.submitList(matchItems);
        matchesAdapter.notifyDataSetChanged(); // "Matched on" lines may change

        bindActionButton(mine, matches.size());
    }

    private void addInfoRow(@DrawableRes int icon, String label, String value) {
        ItemInfoRowBinding row = ItemInfoRowBinding.inflate(LayoutInflater.from(this), b.infoRows, true);
        row.icon.setImageResource(icon);
        row.label.setText(label);
        row.value.setText(value);
    }

    /** What the sticky button says and does depends on who is looking. */
    private void bindActionButton(boolean mine, int matchCount) {
        Button button = b.actionButton;
        button.setEnabled(true);
        if (mine) {
            if (matchCount > 0) {
                button.setText(getResources().getQuantityString(R.plurals.action_view_matches, matchCount, matchCount));
                button.setOnClickListener(v -> scrollToMatches());
            } else {
                button.setText(R.string.action_edit_post);
                button.setOnClickListener(v -> startActivity(AddItemActivity.newIntent(this, item.id)));
            }
        } else if (myClaim != null && myClaim.isApproved()) {
            button.setText(R.string.action_open_chat);
            button.setOnClickListener(v -> startActivity(ChatActivity.newIntent(this, myClaim.id)));
        } else if (myClaim != null) {
            button.setText(myClaim.isPending() ? R.string.action_claim_pending : R.string.action_claim_rejected);
            button.setEnabled(false);
        } else if (item.isReturned()) {
            button.setText(R.string.action_already_returned);
            button.setEnabled(false);
        } else {
            button.setText(item.isLost() ? R.string.action_i_found_this : R.string.action_this_is_mine);
            button.setOnClickListener(v -> showClaimDialog());
        }
    }

    private void scrollToMatches() {
        b.appBar.setExpanded(false, true);
        b.scroll.post(() -> b.scroll.smoothScrollTo(0, b.matchesTitle.getTop()));
    }

    /**
     * FOUND post: the claimant answers the finder's proof question.
     * LOST post: the finder says where they found it.
     */
    private void showClaimDialog() {
        DialogClaimBinding d = DialogClaimBinding.inflate(getLayoutInflater());
        String question = item.isLost() ? getString(R.string.claim_lost_question)
                : (item.proofQuestion.isEmpty() ? "Describe one detail only the owner would know." : item.proofQuestion);
        d.question.setText(question);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(item.isLost() ? R.string.claim_lost_title : R.string.claim_found_title)
                .setView(d.getRoot())
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.claim_send, null) // set below, so errors keep the dialog open
                .create();
        dialog.setOnShowListener(x -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String answer = Ui.text(d.answer).trim();
            String error = Validators.claimAnswer(answer);
            d.answerLayout.setError(error);
            if (error != null) return;
            v.setEnabled(false);
            sendClaim(question, answer, dialog);
        }));
        dialog.show();
    }

    private void sendClaim(String question, String answer, AlertDialog dialog) {
        Claim claim = new Claim();
        claim.itemId = item.id;
        claim.itemTitle = item.title;
        claim.itemType = item.type;
        claim.ownerId = item.ownerId;
        claim.ownerName = item.ownerName;
        claim.claimantId = me.uid;
        claim.claimantName = me.name;
        claim.question = question;
        claim.answer = answer;
        claim.createdAt = System.currentTimeMillis();
        claim.updatedAt = claim.createdAt;

        Repositories.claims().create(claim, new Callback<String>() {
            @Override
            public void onSuccess(String id) {
                dialog.dismiss();
                Snackbar.make(b.root, getString(R.string.claim_sent, item.ownerName), Snackbar.LENGTH_LONG)
                        .setAnchorView(b.actionButton).show();
            }

            @Override
            public void onError(String message) {
                dialog.dismiss();
                Snackbar.make(b.root, message, Snackbar.LENGTH_LONG).setAnchorView(b.actionButton).show();
            }
        });
    }
}
