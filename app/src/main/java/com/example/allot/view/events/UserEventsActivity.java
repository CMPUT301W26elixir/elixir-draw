package com.example.allot.view.events;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.example.allot.R;
import com.example.allot.common.OnCompleteListener;
import com.example.allot.controller.events.UserEventsController;
import com.example.allot.view.event.CreateEventActivity;
import com.example.allot.view.event.EventDetailActivity;
import com.example.allot.view.shared.AppNavigator;
import com.example.allot.view.shared.BottomNavBarView;
import com.example.allot.view.shared.EventListItem;
import com.example.allot.view.shared.UiHelper;
import java.util.List;
/**
 * Shows the user's registered and hosted event lists.
 */
public class UserEventsActivity extends AppCompatActivity {

    /**
     * Intent extra key used to specify which top tab should be shown first.
     */
    public static final String EXTRA_INITIAL_TAB = "initial_tab";

    /**
     * Intent extra value indicating that the hosting tab should be selected initially.
     */
    public static final String INITIAL_TAB_HOSTING = "hosting";

    private BottomNavBarView bottomNavBar;
    private TextView registeredTabText;
    private TextView hostingTabText;
    private ProgressBar loadingIndicator;
    private TextView stateText;
    private LinearLayout registeredSectionsContainer;
    private LinearLayout invitedContainer;
    private LinearLayout coOrganizerInvitesContainer;
    private LinearLayout selectedContainer;
    private LinearLayout waitingContainer;
    private LinearLayout notSelectedContainer;
    private LinearLayout pastContainer;
    private LinearLayout hostingSectionsContainer;
    private LinearLayout ongoingContainer;
    private LinearLayout completedContainer;
    private TextView createEventButton;

    private UserEventsController myEventsController;
    private LayoutInflater layoutInflater;
    private TopTab currentTab = TopTab.REGISTERED;

    /**
     * Handles the create callback.
     *
     * @param savedInstanceState the saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);

        myEventsController = new UserEventsController(this);
        layoutInflater = LayoutInflater.from(this);

        bindViews();
        setupTopTabs();
        setupBottomNav();
        if (INITIAL_TAB_HOSTING.equals(getIntent().getStringExtra(EXTRA_INITIAL_TAB))) {
            showHostingTab();
        } else {
            showRegisteredTab();
        }
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
        registeredTabText = findViewById(R.id.registeredTabText);
        hostingTabText = findViewById(R.id.hostingTabText);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        stateText = findViewById(R.id.stateText);
        registeredSectionsContainer = findViewById(R.id.registeredSectionsContainer);
        invitedContainer = findViewById(R.id.invitedContainer);
        coOrganizerInvitesContainer = findViewById(R.id.coOrganizerInvitesContainer);
        selectedContainer = findViewById(R.id.selectedContainer);
        waitingContainer = findViewById(R.id.waitingContainer);
        notSelectedContainer = findViewById(R.id.notSelectedContainer);
        pastContainer = findViewById(R.id.pastContainer);
        hostingSectionsContainer = findViewById(R.id.hostingSectionsContainer);
        ongoingContainer = findViewById(R.id.ongoingContainer);
        completedContainer = findViewById(R.id.completedContainer);
        createEventButton = findViewById(R.id.createEventButton);
    }

    /**
     * Updates the up top tabs.
     */
    private void setupTopTabs() {
        registeredTabText.setOnClickListener(view -> showRegisteredTab());
        hostingTabText.setOnClickListener(view -> showHostingTab());
        createEventButton.setOnClickListener(view -> openCreateEventScreen());
    }

    /**
     * Updates the up bottom nav.
     */
    private void setupBottomNav() {
        bottomNavBar.setSelectedTab(BottomNavBarView.Tab.MY_EVENTS);
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.EXPLORE, view -> AppNavigator.openExplore(this, true));
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.SAVED, view -> AppNavigator.openSaved(this, true));
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.PROFILE, view -> AppNavigator.openProfile(this, true));
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.SCAN, view -> AppNavigator.openScan(this, false));
    }

    /**
     * Performs show registered tab.
     */
    private void showRegisteredTab() {
        currentTab = TopTab.REGISTERED;
        updateTopTabStyles();
        createEventButton.setVisibility(View.GONE);
        loadRegisteredEvents();
    }

    /**
     * Performs show hosting tab.
     */
    private void showHostingTab() {
        currentTab = TopTab.HOSTING;
        updateTopTabStyles();
        createEventButton.setVisibility(View.VISIBLE);
        loadHostedEvents();
    }

    /**
     * Performs update top tab styles.
     */
    private void updateTopTabStyles() {
        registeredTabText.setTextColor(ContextCompat.getColor(this,
                currentTab == TopTab.REGISTERED ? R.color.text_primary : R.color.my_events_tab_inactive));
        hostingTabText.setTextColor(ContextCompat.getColor(this,
                currentTab == TopTab.HOSTING ? R.color.text_primary : R.color.my_events_tab_inactive));
    }

    /**
     * Performs load registered events.
     */
    private void loadRegisteredEvents() {
        setLoadingState();
        myEventsController.loadRegisteredGroups((UserEventsController.RegisteredEventGroups groups, boolean success) -> {
            if (currentTab != TopTab.REGISTERED) {
                return;
            }

            if (!success || groups == null) {
                setErrorState();
                return;
            }

            bindRegisteredSections(
                    groups.getInvitedItems(),
                    groups.getCoOrganizerInviteItems(),
                    groups.getSelectedItems(),
                    groups.getWaitingItems(),
                    groups.getNotSelectedItems(),
                    groups.getPastItems(),
                    areAllRegisteredSectionsEmpty(groups) ? R.string.my_events_state_empty : 0
            );
        });
    }

    /**
     * Performs load hosted events.
     */
    private void loadHostedEvents() {
        setLoadingState();
        myEventsController.loadHostedGroups((UserEventsController.HostedEventGroups groups, boolean success) -> {
            if (currentTab != TopTab.HOSTING) {
                return;
            }

            if (!success || groups == null) {
                setErrorState();
                return;
            }

            bindHostedSections(
                    groups.getOngoingItems(),
                    groups.getCompletedItems(),
                    areAllHostedSectionsEmpty(groups) ? R.string.my_events_hosting_empty : 0
            );
        });
    }

    /**
     * Performs bind registered sections.
     *
     * @param invitedItems the invited items
     * @param coOrganizerInviteItems the co organizer invite items
     * @param selectedItems the selected items
     * @param waitingItems the waiting items
     * @param notSelectedItems the not selected items
     * @param pastItems the past items
     * @param stateMessageRes the state message res
     */
    private void bindRegisteredSections(List<EventListItem> invitedItems,
                                        List<EventListItem> coOrganizerInviteItems,
                                        List<EventListItem> selectedItems,
                                        List<EventListItem> waitingItems,
                                        List<EventListItem> notSelectedItems,
                                        List<EventListItem> pastItems,
                                        int stateMessageRes) {
        bindSection(invitedContainer, invitedItems, R.string.my_events_empty_invited);
        bindCoOrganizerInviteSection(coOrganizerInviteItems);
        bindSection(selectedContainer, selectedItems, R.string.my_events_empty_selected);
        bindSection(waitingContainer, waitingItems, R.string.my_events_empty_waiting);
        bindSection(notSelectedContainer, notSelectedItems, R.string.my_events_empty_not_selected);
        bindSection(pastContainer, pastItems, R.string.my_events_empty_past);

        loadingIndicator.setVisibility(View.GONE);
        stateText.setVisibility(stateMessageRes != 0 ? View.VISIBLE : View.GONE);
        if (stateMessageRes != 0) {
            stateText.setText(stateMessageRes);
        }
        registeredSectionsContainer.setVisibility(View.VISIBLE);
        hostingSectionsContainer.setVisibility(View.GONE);
    }

    /**
     * Performs bind hosted sections.
     *
     * @param ongoingItems the ongoing items
     * @param completedItems the completed items
     * @param stateMessageRes the state message res
     */
    private void bindHostedSections(List<EventListItem> ongoingItems,
                                    List<EventListItem> completedItems,
                                    int stateMessageRes) {
        bindSection(ongoingContainer, ongoingItems, R.string.my_events_empty_ongoing);
        bindSection(completedContainer, completedItems, R.string.my_events_empty_completed);

        loadingIndicator.setVisibility(View.GONE);
        stateText.setVisibility(stateMessageRes != 0 ? View.VISIBLE : View.GONE);
        if (stateMessageRes != 0) {
            stateText.setText(stateMessageRes);
        }
        registeredSectionsContainer.setVisibility(View.GONE);
        hostingSectionsContainer.setVisibility(View.VISIBLE);
    }

    /**
     * Performs bind section.
     *
     * @param container the container
     * @param items the items
     * @param emptyMessageRes the empty message res
     */
    private void bindSection(LinearLayout container, List<EventListItem> items, int emptyMessageRes) {
        container.removeAllViews();

        if (items == null || items.isEmpty()) {
            container.addView(createEmptyTextView(emptyMessageRes));
            return;
        }

        for (EventListItem item : items) {
            View cardView = layoutInflater.inflate(R.layout.item_my_event_status_card, container, false);
            bindCard(cardView, item);
            container.addView(cardView);
        }
    }

    /**
     * Performs bind co organizer invite section.
     *
     * @param items the items
     */
    private void bindCoOrganizerInviteSection(List<EventListItem> items) {
        coOrganizerInvitesContainer.removeAllViews();

        if (items == null || items.isEmpty()) {
            coOrganizerInvitesContainer.addView(createEmptyTextView(R.string.my_events_empty_coorganizer_invites));
            return;
        }

        for (EventListItem item : items) {
            View cardView = layoutInflater.inflate(R.layout.item_coorganizer_invite_card, coOrganizerInvitesContainer, false);
            bindCoOrganizerInviteCard(cardView, item);
            coOrganizerInvitesContainer.addView(cardView);
        }
    }

    /**
     * Returns the result of create empty text view.
     *
     * @param textRes the text res
     * @return the result of this call
     */
    private View createEmptyTextView(int textRes) {
        TextView emptyView = new TextView(this);
        emptyView.setText(textRes);
        emptyView.setTextColor(ContextCompat.getColor(this, R.color.my_events_section_empty));
        emptyView.setTextSize(14);
        emptyView.setPadding(0, 0, 0, dpToPx(12));
        return emptyView;
    }

    /**
     * Performs bind card.
     *
     * @param cardView the card view
     * @param eventItem the event item
     */
    private void bindCard(View cardView, EventListItem eventItem) {
        ImageView posterImage = cardView.findViewById(R.id.eventPosterImage);
        TextView titleText = cardView.findViewById(R.id.titleText);
        TextView locationText = cardView.findViewById(R.id.locationText);
        TextView dateText = cardView.findViewById(R.id.dateText);

        titleText.setText(eventItem == null ? null : eventItem.title);
        locationText.setText(eventItem == null ? null : eventItem.street);
        dateText.setText(eventItem == null ? null : eventItem.date);

        Glide.with(cardView.getContext()).clear(posterImage);
        posterImage.setImageResource(R.drawable.no_image);
        if (eventItem != null && !TextUtils.isEmpty(eventItem.getPosterUrl())) {
            Glide.with(cardView.getContext())
                    .load(eventItem.getPosterUrl())
                    .centerCrop()
                    .placeholder(R.drawable.no_image)
                    .error(R.drawable.no_image)
                    .into(posterImage);
        }

        cardView.setOnClickListener(view -> openEventDetailScreen(eventItem));
    }

    /**
     * Performs bind co organizer invite card.
     *
     * @param cardView the card view
     * @param eventItem the event item
     */
    private void bindCoOrganizerInviteCard(View cardView, EventListItem eventItem) {
        TextView titleText = cardView.findViewById(R.id.titleText);
        TextView locationText = cardView.findViewById(R.id.locationText);
        TextView dateText = cardView.findViewById(R.id.dateText);
        TextView acceptButton = cardView.findViewById(R.id.acceptButton);
        TextView declineButton = cardView.findViewById(R.id.declineButton);

        titleText.setText(eventItem == null ? null : eventItem.title);
        locationText.setText(eventItem == null ? null : eventItem.street);
        dateText.setText(eventItem == null ? null : eventItem.date);

        acceptButton.setOnClickListener(view -> handleCoOrganizerInvite(eventItem, true));
        declineButton.setOnClickListener(view -> handleCoOrganizerInvite(eventItem, false));
    }

    /**
     * Performs handle co organizer invite.
     *
     * @param eventItem the event item
     * @param isAccepting whether accepting
     */
    private void handleCoOrganizerInvite(EventListItem eventItem, boolean isAccepting) {
        if (eventItem == null || TextUtils.isEmpty(eventItem.eventId)) {
            return;
        }

        OnCompleteListener<Boolean> listener = (result, success) -> {
            if (!success || result == null || !result) {
                Toast.makeText(this, R.string.my_events_invite_action_failure, Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this,
                    isAccepting ? R.string.my_events_invite_accept_success : R.string.my_events_invite_decline_success,
                    Toast.LENGTH_SHORT).show();
            loadRegisteredEvents();
            if (isAccepting) {
                showHostingTab();
            }
        };

        if (isAccepting) {
            myEventsController.acceptCoOrganizerInvite(eventItem.eventId, listener);
        } else {
            myEventsController.declineCoOrganizerInvite(eventItem.eventId, listener);
        }
    }

    /**
     * Updates the loading state.
     */
    private void setLoadingState() {
        loadingIndicator.setVisibility(View.VISIBLE);
        stateText.setVisibility(View.VISIBLE);
        stateText.setText(R.string.my_events_state_loading);
        registeredSectionsContainer.setVisibility(View.GONE);
        hostingSectionsContainer.setVisibility(View.GONE);
    }

    /**
     * Updates the error state.
     */
    private void setErrorState() {
        loadingIndicator.setVisibility(View.GONE);
        stateText.setVisibility(View.VISIBLE);
        stateText.setText(R.string.my_events_state_error);
        registeredSectionsContainer.setVisibility(View.GONE);
        hostingSectionsContainer.setVisibility(View.GONE);
    }

    /**
     * Performs open event detail screen.
     *
     * @param eventItem the event item
     */
    private void openEventDetailScreen(EventListItem eventItem) {
        if (eventItem == null || TextUtils.isEmpty(eventItem.eventId)) {
            return;
        }

        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_ID, eventItem.eventId);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_TITLE, eventItem.title);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_LOCATION, eventItem.street);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_DATE, eventItem.date);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_PRICE, eventItem.price);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_DEADLINE, eventItem.daysLeft);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_CATEGORY, eventItem.category);
        startActivity(intent);
    }

    /**
     * Performs open create event screen.
     */
    private void openCreateEventScreen() {
        startActivity(new Intent(this, CreateEventActivity.class));
        overridePendingTransition(0, 0);
    }

    /**
     * Returns the result of dp to px.
     *
     * @param dp the dp
     * @return the result of this call
     */
    private int dpToPx(int dp) {
        return UiHelper.dpToPx(this, dp);
    }

    /**
     * Represents the two top tabs available in the activity.
     */
    private enum TopTab {
        REGISTERED,
        HOSTING
    }

    /**
     * Returns the result of are all registered sections empty.
     *
     * @param groups the groups
     * @return the result of this call
     */
    private boolean areAllRegisteredSectionsEmpty(UserEventsController.RegisteredEventGroups groups) {
        return groups.getInvitedItems().isEmpty()
                && groups.getCoOrganizerInviteItems().isEmpty()
                && groups.getSelectedItems().isEmpty()
                && groups.getWaitingItems().isEmpty()
                && groups.getNotSelectedItems().isEmpty()
                && groups.getPastItems().isEmpty();
    }

    /**
     * Returns the result of are all hosted sections empty.
     *
     * @param groups the groups
     * @return the result of this call
     */
    private boolean areAllHostedSectionsEmpty(UserEventsController.HostedEventGroups groups) {
        return groups.getOngoingItems().isEmpty() && groups.getCompletedItems().isEmpty();
    }
}









