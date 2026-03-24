package com.example.allot.view.organizer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.allot.R;
import com.example.allot.qr.QrCodeGenerator;
import com.example.allot.qr.QrCodePayloadBuilder;
import com.example.allot.view.event.EventCreatedActivity;
import com.example.allot.view.event.EventDetailActivity;
import com.example.allot.view.shared.AppNavigator;
import com.example.allot.view.shared.BottomNavBarView;
/**
 * Displays the QR code used to share or scan into an event.
 */
public class EventQrCodeActivity extends AppCompatActivity {
    private static final int QR_SIZE_PX = 920;

    private BottomNavBarView bottomNavBar;
    private ImageView qrImageView;
    private TextView qrErrorText;

    private String currentEventId;
    private String currentEventTitle;
    private String currentEventLocation;
    private String currentEventDate;
    private String currentEventCategory;
    private String currentEventVisibility;

    /**
     * Initializes the activity, reads event data from the intent,
     * binds views, generates the QR code, and sets up the bottom navigation bar.
     *
     * @param savedInstanceState the saved activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_qr_code);

        readExtras();
        bindViews();
        bindQrCode();
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
        currentEventId = intent.getStringExtra(EventCreatedActivity.EXTRA_EVENT_ID);
        currentEventTitle = intent.getStringExtra(EventCreatedActivity.EXTRA_EVENT_TITLE);
        currentEventLocation = intent.getStringExtra(EventCreatedActivity.EXTRA_EVENT_LOCATION);
        currentEventDate = intent.getStringExtra(EventCreatedActivity.EXTRA_EVENT_DATE);
        currentEventCategory = intent.getStringExtra(EventCreatedActivity.EXTRA_EVENT_CATEGORY);
        currentEventVisibility = intent.getStringExtra(EventCreatedActivity.EXTRA_EVENT_VISIBILITY);
    }

    /**
     * Binds all view references used by the activity and sets button listeners.
     */
    private void bindViews() {
        bottomNavBar = findViewById(R.id.bottomNavBar);
        qrImageView = findViewById(R.id.qrImageView);
        qrErrorText = findViewById(R.id.qrErrorText);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> AppNavigator.openMyEventsHosting(this, true));

        TextView viewEventPageButton = findViewById(R.id.viewEventPageButton);
        viewEventPageButton.setOnClickListener(view -> openEventPage());
    }

    /**
     * Generates and displays the QR code for the current event.
     * Shows an error message if QR code generation fails.
     */
    private void bindQrCode() {
        if (!com.example.allot.model.event.Event.VISIBILITY_PUBLIC.equalsIgnoreCase(currentEventVisibility)) {
            qrImageView.setImageDrawable(null);
            qrImageView.setVisibility(android.view.View.GONE);
            qrErrorText.setVisibility(android.view.View.VISIBLE);
            qrErrorText.setText(R.string.event_qr_private_unavailable);
            Toast.makeText(this, R.string.event_qr_private_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String payload = QrCodePayloadBuilder.buildEventPayload(currentEventId);
            Bitmap qrBitmap = QrCodeGenerator.generate(payload, QR_SIZE_PX);
            qrImageView.setImageBitmap(qrBitmap);
            qrImageView.setContentDescription(getString(R.string.event_qr_image_description));
            qrErrorText.setVisibility(android.view.View.GONE);
            qrImageView.setVisibility(android.view.View.VISIBLE);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            qrImageView.setImageDrawable(null);
            qrImageView.setVisibility(android.view.View.GONE);
            qrErrorText.setVisibility(android.view.View.VISIBLE);
            qrErrorText.setText(R.string.event_qr_generation_failure);
            Toast.makeText(this, R.string.event_qr_generation_failure, Toast.LENGTH_SHORT).show();
        }
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
     * Opens the event detail page for the current event.
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
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_PRICE,
                getIntent().getStringExtra(EventCreatedActivity.EXTRA_EVENT_PRICE));
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_DEADLINE,
                getIntent().getStringExtra(EventCreatedActivity.EXTRA_EVENT_DEADLINE));
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_CATEGORY, currentEventCategory);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

}








