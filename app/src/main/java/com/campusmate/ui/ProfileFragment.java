package com.campusmate.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.campusmate.BuildConfig;
import com.campusmate.R;
import com.campusmate.data.FeedState;
import com.campusmate.data.Repositories;
import com.campusmate.data.Session;
import com.campusmate.databinding.FragmentProfileBinding;
import com.campusmate.logic.NotificationHelper;
import com.campusmate.logic.ThemePrefs;
import com.campusmate.model.AppUser;
import com.campusmate.model.Claim;
import com.campusmate.model.Item;

import java.util.List;

/** Who is logged in, a few numbers, dark mode, notifications and log out. */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding b;
    private AppUser user;
    private FeedState feed;
    private List<Claim> claims;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle saved) {
        b = FragmentProfileBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saved) {
        user = Repositories.auth().currentUser();
        if (user == null) return;

        b.avatar.setText(user.initials());
        b.name.setText(user.name);
        b.email.setText(user.email);
        b.dataSource.setText(Repositories.isDemoMode() ? R.string.data_demo : R.string.data_firebase);
        b.version.setText(getString(R.string.app_version, BuildConfig.VERSION_NAME));

        // Set the switch before adding the listener, so it does not fire on start.
        b.darkMode.setChecked(ThemePrefs.isDark(requireContext()));
        b.darkMode.setOnCheckedChangeListener((button, checked) -> ThemePrefs.setDark(requireContext(), checked));

        b.notificationsRow.setOnClickListener(v -> onNotificationsClicked());
        b.logoutButton.setOnClickListener(v -> logOut());

        Repositories.items().feed().observe(getViewLifecycleOwner(), s -> {
            feed = s;
            renderStats();
        });
        Repositories.claims().claims().observe(getViewLifecycleOwner(), list -> {
            claims = list;
            renderStats();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // The student may have changed notification settings outside the app.
        if (b != null) b.notificationsStatus.setText(NotificationHelper.canNotify(requireContext())
                ? R.string.notifications_on : R.string.notifications_off);
    }

    private void renderStats() {
        if (b == null) return;
        int posts = 0;
        int returned = 0;
        if (feed != null) {
            for (Item item : feed.items) {
                if (!item.ownerId.equals(user.uid)) continue;
                posts++;
                if (item.isReturned()) returned++;
            }
        }
        b.stats.setText(getString(R.string.stats, posts, returned, claims == null ? 0 : claims.size()));
    }

    private void onNotificationsClicked() {
        if (NotificationHelper.canNotify(requireContext())) return;
        if (Build.VERSION.SDK_INT >= 33 && requireActivity() instanceof MainActivity
                && shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
            ((MainActivity) requireActivity()).requestNotificationPermission();
            return;
        }
        // Otherwise open the app's notification settings page.
        Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().getPackageName());
        startActivity(intent);
    }

    private void logOut() {
        Session.logOut();
        startActivity(new Intent(requireContext(), LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }
}
