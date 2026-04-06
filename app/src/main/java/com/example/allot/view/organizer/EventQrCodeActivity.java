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
import com.example.allot.controller.organizer.EventQrCodeService;
import com.example.allot.model.event.EventQrCodePayloadBuilder;
import com.example.allot.view.event.EventCreatedActivity;
import com.example.allot.view.event.EventDetailActivity;
import com.example.allot.view.shared.AppNavigator;
import com.example.allot.view.shared.BottomNavBarView;
import java.io.IOException;
/**
 * Displays the QR code used to share or scan into an event.
 */
public class EventQrCodeActivity extends AppCompatActivity {
    private static final int QR_SIZE_PX = 920;

    private BottomNavBarView bottomNavBar;
    private ImageView qrImageView;
    private TextView qrErrorText;
    private TextView saveQrButton;

    private String currentEventId;
    private String currentEventTitle;
    private String currentEventLocation;
    private String currentEventDate;
    private String currentEventCategory;
    private Bitmap currentQrBitmap;
    private EventQrCodeService qrCodeService;

    /**
     * Handles the create callback.
     *
     * @param savedInstanceState the saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_qr_code);

        qrCodeService = new EventQrCodeService();
        readExtras();
        bindViews();
        bindQrCode();
        setupBottomNav();
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
     * Performs read extras.
     */
    private void readExtras() {
        Intent intent = getIntent();
        currentEventId = intent.getStringExtra(EventCreatedActivity.EXTRA_EVENT_ID);
        currentEventTitle = intent.getStringExtra(EventCreatedActivity.EXTRA_EVENT_TITLE);
        currentEventLocation = intent.getStringExtra(EventCreatedActivity.EXTRA_EVENT_LOCATION);
        currentEventDate = intent.getStringExtra(EventCreatedActivity.EXTRA_EVENT_DATE);
        currentEventCategory = intent.getStringExtra(EventCreatedActivity.EXTRA_EVENT_CATEGORY);
    }

    /**
     * Performs bind views.
     */
    private void bindViews() {
        bottomNavBar = findViewById(R.id.bottomNavBar);
        qrImageView = findViewById(R.id.qrImageView);
        qrErrorText = findViewById(R.id.qrErrorText);
        saveQrButton = findViewById(R.id.saveQrButton);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> AppNavigator.openMyEventsHosting(this, true));

        saveQrButton.setOnClickListener(view -> saveQrToGallery());

        TextView viewEventPageButton = findViewById(R.id.viewEventPageButton);
        viewEventPageButton.setOnClickListener(view -> openEventPage());
    }

    /**
     * Performs bind qr code.
     */
    private void bindQrCode() {
        try {
            String payload = EventQrCodePayloadBuilder.buildEventPayload(currentEventId);
            currentQrBitmap = qrCodeService.generate(payload, QR_SIZE_PX);
            qrImageView.setImageBitmap(currentQrBitmap);
            qrImageView.setContentDescription(getString(R.string.event_qr_image_description));
            qrImageView.setVisibility(android.view.View.VISIBLE);
            qrErrorText.setVisibility(android.view.View.GONE);
            saveQrButton.setEnabled(true);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            currentQrBitmap = null;
            qrImageView.setImageDrawable(null);
            qrImageView.setVisibility(android.view.View.GONE);
            qrErrorText.setVisibility(android.view.View.VISIBLE);
            qrErrorText.setText(R.string.event_qr_generation_failure);
            saveQrButton.setEnabled(false);
            Toast.makeText(this, R.string.event_qr_generation_failure, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Performs save qr to gallery.
     */
    private void saveQrToGallery() {
        if (currentQrBitmap == null) {
            Toast.makeText(this, R.string.event_qr_save_failure, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            qrCodeService.saveToGallery(this, currentQrBitmap, currentEventTitle, currentEventId);
            Toast.makeText(this, R.string.event_qr_save_success, Toast.LENGTH_SHORT).show();
        } catch (IOException | SecurityException exception) {
            Toast.makeText(this, R.string.event_qr_save_failure, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Updates the up bottom nav.
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
     * Performs open event page.
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








