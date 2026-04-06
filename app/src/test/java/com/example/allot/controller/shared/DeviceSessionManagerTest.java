package com.example.allot.controller.shared;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.allot.data.DeviceSessionManager;
import org.junit.Test;
/**
 * Tests the device session manager.
 */
public class DeviceSessionManagerTest {
    /**
     * Performs returns existing saved device id.
     */
    @Test
    public void returnsExistingSavedDeviceId() {
        FakeDeviceSessionStore store = new FakeDeviceSessionStore("saved-device-id");

        DeviceSessionManager manager = new DeviceSessionManager(store);

        assertEquals("saved-device-id", manager.getCurrentDeviceId());
        assertFalse(manager.isNewDeviceId());
        assertEquals("saved-device-id", store.savedDeviceId);
    }

    /**
     * Performs creates and stores new device id when missing.
     */
    @Test
    public void createsAndStoresNewDeviceIdWhenMissing() {
        FakeDeviceSessionStore store = new FakeDeviceSessionStore(null);

        DeviceSessionManager manager = new DeviceSessionManager(store);

        assertNotNull(manager.getCurrentDeviceId());
        assertFalse(manager.getCurrentDeviceId().trim().isEmpty());
        assertEquals(manager.getCurrentDeviceId(), store.savedDeviceId);
        assertTrue(manager.isNewDeviceId());
    }

    /**
     * Performs treats blank saved value as missing.
     */
    @Test
    public void treatsBlankSavedValueAsMissing() {
        FakeDeviceSessionStore store = new FakeDeviceSessionStore("   ");

        DeviceSessionManager manager = new DeviceSessionManager(store);

        assertNotNull(manager.getCurrentDeviceId());
        assertFalse(manager.getCurrentDeviceId().trim().isEmpty());
        assertTrue(manager.isNewDeviceId());
    }

    /**
     * Represents the fake device session store.
     */
    private static class FakeDeviceSessionStore implements DeviceSessionManager.DeviceSessionStore {
        private String savedDeviceId;

        /**
         * Creates a new FakeDeviceSessionStore instance.
         *
         * @param savedDeviceId the saved device id
         */
        private FakeDeviceSessionStore(String savedDeviceId) {
            this.savedDeviceId = savedDeviceId;
        }

        /**
         * Returns the device id.
         *
         * @return the device id
         */
        @Override
        public String getDeviceId() {
            return savedDeviceId;
        }

        /**
         * Performs save device id.
         *
         * @param deviceId the device id
         */
        @Override
        public void saveDeviceId(String deviceId) {
            this.savedDeviceId = deviceId;
        }
    }
}









