package com.campusmate.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.campusmate.R;
import com.campusmate.databinding.ItemClaimBinding;
import com.campusmate.model.Claim;

/** Claim cards with Approve / Reject (post owner) and Open chat (after approval). */
public class ClaimAdapter extends ListAdapter<Claim, ClaimAdapter.Holder> {

    public interface Listener {
        void onApprove(Claim claim);

        void onReject(Claim claim);

        void onOpenChat(Claim claim);

        void onOpenItem(Claim claim);
    }

    private final String myUid;
    private final Listener listener;

    public ClaimAdapter(String myUid, Listener listener) {
        super(DIFF);
        this.myUid = myUid;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemClaimBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Claim claim = getItem(position);
        ItemClaimBinding b = h.binding;
        boolean iAmOwner = claim.isOwner(myUid);

        b.itemTitle.setText(claim.itemTitle);
        Ui.bindClaimStatus(b.status, claim.status);
        b.person.setText(b.getRoot().getContext().getString(
                iAmOwner ? R.string.claim_from : R.string.claim_to, claim.otherPersonName(myUid)));
        b.question.setText(b.getRoot().getContext().getString(R.string.question_label, claim.question));
        b.answer.setText(b.getRoot().getContext().getString(R.string.answer_label, claim.answer));

        b.decisionButtons.setVisibility(iAmOwner && claim.isPending() ? View.VISIBLE : View.GONE);
        b.chatButton.setVisibility(claim.isApproved() ? View.VISIBLE : View.GONE);

        b.approveButton.setOnClickListener(v -> listener.onApprove(claim));
        b.rejectButton.setOnClickListener(v -> listener.onReject(claim));
        b.chatButton.setOnClickListener(v -> listener.onOpenChat(claim));
        b.getRoot().setOnClickListener(v -> listener.onOpenItem(claim));
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ItemClaimBinding binding;

        Holder(ItemClaimBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private static final DiffUtil.ItemCallback<Claim> DIFF = new DiffUtil.ItemCallback<Claim>() {
        @Override
        public boolean areItemsTheSame(@NonNull Claim a, @NonNull Claim b) {
            return a.id.equals(b.id);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Claim a, @NonNull Claim b) {
            return a.status.equals(b.status) && a.answer.equals(b.answer) && a.itemTitle.equals(b.itemTitle);
        }
    };
}
