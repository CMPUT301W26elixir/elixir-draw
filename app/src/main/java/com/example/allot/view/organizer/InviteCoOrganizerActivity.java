package com.example.allot.view.organizer;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.allot.R;
import com.example.allot.controller.organizer.InviteCoOrganizerController;
import com.example.allot.model.event.Event;
import com.example.allot.model.profile.User;
import com.example.allot.view.shared.UiHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * Lets organizers invite users to co-organize an event.
 */
public class InviteCoOrganizerActivity extends AppCompatActivity {
    public static final String EXTRA_EVENT_ID = "event_id";
    public static final String EXTRA_UI_TEST_MODE = "ui_test_mode";

    private InviteCoOrganizerController controller;
    private String currentEventId;
    private Event currentEvent;

    private EditText searchInput;
    private TextView searchButton;
    private LinearLayout resultsContainer;
    private TextView emptyText;
    private ProgressBar loadingIndicator;

    /**
     * Handles on Create.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_coorganizer);

        controller = new InviteCoOrganizerController(this);
        currentEventId = getIntent().getStringExtra(EXTRA_EVENT_ID);

        bindViews();
        setupHeader();
        setupListeners();
        loadEvent();
    }

    /**
     * Binds views.
     */
    private void bindViews() {
        searchInput = findViewById(R.id.searchInput);
        searchButton = findViewById(R.id.searchButton);
        resultsContainer = findViewById(R.id.resultsContainer);
        emptyText = findViewById(R.id.emptyText);
        loadingIndicator = findViewById(R.id.loadingIndicator);
    }

    /**
     * Updates up header.
     */
    private void setupHeader() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
    }

    /**
     * Updates up listeners.
     */
    private void setupListeners() {
        searchButton.setOnClickListener(view -> performSearch());
    }

    /**
     * Loads event.
     */
    private void loadEvent() {
        if (getIntent().getBooleanExtra(EXTRA_UI_TEST_MODE, false)) {
            currentEvent = new Event();
            showEmptyState(true);
            return;
        }

        if (TextUtils.isEmpty(currentEventId)) {
            Toast.makeText(this, R.string.manage_event_load_failure, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setLoading(true);
        controller.loadEvent(currentEventId, (event, success) -> {
            setLoading(false);
            if (!success || event == null) {
                Toast.makeText(this, R.string.manage_event_load_failure, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            if (!controller.isOrganizerOrCoOrganizer(event)) {
                Toast.makeText(this, R.string.manage_event_load_failure, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            currentEvent = event;
        });
    }

    /**
     * Handles perform Search.
     */
    private void performSearch() {
        String query = searchInput.getText().toString().trim();
        if (query.isEmpty()) {
            showEmptyState(true);
            return;
        }

        setLoading(true);
        controller.searchUsers(query, (users, success) -> {
            setLoading(false);
            if (!success || users == null) {
                showEmptyState(true);
                return;
            }

            bindResults(filterCandidates(users));
        });
    }

    /**
     * Filters candidates.
     */
    private List<User> filterCandidates(List<User> users) {
        List<User> filtered = new ArrayList<>();
        if (users == null) {
            return filtered;
        }

        String currentDeviceId = controller.getCurrentDeviceId();
        for (User user : users) {
            if (user == null || UiHelper.isBlank(user.getDeviceId())) {
                continue;
            }
            if (user.getDeviceId().equals(currentDeviceId)) {
                continue;
            }
            if (currentEvent != null) {
                if (user.getDeviceId().equals(currentEvent.getOrganizerId())) {
                    continue;
                }
                /**
                 * Returns whether get Device Id.
                 */
                if (currentEvent.getCoOrganizers() != null
                        && currentEvent.getCoOrganizers().contains(user.getDeviceId())) {
                    continue;
                }
                /**
                 * Returns whether get Device Id.
                 */
                if (currentEvent.getCoOrganizerInvites() != null
                        && currentEvent.getCoOrganizerInvites().contains(user.getDeviceId())) {
                    continue;
                }
            }
            filtered.add(user);
        }
        return filtered;
    }

    /**
     * Binds results.
     */
    private void bindResults(List<User> users) {
        resultsContainer.removeAllViews();

        if (users == null || users.isEmpty()) {
            showEmptyState(true);
            return;
        }

        showEmptyState(false);
        for (User user : users) {
            View row = getLayoutInflater().inflate(R.layout.item_invite_coorganizer_user, resultsContainer, false);
            TextView nameText = row.findViewById(R.id.userNameText);
            TextView contactText = row.findViewById(R.id.userContactText);
            TextView inviteButton = row.findViewById(R.id.inviteButton);

            nameText.setText(UiHelper.defaultText(user.getName(), getString(R.string.event_detail_organizer_tba)));
            contactText.setText(resolveContact(user));

            inviteButton.setOnClickListener(view -> inviteUser(user, inviteButton));
            resultsContainer.addView(row);
        }
    }

    /**
     * Handles invite User.
     */
    private void inviteUser(User user, TextView inviteButton) {
        if (user == null || TextUtils.isEmpty(currentEventId)) {
            return;
        }

        inviteButton.setEnabled(false);
        controller.inviteCoOrganizer(currentEventId, user.getDeviceId(), (result, success) -> {
            inviteButton.setEnabled(true);
            if (!success || result == null || !result) {
                Toast.makeText(this, R.string.invite_coorganizer_failure, Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, R.string.invite_coorganizer_success, Toast.LENGTH_SHORT).show();
            if (currentEvent != null) {
                currentEvent.getCoOrganizerInvites().add(user.getDeviceId());
            }
            performSearch();
        });
    }

    /**
     * Handles resolve Contact.
     */
    private String resolveContact(User user) {
        if (user == null) {
            return "";
        }
        if (!UiHelper.isBlank(user.getEmail())) {
            return user.getEmail();
        }
        if (!UiHelper.isBlank(user.getPhone())) {
            return user.getPhone();
        }
        return "";
    }

    /**
     * Updates loading.
     */
    private void setLoading(boolean isLoading) {
        loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    /**
     * Shows empty state.
     */
    private void showEmptyState(boolean show) {
        emptyText.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
