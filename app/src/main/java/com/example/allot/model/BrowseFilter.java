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
     * Creates a browse filter with the provided search term and category.
     *
     * @param searchTerm the search text entered by the user
     * @param selectedCategory the selected category filter
     */
    public BrowseFilter(String searchTerm, String selectedCategory) {
        this(searchTerm, selectedCategory, null, null, null, null, null, null, null);
    }

    /**
     * Creates a browse filter with search, category, and optional advanced filters.
     *
     * @param searchTerm the search text entered by the user
     * @param selectedCategory the selected category filter
     * @param keywords optional keywords for title/description matching
     * @param startDate optional minimum event date
     * @param latitude optional filter latitude
     * @param longitude optional filter longitude
     * @param distanceKm optional filter distance in kilometers
     * @param onlyOpenSpots optional open-spots requirement
     * @param minimumCapacity optional minimum effective capacity
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
     * Returns the active search term.
     *
     * @return the search term
     */
    public String getSearchTerm() {
        return searchTerm;
    }

    /**
     * Returns the active category filter.
     *
     * @return the selected category
     */
    public String getSelectedCategory() {
        return selectedCategory;
    }

    /**
     * Returns whether g.et Keywords
     */
    public String getKeywords() {
        return keywords;
    }

    /**
     * Returns whether g.et Start Date
     */
    public java.util.Date getStartDate() {
        return startDate;
    }

    /**
     * Returns whether g.et Latitude
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * Returns whether g.et Longitude
     */
    public Double getLongitude() {
        return longitude;
    }

    /**
     * Returns whether g.et Distance Km
     */
    public Double getDistanceKm() {
        return distanceKm;
    }

    /**
     * Returns whether g.et Only Open Spots
     */
    public Boolean getOnlyOpenSpots() {
        return onlyOpenSpots;
    }

    /**
     * Returns whether g.et Minimum Capacity
     */
    public Integer getMinimumCapacity() {
        return minimumCapacity;
    }
}








