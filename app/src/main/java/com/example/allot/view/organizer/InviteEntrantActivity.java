package com.example.allot.view.organizer;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.allot.R;
import com.example.allot.common.OnCompleteListener;
import com.example.allot.controller.organizer.InviteEntrantController;
import com.example.allot.model.event.Event;
import com.example.allot.model.profile.User;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Lets organizers invite users to private events.
 */
public class InviteEntrantActivity extends AppCompatActivity {
    public static final String EXTRA_EVENT_ID = "event_id";

    private InviteEntrantController controller;
    private String eventId;
    private Event currentEvent;

    private EditText searchInput;
    private TextView searchButton;
    private TextView emptyStateText;
    private LinearLayout resultsContainer;
    private LayoutInflater layoutInflater;

    private final Set<String> excludedUserIds = new HashSet<>();

    /**
     * Handles the create callback.
     *
     * @param savedInstanceState the saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_entrant);

        controller = new InviteEntrantController(this);
        eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        layoutInflater = LayoutInflater.from(this);

        bindViews();
        loadEvent();
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
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        searchInput = findViewById(R.id.searchInput);
        searchButton = findViewById(R.id.searchButton);
        emptyStateText = findViewById(R.id.emptyStateText);
        resultsContainer = findViewById(R.id.resultsContainer);

        searchButton.setOnClickListener(view -> runSearch());
    }

    /**
     * Performs load event.
     */
    private void loadEvent() {
        if (TextUtils.isEmpty(eventId)) {
            Toast.makeText(this, R.string.invite_entrant_load_failure, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        controller.loadEvent(eventId, (event, success) -> {
            if (!success || event == null) {
                Toast.makeText(this, R.string.invite_entrant_load_failure, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            currentEvent = event;
            if (!event.isPrivate()) {
                Toast.makeText(this, R.string.invite_entrant_private_only, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            buildExcludedUsers(event);
        });
    }

    /**
     * Performs build excluded users.
     *
     * @param event the event
     */
    private void buildExcludedUsers(Event event) {
        excludedUserIds.clear();
        if (event == null) {
            return;
        }

        if (!TextUtils.isEmpty(event.getOrganizerId())) {
            excludedUserIds.add(event.getOrganizerId());
        }
        if (event.getInvited() != null) {
            excludedUserIds.addAll(event.getInvited());
        }
        if (event.getWaitingList() != null && event.getWaitingList().list != null) {
            excludedUserIds.addAll(event.getWaitingList().list);
        }
        if (event.getEnrolled() != null) {
            excludedUserIds.addAll(event.getEnrolled());
        }
        if (event.getChosen() != null) {
            excludedUserIds.addAll(event.getChosen());
        }
    }

    /**
     * Performs run search.
     */
    private void runSearch() {
        String query = searchInput.getText() == null ? "" : searchInput.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(this, R.string.invite_entrant_search_prompt, Toast.LENGTH_SHORT).show();
            return;
        }

        controller.searchUsers(query, (users, success) -> {
            if (!success || users == null) {
                Toast.makeText(this, R.string.invite_entrant_search_failure, Toast.LENGTH_SHORT).show();
                return;
            }

            renderResults(filterUsers(users));
        });
    }

    /**
     * Returns the result of filter users.
     *
     * @param users the users
     * @return the result of this call
     */
    private List<User> filterUsers(List<User> users) {
        List<User> filtered = new ArrayList<>();
        if (users == null) {
            return filtered;
        }

        for (User user : users) {
            if (user == null || TextUtils.isEmpty(user.getDeviceId())) {
                continue;
            }
            if (excludedUserIds.contains(user.getDeviceId())) {
                continue;
            }
            filtered.add(user);
        }
        return filtered;
    }

    /**
     * Performs render results.
     *
     * @param users the users
     */
    private void renderResults(List<User> users) {
        resultsContainer.removeAllViews();
        if (users == null || users.isEmpty()) {
            emptyStateText.setText(R.string.invite_entrant_empty_state);
            emptyStateText.setVisibility(View.VISIBLE);
            return;
        }

        emptyStateText.setVisibility(View.GONE);
        for (User user : users) {
            View row = layoutInflater.inflate(R.layout.item_invite_user, resultsContainer, false);
            bindRow(row, user);
            resultsContainer.addView(row);
        }
    }

    /**
     * Performs bind row.
     *
     * @param row the row
     * @param user the user
     */
    private void bindRow(View row, User user) {
        TextView userNameText = row.findViewById(R.id.userNameText);
        TextView userDetailText = row.findViewById(R.id.userDetailText);
        TextView inviteButton = row.findViewById(R.id.inviteButton);

        userNameText.setText(TextUtils.isEmpty(user.getName()) ? getString(R.string.invite_entrant_unknown_name) : user.getName());
        userDetailText.setText(buildUserDetail(user));

        inviteButton.setOnClickListener(view -> inviteUser(user, inviteButton));
    }

    /**
     * Performs invite user.
     *
     * @param user the user
     * @param inviteButton the invite button
     */
    private void inviteUser(User user, TextView inviteButton) {
        if (user == null || TextUtils.isEmpty(user.getDeviceId())) {
            return;
        }
        inviteButton.setEnabled(false);

        controller.inviteUser(eventId, user.getDeviceId(), (result, success) -> {
            inviteButton.setEnabled(true);
            if (!success || result == null || !result) {
                Toast.makeText(this, R.string.invite_entrant_invite_failure, Toast.LENGTH_SHORT).show();
                return;
            }

            excludedUserIds.add(user.getDeviceId());
            Toast.makeText(this, R.string.invite_entrant_invite_success, Toast.LENGTH_SHORT).show();
            runSearch();
        });
    }

    /**
     * Returns the result of build user detail.
     *
     * @param user the user
     * @return the result of this call
     */
    private String buildUserDetail(User user) {
        String email = user.getEmail();
        String phone = user.getPhone();
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(phone)) {
            return email + " • " + phone;
        }
        if (!TextUtils.isEmpty(email)) {
            return email;
        }
        if (!TextUtils.isEmpty(phone)) {
            return phone;
        }
        return getString(R.string.invite_entrant_no_contact);
    }
}
