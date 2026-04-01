package com.example.allot.view.explore;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.allot.R;
import com.example.allot.controller.event.AndroidEventLocationGeocodingService;
import com.example.allot.controller.event.EventLocationCoordinates;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Collects optional filters for browsing events.
 */
public class EventFilterActivity extends AppCompatActivity {
    public static final String EXTRA_DATE_BEGIN = "extra_date_begin";
    public static final String EXTRA_ADDRESS = "extra_address";
    public static final String EXTRA_DISTANCE_KM = "extra_distance_km";
    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";
    public static final String EXTRA_KEYWORDS = "extra_keywords";

    private EditText dateInput;
    private EditText addressInput;
    private EditText distanceInput;
    private EditText keywordsInput;
    private TextView saveButton;
    private ProgressBar loadingIndicator;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_filters);

        dateFormat.setLenient(false);

        bindViews();
        bindInitialValues();
        setupListeners();
    }

    private void bindViews() {
        dateInput = findViewById(R.id.filterDateInput);
        addressInput = findViewById(R.id.filterAddressInput);
        distanceInput = findViewById(R.id.filterDistanceInput);
        keywordsInput = findViewById(R.id.filterKeywordsInput);
        saveButton = findViewById(R.id.filterSaveButton);
        loadingIndicator = findViewById(R.id.filterLoadingIndicator);
    }

    private void bindInitialValues() {
        dateInput.setText(safeString(getIntent().getStringExtra(EXTRA_DATE_BEGIN)));
        addressInput.setText(safeString(getIntent().getStringExtra(EXTRA_ADDRESS)));
        keywordsInput.setText(safeString(getIntent().getStringExtra(EXTRA_KEYWORDS)));

        if (getIntent().hasExtra(EXTRA_DISTANCE_KM)) {
            double distance = getIntent().getDoubleExtra(EXTRA_DISTANCE_KM, 0);
            distanceInput.setText(distance > 0 ? String.valueOf(distance) : "");
        }
    }

    private void setupListeners() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        saveButton.setOnClickListener(view -> saveFilters());
    }

    private void saveFilters() {
        String rawDate = safeString(dateInput.getText());
        if (!rawDate.isEmpty() && !isValidDate(rawDate)) {
            Toast.makeText(this, R.string.filter_error_date, Toast.LENGTH_SHORT).show();
            return;
        }

        String address = safeString(addressInput.getText());
        String keywords = safeString(keywordsInput.getText());
        Double distanceKm = parseDistanceKm(safeString(distanceInput.getText()));

        if (!address.isEmpty() && distanceKm == null) {
            Toast.makeText(this, R.string.filter_error_distance, Toast.LENGTH_SHORT).show();
            return;
        }

        if (address.isEmpty() && distanceKm != null) {
            Toast.makeText(this, R.string.filter_error_address, Toast.LENGTH_SHORT).show();
            return;
        }

        if (address.isEmpty()) {
            finishWithResults(rawDate, address, distanceKm, null, null, keywords);
            return;
        }

        geocodeAndFinish(rawDate, address, distanceKm, keywords);
    }

    private void geocodeAndFinish(String rawDate, String address, Double distanceKm, String keywords) {
        setLoading(true);
        new Thread(() -> {
            AndroidEventLocationGeocodingService geocodingService = new AndroidEventLocationGeocodingService(this);
            EventLocationCoordinates coordinates = geocodingService.geocode(address);
            runOnUiThread(() -> {
                setLoading(false);
                if (coordinates == null) {
                    Toast.makeText(this, R.string.filter_error_address_not_found, Toast.LENGTH_SHORT).show();
                    return;
                }
                finishWithResults(rawDate, address, distanceKm,
                        coordinates.getLatitude(), coordinates.getLongitude(), keywords);
            });
        }).start();
    }

    private void finishWithResults(String rawDate,
                                   String address,
                                   Double distanceKm,
                                   Double latitude,
                                   Double longitude,
                                   String keywords) {
        android.content.Intent result = new android.content.Intent();
        result.putExtra(EXTRA_DATE_BEGIN, safeString(rawDate));
        result.putExtra(EXTRA_ADDRESS, safeString(address));
        result.putExtra(EXTRA_KEYWORDS, safeString(keywords));
        if (distanceKm != null) {
            result.putExtra(EXTRA_DISTANCE_KM, distanceKm);
        }
        if (latitude != null && longitude != null) {
            result.putExtra(EXTRA_LATITUDE, latitude);
            result.putExtra(EXTRA_LONGITUDE, longitude);
        }
        setResult(RESULT_OK, result);
        finish();
    }

    private boolean isValidDate(String value) {
        try {
            dateFormat.parse(value.trim());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Double parseDistanceKm(String value) {
        if (TextUtils.isEmpty(value)) {
            return null;
        }
        try {
            double parsed = Double.parseDouble(value.trim());
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void setLoading(boolean isLoading) {
        loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        saveButton.setEnabled(!isLoading);
    }

    private String safeString(CharSequence value) {
        return value == null ? "" : value.toString().trim();
    }
}
