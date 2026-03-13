package com.example.allot.view;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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

    private String originalFirstName = "";
    private String originalLastName = "";
    private String originalEmail = "";
    private String originalPhone = "";
    private boolean originalNotificationsEnabled = false;
    private boolean isBindingProfile;
    private boolean isSaving;

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
    }

    private void setupBottomNav() {
        bottomNavBar.setSelectedTab(BottomNavBarView.Tab.PROFILE);
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.EXPLORE, view -> openExploreScreen());
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
        if (isSaving || !hasUnsavedChanges()) {
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
        int color = hasUnsavedChanges() && !isSaving ? SAVE_ACTIVE_COLOR : SAVE_INACTIVE_COLOR;
        saveChangesButton.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    private String currentText(EditText editText) {
        return safeValue(editText.getText() == null ? null : editText.getText().toString());
    }

    private String safeValue(String value) {
        return value == null ? "" : value.trim();
    }

    private void openExploreScreen() {
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
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
}
