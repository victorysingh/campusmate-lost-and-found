package com.campusmate.ui;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.widget.ImageViewCompat;
import androidx.lifecycle.ViewModelProvider;

import com.campusmate.Config;
import com.campusmate.R;
import com.campusmate.data.Callback;
import com.campusmate.data.FeedState;
import com.campusmate.data.Repositories;
import com.campusmate.databinding.ActivityAddItemBinding;
import com.campusmate.logic.Formatters;
import com.campusmate.logic.ImageUtils;
import com.campusmate.logic.MatchService;
import com.campusmate.logic.Validators;
import com.campusmate.model.AppUser;
import com.campusmate.model.Item;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Post a new item, or edit one, in 3 steps: Photo -> Details -> Location.
 * Each step is checked before moving on.
 */
public class AddItemActivity extends AppCompatActivity {

    private static final String EXTRA_EDIT_ID = "edit_id";
    private static final String SEPARATOR = " · ";

    private ActivityAddItemBinding b;
    private AddItemViewModel vm;
    private AppUser me;
    @Nullable
    private Item editing; // null when posting a new item
    private boolean saving;

    private final ExecutorService background = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    private ActivityResultLauncher<PickVisualMediaRequest> pickPhoto;
    private ActivityResultLauncher<Uri> takePhoto;

    public static Intent newIntent(Context context, @Nullable String editItemId) {
        Intent intent = new Intent(context, AddItemActivity.class);
        if (editItemId != null) intent.putExtra(EXTRA_EDIT_ID, editItemId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityAddItemBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        vm = new ViewModelProvider(this).get(AddItemViewModel.class);
        me = Repositories.auth().currentUser();
        if (me == null) {
            finish();
            return;
        }

        String editId = getIntent().getStringExtra(EXTRA_EDIT_ID);
        if (editId != null) {
            editing = Repositories.items().findById(editId);
            if (editing == null) {
                finish();
                return;
            }
            b.toolbar.setTitle(R.string.edit_title);
        }

        b.toolbar.setNavigationOnClickListener(v -> finish());
        b.category.setSimpleItems(Config.CATEGORIES);
        b.place.setSimpleItems(Config.CAMPUS_LOCATIONS);
        b.typeToggle.addOnButtonCheckedListener((group, id, checked) -> {
            if (checked) updateForType();
        });
        b.date.setOnClickListener(v -> pickDate());

        pickPhoto = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) usePhoto(uri);
        });
        takePhoto = registerForActivityResult(new ActivityResultContracts.TakePicture(), ok -> {
            if (Boolean.TRUE.equals(ok) && vm.cameraUri != null) usePhoto(vm.cameraUri);
        });
        b.galleryButton.setOnClickListener(v -> pickPhoto.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build()));
        b.cameraButton.setOnClickListener(v -> openCamera());
        b.removePhoto.setOnClickListener(v -> {
            vm.photoBase64 = null;
            showPhoto();
        });

        b.nextButton.setOnClickListener(v -> next());
        b.backButton.setOnClickListener(v -> showStep(vm.step - 1));
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (vm.step > 0) showStep(vm.step - 1);
                else finish();
            }
        });

        if (editing != null && !vm.loadedForEdit) fillFromPost(editing);
        updateForType();
        showDate();
        showPhoto();
        showStep(vm.step);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        background.shutdown();
    }

    private void fillFromPost(Item item) {
        vm.loadedForEdit = true;
        vm.photoBase64 = item.imageBase64;
        vm.date = item.date;
        b.typeToggle.check(item.isLost() ? R.id.type_lost : R.id.type_found);
        b.title.setText(item.title);
        b.category.setText(item.category, false);
        b.description.setText(item.description);
        b.proofQuestion.setText(item.proofQuestion);
        // "Library · 1st floor" -> place "Library", detail "1st floor"
        String place = item.location;
        String detail = "";
        int sep = item.location.indexOf(SEPARATOR);
        if (sep > 0) {
            place = item.location.substring(0, sep);
            detail = item.location.substring(sep + SEPARATOR.length());
        }
        if (Arrays.asList(Config.CAMPUS_LOCATIONS).contains(place)) {
            b.place.setText(place, false);
            b.placeDetail.setText(detail);
        } else {
            b.place.setText("Other", false);
            b.placeDetail.setText(item.location);
        }
    }

    private boolean isFound() {
        return b.typeToggle.getCheckedButtonId() == R.id.type_found;
    }

    /** "Date found" and the proof question only make sense for FOUND posts. */
    private void updateForType() {
        b.dateLayout.setHint(isFound() ? R.string.date_found_hint : R.string.date_lost_hint);
        b.proofLayout.setVisibility(isFound() ? View.VISIBLE : View.GONE);
    }

    // ---- Steps -------------------------------------------------------------

    private void showStep(int step) {
        vm.step = Math.max(0, Math.min(2, step));
        b.flipper.setDisplayedChild(vm.step);
        b.stepProgress.setProgressCompat(vm.step + 1, true);
        TextView[] labels = {b.step1, b.step2, b.step3};
        int active = MaterialColors.getColor(b.step1, androidx.appcompat.R.attr.colorPrimary);
        int inactive = MaterialColors.getColor(b.step1, com.google.android.material.R.attr.colorOnSurfaceVariant);
        for (int i = 0; i < labels.length; i++) labels[i].setTextColor(i <= vm.step ? active : inactive);
        b.backButton.setVisibility(vm.step == 0 ? View.GONE : View.VISIBLE);
        if (vm.step < 2) b.nextButton.setText(R.string.next);
        else b.nextButton.setText(editing == null ? R.string.post : R.string.save_changes);
    }

    private void next() {
        if (saving) return;
        Ui.hideKeyboard(b.getRoot());
        if (vm.step == 0) {
            showStep(1);
        } else if (vm.step == 1) {
            if (validateDetails()) showStep(2);
        } else if (validateLocation()) {
            save();
        }
    }

    private boolean validateDetails() {
        String titleError = Validators.title(Ui.text(b.title));
        String category = Ui.text(b.category);
        String categoryError = Arrays.asList(Config.CATEGORIES).contains(category) ? null : getString(R.string.choose_category);
        String descriptionError = Validators.description(Ui.text(b.description));
        String dateError = vm.date > System.currentTimeMillis() + 60_000 ? "The date cannot be in the future" : null;
        String proofError = isFound() ? Validators.proofQuestion(Ui.text(b.proofQuestion)) : null;

        b.titleLayout.setError(titleError);
        b.categoryLayout.setError(categoryError);
        b.descriptionLayout.setError(descriptionError);
        b.dateLayout.setError(dateError);
        b.proofLayout.setError(proofError);
        boolean ok = titleError == null && categoryError == null && descriptionError == null
                && dateError == null && proofError == null;
        if (!ok) b.detailsScroll.smoothScrollTo(0, 0);
        return ok;
    }

    private boolean validateLocation() {
        String place = Ui.text(b.place);
        String error = Arrays.asList(Config.CAMPUS_LOCATIONS).contains(place) ? null : getString(R.string.choose_place);
        b.placeLayout.setError(error);
        return error == null;
    }

    // ---- Date --------------------------------------------------------------

    private void pickDate() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(isFound() ? R.string.date_found_hint : R.string.date_lost_hint)
                .setSelection(vm.date)
                .setCalendarConstraints(new CalendarConstraints.Builder()
                        .setValidator(DateValidatorPointBackward.now()).build())
                .build();
        picker.addOnPositiveButtonClickListener(selection -> {
            vm.date = selection;
            showDate();
        });
        picker.show(getSupportFragmentManager(), "date");
    }

    private void showDate() {
        b.date.setText(Formatters.date(vm.date));
    }

    // ---- Photo -------------------------------------------------------------

    private void openCamera() {
        try {
            File dir = new File(getCacheDir(), "camera");
            if (!dir.exists() && !dir.mkdirs()) throw new IllegalStateException("No cache folder");
            File file = new File(dir, "photo.jpg");
            vm.cameraUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
            takePhoto.launch(vm.cameraUri);
        } catch (ActivityNotFoundException | IllegalStateException | IllegalArgumentException e) {
            Snackbar.make(b.root, R.string.no_camera_app, Snackbar.LENGTH_LONG).show();
        }
    }

    /** Reads and compresses the photo on a background thread, then shows it. */
    private void usePhoto(Uri uri) {
        b.photoProgress.setVisibility(View.VISIBLE);
        b.nextButton.setEnabled(false);
        background.execute(() -> {
            String base64 = null;
            try {
                Bitmap bitmap = ImageUtils.decodeUri(getContentResolver(), uri);
                base64 = ImageUtils.toBase64(bitmap, Config.MAX_IMAGE_BYTES);
            } catch (Exception e) {
                // handled below: base64 stays null
            }
            String result = base64;
            main.post(() -> {
                if (isFinishing()) return;
                b.photoProgress.setVisibility(View.GONE);
                b.nextButton.setEnabled(true);
                if (result == null) {
                    Snackbar.make(b.root, R.string.photo_failed, Snackbar.LENGTH_LONG).show();
                    return;
                }
                vm.photoBase64 = result;
                showPhoto();
            });
        });
    }

    private void showPhoto() {
        Bitmap bitmap = ImageUtils.fromBase64(vm.photoBase64);
        ImageView photo = b.photo;
        if (bitmap != null) {
            ImageViewCompat.setImageTintList(photo, null);
            photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
            photo.setImageBitmap(bitmap);
            b.removePhoto.setVisibility(View.VISIBLE);
        } else {
            ImageViewCompat.setImageTintList(photo, android.content.res.ColorStateList.valueOf(
                    MaterialColors.getColor(photo, com.google.android.material.R.attr.colorOnSurfaceVariant)));
            photo.setScaleType(ImageView.ScaleType.CENTER);
            photo.setImageResource(R.drawable.ic_image);
            b.removePhoto.setVisibility(View.GONE);
        }
    }

    // ---- Save --------------------------------------------------------------

    private Item buildItem() {
        Item item = editing != null ? editing.copy() : new Item();
        item.type = isFound() ? Item.FOUND : Item.LOST;
        item.title = Ui.text(b.title).trim();
        item.category = Ui.text(b.category);
        item.description = Ui.text(b.description).trim();
        item.date = vm.date;
        item.proofQuestion = isFound() ? Ui.text(b.proofQuestion).trim() : "";
        String detail = Ui.text(b.placeDetail).trim();
        item.location = detail.isEmpty() ? Ui.text(b.place) : Ui.text(b.place) + SEPARATOR + detail;
        item.imageBase64 = vm.photoBase64;
        if (editing == null) {
            item.ownerId = me.uid;
            item.ownerName = me.name;
            item.ownerEmail = me.email;
            item.createdAt = System.currentTimeMillis();
            item.status = Item.OPEN;
        }
        return item;
    }

    private void save() {
        Item item = buildItem();
        setSaving(true);
        boolean offline = !Repositories.isDemoMode() && !Ui.isOnline(this);

        if (editing != null) {
            Repositories.items().update(item, new Callback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    finishWithMessage(item.id, getString(offline ? R.string.saved_offline : R.string.saved_changes));
                }

                @Override
                public void onError(String message) {
                    showError(message);
                }
            });
            return;
        }

        Repositories.items().add(item, new Callback<String>() {
            @Override
            public void onSuccess(String newId) {
                item.id = newId;
                int matches = MatchService.findMatches(item, currentItems()).size();
                String message = offline ? getString(R.string.saved_offline)
                        : matches > 0 ? getString(R.string.posted_with_matches, matches)
                        : getString(R.string.posted_no_matches);
                finishWithMessage(newId, message);
            }

            @Override
            public void onError(String message) {
                showError(message);
            }
        });
    }

    private List<Item> currentItems() {
        FeedState state = Repositories.items().feed().getValue();
        return state == null ? new ArrayList<>() : state.items;
    }

    private void finishWithMessage(String itemId, String message) {
        if (editing == null) {
            startActivity(ItemDetailActivity.newIntent(this, itemId).putExtra(ItemDetailActivity.EXTRA_MESSAGE, message));
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void showError(String message) {
        setSaving(false);
        Snackbar.make(b.root, message, Snackbar.LENGTH_LONG).setAnchorView(b.nextButton).show();
    }

    private void setSaving(boolean value) {
        saving = value;
        b.nextButton.setEnabled(!value);
        b.backButton.setEnabled(!value);
    }
}
