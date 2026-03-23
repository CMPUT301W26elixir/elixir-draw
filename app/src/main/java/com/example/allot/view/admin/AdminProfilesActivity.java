package com.example.allot.view.admin;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.allot.R;
import com.example.allot.controller.admin.AdminProfileController;
import com.example.allot.model.profile.User;
import com.example.allot.view.shared.AppDialogHelper;
import com.example.allot.view.shared.AppNavigator;
import com.example.allot.view.shared.BottomNavBarView;
import com.example.allot.view.shared.UiHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity for admin profile management.
 * Allows admins to browse all user profiles and delete them.
 */
public class AdminProfilesActivity extends AppCompatActivity {
    private AdminProfileController adminProfileController;
    private BottomNavBarView bottomNavBar;
    private RecyclerView profilesRecyclerView;
    private View loadingLayout;
    private TextView emptyStateText;
    private ImageView backButton;
    private Button viewEventsButton;
    private AdminProfileListAdapter adapter;
    private List<User> profilesList;

    /**
     * Initializes the activity, binds views, sets up navigation,
     * and loads all profiles.
     *
     * @param savedInstanceState the previously saved activity state, if one exists
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profiles);

        adminProfileController = new AdminProfileController(this);
        profilesList = new ArrayList<>();

        bindViews();
        setupBottomNav();
        setupRecyclerView();
        setupBackButton();
        setupViewEventsButton();
        loadProfiles();
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
        profilesRecyclerView = findViewById(R.id.profilesRecyclerView);
        loadingLayout = findViewById(R.id.loadingLayout);
        emptyStateText = findViewById(R.id.emptyStateText);
        backButton = findViewById(R.id.backButton);
        viewEventsButton = findViewById(R.id.viewEventsButton);
    }

    /**
     * Configures the bottom navigation bar.
     * No tab is selected since Admin is a separate screen.
     */
    private void setupBottomNav() {
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.EXPLORE, view -> AppNavigator.openExplore(this, true));
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.SAVED, view -> AppNavigator.openSaved(this, true));
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.MY_EVENTS, view -> AppNavigator.openMyEvents(this, true));
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.SCAN, view -> AppNavigator.openScan(this, false));
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.PROFILE, view -> AppNavigator.openProfile(this, true));
    }

    /**
     * Sets up the RecyclerView with an adapter.
     */
    private void setupRecyclerView() {
        adapter = new AdminProfileListAdapter(profilesList, this::onDeleteClick);
        profilesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        profilesRecyclerView.setAdapter(adapter);
    }

    /**
     * Sets up the back button to finish the activity.
     */
    private void setupBackButton() {
        backButton.setOnClickListener(view -> finish());
    }

    /**
     * Sets up the view events button to navigate to AdminActivity.
     */
    private void setupViewEventsButton() {
        viewEventsButton.setOnClickListener(view -> openEventsView());
    }

    /**
     * Opens the admin events view.
     */
    private void openEventsView() {
        Intent intent = new Intent(this, AdminActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    /**
     * Loads all profiles from the database.
     * Shows loading state while loading, then displays profiles or empty state.
     */
    private void loadProfiles() {
        showLoadingState();
        adminProfileController.loadAllProfiles((profiles, success) -> {
            if (!success || profiles == null) {
                showEmptyState(getString(R.string.admin_error_loading_profiles));
                Toast.makeText(this, R.string.admin_error_loading_profiles, Toast.LENGTH_SHORT).show();
                return;
            }

            if (profiles.isEmpty()) {
                showEmptyState(getString(R.string.admin_no_profiles));
                return;
            }

            profilesList.clear();
            profilesList.addAll(profiles);
            showProfilesState();
            adapter.notifyDataSetChanged();
        });
    }

    /**
     * Handles delete button click for a profile.
     * Shows confirmation dialog before deleting.
     *
     * @param user the user profile to delete
     * @param position the position in the list
     */
    private void onDeleteClick(User user, int position) {
        showDeleteConfirmationDialog(user, position);
    }

    /**
     * Shows the delete confirmation dialog.
     *
     * @param user the user profile to delete
     * @param position the position in the list
     */
    private void showDeleteConfirmationDialog(User user, int position) {
        Dialog dialog = AppDialogHelper.createDialog(this, R.layout.dialog_admin_delete_profile, true);
        View dialogView = dialog.findViewById(android.R.id.content);

        TextView userNameText = dialogView.findViewById(R.id.userNameText);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button deleteButton = dialogView.findViewById(R.id.deleteProfileButton);

        String displayName = (user.getFirstName() != null && !user.getFirstName().isEmpty() ?
                user.getFirstName() : "") +
                (user.getLastName() != null && !user.getLastName().isEmpty() ?
                " " + user.getLastName() : "");
        if (displayName.trim().isEmpty()) {
            displayName = user.getEmail() != null ? user.getEmail() : user.getDeviceId();
        }
        userNameText.setText(displayName);

        cancelButton.setOnClickListener(view -> dialog.dismiss());
        deleteButton.setOnClickListener(view -> deleteProfile(user, position, dialog, cancelButton, deleteButton));

        AppDialogHelper.show(dialog, UiHelper.dpToPx(this, 300), UiHelper.dpToPx(this, 200));
    }

    /**
     * Deletes a profile after confirmation.
     *
     * @param user the user profile to delete
     * @param position the position in the list
     * @param dialog the confirmation dialog
     * @param cancelButton the cancel button
     * @param deleteButton the delete button
     */
    private void deleteProfile(User user, int position, Dialog dialog, Button cancelButton, Button deleteButton) {
        cancelButton.setEnabled(false);
        deleteButton.setEnabled(false);

        adminProfileController.deleteProfile(user.getDeviceId(), (success, result) -> {
            if (!success || !result) {
                cancelButton.setEnabled(true);
                deleteButton.setEnabled(true);
                Toast.makeText(this, R.string.admin_error_deleting_profile, Toast.LENGTH_SHORT).show();
                return;
            }

            dialog.dismiss();
            Toast.makeText(this, R.string.admin_profile_deleted, Toast.LENGTH_SHORT).show();

            // Remove from list
            profilesList.remove(position);
            adapter.notifyItemRemoved(position);

            // Show empty state if no profiles left
            if (profilesList.isEmpty()) {
                showEmptyState(getString(R.string.admin_no_profiles));
            }
        });
    }

    /**
     * Shows the loading state with progress bar and hides the profiles list.
     */
    private void showLoadingState() {
        loadingLayout.setVisibility(View.VISIBLE);
        profilesRecyclerView.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.GONE);
    }

    /**
     * Shows the profiles list and hides the loading state and empty state.
     */
    private void showProfilesState() {
        loadingLayout.setVisibility(View.GONE);
        profilesRecyclerView.setVisibility(View.VISIBLE);
        emptyStateText.setVisibility(View.GONE);
    }

    /**
     * Shows the empty state message and hides the profiles list and loading state.
     *
     * @param message the message to display
     */
    private void showEmptyState(String message) {
        loadingLayout.setVisibility(View.GONE);
        profilesRecyclerView.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.VISIBLE);
        emptyStateText.setText(message);
    }
}
