package com.example.allot.view;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_qr_code);

        readExtras();
        bindViews();
        bindQrCode();
        setupBottomNav();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    private void readExtras() {
        Intent intent = getIntent();
        currentEventId = intent.getStringExtra(CreateEventSuccessActivity.EXTRA_EVENT_ID);
        currentEventTitle = intent.getStringExtra(CreateEventSuccessActivity.EXTRA_EVENT_TITLE);
        currentEventLocation = intent.getStringExtra(CreateEventSuccessActivity.EXTRA_EVENT_LOCATION);
        currentEventDate = intent.getStringExtra(CreateEventSuccessActivity.EXTRA_EVENT_DATE);
        currentEventCategory = intent.getStringExtra(CreateEventSuccessActivity.EXTRA_EVENT_CATEGORY);
    }

    private void bindViews() {
        bottomNavBar = findViewById(R.id.bottomNavBar);
        qrImageView = findViewById(R.id.qrImageView);
        qrErrorText = findViewById(R.id.qrErrorText);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        TextView viewEventPageButton = findViewById(R.id.viewEventPageButton);
        viewEventPageButton.setOnClickListener(view -> openEventPage());
    }

    private void bindQrCode() {
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

    private void setupBottomNav() {
        bottomNavBar.setSelectedTab(BottomNavBarView.Tab.MY_EVENTS);
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.EXPLORE, view -> openExploreScreen());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.SAVED, view -> openSavedScreen());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.MY_EVENTS, view -> openHostingScreen());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.SCAN, view -> openScanScreen());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.PROFILE, view -> openProfileScreen());
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
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_PRICE,
                getIntent().getStringExtra(CreateEventSuccessActivity.EXTRA_EVENT_PRICE));
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_DEADLINE,
                getIntent().getStringExtra(CreateEventSuccessActivity.EXTRA_EVENT_DEADLINE));
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
}
