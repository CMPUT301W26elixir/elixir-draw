package com.example.allot.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

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
    public String posterUrl;

    public int limit = -1;
    public WaitingList waitingList;

    public ArrayList<String> chosen;
    public ArrayList<String> enrolled;
    public ArrayList<String> cancelled;
    public ArrayList<String> notEnrolled;

    public ArrayList<String> galleryUrls;

    public Boolean geoloc;

    /**
     * Default constructor required for Firestore document deserialization.
     * Initializes list fields to avoid null references.
     */
    public Event() {
        this.galleryUrls = new ArrayList<>();
        this.cancelled = new ArrayList<>();
    }

    /**
     * Creates a new Event object when an organizer creates an event.
     * Initializes the waiting list and default event state.
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
     * Adds a new photo URL to the event gallery.
     */
    public void addPhotoToGallery(String newPhotoUrl) {
        if (this.galleryUrls == null) {
            this.galleryUrls = new ArrayList<>();
        }
        this.galleryUrls.add(newPhotoUrl);
    }

    /**
     * Runs the event lottery selection process using the waiting list.
     * Selected participants are stored in the chosen list.
     *
     * AI assistance used for structuring the waiting list lottery selection logic.
     * Tool: ChatGPT (OpenAI), 2026.
     */
    public void lottery() {
        this.waitingList.selectedList();
        this.chosen = this.waitingList.chosen;
    }

    /**
     * Retrieves the list of users who successfully enrolled after the lottery.
     */
    public void enrolled() {
        this.enrolled = this.waitingList.enrolled();
    }

    /**
     * Retrieves the list of users who were not enrolled after the lottery.
     */
    public void notenrolled() {
        this.notEnrolled = this.waitingList.notEnrolled();
    }

    /**
     * Returns the event's waiting list object.
     * If it does not exist yet, it is initialized using the event limit.
     */
    public WaitingList getWaitingList() {
        if (waitingList == null) {
            waitingList = new WaitingList(limit);
        }
        return waitingList;
    }

    /**
     * Returns the status map of users in the waiting list.
     * The map tracks each user's selection or enrollment state.
     */
    public HashMap<String, Boolean> status() {
        return this.waitingList.status;
    }
}