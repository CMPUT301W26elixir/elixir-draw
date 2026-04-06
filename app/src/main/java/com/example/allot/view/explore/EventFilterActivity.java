package com.example.allot.view.explore;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import com.example.allot.R;
import com.example.allot.controller.event.AndroidEventLocationGeocodingService;
import com.example.allot.controller.event.EventLocationCoordinates;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Collects optional filters for browsing events.
 */
public class EventFilterActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST = 3001;

    public static final String EXTRA_DATE_BEGIN = "extra_date_begin";
    public static final String EXTRA_ADDRESS = "extra_address";
    public static final String EXTRA_DISTANCE_KM = "extra_distance_km";
    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";
    public static final String EXTRA_KEYWORDS = "extra_keywords";
    public static final String EXTRA_ONLY_OPEN_SPOTS = "extra_only_open_spots";
    public static final String EXTRA_MINIMUM_CAPACITY = "extra_minimum_capacity";

    private EditText dateInput;
    private EditText addressInput;
    private EditText distanceInput;
    private EditText keywordsInput;
    private EditText minimumCapacityInput;
    private CheckBox openSpotsCheckbox;
    private TextView saveButton;
    private ProgressBar loadingIndicator;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private boolean isAutoFillingAddress;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    /**
     * Handles on Create.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_filters);

        dateFormat.setLenient(false);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        bindViews();
        bindInitialValues();
        setupListeners();
        maybeAutoFillCurrentLocationAddress();
    }

    /**
     * Binds views.
     */
    private void bindViews() {
        dateInput = findViewById(R.id.filterDateInput);
        addressInput = findViewById(R.id.filterAddressInput);
        distanceInput = findViewById(R.id.filterDistanceInput);
        keywordsInput = findViewById(R.id.filterKeywordsInput);
        minimumCapacityInput = findViewById(R.id.filterMinimumCapacityInput);
        openSpotsCheckbox = findViewById(R.id.filterOpenSpotsCheckbox);
        saveButton = findViewById(R.id.filterSaveButton);
        loadingIndicator = findViewById(R.id.filterLoadingIndicator);
    }

    /**
     * Binds initial values.
     */
    private void bindInitialValues() {
        dateInput.setText(safeString(getIntent().getStringExtra(EXTRA_DATE_BEGIN)));
        addressInput.setText(safeString(getIntent().getStringExtra(EXTRA_ADDRESS)));
        keywordsInput.setText(safeString(getIntent().getStringExtra(EXTRA_KEYWORDS)));
        minimumCapacityInput.setText(readMinimumCapacityValue());
        openSpotsCheckbox.setChecked(getIntent().getBooleanExtra(EXTRA_ONLY_OPEN_SPOTS, false));

        if (getIntent().hasExtra(EXTRA_DISTANCE_KM)) {
            double distance = getIntent().getDoubleExtra(EXTRA_DISTANCE_KM, 0);
            distanceInput.setText(distance > 0 ? String.valueOf(distance) : "");
        }
    }

    /**
     * Updates up listeners.
     */
    private void setupListeners() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        saveButton.setOnClickListener(view -> saveFilters());
    }

    /**
     * Handles maybe Auto Fill Current Location Address.
     */
    private void maybeAutoFillCurrentLocationAddress() {
        if (!safeString(addressInput.getText()).isEmpty() || isAutoFillingAddress) {
            return;
        }

        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST
            );
            return;
        }

        loadCurrentLocationAddress();
    }

    /**
     * Saves filters.
     */
    private void saveFilters() {
        String rawDate = safeString(dateInput.getText());
        if (!rawDate.isEmpty() && !isValidDate(rawDate)) {
            Toast.makeText(this, R.string.filter_error_date, Toast.LENGTH_SHORT).show();
            return;
        }

        String address = safeString(addressInput.getText());
        String keywords = safeString(keywordsInput.getText());
        Double distanceKm = parseDistanceKm(safeString(distanceInput.getText()));
        Integer minimumCapacity = parseMinimumCapacity(safeString(minimumCapacityInput.getText()));
        boolean onlyOpenSpots = openSpotsCheckbox.isChecked();

        if (address.isEmpty() && distanceKm != null) {
            Toast.makeText(this, R.string.filter_error_address, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!safeString(minimumCapacityInput.getText()).isEmpty() && minimumCapacity == null) {
            Toast.makeText(this, R.string.filter_error_capacity, Toast.LENGTH_SHORT).show();
            return;
        }

        if (address.isEmpty()) {
            finishWithResults(rawDate, address, distanceKm, null, null, keywords, onlyOpenSpots, minimumCapacity);
            return;
        }

        geocodeAndFinish(rawDate, address, distanceKm, keywords, onlyOpenSpots, minimumCapacity);
    }

    /**
     * Handles geocode And Finish.
     */
    private void geocodeAndFinish(String rawDate,
                                  String address,
                                  Double distanceKm,
                                  String keywords,
                                  boolean onlyOpenSpots,
                                  Integer minimumCapacity) {
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
                        coordinates.getLatitude(), coordinates.getLongitude(), keywords,
                        onlyOpenSpots, minimumCapacity);
            });
        }).start();
    }

    /**
     * Returns whether h.as Location Permission
     */
    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Loads current location address.
     */
    private void loadCurrentLocationAddress() {
        isAutoFillingAddress = true;
        setLoading(true);

        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
        fusedLocationProviderClient
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                .addOnSuccessListener(this, location -> {
                    if (location == null) {
                        finishAutoFill();
                        return;
                    }

                    reverseGeocodeCurrentLocation(location);
                })
                .addOnFailureListener(this, exception -> finishAutoFill());
    }

    /**
     * Handles reverse Geocode Current Location.
     */
    private void reverseGeocodeCurrentLocation(Location location) {
        new Thread(() -> {
            AndroidEventLocationGeocodingService geocodingService = new AndroidEventLocationGeocodingService(this);
            String resolvedAddress = geocodingService.reverseGeocode(
                    location.getLatitude(),
                    location.getLongitude()
            );
            /**
             * Returns whether is Empty.
             */
            runOnUiThread(() -> {
                if (!isFinishing() && !isDestroyed() && safeString(addressInput.getText()).isEmpty()
                        && !TextUtils.isEmpty(resolvedAddress)) {
                    addressInput.setText(resolvedAddress);
                }
                finishAutoFill();
            });
        }).start();
    }

    /**
     * Handles finish Auto Fill.
     */
    private void finishAutoFill() {
        isAutoFillingAddress = false;
        setLoading(false);
    }

    /**
     * Handles finish With Results.
     */
    private void finishWithResults(String rawDate,
                                   String address,
                                   Double distanceKm,
                                   Double latitude,
                                   Double longitude,
                                   String keywords,
                                   boolean onlyOpenSpots,
                                   Integer minimumCapacity) {
        android.content.Intent result = new android.content.Intent();
        result.putExtra(EXTRA_DATE_BEGIN, safeString(rawDate));
        result.putExtra(EXTRA_ADDRESS, safeString(address));
        result.putExtra(EXTRA_KEYWORDS, safeString(keywords));
        result.putExtra(EXTRA_ONLY_OPEN_SPOTS, onlyOpenSpots);
        if (distanceKm != null) {
            result.putExtra(EXTRA_DISTANCE_KM, distanceKm);
        }
        if (minimumCapacity != null) {
            result.putExtra(EXTRA_MINIMUM_CAPACITY, minimumCapacity);
        }
        if (latitude != null && longitude != null) {
            result.putExtra(EXTRA_LATITUDE, latitude);
            result.putExtra(EXTRA_LONGITUDE, longitude);
        }
        setResult(RESULT_OK, result);
        finish();
    }

    /**
     * Returns whether i.s Valid Date
     */
    private boolean isValidDate(String value) {
        try {
            dateFormat.parse(value.trim());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Handles parse Distance Km.
     */
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

    /**
     * Handles parse Minimum Capacity.
     */
    private Integer parseMinimumCapacity(String value) {
        if (TextUtils.isEmpty(value)) {
            return null;
        }
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Handles read Minimum Capacity Value.
     */
    private String readMinimumCapacityValue() {
        if (!getIntent().hasExtra(EXTRA_MINIMUM_CAPACITY)) {
            return "";
        }
        int value = getIntent().getIntExtra(EXTRA_MINIMUM_CAPACITY, 0);
        return value > 0 ? String.valueOf(value) : "";
    }

    /**
     * Updates loading.
     */
    private void setLoading(boolean isLoading) {
        loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        saveButton.setEnabled(!isLoading);
    }

    /**
     * Handles on Request Permissions Result.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != LOCATION_PERMISSION_REQUEST) {
            return;
        }

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadCurrentLocationAddress();
        }
    }

    /**
     * Handles safe String.
     */
    private String safeString(CharSequence value) {
        return value == null ? "" : value.toString().trim();
    }
}
