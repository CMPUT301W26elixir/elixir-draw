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
     * Creates a new DeviceSessionManager instance.
     *
     * @param context the context
     */
    public DeviceSessionManager(Context context) {
        this(new SharedPreferencesDeviceSessionStore(
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        ));
    }

    /**
     * Creates a new DeviceSessionManager instance.
     *
     * @param deviceSessionStore the device session store
     */
    public DeviceSessionManager(DeviceSessionStore deviceSessionStore) {
        // Get the saved device ID, or create one if it does not exist yet
        DeviceIdResult deviceIdResult = getOrCreateDeviceId(deviceSessionStore);
        this.deviceId = deviceIdResult.deviceId;
        this.newDeviceId = deviceIdResult.wasCreated;
    }

    /**
     * Returns the or create device id.
     *
     * @param deviceSessionStore the device session store
     * @return the or create device id
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
     * Returns the current device id.
     *
     * @return the current device id
     */
    public String getCurrentDeviceId() {
        return deviceId;
    }

    /**
     * Returns whether new device id.
     *
     * @return whether new device id
     */
    public boolean isNewDeviceId() {
        return newDeviceId;
    }

    public interface DeviceSessionStore {
        /**
         * Returns the device id.
         *
         * @return the device id
         */
        String getDeviceId();
        /**
         * Performs save device id.
         *
         * @param deviceId the device id
         */
        void saveDeviceId(String deviceId);
    }

    private static class SharedPreferencesDeviceSessionStore implements DeviceSessionStore {
        private final SharedPreferences prefs;

        /**
         * Creates a new SharedPreferencesDeviceSessionStore instance.
         *
         * @param prefs the prefs
         */
        private SharedPreferencesDeviceSessionStore(SharedPreferences prefs) {
            this.prefs = prefs;
        }

        /**
         * Returns the device id.
         *
         * @return the device id
         */
        @Override
        public String getDeviceId() {
            return prefs.getString(DEVICE_ID_KEY, null);
        }

        /**
         * Performs save device id.
         *
         * @param deviceId the device id
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
         * Creates a new DeviceIdResult instance.
         *
         * @param deviceId the device id
         * @param wasCreated the was created
         */
        private DeviceIdResult(String deviceId, boolean wasCreated) {
            this.deviceId = deviceId;
            this.wasCreated = wasCreated;
        }
    }
}







