package com.example.allot.model.event;

import java.util.Date;

/**
 * Stores one entrant's captured location details when joining a waitlist.
 */
public class WaitlistJoinLocation {
    private Double latitude;
    private Double longitude;
    private Date joinedAt;

    public WaitlistJoinLocation() {
    }

    public WaitlistJoinLocation(Double latitude, Double longitude, Date joinedAt) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.joinedAt = joinedAt;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Date getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Date joinedAt) {
        this.joinedAt = joinedAt;
    }
}
