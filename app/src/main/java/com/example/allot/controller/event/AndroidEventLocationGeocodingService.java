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

    /**
     * Creates a new AndroidEventLocationGeocodingService instance.
     */
    public AndroidEventLocationGeocodingService(Context context) {
        this.context = context == null ? null : context.getApplicationContext();
    }

    /**
     * Handles geocode.
     */
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

    /**
     * Handles reverse Geocode.
     */
    public String reverseGeocode(double latitude, double longitude) {
        if (context == null || !Geocoder.isPresent()) {
            return null;
        }

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses == null || addresses.isEmpty()) {
                return null;
            }

            Address address = addresses.get(0);
            String addressLine = address.getAddressLine(0);
            if (!TextHelper.isBlank(addressLine)) {
                return addressLine.trim();
            }

            StringBuilder builder = new StringBuilder();
            appendAddressPart(builder, address.getFeatureName());
            appendAddressPart(builder, address.getThoroughfare());
            appendAddressPart(builder, address.getLocality());
            appendAddressPart(builder, address.getAdminArea());
            appendAddressPart(builder, address.getPostalCode());
            return builder.length() == 0 ? null : builder.toString();
        } catch (IOException | IllegalArgumentException exception) {
            return null;
        }
    }

    /**
     * Handles append Address Part.
     */
    private void appendAddressPart(StringBuilder builder, String value) {
        if (TextHelper.isBlank(value)) {
            return;
        }

        if (builder.length() > 0) {
            builder.append(", ");
        }
        builder.append(value.trim());
    }
}
