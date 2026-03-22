package com.example.allot.controller.shared;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.allot.data.DeviceSessionManager;
import org.junit.Test;
public class DeviceSessionManagerTest {
    @Test
    public void returnsExistingSavedDeviceId() {
        FakeDeviceSessionStore store = new FakeDeviceSessionStore("saved-device-id");

        DeviceSessionManager manager = new DeviceSessionManager(store);

        assertEquals("saved-device-id", manager.getCurrentDeviceId());
        assertFalse(manager.isNewDeviceId());
        assertEquals("saved-device-id", store.savedDeviceId);
    }

    @Test
    public void createsAndStoresNewDeviceIdWhenMissing() {
        FakeDeviceSessionStore store = new FakeDeviceSessionStore(null);

        DeviceSessionManager manager = new DeviceSessionManager(store);

        assertNotNull(manager.getCurrentDeviceId());
        assertFalse(manager.getCurrentDeviceId().trim().isEmpty());
        assertEquals(manager.getCurrentDeviceId(), store.savedDeviceId);
        assertTrue(manager.isNewDeviceId());
    }

    @Test
    public void treatsBlankSavedValueAsMissing() {
        FakeDeviceSessionStore store = new FakeDeviceSessionStore("   ");

        DeviceSessionManager manager = new DeviceSessionManager(store);

        assertNotNull(manager.getCurrentDeviceId());
        assertFalse(manager.getCurrentDeviceId().trim().isEmpty());
        assertTrue(manager.isNewDeviceId());
    }

    private static class FakeDeviceSessionStore implements DeviceSessionManager.DeviceSessionStore {
        private String savedDeviceId;

        private FakeDeviceSessionStore(String savedDeviceId) {
            this.savedDeviceId = savedDeviceId;
        }

        @Override
        public String getDeviceId() {
            return savedDeviceId;
        }

        @Override
        public void saveDeviceId(String deviceId) {
            this.savedDeviceId = deviceId;
        }
    }
}









