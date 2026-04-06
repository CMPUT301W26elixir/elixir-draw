package com.example.allot.model.profile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import org.junit.Test;

public class UserTest {
    /**
     * Performs default constructor initializes event lists.
     */
    @Test
    public void defaultConstructor_initializesEventLists() {
        User user = new User();

        assertNotNull(user.getHistory());
        assertNotNull(user.getMyEvents());
        assertNotNull(user.getSavedEvents());
        assertTrue(user.getHistory().isEmpty());
    }

    /**
     * Performs get name handles missing and trimmed parts.
     */
    @Test
    public void getName_handlesMissingAndTrimmedParts() {
        User user = new User();
        user.setFirstName("  Taylor ");
        user.setLastName("  Swift ");
        assertEquals("Taylor Swift", user.getName());

        user.setLastName(" ");
        assertEquals("Taylor", user.getName());

        user.setFirstName(null);
        user.setLastName("Stone");
        assertEquals("Stone", user.getName());
    }

    /**
     * Performs getters reinitialize null lists.
     */
    @Test
    public void getters_reinitializeNullLists() {
        User user = new User();
        user.setHistory(null);
        user.setMyEvents(null);
        user.setSavedEvents(null);

        assertNotNull(user.getHistory());
        assertNotNull(user.getMyEvents());
        assertNotNull(user.getSavedEvents());
        user.setHistory(new ArrayList<>());
        assertTrue(user.getHistory().isEmpty());
    }
}
