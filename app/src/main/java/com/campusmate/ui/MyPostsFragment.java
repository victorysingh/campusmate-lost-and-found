package com.campusmate.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.campusmate.R;
import com.campusmate.data.Callback;
import com.campusmate.data.FeedState;
import com.campusmate.data.Repositories;
import com.campusmate.databinding.FragmentMyPostsBinding;
import com.campusmate.model.AppUser;
import com.campusmate.model.Claim;
import com.campusmate.model.Item;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Posts I made: edit, mark as returned, or delete (with a confirm dialog). */
public class MyPostsFragment extends Fragment {

    private FragmentMyPostsBinding b;
    private ItemAdapter adapter;
    private AppUser user;
    private FeedState feed;
    private final Map<String, Integer> waitingClaims = new HashMap<>(); // item id -> pending claims

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle saved) {
        b = FragmentMyPostsBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saved) {
        user = Repositories.auth().currentUser();

        adapter = new ItemAdapter(true, (item, image) -> ItemDetailActivity.open(requireActivity(), item, image));
        adapter.setExtraText(item -> {
            Integer waiting = waitingClaims.get(item.id);
            return waiting == null ? null : getResources().getQuantityString(R.plurals.claims_on_post, waiting, waiting);
        });
        adapter.setOnMoreClick(this::showMenu);
        b.list.setLayoutManager(new LinearLayoutManager(requireContext()));
        b.list.setAdapter(adapter);

        b.empty.emptyIcon.setImageResource(R.drawable.ic_list);
        b.empty.emptyTitle.setText(R.string.my_posts_empty_title);
        b.empty.emptyMessage.setText(R.string.my_posts_empty_message);
        b.empty.emptyAction.setVisibility(View.VISIBLE);
        b.empty.emptyAction.setText(R.string.post_an_item);
        b.empty.emptyAction.setOnClickListener(v -> startActivity(AddItemActivity.newIntent(requireContext(), null)));

        Repositories.items().feed().observe(getViewLifecycleOwner(), s -> {
            feed = s;
            render();
        });
        Repositories.claims().claims().observe(getViewLifecycleOwner(), this::countWaitingClaims);
    }

    private void countWaitingClaims(@Nullable List<Claim> claims) {
        waitingClaims.clear();
        if (claims != null && user != null) {
            for (Claim c : claims) {
                if (c.isOwner(user.uid) && c.isPending()) {
                    Integer n = waitingClaims.get(c.itemId);
                    waitingClaims.put(c.itemId, n == null ? 1 : n + 1);
                }
            }
        }
        adapter.notifyDataSetChanged(); // only the extra line changes
    }

    private void render() {
        if (b == null || feed == null || feed.loading || user == null) return;
        List<Item> mine = new ArrayList<>();
        for (Item item : feed.items) if (item.ownerId.equals(user.uid)) mine.add(item);
        adapter.submitList(mine);
        boolean empty = mine.isEmpty();
        b.list.setVisibility(empty ? View.GONE : View.VISIBLE);
        b.emptyScroll.setVisibility(empty ? View.VISIBLE : View.GONE);
        b.empty.getRoot().setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void showMenu(Item item, View anchor) {
        PopupMenu menu = new PopupMenu(requireContext(), anchor);
        menu.inflate(R.menu.menu_my_post);
        menu.getMenu().findItem(R.id.action_toggle_returned)
                .setTitle(item.isReturned() ? R.string.reopen : R.string.mark_returned);
        menu.setOnMenuItemClickListener((MenuItem m) -> {
            int id = m.getItemId();
            if (id == R.id.action_edit) {
                startActivity(AddItemActivity.newIntent(requireContext(), item.id));
            } else if (id == R.id.action_toggle_returned) {
                toggleReturned(item);
            } else if (id == R.id.action_delete) {
                confirmDelete(item);
            }
            return true;
        });
        menu.show();
    }

    private void toggleReturned(Item item) {
        String newStatus = item.isReturned() ? Item.OPEN : Item.RETURNED;
        Repositories.items().setStatus(item.id, newStatus, snackbarCallback(
                newStatus.equals(Item.RETURNED) ? R.string.marked_returned : R.string.marked_open));
    }

    private void confirmDelete(Item item) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_title)
                .setMessage(getString(R.string.delete_message, item.title))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (d, w) ->
                        Repositories.items().delete(item.id, snackbarCallback(R.string.deleted)))
                .show();
    }

    private Callback<Void> snackbarCallback(int successMessage) {
        return new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                snack(getString(successMessage));
            }

            @Override
            public void onError(String message) {
                snack(message);
            }
        };
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
