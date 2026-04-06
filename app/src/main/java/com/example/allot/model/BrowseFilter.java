package com.example.allot.model;
/**
 * Stores the search text and category filter for browsing events.
 */
public class BrowseFilter {
    private final String searchTerm;
    private final String selectedCategory;
    private final String keywords;
    private final java.util.Date startDate;
    private final Double latitude;
    private final Double longitude;
    private final Double distanceKm;
    private final Boolean onlyOpenSpots;
    private final Integer minimumCapacity;

    /**
     * Creates a new BrowseFilter instance.
     *
     * @param searchTerm the search term
     * @param selectedCategory the selected category
     */
    public BrowseFilter(String searchTerm, String selectedCategory) {
        this(searchTerm, selectedCategory, null, null, null, null, null, null, null);
    }

    /**
     * Creates a new BrowseFilter instance.
     *
     * @param searchTerm the search term
     * @param selectedCategory the selected category
     * @param keywords the keywords
     * @param startDate the start date
     * @param latitude the latitude
     * @param longitude the longitude
     * @param distanceKm the distance km
     * @param onlyOpenSpots the only open spots
     * @param minimumCapacity the minimum capacity
     */
    public BrowseFilter(String searchTerm,
                        String selectedCategory,
                        String keywords,
                        java.util.Date startDate,
                        Double latitude,
                        Double longitude,
                        Double distanceKm,
                        Boolean onlyOpenSpots,
                        Integer minimumCapacity) {
        this.searchTerm = searchTerm;
        this.selectedCategory = selectedCategory;
        this.keywords = keywords;
        this.startDate = startDate;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distanceKm = distanceKm;
        this.onlyOpenSpots = onlyOpenSpots;
        this.minimumCapacity = minimumCapacity;
    }

    /**
     * Returns the search term.
     *
     * @return the search term
     */
    public String getSearchTerm() {
        return searchTerm;
    }

    /**
     * Returns the selected category.
     *
     * @return the selected category
     */
    public String getSelectedCategory() {
        return selectedCategory;
    }

    /**
     * Returns the keywords.
     *
     * @return the keywords
     */
    public String getKeywords() {
        return keywords;
    }

    /**
     * Returns the start date.
     *
     * @return the start date
     */
    public java.util.Date getStartDate() {
        return startDate;
    }

    /**
     * Returns the latitude.
     *
     * @return the latitude
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * Returns the longitude.
     *
     * @return the longitude
     */
    public Double getLongitude() {
        return longitude;
    }

    /**
     * Returns the distance km.
     *
     * @return the distance km
     */
    public Double getDistanceKm() {
        return distanceKm;
    }

    /**
     * Returns the only open spots.
     *
     * @return the only open spots
     */
    public Boolean getOnlyOpenSpots() {
        return onlyOpenSpots;
    }

    /**
     * Returns the minimum capacity.
     *
     * @return the minimum capacity
     */
    public Integer getMinimumCapacity() {
        return minimumCapacity;
    }
}








