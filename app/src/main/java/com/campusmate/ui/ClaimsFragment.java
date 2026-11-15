package com.campusmate.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.campusmate.R;
import com.campusmate.data.Callback;
import com.campusmate.data.Repositories;
import com.campusmate.databinding.FragmentClaimsBinding;
import com.campusmate.model.AppUser;
import com.campusmate.model.Claim;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Two lists: claims other students sent on my posts (approve / reject),
 * and claims I sent. Approved claims open the chat.
 */
public class ClaimsFragment extends Fragment implements ClaimAdapter.Listener {

    private FragmentClaimsBinding b;
    private ClaimAdapter adapter;
    private AppUser user;
    private List<Claim> claims;
    private int tab; // 0 = on my posts, 1 = sent by me

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle saved) {
        b = FragmentClaimsBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saved) {
        user = Repositories.auth().currentUser();
        if (user == null) return;

        adapter = new ClaimAdapter(user.uid, this);
        b.list.setLayoutManager(new LinearLayoutManager(requireContext()));
        b.list.setAdapter(adapter);

        b.tabs.addTab(b.tabs.newTab().setText(R.string.tab_requests));
        b.tabs.addTab(b.tabs.newTab().setText(R.string.tab_my_claims));
        b.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab t) {
                tab = t.getPosition();
                render();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab t) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab t) {
            }
        });

        Repositories.claims().claims().observe(getViewLifecycleOwner(), list -> {
            claims = list;
            render();
        });
    }

    private void render() {
        if (b == null || claims == null) return;
        List<Claim> onMyPosts = new ArrayList<>();
        List<Claim> sentByMe = new ArrayList<>();
        for (Claim c : claims) {
            if (c.isOwner(user.uid)) onMyPosts.add(c);
            else sentByMe.add(c);
        }
        b.tabs.getTabAt(0).setText(getString(R.string.tab_requests) + " (" + onMyPosts.size() + ")");
        b.tabs.getTabAt(1).setText(getString(R.string.tab_my_claims) + " (" + sentByMe.size() + ")");

        List<Claim> shown = tab == 0 ? onMyPosts : sentByMe;
        adapter.submitList(shown);
        boolean empty = shown.isEmpty();
        b.list.setVisibility(empty ? View.GONE : View.VISIBLE);
        b.emptyScroll.setVisibility(empty ? View.VISIBLE : View.GONE);
        b.empty.getRoot().setVisibility(empty ? View.VISIBLE : View.GONE);
        b.empty.emptyIcon.setImageResource(R.drawable.ic_inbox);
        b.empty.emptyTitle.setText(tab == 0 ? R.string.claims_empty_requests_title : R.string.claims_empty_mine_title);
        b.empty.emptyMessage.setText(tab == 0 ? R.string.claims_empty_requests_message : R.string.claims_empty_mine_message);
    }

    /** Shows the tab with claims on my posts (used by notifications). */
    public void showRequestsTab() {
        if (b != null) b.tabs.selectTab(b.tabs.getTabAt(0));
    }

    @Override
    public void onApprove(Claim claim) {
        Repositories.claims().setStatus(claim.id, Claim.APPROVED, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (b == null) return;
                Snackbar.make(b.getRoot(), R.string.approved_message, Snackbar.LENGTH_LONG)
                        .setAnchorView(requireActivity().findViewById(R.id.fab))
                        .setAction(R.string.action_open_chat, v -> onOpenChat(claim))
                        .show();
            }

            @Override
            public void onError(String message) {
                snack(message);
            }
        });
    }

    @Override
    public void onReject(Claim claim) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.reject)
                .setMessage(getString(R.string.answer_label, claim.answer))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.reject, (d, w) ->
                        Repositories.claims().setStatus(claim.id, Claim.REJECTED, new Callback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                snack(getString(R.string.rejected_message));
                            }

                            @Override
                            public void onError(String message) {
                                snack(message);
                            }
                        }))
                .show();
    }

    @Override
    public void onOpenChat(Claim claim) {
        startActivity(ChatActivity.newIntent(requireContext(), claim.id));
    }

    @Override
    public void onOpenItem(Claim claim) {
        startActivity(ItemDetailActivity.newIntent(requireContext(), claim.itemId));
    }

    private void snack(String message) {
        if (b == null) return;
        Snackbar.make(b.getRoot(), message, Snackbar.LENGTH_SHORT)
                .setAnchorView(requireActivity().findViewById(R.id.fab))
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }
}
