package com.example.allot.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Represents an event in the application, including its details,
 * waiting list, selected entrants, and enrollment state.
 */
public class Event {
    public String eventId;
    public String organizerId;
    public String title;
    public String description;
    public String location;
    public String category;
    public int capacity;
    public Double price;
    public Date eventDate;
    public Date registrationOpen;
    public Date registrationDeadline;
    public String status;
    public String posterUrl;  // This is the main image for the event

    public int limit = -1;
    public WaitingList waitingList;
    public ArrayList<String> chosen;
    public ArrayList<String> enrolled;
    public ArrayList<String> cancelled;
    public ArrayList<String> notEnrolled;

    public ArrayList<String> galleryUrls; // This is for the Gallery

    public Boolean geoloc;

    /**
     * Creates an empty Event object for Firestore document deserialization.
     */
    public Event() {
        this.galleryUrls = new ArrayList<>();
        this.cancelled = new ArrayList<>();
    }

    /**
     * Creates a new event with the basic organizer and waiting list information.
     *
     * @param eventId the unique ID of the event
     * @param organizerId the device ID of the organizer
     * @param title the title of the event
     * @param limit the maximum number of entrants to select
     */
    public Event(String eventId, String organizerId, String title, int limit) {
        this.eventId = eventId;
        this.organizerId = organizerId;
        this.title = title;
        this.status = "open";
        this.galleryUrls = new ArrayList<>();
        this.cancelled = new ArrayList<>();
        this.limit = limit;
        this.waitingList = new WaitingList(limit);
    }

    /**
     * Adds a photo URL to the event gallery.
     *
     * @param newPhotoUrl the URL of the photo to add
     */
    public void addPhotoToGallery(String newPhotoUrl) {
        if (this.galleryUrls == null) {
            this.galleryUrls = new ArrayList<>();
        }
        this.galleryUrls.add(newPhotoUrl);
    }

    /**
     * Runs the lottery selection for the event and stores the chosen entrants.
     */
    public void lottery(){
        this.waitingList.selectedList();
        this.chosen = this.waitingList.chosen;
    }

    /**
     * Updates the enrolled entrant list from the waiting list status.
     */
    public void enrolled(){
        this.enrolled = this.waitingList.enrolled();
    }

    /**
     * Updates the not-enrolled entrant list from the waiting list status.
     */
    public void notenrolled(){
        this.notEnrolled = this.waitingList.notEnrolled();
    }

    /**
     * Returns the waiting list for the event, creating one if it does not exist.
     *
     * @return the waiting list for the event
     */
    public WaitingList getWaitingList() {
        if (waitingList == null) waitingList = new WaitingList(limit);
        return waitingList;
    }

    /**
     * Returns the enrollment status map for selected entrants.
     *
     * @return a map of entrant IDs to enrollment status values
     */
    public HashMap<String, Boolean> status(){
        return this.waitingList.status;
    }
}