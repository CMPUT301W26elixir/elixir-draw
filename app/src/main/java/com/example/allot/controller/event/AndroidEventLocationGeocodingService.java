package com.example.allot.controller.event;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import com.example.allot.common.TextHelper;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Uses the Android geocoder to resolve event addresses into coordinates.
 */
public class AndroidEventLocationGeocodingService implements EventLocationGeocodingService {
    private final Context context;

    public AndroidEventLocationGeocodingService(Context context) {
        this.context = context == null ? null : context.getApplicationContext();
    }

    @Override
    public EventLocationCoordinates geocode(String location) {
        if (context == null || TextHelper.isBlank(location) || !Geocoder.isPresent()) {
            return null;
        }

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(location.trim(), 1);
            if (addresses == null || addresses.isEmpty()) {
                return null;
            }

            Address address = addresses.get(0);
            return new EventLocationCoordinates(address.getLatitude(), address.getLongitude());
        } catch (IOException | IllegalArgumentException exception) {
            return null;
        }
    }
}
