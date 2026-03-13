package com.example.allot.view;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.allot.R;
import com.example.allot.controller.UserController;
import com.example.allot.model.User;

/**
 * Activity that displays and manages the user's profile information.
 *
 * <p>This screen allows the user to view and edit their profile details,
 * update notification preferences, save profile changes, delete their profile,
 * and navigate to other sections of the app using the bottom navigation bar.
 */
public class ProfileActivity extends AppCompatActivity {
    /**
     * Background color used for the save button when saving is not currently available.
     */
    private static final int SAVE_INACTIVE_COLOR = Color.parseColor("#A6A8A5");

    /**
     * Background color used for the save button when unsaved changes are present.
     */
    private static final int SAVE_ACTIVE_COLOR = Color.parseColor("#FFFFFF");

    private BottomNavBarView bottomNavBar;
    private UserController userController;
    private EditText firstNameInput;
    private EditText lastNameInput;
    private EditText emailInput;
    private EditText phoneInput;
    private CheckBox eventUpdatesCheckbox;
    private Button saveChangesButton;
    private TextView deleteProfileText;

    private String originalFirstName = "";
    private String originalLastName = "";
    private String originalEmail = "";
    private String originalPhone = "";
    private boolean originalNotificationsEnabled = false;
    private boolean isBindingProfile;
    private boolean isSaving;
    private boolean isDeleting;

    /**
     * Initializes the activity, binds views, sets up listeners and navigation,
     * and loads the user's profile data.
     *
     * @param savedInstanceState the previously saved activity state, if one exists
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userController = new UserController(this);
        bindViews();
        setupBottomNav();
        setupFormListeners();
        updateSaveButtonState();
        loadProfile();
    }

    /**
     * Finishes the activity without any transition animation.
     */
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    /**
     * Binds all layout views to their corresponding fields.
     */
    private void bindViews() {
        bottomNavBar = findViewById(R.id.bottomNavBar);
        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        eventUpdatesCheckbox = findViewById(R.id.eventUpdatesCheckbox);
        saveChangesButton = findViewById(R.id.saveChangesButton);
        deleteProfileText = findViewById(R.id.deleteProfileText);
    }

    /**
     * Configures the bottom navigation bar and assigns click handlers
     * for switching to other app screens.
     */
    private void setupBottomNav() {
        bottomNavBar.setSelectedTab(BottomNavBarView.Tab.PROFILE);
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.EXPLORE, view -> openExploreScreen());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.SAVED, view -> openSavedScreen());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.MY_EVENTS, view -> openMyEventsScreen());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.SCAN, view -> openScanScreen());
    }

    /**
     * Opens the saved screen by launching {@link MainActivity} and requesting
     * that it display the saved tab.
     */
    private void openSavedScreen() {
        // Sends the user back to MainActivity but tells it to open the Saved Tab immediately
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        intent.putExtra("navigate_to", "saved");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    /**
     * Sets up listeners for form inputs, the notification checkbox,
     * the save button, and the delete profile action.
     */
    private void setupFormListeners() {
        TextWatcher dirtyStateWatcher = new SimpleTextWatcher() {
            /**
             * Updates the save button state after text changes,
             * unless the profile is currently being bound to the UI.
             *
             * @param editable the editable text after the change
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
        deleteProfileText.setOnClickListener(view -> showDeleteProfileDialog());
    }

    /**
     * Loads the current user's profile data and binds it to the UI.
     *
     * <p>If loading fails, an error message is shown and the save button
     * state is refreshed.
     */
    private void loadProfile() {
        isBindingProfile = true;
        userController.loadOrCreateUser((user, success) -> {
            isBindingProfile = false;
            if (!success || user == null) {
                Toast.makeText(ProfileActivity.this,
                        "Could not load your profile.",
                        Toast.LENGTH_SHORT).show();
                updateSaveButtonState();
                return;
            }

            bindProfile(user);
        });
    }

    /**
     * Binds a user's profile information to the form fields and records
     * the original values for dirty-state tracking.
     *
     * @param user the user whose profile data should be displayed
     */
    private void bindProfile(User user) {
        isBindingProfile = true;

        originalFirstName = safeValue(user.getFirstName());
        originalLastName = safeValue(user.getLastName());
        originalEmail = safeValue(user.getEmail());
        originalPhone = safeValue(user.getPhone());
        originalNotificationsEnabled = user.isNotiEnabled();

        firstNameInput.setText(originalFirstName);
        lastNameInput.setText(originalLastName);
        emailInput.setText(originalEmail);
        phoneInput.setText(originalPhone);
        eventUpdatesCheckbox.setChecked(originalNotificationsEnabled);

        isBindingProfile = false;
        updateSaveButtonState();
    }

    /**
     * Saves the current profile data if there are unsaved changes and
     * no save or delete operation is already in progress.
     *
     * <p>If the save succeeds, the UI is rebound with the updated profile.
     * If it fails, an error message is shown.
     */
    private void saveProfile() {
        if (isSaving || isDeleting || !hasUnsavedChanges()) {
            return;
        }

        isSaving = true;
        updateSaveButtonState();

        userController.updateUserProfile(
                currentText(firstNameInput),
                currentText(lastNameInput),
                currentText(emailInput),
                currentText(phoneInput),
                eventUpdatesCheckbox.isChecked(),
                (user, success) -> {
                    isSaving = false;
                    if (!success || user == null) {
                        Toast.makeText(ProfileActivity.this,
                                "Could not save your profile. Please try again.",
                                Toast.LENGTH_SHORT).show();
                        updateSaveButtonState();
                        return;
                    }

                    bindProfile(user);
                    Toast.makeText(ProfileActivity.this,
                            "Profile updated.",
                            Toast.LENGTH_SHORT).show();
                }
        );
    }

    /**
     * Determines whether the form contains unsaved changes compared to
     * the original loaded profile values.
     *
     * @return true if at least one profile field or preference has changed;
     * false otherwise
     */
    private boolean hasUnsavedChanges() {
        return !currentText(firstNameInput).equals(originalFirstName)
                || !currentText(lastNameInput).equals(originalLastName)
                || !currentText(emailInput).equals(originalEmail)
                || !currentText(phoneInput).equals(originalPhone)
                || eventUpdatesCheckbox.isChecked() != originalNotificationsEnabled;
    }

    /**
     * Updates the save button appearance and enables or disables controls
     * based on the current save/delete state and whether unsaved changes exist.
     */
    private void updateSaveButtonState() {
        int color = hasUnsavedChanges() && !isSaving && !isDeleting
                ? SAVE_ACTIVE_COLOR
                : SAVE_INACTIVE_COLOR;
        saveChangesButton.setBackgroundTintList(ColorStateList.valueOf(color));
        saveChangesButton.setEnabled(!isDeleting);
        deleteProfileText.setEnabled(!isDeleting);
    }

    /**
     * Displays the delete profile confirmation dialog.
     *
     * <p>The dialog allows the user to cancel or confirm profile deletion.
     */
    private void showDeleteProfileDialog() {
        if (isDeleting) {
            return;
        }

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_delete_profile, null);
        dialog.setContentView(dialogView);
        dialog.setCancelable(true);

        ImageView closeButton = dialogView.findViewById(R.id.closeDeleteDialogButton);
        Button stayButton = dialogView.findViewById(R.id.stayButton);
        Button confirmDeleteButton = dialogView.findViewById(R.id.confirmDeleteProfileButton);

        closeButton.setOnClickListener(view -> dialog.dismiss());
        stayButton.setOnClickListener(view -> dialog.dismiss());
        confirmDeleteButton.setOnClickListener(view -> deleteProfile(dialog, stayButton, confirmDeleteButton));

        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(dpToPx(342), dpToPx(342));
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    /**
     * Deletes the current user's profile after confirmation from the dialog.
     *
     * <p>While deletion is in progress, the dialog buttons are disabled.
     * On success, the user is returned to the splash screen. On failure,
     * the dialog remains open and an error message is shown.
     *
     * @param dialog the confirmation dialog being displayed
     * @param stayButton the button used to cancel deletion
     * @param confirmDeleteButton the button used to confirm deletion
     */
    private void deleteProfile(Dialog dialog, Button stayButton, Button confirmDeleteButton) {
        if (isDeleting) {
            return;
        }

        isDeleting = true;
        updateSaveButtonState();
        stayButton.setEnabled(false);
        confirmDeleteButton.setEnabled(false);

        userController.deleteCurrentUser((result, success) -> {
            isDeleting = false;
            updateSaveButtonState();

            if (!success || result == null || !result) {
                stayButton.setEnabled(true);
                confirmDeleteButton.setEnabled(true);
                Toast.makeText(ProfileActivity.this,
                        R.string.delete_profile_failure,
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
     * Returns the trimmed text currently contained in the given input field.
     *
     * @param editText the input field whose text should be read
     * @return the trimmed text value, or an empty string if null
     */
    private String currentText(EditText editText) {
        return safeValue(editText.getText() == null ? null : editText.getText().toString());
    }

    /**
     * Returns a safe, trimmed string value.
     *
     * @param value the string to normalize
     * @return an empty string if the value is null; otherwise the trimmed string
     */
    private String safeValue(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Converts a density-independent pixel value to pixels.
     *
     * @param dp the density-independent pixel value
     * @return the equivalent pixel value
     */
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    /**
     * Opens the explore screen.
     */
    private void openExploreScreen() {
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    /**
     * Opens the My Events screen.
     */
    private void openMyEventsScreen() {
        Intent intent = new Intent(ProfileActivity.this, MyEventsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    /**
     * Simplified abstract implementation of {@link TextWatcher} that provides
     * empty default implementations for methods that may not be needed.
     */
    private abstract static class SimpleTextWatcher implements TextWatcher {
        /**
         * Called before the text is changed.
         *
         * @param s the text before the change
         * @param start the start position of the change
         * @param count the length of the old text that is about to be replaced
         * @param after the length of the new text that will replace it
         */
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        /**
         * Called as the text is being changed.
         *
         * @param s the text after the change begins
         * @param start the start position of the change
         * @param before the length of the old text that was replaced
         * @param count the length of the new text inserted
         */
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }

    /**
     * Opens the scan screen.
     *
     * <p>Existing note preserved:
     * Do not call {@code finish()} here if pasting this into {@code MainActivity.java}.
     * You can call {@code finish()} here if pasting into {@code MyEventsActivity}
     * or {@code ProfileActivity}.
     */
    private void openScanScreen() {
        Intent intent = new Intent(this, ScanActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        // Note: Do NOT call finish() here if pasting this into MainActivity.java!
        // You CAN call finish() here if pasting into MyEventsActivity or ProfileActivity.
    }
}