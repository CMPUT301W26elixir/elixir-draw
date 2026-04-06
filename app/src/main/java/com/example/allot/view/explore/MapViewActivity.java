package com.example.allot.view.explore;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.allot.R;
import com.example.allot.controller.shared.UserController;
import com.example.allot.data.EventRepository;
import com.example.allot.model.event.Event;
import com.example.allot.model.event.WaitlistJoinLocation;
import com.example.allot.model.profile.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Shows a standalone map screen that is ready for event marker integration.
 */
public class MapViewActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final String EXTRA_EVENT_ID = "event_id";

    private static final LatLng DEFAULT_CENTER = new LatLng(53.5232, -113.5263);
    private static final float DEFAULT_ZOOM = 13.5f;
    private static final int MAP_BOUNDS_PADDING = 140;

    private final EventRepository eventRepository = new EventRepository();
    private UserController userController;
    private final Map<String, String> userNameCache = new HashMap<>();
    private final Set<String> pendingUserIds = new HashSet<>();
    private GoogleMap googleMap;
    private Event currentEvent;
    private String currentEventId;
    private boolean eventLoadFinished;
    private boolean emptyStateShown;

    /**
     * Handles on Create.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        userController = new UserController(this);
        currentEventId = getIntent().getStringExtra(EXTRA_EVENT_ID);

        bindViews();
        setupHeader();
        attachMapFragment(savedInstanceState);
        loadEventIfNeeded();
    }

    /**
     * Handles finish.
     */
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    /**
     * Binds views.
     */
    private void bindViews() {
    }

    /**
     * Updates up header.
     */
    private void setupHeader() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
    }

    /**
     * Handles attach Map Fragment.
     */
    private void attachMapFragment(Bundle savedInstanceState) {
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapContainer);

        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mapContainer, mapFragment)
                    .commit();
        }

        if (savedInstanceState == null && mapFragment != null) {
            mapFragment.getMapAsync(this);
            return;
        }

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * Handles on Map Ready.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        renderMapContent();
    }

    /**
     * Loads event if needed.
     */
    private void loadEventIfNeeded() {
        if (TextUtils.isEmpty(currentEventId)) {
            eventLoadFinished = true;
            renderMapContent();
            return;
        }

        eventRepository.getEventById(currentEventId, (event, success) -> {
            eventLoadFinished = true;
            if (!success || event == null) {
                Toast.makeText(this, R.string.map_view_event_load_failure, Toast.LENGTH_SHORT).show();
                renderMapContent();
                return;
            }

            currentEvent = event;
            renderMapContent();
        });
    }

    /**
     * Handles render Map Content.
     */
    private void renderMapContent() {
        if (googleMap == null || !eventLoadFinished) {
            return;
        }

        googleMap.clear();

        if (TextUtils.isEmpty(currentEventId) || currentEvent == null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_CENTER, DEFAULT_ZOOM));
            return;
        }

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        int markerCount = 0;
        markerCount += addEventMarker(boundsBuilder);
        if (isEntrantLocationEnabled()) {
            markerCount += addEntrantMarkers(boundsBuilder);
        }

        if (markerCount == 0) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_CENTER, DEFAULT_ZOOM));
            showEmptyStateOnce();
            return;
        }

        if (markerCount == 1) {
            LatLng singleTarget = getSingleMarkerTarget();
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(singleTarget, DEFAULT_ZOOM));
            return;
        }

        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), MAP_BOUNDS_PADDING));
    }

    /**
     * Handles add Event Marker.
     */
    private int addEventMarker(LatLngBounds.Builder boundsBuilder) {
        if (currentEvent.getEventLatitude() == null || currentEvent.getEventLongitude() == null) {
            return 0;
        }

        LatLng eventLocation = new LatLng(currentEvent.getEventLatitude(), currentEvent.getEventLongitude());
        googleMap.addMarker(new MarkerOptions()
                .position(eventLocation)
                .title(TextUtils.isEmpty(currentEvent.getTitle()) ? getString(R.string.map_view_event_marker) : currentEvent.getTitle())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        boundsBuilder.include(eventLocation);
        return 1;
    }

    /**
     * Handles add Entrant Markers.
     */
    private int addEntrantMarkers(LatLngBounds.Builder boundsBuilder) {
        if (currentEvent.getWaitingList() == null || currentEvent.getWaitingList().getJoinLocations().isEmpty()) {
            return 0;
        }

        int markerCount = 0;
        for (Map.Entry<String, WaitlistJoinLocation> entry : currentEvent.getWaitingList().getJoinLocations().entrySet()) {
            String deviceId = entry.getKey();
            WaitlistJoinLocation joinLocation = entry.getValue();
            if (joinLocation == null || joinLocation.getLatitude() == null || joinLocation.getLongitude() == null) {
                continue;
            }

            LatLng entrantLocation = new LatLng(joinLocation.getLatitude(), joinLocation.getLongitude());
            googleMap.addMarker(new MarkerOptions()
                    .position(entrantLocation)
                    .title(getEntrantMarkerTitle(deviceId)));
            boundsBuilder.include(entrantLocation);
            markerCount++;
        }
        return markerCount;
    }

    /**
     * Returns whether i.s Entrant Location Enabled
     */
    private boolean isEntrantLocationEnabled() {
        return currentEvent != null && Boolean.TRUE.equals(currentEvent.getGeoloc());
    }

    /**
     * Returns whether g.et Entrant Marker Title
     */
    private String getEntrantMarkerTitle(String deviceId) {
        String cachedName = userNameCache.get(deviceId);
        if (!TextUtils.isEmpty(cachedName)) {
            return cachedName;
        }

        loadEntrantName(deviceId);
        return deviceId;
    }

    /**
     * Loads entrant name.
     */
    private void loadEntrantName(String deviceId) {
        if (TextUtils.isEmpty(deviceId) || pendingUserIds.contains(deviceId) || userNameCache.containsKey(deviceId)) {
            return;
        }

        pendingUserIds.add(deviceId);
        userController.getUserByDeviceId(deviceId, (User user, boolean success) -> {
            String displayName = deviceId;
            if (success && user != null && !TextUtils.isEmpty(user.getName())) {
                displayName = user.getName();
            }

            userNameCache.put(deviceId, displayName);
            pendingUserIds.remove(deviceId);
            runOnUiThread(this::renderMapContent);
        });
    }

    /**
     * Returns whether g.et Single Marker Target
     */
    private LatLng getSingleMarkerTarget() {
        if (currentEvent.getWaitingList() != null) {
            for (WaitlistJoinLocation joinLocation : currentEvent.getWaitingList().getJoinLocations().values()) {
                if (joinLocation != null && joinLocation.getLatitude() != null && joinLocation.getLongitude() != null) {
                    return new LatLng(joinLocation.getLatitude(), joinLocation.getLongitude());
                }
            }
        }

        if (currentEvent.getEventLatitude() != null && currentEvent.getEventLongitude() != null) {
            return new LatLng(currentEvent.getEventLatitude(), currentEvent.getEventLongitude());
        }

        return DEFAULT_CENTER;
    }

    /**
     * Shows empty state once.
     */
    private void showEmptyStateOnce() {
        if (emptyStateShown) {
            return;
        }

        emptyStateShown = true;
        Toast.makeText(this, R.string.map_view_no_join_locations, Toast.LENGTH_SHORT).show();
    }
}
