package com.example.allot.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.allot.R;

/**
 * Activity shown after an event is successfully created.
 * Displays a summary card for the event and provides navigation
 * to related screens such as QR code generation and the event page.
 */
public class CreateEventSuccessActivity extends AppCompatActivity {
    public static final String EXTRA_EVENT_ID = "event_id";
    public static final String EXTRA_EVENT_TITLE = "event_title";
    public static final String EXTRA_EVENT_LOCATION = "event_location";
    public static final String EXTRA_EVENT_DATE = "event_date";
    public static final String EXTRA_EVENT_PRICE = "event_price";
    public static final String EXTRA_EVENT_DEADLINE = "event_deadline";
    public static final String EXTRA_EVENT_CATEGORY = "event_category";

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

        TextView viewEventPageButton = findViewById(R.id.viewEventPageButton);
        viewEventPageButton.setOnClickListener(view -> openEventPage());
    }

    /**
     * Displays the event information on the summary card and selects the background image.
     */
    private void bindEventCard() {
        titleText.setText(defaultText(currentEventTitle, getString(R.string.default_event_name)));
        locationText.setText(defaultText(currentEventLocation, getString(R.string.default_street_name)));
        dateText.setText(defaultText(currentEventDate, getString(R.string.default_date)));
        imageBackground.setBackgroundResource(shouldUsePrimaryImage(currentEventCategory)
                ? R.drawable.bg_event_image_one
                : R.drawable.bg_event_image_two);
    }

    /**
     * Configures the bottom navigation bar and assigns click actions for each tab.
     */
    private void setupBottomNav() {
        bottomNavBar.setSelectedTab(BottomNavBarView.Tab.MY_EVENTS);
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.EXPLORE, view -> openExploreScreen());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.SAVED, view -> openSavedScreen());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.MY_EVENTS, view -> openHostingScreen());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.SCAN, view -> openScanScreen());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.PROFILE, view -> openProfileScreen());
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

        Intent intent = new Intent(this, EventQrCodeActivity.class);
        intent.putExtra(EXTRA_EVENT_ID, currentEventId);
        intent.putExtra(EXTRA_EVENT_TITLE, currentEventTitle);
        intent.putExtra(EXTRA_EVENT_LOCATION, currentEventLocation);
        intent.putExtra(EXTRA_EVENT_DATE, currentEventDate);
        intent.putExtra(EXTRA_EVENT_PRICE, getIntent().getStringExtra(EXTRA_EVENT_PRICE));
        intent.putExtra(EXTRA_EVENT_DEADLINE, getIntent().getStringExtra(EXTRA_EVENT_DEADLINE));
        intent.putExtra(EXTRA_EVENT_CATEGORY, currentEventCategory);
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

    /**
     * Opens the explore screen and clears intermediate activities.
     */
    private void openExploreScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    /**
     * Opens the saved events screen and clears intermediate activities.
     */
    private void openSavedScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("navigate_to", "saved");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    /**
     * Opens the hosting tab of the My Events screen and clears intermediate activities.
     */
    private void openHostingScreen() {
        Intent intent = new Intent(this, MyEventsActivity.class);
        intent.putExtra(MyEventsActivity.EXTRA_INITIAL_TAB, MyEventsActivity.INITIAL_TAB_HOSTING);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    /**
     * Opens the scan screen and clears intermediate activities.
     */
    private void openScanScreen() {
        Intent intent = new Intent(this, ScanActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    /**
     * Opens the profile screen and clears intermediate activities.
     */
    private void openProfileScreen() {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    /**
     * Determines which event card image should be used for the given category.
     *
     * @param category the event category
     * @return true if the primary image should be used, otherwise false
     */
    private boolean shouldUsePrimaryImage(String category) {
        if (TextUtils.isEmpty(category)) {
            return true;
        }
        return Math.abs(category.hashCode()) % 2 == 0;
    }

    /**
     * Returns the given value if it is not empty, otherwise returns the fallback text.
     *
     * @param value the text value to check
     * @param fallback the fallback text to use if the value is empty
     * @return the value or the fallback text
     */
    private String defaultText(String value, String fallback) {
        return TextUtils.isEmpty(value) ? fallback : value;
    }
}