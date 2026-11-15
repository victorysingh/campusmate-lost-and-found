package com.campusmate.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.campusmate.Config;
import com.campusmate.R;
import com.campusmate.data.FeedState;
import com.campusmate.data.Repositories;
import com.campusmate.databinding.FragmentHomeBinding;
import com.campusmate.model.AppUser;
import com.campusmate.model.Item;
import com.google.android.material.chip.Chip;
import com.google.android.material.color.MaterialColors;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Home feed: greeting, search, category chips, Lost / Found switch and the
 * list of posts. Pull down to refresh. Long-press the logo for the demo menu.
 */
public class HomeFragment extends Fragment {

    private FragmentHomeBinding b;
    private ItemAdapter adapter;
    private FeedState state;
    private String type = Item.LOST;
    private String category;      // null = all categories
    private String query = "";
    private boolean firstListShown;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle saved) {
        b = FragmentHomeBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saved) {
        AppUser user = Repositories.auth().currentUser();
        b.greeting.setText(getString(R.string.greeting, user == null ? "there" : user.firstName()));
        b.logo.setOnLongClickListener(v -> {
            DemoMenu.show(requireActivity());
            return true;
        });

        adapter = new ItemAdapter(false, (item, image) -> ItemDetailActivity.open(requireActivity(), item, image));
        b.list.setLayoutManager(new LinearLayoutManager(requireContext()));
        b.list.setAdapter(adapter);

        addCategoryChips();
        b.categoryChips.setOnCheckedStateChangeListener((group, ids) -> {
            category = ids.isEmpty() ? null : (String) group.findViewById(ids.get(0)).getTag();
            render();
        });

        b.typeToggle.check(R.id.lost_button);
        b.typeToggle.addOnButtonCheckedListener((group, id, checked) -> {
            if (!checked) return;
            type = id == R.id.lost_button ? Item.LOST : Item.FOUND;
            render();
            b.list.scrollToPosition(0);
        });

        b.search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                query = s.toString().trim().toLowerCase(Locale.ROOT);
                render();
            }
        });

        b.swipeRefresh.setColorSchemeColors(MaterialColors.getColor(b.swipeRefresh, androidx.appcompat.R.attr.colorPrimary));
        b.swipeRefresh.setOnRefreshListener(() -> Repositories.items().refresh());
        // The list sits inside a FrameLayout, so tell the refresh layout when the list can still scroll up.
        b.swipeRefresh.setOnChildScrollUpCallback((parent, child) ->
                b.list.getVisibility() == View.VISIBLE && b.list.canScrollVertically(-1));

        Repositories.items().feed().observe(getViewLifecycleOwner(), s -> {
            state = s;
            render();
        });
    }

    private void addCategoryChips() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (String name : Config.CATEGORIES) {
            Chip chip = (Chip) inflater.inflate(R.layout.chip_filter, b.categoryChips, false);
            chip.setId(View.generateViewId());
            chip.setText(name);
            chip.setTag(name);
            chip.setChipIcon(ContextCompat.getDrawable(requireContext(), Ui.categoryIcon(name)));
            b.categoryChips.addView(chip);
        }
    }

    /** Draws the screen for the current data and filters. */
    private void render() {
        if (b == null || state == null) return;

        if (state.loading) {
            if (b.swipeRefresh.isRefreshing()) return; // keep the old list under the spinner
            b.skeleton.getRoot().setVisibility(View.VISIBLE);
            b.list.setVisibility(View.GONE);
            b.emptyScroll.setVisibility(View.GONE);
            b.lostButton.setText(getString(R.string.lost_count, 0));
            b.foundButton.setText(getString(R.string.found_count, 0));
            return;
        }
        b.swipeRefresh.setRefreshing(false);
        b.skeleton.getRoot().setVisibility(View.GONE);

        if (state.error != null) {
            showEmpty(R.drawable.ic_wifi_off, getString(R.string.error_title), state.error,
                    getString(R.string.try_again), v -> Repositories.items().refresh());
            return;
        }

        int lost = 0;
        int found = 0;
        List<Item> shown = new ArrayList<>();
        for (Item item : state.items) {
            if (!matchesFilters(item)) continue;
            if (item.isLost()) lost++;
            else found++;
            if (item.type.equals(type)) shown.add(item);
        }
        b.lostButton.setText(getString(R.string.lost_count, lost));
        b.foundButton.setText(getString(R.string.found_count, found));

        adapter.submitList(shown);
        if (!shown.isEmpty()) {
            b.list.setVisibility(View.VISIBLE);
            b.emptyScroll.setVisibility(View.GONE);
            if (!firstListShown) {
                firstListShown = true;
                b.list.scheduleLayoutAnimation(); // cards slide in the first time
            }
            return;
        }

        boolean filtering = category != null || !query.isEmpty();
        if (filtering) {
            showEmpty(R.drawable.ic_search, getString(R.string.empty_search_title),
                    getString(R.string.empty_search_message), getString(R.string.clear_filters), v -> clearFilters());
        } else {
            showEmpty(Item.LOST.equals(type) ? R.drawable.ic_help : R.drawable.ic_check_circle,
                    getString(Item.LOST.equals(type) ? R.string.empty_lost_title : R.string.empty_found_title),
                    getString(R.string.empty_feed_message), getString(R.string.post_an_item),
                    v -> startActivity(AddItemActivity.newIntent(requireContext(), null)));
        }
    }

    private boolean matchesFilters(Item item) {
        if (category != null && !category.equals(item.category)) return false;
        if (query.isEmpty()) return true;
        String text = (item.title + " " + item.description + " " + item.location + " " + item.category)
                .toLowerCase(Locale.ROOT);
        return text.contains(query);
    }

    private void showEmpty(int icon, String title, String message, String action, View.OnClickListener onAction) {
        b.list.setVisibility(View.GONE);
        b.emptyScroll.setVisibility(View.VISIBLE);
        b.empty.getRoot().setVisibility(View.VISIBLE);
        b.empty.emptyIcon.setImageResource(icon);
        b.empty.emptyTitle.setText(title);
        b.empty.emptyMessage.setText(message);
        b.empty.emptyAction.setVisibility(View.VISIBLE);
        b.empty.emptyAction.setText(action);
        b.empty.emptyAction.setOnClickListener(onAction);
    }

    private void clearFilters() {
        b.categoryChips.clearCheck();
        b.search.setText("");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }
}
