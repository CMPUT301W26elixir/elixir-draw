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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event_success);

        readExtras();
        bindViews();
        bindEventCard();
        setupBottomNav();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    private void readExtras() {
        Intent intent = getIntent();
        currentEventId = intent.getStringExtra(EXTRA_EVENT_ID);
        currentEventTitle = intent.getStringExtra(EXTRA_EVENT_TITLE);
        currentEventLocation = intent.getStringExtra(EXTRA_EVENT_LOCATION);
        currentEventDate = intent.getStringExtra(EXTRA_EVENT_DATE);
        currentEventCategory = intent.getStringExtra(EXTRA_EVENT_CATEGORY);
    }

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

    private void bindEventCard() {
        titleText.setText(defaultText(currentEventTitle, getString(R.string.default_event_name)));
        locationText.setText(defaultText(currentEventLocation, getString(R.string.default_street_name)));
        dateText.setText(defaultText(currentEventDate, getString(R.string.default_date)));
        imageBackground.setBackgroundResource(shouldUsePrimaryImage(currentEventCategory)
                ? R.drawable.bg_event_image_one
                : R.drawable.bg_event_image_two);
    }

    private void setupBottomNav() {
        bottomNavBar.setSelectedTab(BottomNavBarView.Tab.MY_EVENTS);
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.EXPLORE, view -> openExploreScreen());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.SAVED, view -> openSavedScreen());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.MY_EVENTS, view -> openHostingScreen());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.SCAN, view -> openScanScreen());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.PROFILE, view -> openProfileScreen());
    }

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

    private void openExploreScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    private void openSavedScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("navigate_to", "saved");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    private void openHostingScreen() {
        Intent intent = new Intent(this, MyEventsActivity.class);
        intent.putExtra(MyEventsActivity.EXTRA_INITIAL_TAB, MyEventsActivity.INITIAL_TAB_HOSTING);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    private void openScanScreen() {
        Intent intent = new Intent(this, ScanActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    private void openProfileScreen() {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    private boolean shouldUsePrimaryImage(String category) {
        if (TextUtils.isEmpty(category)) {
            return true;
        }
        return Math.abs(category.hashCode()) % 2 == 0;
    }

    private String defaultText(String value, String fallback) {
        return TextUtils.isEmpty(value) ? fallback : value;
    }
}
