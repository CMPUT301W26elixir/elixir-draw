package com.example.allot.view.explore;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.allot.R;
import com.example.allot.data.EventRepository;
import com.example.allot.model.event.Event;
import com.example.allot.model.event.WaitlistJoinLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.Map;

/**
 * Shows a standalone map screen that is ready for event marker integration.
 */
public class MapViewActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final String EXTRA_EVENT_ID = "event_id";

    private static final LatLng DEFAULT_CENTER = new LatLng(53.5232, -113.5263);
    private static final float DEFAULT_ZOOM = 13.5f;
    private static final int MAP_BOUNDS_PADDING = 140;

    private final EventRepository eventRepository = new EventRepository();
    private GoogleMap googleMap;
    private Event currentEvent;
    private String currentEventId;
    private boolean eventLoadFinished;
    private boolean emptyStateShown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);

        currentEventId = getIntent().getStringExtra(EXTRA_EVENT_ID);

        bindViews();
        setupHeader();
        attachMapFragment(savedInstanceState);
        loadEventIfNeeded();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    private void bindViews() {
    }

    private void setupHeader() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
    }

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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        renderMapContent();
    }

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
        markerCount += addEntrantMarkers(boundsBuilder);

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

    private int addEntrantMarkers(LatLngBounds.Builder boundsBuilder) {
        if (currentEvent.getWaitingList() == null || currentEvent.getWaitingList().getJoinLocations().isEmpty()) {
            return 0;
        }

        int markerCount = 0;
        for (Map.Entry<String, WaitlistJoinLocation> entry : currentEvent.getWaitingList().getJoinLocations().entrySet()) {
            WaitlistJoinLocation joinLocation = entry.getValue();
            if (joinLocation == null || joinLocation.getLatitude() == null || joinLocation.getLongitude() == null) {
                continue;
            }

            LatLng entrantLocation = new LatLng(joinLocation.getLatitude(), joinLocation.getLongitude());
            googleMap.addMarker(new MarkerOptions()
                    .position(entrantLocation)
                    .title(entry.getKey()));
            boundsBuilder.include(entrantLocation);
            markerCount++;
        }
        return markerCount;
    }

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

    private void showEmptyStateOnce() {
        if (emptyStateShown) {
            return;
        }

        emptyStateShown = true;
        Toast.makeText(this, R.string.map_view_no_join_locations, Toast.LENGTH_SHORT).show();
    }
}
