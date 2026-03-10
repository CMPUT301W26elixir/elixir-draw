package com.example.allot;

import java.util.Date;

/**
 * CRC Card: WaitingList for the event
 */
public class WaitingList {
    public String entrantId;
    public String eventId;
    public String status; // "waiting", "selected", "declined", "cancelled"
    public Date registrationTime;
    public double latitude; //might need this
    public double longitude; //might need this
    public WaitingList() {}
    public WaitingList(String entrantId, String eventId) {
        this.entrantId = entrantId;
        this.eventId = eventId;
        this.status = "waiting";
        this.registrationTime = new Date();
    }
}