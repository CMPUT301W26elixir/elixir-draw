package com.example.allot.view.profile;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.allot.R;
import com.example.allot.controller.profile.ProfileController;
import com.example.allot.controller.profile.ProfilePhotoController;
import com.example.allot.controller.shared.UserController;
import com.example.allot.model.profile.ProfileActionResult;
import com.example.allot.model.profile.ProfileFormSnapshot;
import com.example.allot.model.profile.User;
import com.example.allot.view.SplashActivity;
import com.example.allot.view.shared.AppDialogHelper;
import com.example.allot.view.shared.AppNavigator;
import com.example.allot.view.shared.BottomNavBarView;
import com.example.allot.view.shared.SimpleTextWatcher;
import com.example.allot.view.shared.UiHelper;
/**
 * Shows the main profile screen where the user can view and update their details.
 */
public class ProfileActivity extends AppCompatActivity {
    public static final String EXTRA_UI_TEST_PROFILE_MODE = "ui_test_profile_mode";
    public static final String EXTRA_UI_TEST_FIRST_NAME = "ui_test_first_name";
    public static final String EXTRA_UI_TEST_LAST_NAME = "ui_test_last_name";
    public static final String EXTRA_UI_TEST_EMAIL = "ui_test_email";
    public static final String EXTRA_UI_TEST_PHONE = "ui_test_phone";
    public static final String EXTRA_UI_TEST_NOTIFICATIONS_ENABLED = "ui_test_notifications_enabled";
    /**
     * Background color used for the save button when saving is not currently available.
     */
    private static final int SAVE_INACTIVE_COLOR = Color.parseColor("#A6A8A5");

    /**
     * Background color used for the save button when unsaved changes are present.
     */
    private static final int SAVE_ACTIVE_COLOR = Color.parseColor("#FFFFFF");

    private BottomNavBarView bottomNavBar;
    private ProfileController profileController;
    private UserController userController;
    private ProfilePhotoController profilePhotoController;
    private EditText firstNameInput;
    private EditText lastNameInput;
    private EditText emailInput;
    private EditText phoneInput;
    private View profileAvatarContainer;
    private View profileAvatarPlaceholder;
    private ImageView profileAvatarImage;
    private CheckBox eventUpdatesCheckbox;
    private Button saveChangesButton;
    private TextView adminPanelButton;
    private TextView deleteProfileText;

    private ProfileFormSnapshot originalProfileSnapshot = new ProfileFormSnapshot("", "", "", "", false);
    private boolean isBindingProfile;
    private boolean isSaving;
    private boolean isDeleting;
    private boolean isUploadingPhoto;
    private String currentProfilePhotoUrl;

    private final ActivityResultLauncher<String> profilePhotoPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) {
                    return;
                }
                openProfilePhotoCropper(uri);
            });

    private final ActivityResultLauncher<Intent> profilePhotoCropLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                    return;
                }

                String outputUriValue = result.getData().getStringExtra(ProfilePhotoCropActivity.EXTRA_OUTPUT_URI);
                if (TextUtils.isEmpty(outputUriValue)) {
                    Toast.makeText(this, R.string.profile_photo_crop_save_failure, Toast.LENGTH_SHORT).show();
                    return;
                }

                uploadProfilePhoto(Uri.parse(outputUriValue));
            });

    /**
     * Handles the create callback.
     *
     * @param savedInstanceState the saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileController = new ProfileController(this);
        userController = new UserController(this);
        profilePhotoController = new ProfilePhotoController();
        bindViews();
        setupBottomNav();
        setupFormListeners();
        setupProfilePhotoPicker();
        updateSaveButtonState();
        if (isUiTestProfileMode()) {
            bindProfileState(buildUiTestProfileSnapshot());
            renderProfilePhoto(null);
            adminPanelButton.setVisibility(View.GONE);
            return;
        }
        checkAdminStatus();
        loadProfile();
        loadProfilePhoto();
    }

    /**
     * Performs finish.
     */
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    /**
     * Performs bind views.
     */
    private void bindViews() {
        bottomNavBar = findViewById(R.id.bottomNavBar);
        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        profileAvatarContainer = findViewById(R.id.profileAvatarContainer);
        profileAvatarPlaceholder = findViewById(R.id.profileAvatarPlaceholder);
        profileAvatarImage = findViewById(R.id.profileAvatarImage);
        eventUpdatesCheckbox = findViewById(R.id.eventUpdatesCheckbox);
        saveChangesButton = findViewById(R.id.saveChangesButton);
        adminPanelButton = findViewById(R.id.adminPanelButton);
        deleteProfileText = findViewById(R.id.deleteProfileText);
    }

    /**
     * Updates the up profile photo picker.
     */
    private void setupProfilePhotoPicker() {
        profileAvatarContainer.setOnClickListener(view -> {
            if (isDeleting || isUploadingPhoto) {
                return;
            }
            profilePhotoPickerLauncher.launch("image/*");
        });
    }

    /**
     * Performs open profile photo cropper.
     *
     * @param sourceUri the source uri
     */
    private void openProfilePhotoCropper(Uri sourceUri) {
        if (sourceUri == null) {
            return;
        }

        Intent intent = new Intent(this, ProfilePhotoCropActivity.class);
        intent.putExtra(ProfilePhotoCropActivity.EXTRA_INPUT_URI, sourceUri.toString());
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        profilePhotoCropLauncher.launch(intent);
    }

    /**
     * Updates the up bottom nav.
     */
    private void setupBottomNav() {
        bottomNavBar.setSelectedTab(BottomNavBarView.Tab.PROFILE);
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.EXPLORE, view -> AppNavigator.openExplore(this, true));
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.SAVED, view -> AppNavigator.openSaved(this, true));
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.MY_EVENTS, view -> AppNavigator.openMyEvents(this, true));
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.SCAN, view -> AppNavigator.openScan(this, false));
    }

    /**
     * Updates the up form listeners.
     */
    private void setupFormListeners() {
        SimpleTextWatcher dirtyStateWatcher = new SimpleTextWatcher() {
            /**
             * Performs after text changed.
             *
             * @param editable the editable
             */
            @Override
            public void afterTextChanged(Editable editable) {
                if (!isBindingProfile) {
                    updateSaveButtonState();
                }
            }
        };

        firstNameInput.addTextChangedListener(dirtyStateWatcher);
        lastNameInput.addTextChangedListener(dirtyStateWatcher);
        emailInput.addTextChangedListener(dirtyStateWatcher);
        phoneInput.addTextChangedListener(dirtyStateWatcher);
        eventUpdatesCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isBindingProfile) {
                updateSaveButtonState();
            }
        });

        saveChangesButton.setOnClickListener(view -> saveProfile());
        adminPanelButton.setOnClickListener(view -> openAdminPanel());
        deleteProfileText.setOnClickListener(view -> showDeleteProfileDialog());
    }

    /**
     * Performs load profile.
     */
    private void loadProfile() {
        isBindingProfile = true;
        profileController.loadProfile((ProfileFormSnapshot snapshot, boolean success) -> {
            isBindingProfile = false;
            if (!success || snapshot == null) {
                Toast.makeText(ProfileActivity.this,
                        "Could not load your profile.",
                        Toast.LENGTH_SHORT).show();
                updateSaveButtonState();
                return;
            }

            bindProfileState(snapshot);
        });
    }

    /**
     * Performs load profile photo.
     */
    private void loadProfilePhoto() {
        userController.loadCurrentUser((user, success) -> {
            if (!success || user == null) {
                renderProfilePhoto(null);
                return;
            }

            currentProfilePhotoUrl = user.getProfilePhotoUrl();
            renderProfilePhoto(currentProfilePhotoUrl);
        });
    }

    /**
     * Performs upload profile photo.
     *
     * @param photoUri the photo uri
     */
    private void uploadProfilePhoto(Uri photoUri) {
        if (photoUri == null || isUploadingPhoto) {
            return;
        }

        isUploadingPhoto = true;
        String deviceId = userController.getCurrentDeviceId();
        profilePhotoController.uploadPhoto(deviceId, photoUri, (photoUrl, success) -> {
            isUploadingPhoto = false;
            if (!success || TextUtils.isEmpty(photoUrl)) {
                Toast.makeText(this, R.string.profile_photo_upload_failure, Toast.LENGTH_SHORT).show();
                return;
            }

            currentProfilePhotoUrl = photoUrl;
            renderProfilePhoto(photoUrl);
            Toast.makeText(this, R.string.profile_photo_upload_success, Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Performs render profile photo.
     *
     * @param profilePhotoUrl the profile photo url
     */
    private void renderProfilePhoto(String profilePhotoUrl) {
        if (TextUtils.isEmpty(profilePhotoUrl)) {
            profileAvatarPlaceholder.setVisibility(View.VISIBLE);
            profileAvatarImage.setVisibility(View.GONE);
            profileAvatarImage.setImageDrawable(null);
            return;
        }

        profileAvatarPlaceholder.setVisibility(View.GONE);
        profileAvatarImage.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(profilePhotoUrl)
                .centerCrop()
                .into(profileAvatarImage);
    }

    /**
     * Performs check admin status.
     */
    private void checkAdminStatus() {
        userController.isCurrentUserAdmin((isAdmin, success) -> {
            if (success && isAdmin) {
                adminPanelButton.setVisibility(android.view.View.VISIBLE);
            } else {
                adminPanelButton.setVisibility(android.view.View.GONE);
            }
        });
    }

    /**
     * Performs open admin panel.
     */
    private void openAdminPanel() {
        Intent intent = new Intent(this, com.example.allot.view.admin.AdminActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    /**
     * Performs bind profile.
     *
     * @param user the user
     */
    private void bindProfile(User user) {
        isBindingProfile = true;

        originalProfileSnapshot = ProfileFormSnapshot.fromUser(user);

        firstNameInput.setText(originalProfileSnapshot.getFirstName());
        lastNameInput.setText(originalProfileSnapshot.getLastName());
        emailInput.setText(originalProfileSnapshot.getEmail());
        phoneInput.setText(originalProfileSnapshot.getPhone());
        eventUpdatesCheckbox.setChecked(originalProfileSnapshot.isNotificationsEnabled());

        isBindingProfile = false;
        updateSaveButtonState();
    }

    /**
     * Performs save profile.
     */
    private void saveProfile() {
        if (isSaving || isDeleting || !hasUnsavedChanges()) {
            return;
        }

        if (isUiTestProfileMode()) {
            bindProfileState(buildCurrentProfileSnapshot());
            Toast.makeText(ProfileActivity.this, "Profile updated.", Toast.LENGTH_SHORT).show();
            return;
        }

        isSaving = true;
        updateSaveButtonState();

        profileController.saveProfile(buildCurrentProfileSnapshot(), (ProfileActionResult result, boolean success) -> {
            isSaving = false;
            if (!success || result == null || result.getFormSnapshot() == null) {
                Toast.makeText(ProfileActivity.this,
                        result == null ? "Could not save your profile. Please try again." : result.getMessage(),
                        Toast.LENGTH_SHORT).show();
                updateSaveButtonState();
                return;
            }

            bindProfileState(result.getFormSnapshot());
            Toast.makeText(ProfileActivity.this,
                    result.getMessage(),
                    Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Returns whether this instance has unsaved changes.
     *
     * @return whether this instance has unsaved changes
     */
    private boolean hasUnsavedChanges() {
        return profileController.isSaveAvailable(
                originalProfileSnapshot,
                buildCurrentProfileSnapshot(),
                isSaving,
                isDeleting
        );
    }

    /**
     * Performs update save button state.
     */
    private void updateSaveButtonState() {
        boolean saveAvailable = profileController.isSaveAvailable(
                originalProfileSnapshot,
                buildCurrentProfileSnapshot(),
                isSaving,
                isDeleting
        );
        int color = saveAvailable
                ? SAVE_ACTIVE_COLOR
                : SAVE_INACTIVE_COLOR;
        saveChangesButton.setBackgroundTintList(ColorStateList.valueOf(color));
        saveChangesButton.setEnabled(!isDeleting);
        deleteProfileText.setEnabled(!isDeleting);
    }

    /**
     * Performs show delete profile dialog.
     */
    private void showDeleteProfileDialog() {
        if (isDeleting) {
            return;
        }

        Dialog dialog = AppDialogHelper.createDialog(this, R.layout.dialog_delete_profile, true);
        android.view.View dialogView = dialog.findViewById(android.R.id.content);

        ImageView closeButton = dialogView.findViewById(R.id.closeDeleteDialogButton);
        Button stayButton = dialogView.findViewById(R.id.stayButton);
        Button confirmDeleteButton = dialogView.findViewById(R.id.confirmDeleteProfileButton);

        closeButton.setOnClickListener(view -> dialog.dismiss());
        stayButton.setOnClickListener(view -> dialog.dismiss());
        confirmDeleteButton.setOnClickListener(view -> deleteProfile(dialog, stayButton, confirmDeleteButton));

        AppDialogHelper.show(dialog, UiHelper.dpToPx(this, 342), UiHelper.dpToPx(this, 342));
    }

    /**
     * Performs delete profile.
     *
     * @param dialog the dialog
     * @param stayButton the stay button
     * @param confirmDeleteButton the confirm delete button
     */
    private void deleteProfile(Dialog dialog, Button stayButton, Button confirmDeleteButton) {
        if (isDeleting) {
            return;
        }

        isDeleting = true;
        updateSaveButtonState();
        stayButton.setEnabled(false);
        confirmDeleteButton.setEnabled(false);

        profileController.deleteProfile((ProfileActionResult result, boolean success) -> {
            isDeleting = false;
            updateSaveButtonState();

            if (!success || result == null || !result.isSuccess()) {
                stayButton.setEnabled(true);
                confirmDeleteButton.setEnabled(true);
                Toast.makeText(ProfileActivity.this,
                        result == null ? getString(R.string.delete_profile_failure) : result.getMessage(),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            dialog.dismiss();
            Toast.makeText(ProfileActivity.this,
                    R.string.delete_profile_success,
                    Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(ProfileActivity.this, SplashActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
    }

    /**
     * Returns the result of build current profile snapshot.
     *
     * @return the result of this call
     */
    private ProfileFormSnapshot buildCurrentProfileSnapshot() {
        return new ProfileFormSnapshot(
                UiHelper.readText(firstNameInput),
                UiHelper.readText(lastNameInput),
                UiHelper.readText(emailInput),
                UiHelper.readText(phoneInput),
                eventUpdatesCheckbox.isChecked()
        );
    }

    /**
     * Performs bind profile state.
     *
     * @param snapshot the snapshot
     */
    private void bindProfileState(ProfileFormSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }

        isBindingProfile = true;

        originalProfileSnapshot = snapshot;

        firstNameInput.setText(snapshot.getFirstName());
        lastNameInput.setText(snapshot.getLastName());
        emailInput.setText(snapshot.getEmail());
        phoneInput.setText(snapshot.getPhone());
        eventUpdatesCheckbox.setChecked(snapshot.isNotificationsEnabled());

        isBindingProfile = false;
        updateSaveButtonState();
    }

    /**
     * Returns whether ui test profile mode.
     *
     * @return whether ui test profile mode
     */
    private boolean isUiTestProfileMode() {
        return getIntent().getBooleanExtra(EXTRA_UI_TEST_PROFILE_MODE, false);
    }

    /**
     * Returns the result of build ui test profile snapshot.
     *
     * @return the result of this call
     */
    private ProfileFormSnapshot buildUiTestProfileSnapshot() {
        return new ProfileFormSnapshot(
                getIntent().getStringExtra(EXTRA_UI_TEST_FIRST_NAME),
                getIntent().getStringExtra(EXTRA_UI_TEST_LAST_NAME),
                getIntent().getStringExtra(EXTRA_UI_TEST_EMAIL),
                getIntent().getStringExtra(EXTRA_UI_TEST_PHONE),
                getIntent().getBooleanExtra(EXTRA_UI_TEST_NOTIFICATIONS_ENABLED, false)
        );
    }

}









