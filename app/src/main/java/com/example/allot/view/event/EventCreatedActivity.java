package com.example.allot.view.event;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.allot.R;
import com.example.allot.view.event.EventDetailActivity;
import com.example.allot.view.organizer.EventQrCodeActivity;
import com.example.allot.view.shared.AppNavigator;
import com.example.allot.view.shared.BottomNavBarView;
import com.example.allot.view.shared.UiHelper;
/**
 * Shows the confirmation screen after a new event is created.
 */
public class EventCreatedActivity extends AppCompatActivity {
    public static final String EXTRA_EVENT_ID = "event_id";
    public static final String EXTRA_EVENT_TITLE = "event_title";
    public static final String EXTRA_EVENT_LOCATION = "event_location";
    public static final String EXTRA_EVENT_DATE = "event_date";
    public static final String EXTRA_EVENT_PRICE = "event_price";
    public static final String EXTRA_EVENT_DEADLINE = "event_deadline";
    public static final String EXTRA_EVENT_CATEGORY = "event_category";
    public static final String EXTRA_EVENT_VISIBILITY = "event_visibility";

    private BottomNavBarView bottomNavBar;
    private View imageBackground;
    private TextView titleText;
    private TextView locationText;
    private TextView dateText;

    private String currentEventId;
    private String currentEventTitle;
    private String currentEventLocation;
    private String currentEventDate;
    private String currentEventCategory;
    private String currentEventVisibility;

    /**
     * Initializes the activity, reads event data from the intent,
     * binds views, displays the event card, and configures the bottom navigation bar.
     *
     * @param savedInstanceState the saved activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event_success);

        readExtras();
        bindViews();
        bindEventCard();
        setupBottomNav();
    }

    /**
     * Finishes the activity without transition animation.
     */
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    /**
     * Reads the event details passed into the activity through intent extras.
     */
    private void readExtras() {
        Intent intent = getIntent();
        currentEventId = intent.getStringExtra(EXTRA_EVENT_ID);
        currentEventTitle = intent.getStringExtra(EXTRA_EVENT_TITLE);
        currentEventLocation = intent.getStringExtra(EXTRA_EVENT_LOCATION);
        currentEventDate = intent.getStringExtra(EXTRA_EVENT_DATE);
        currentEventCategory = intent.getStringExtra(EXTRA_EVENT_CATEGORY);
        currentEventVisibility = intent.getStringExtra(EXTRA_EVENT_VISIBILITY);
    }

    /**
     * Binds all view references used by the activity and sets button listeners.
     */
    private void bindViews() {
        bottomNavBar = findViewById(R.id.bottomNavBar);
        imageBackground = findViewById(R.id.imageBackground);
        titleText = findViewById(R.id.titleText);
        locationText = findViewById(R.id.locationText);
        dateText = findViewById(R.id.dateText);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> openHostingScreen());

        TextView generateQrButton = findViewById(R.id.generateQrButton);
        generateQrButton.setOnClickListener(view -> openQrCodeScreen());
        if (!com.example.allot.model.event.Event.VISIBILITY_PUBLIC.equalsIgnoreCase(currentEventVisibility)) {
            generateQrButton.setEnabled(false);
            generateQrButton.setAlpha(0.5f);
        }

        TextView viewEventPageButton = findViewById(R.id.viewEventPageButton);
        viewEventPageButton.setOnClickListener(view -> openEventPage());
    }

    /**
     * Displays the event information on the summary card and selects the background image.
     */
    private void bindEventCard() {
        titleText.setText(UiHelper.defaultText(currentEventTitle, getString(R.string.default_event_name)));
        locationText.setText(UiHelper.defaultText(currentEventLocation, getString(R.string.default_street_name)));
        dateText.setText(UiHelper.defaultText(currentEventDate, getString(R.string.default_date)));
        imageBackground.setBackgroundResource(UiHelper.eventImageBackgroundRes(currentEventCategory));
    }

    /**
     * Configures the bottom navigation bar and assigns click actions for each tab.
     */
    private void setupBottomNav() {
        bottomNavBar.setSelectedTab(BottomNavBarView.Tab.MY_EVENTS);
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.EXPLORE, view -> AppNavigator.openExplore(this, true));
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.SAVED, view -> AppNavigator.openSaved(this, true));
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.MY_EVENTS, view -> AppNavigator.openMyEventsHosting(this, true));
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.SCAN, view -> AppNavigator.openScan(this, true));
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.PROFILE, view -> AppNavigator.openProfile(this, true));
    }

    /**
     * Opens the QR code screen for the created event.
     * Shows an error message if the event ID is missing.
     */
    private void openQrCodeScreen() {
        if (TextUtils.isEmpty(currentEventId)) {
            Toast.makeText(this, R.string.event_qr_generation_failure, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!com.example.allot.model.event.Event.VISIBILITY_PUBLIC.equalsIgnoreCase(currentEventVisibility)) {
            Toast.makeText(this, R.string.event_qr_private_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, EventQrCodeActivity.class);
        intent.putExtra(EXTRA_EVENT_ID, currentEventId);
        intent.putExtra(EXTRA_EVENT_TITLE, currentEventTitle);
        intent.putExtra(EXTRA_EVENT_LOCATION, currentEventLocation);
        intent.putExtra(EXTRA_EVENT_DATE, currentEventDate);
        intent.putExtra(EXTRA_EVENT_PRICE, getIntent().getStringExtra(EXTRA_EVENT_PRICE));
        intent.putExtra(EXTRA_EVENT_DEADLINE, getIntent().getStringExtra(EXTRA_EVENT_DEADLINE));
        intent.putExtra(EXTRA_EVENT_CATEGORY, currentEventCategory);
        intent.putExtra(EXTRA_EVENT_VISIBILITY, currentEventVisibility);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    /**
     * Opens the event detail page for the created event.
     * Shows an error message if the event ID is missing.
     */
    private void openEventPage() {
        if (TextUtils.isEmpty(currentEventId)) {
            Toast.makeText(this, R.string.event_detail_error, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_ID, currentEventId);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_TITLE, currentEventTitle);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_LOCATION, currentEventLocation);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_DATE, currentEventDate);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_PRICE, getIntent().getStringExtra(EXTRA_EVENT_PRICE));
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_DEADLINE, getIntent().getStringExtra(EXTRA_EVENT_DEADLINE));
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_CATEGORY, currentEventCategory);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void openHostingScreen() {
        AppNavigator.openMyEventsHosting(this, true);
    }
}









