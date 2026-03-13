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

public class ProfileActivity extends AppCompatActivity {
    private static final int SAVE_INACTIVE_COLOR = Color.parseColor("#A6A8A5");
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

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

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

    private void setupBottomNav() {
        bottomNavBar.setSelectedTab(BottomNavBarView.Tab.PROFILE);
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.EXPLORE, view -> openExploreScreen());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.SAVED, view -> openSavedScreen());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.MY_EVENTS, view -> openMyEventsScreen());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.SCAN, view -> openScanScreen());
    }

    private void openSavedScreen() {
        // Sends the user back to MainActivity but tells it to open the Saved Tab immediately
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        intent.putExtra("navigate_to", "saved");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    private void setupFormListeners() {
        TextWatcher dirtyStateWatcher = new SimpleTextWatcher() {
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

    private boolean hasUnsavedChanges() {
        return !currentText(firstNameInput).equals(originalFirstName)
                || !currentText(lastNameInput).equals(originalLastName)
                || !currentText(emailInput).equals(originalEmail)
                || !currentText(phoneInput).equals(originalPhone)
                || eventUpdatesCheckbox.isChecked() != originalNotificationsEnabled;
    }

    private void updateSaveButtonState() {
        int color = hasUnsavedChanges() && !isSaving && !isDeleting
                ? SAVE_ACTIVE_COLOR
                : SAVE_INACTIVE_COLOR;
        saveChangesButton.setBackgroundTintList(ColorStateList.valueOf(color));
        saveChangesButton.setEnabled(!isDeleting);
        deleteProfileText.setEnabled(!isDeleting);
    }

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

    private String currentText(EditText editText) {
        return safeValue(editText.getText() == null ? null : editText.getText().toString());
    }

    private String safeValue(String value) {
        return value == null ? "" : value.trim();
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void openExploreScreen() {
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    private void openMyEventsScreen() {
        Intent intent = new Intent(ProfileActivity.this, MyEventsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }
    private void openScanScreen() {
        Intent intent = new Intent(this, ScanActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        // Note: Do NOT call finish() here if pasting this into MainActivity.java!
        // You CAN call finish() here if pasting into MyEventsActivity or ProfileActivity.
    }
}



