package com.example.allot.model.event;

import java.util.Date;

/**
 * Stores one entrant's captured location details when joining a waitlist.
 */
public class WaitlistJoinLocation {
    private Double latitude;
    private Double longitude;
    private Date joinedAt;

    /**
     * Creates a new WaitlistJoinLocation instance.
     */
    public WaitlistJoinLocation() {
    }

    /**
     * Creates a new WaitlistJoinLocation instance.
     *
     * @param latitude the latitude
     * @param longitude the longitude
     * @param joinedAt the joined at
     */
    public WaitlistJoinLocation(Double latitude, Double longitude, Date joinedAt) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.joinedAt = joinedAt;
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
     * Updates the latitude.
     *
     * @param latitude the latitude
     */
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
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
     * Updates the longitude.
     *
     * @param longitude the longitude
     */
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    /**
     * Returns the joined at.
     *
     * @return the joined at
     */
    public Date getJoinedAt() {
        return joinedAt;
    }

    /**
     * Updates the joined at.
     *
     * @param joinedAt the joined at
     */
    public void setJoinedAt(Date joinedAt) {
        this.joinedAt = joinedAt;
    }
}
