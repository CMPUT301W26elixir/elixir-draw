package com.example.allot.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;
import org.junit.Test;

public class BrowseFilterTest {
    @Test
    public void twoArgumentConstructor_setsOnlySearchAndCategory() {
        BrowseFilter filter = new BrowseFilter("music", "Sports");

        assertEquals("music", filter.getSearchTerm());
        assertEquals("Sports", filter.getSelectedCategory());
        assertNull(filter.getKeywords());
        assertNull(filter.getMinimumCapacity());
    }

    @Test
    public void fullConstructor_exposesAdvancedFilterValues() {
        Date startDate = new Date(1000L);
        BrowseFilter filter = new BrowseFilter(
                "music", "Sports", "live", startDate,
                53.5, -113.5, 10.0, true, 50
        );

        assertEquals("live", filter.getKeywords());
        assertEquals(startDate, filter.getStartDate());
        assertEquals(Double.valueOf(53.5), filter.getLatitude());
        assertEquals(Double.valueOf(-113.5), filter.getLongitude());
        assertEquals(Double.valueOf(10.0), filter.getDistanceKm());
        assertEquals(Boolean.TRUE, filter.getOnlyOpenSpots());
        assertEquals(Integer.valueOf(50), filter.getMinimumCapacity());
    }
}
