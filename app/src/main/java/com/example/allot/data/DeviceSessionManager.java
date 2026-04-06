package com.example.allot.data;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.UUID;
/**
 * Manages the device ID for the current app user.
 */
public class DeviceSessionManager {
    private static final String PREFS_NAME = "allot_prefs";
    private static final String DEVICE_ID_KEY = "device_id";

    private final String deviceId;
    private final boolean newDeviceId;

    /**
     * Creates a DeviceSessionManager and loads or creates the current device ID.
     *
     * @param context the context used to access shared preferences
     */
    public DeviceSessionManager(Context context) {
        this(new SharedPreferencesDeviceSessionStore(
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        ));
    }

    /**
     * Creates a new DeviceSessionManager instance.
     */
    public DeviceSessionManager(DeviceSessionStore deviceSessionStore) {
        // Get the saved device ID, or create one if it does not exist yet
        DeviceIdResult deviceIdResult = getOrCreateDeviceId(deviceSessionStore);
        this.deviceId = deviceIdResult.deviceId;
        this.newDeviceId = deviceIdResult.wasCreated;
    }

    /**
     * Gets the saved device ID or creates a new one if needed.
     *
     * @param deviceSessionStore the store used to access saved device session values
     * @return the existing or newly created device ID
     */
    private DeviceIdResult getOrCreateDeviceId(DeviceSessionStore deviceSessionStore) {
        // Get the Device ID
        String savedDeviceId = deviceSessionStore.getDeviceId();

        // Return the saved device ID if it already exists
        if (savedDeviceId != null && !savedDeviceId.trim().isEmpty()) {
            return new DeviceIdResult(savedDeviceId, false);
        }

        // Otherwise create a new unique device ID and save it
        String newDeviceId = UUID.randomUUID().toString();
        deviceSessionStore.saveDeviceId(newDeviceId);
        return new DeviceIdResult(newDeviceId, true);
    }

    /**
     * Returns whether g.et Current Device Id
     */
    public String getCurrentDeviceId() {
        return deviceId;
    }

    /**
     * Returns whether i.s New Device Id
     */
    public boolean isNewDeviceId() {
        return newDeviceId;
    }

    public interface DeviceSessionStore {
        /**
         * Returns whether g.et Device Id
         */
        String getDeviceId();
        /**
         * Saves device id.
         */
        void saveDeviceId(String deviceId);
    }

    private static class SharedPreferencesDeviceSessionStore implements DeviceSessionStore {
        private final SharedPreferences prefs;

        /**
         * Handles shared Preferences Device Session Store.
         */
        private SharedPreferencesDeviceSessionStore(SharedPreferences prefs) {
            this.prefs = prefs;
        }

        /**
         * Returns whether g.et Device Id
         */
        @Override
        public String getDeviceId() {
            return prefs.getString(DEVICE_ID_KEY, null);
        }

        /**
         * Saves device id.
         */
        @Override
        public void saveDeviceId(String deviceId) {
            prefs.edit().putString(DEVICE_ID_KEY, deviceId).apply();
        }
    }

    static class DeviceIdResult {
        private final String deviceId;
        private final boolean wasCreated;

        /**
         * Handles device Id Result.
         */
        private DeviceIdResult(String deviceId, boolean wasCreated) {
            this.deviceId = deviceId;
            this.wasCreated = wasCreated;
        }
    }
}







