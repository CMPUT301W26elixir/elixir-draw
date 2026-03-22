package com.example.allot.model;
/**
 * Stores the search text and category filter for browsing events.
 */
public class BrowseFilter {
    private final String searchTerm;
    private final String selectedCategory;

    /**
     * Creates a browse filter with the provided search term and category.
     *
     * @param searchTerm the search text entered by the user
     * @param selectedCategory the selected category filter
     */
    public BrowseFilter(String searchTerm, String selectedCategory) {
        this.searchTerm = searchTerm;
        this.selectedCategory = selectedCategory;
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
}








