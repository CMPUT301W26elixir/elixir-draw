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
     */
    public WaitlistJoinLocation(Double latitude, Double longitude, Date joinedAt) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.joinedAt = joinedAt;
    }

    /**
     * Returns whether g.et Latitude
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * Updates latitude.
     */
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    /**
     * Returns whether g.et Longitude
     */
    public Double getLongitude() {
        return longitude;
    }

    /**
     * Updates longitude.
     */
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    /**
     * Returns whether g.et Joined At
     */
    public Date getJoinedAt() {
        return joinedAt;
    }

    /**
     * Updates joined at.
     */
    public void setJoinedAt(Date joinedAt) {
        this.joinedAt = joinedAt;
    }
}
