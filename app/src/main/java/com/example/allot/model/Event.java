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
    public String posterUrl;  // This is the main image for the event

    public int limit = -1;
    public WaitingList waitingList;
    public ArrayList<String> chosen;
    public ArrayList<String> enrolled;
    public ArrayList<String> cancelled;
    public ArrayList<String> notEnrolled;

    public ArrayList<String> galleryUrls; // This is for the Gallery

    public Boolean geoloc;

    // Required for Firestore document deserialization.
    public Event() {
        this.galleryUrls = new ArrayList<>();
        this.cancelled = new ArrayList<>();
    }

    // Organizer Creation
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

    //Helper to add a photo to the gallery
    public void addPhotoToGallery(String newPhotoUrl) {
        if (this.galleryUrls == null) {
            this.galleryUrls = new ArrayList<>();
        }
        this.galleryUrls.add(newPhotoUrl);
    }

    public void lottery(){
        this.waitingList.selectedList();
        this.chosen = this.waitingList.chosen;
    }

    public void enrolled(){
        this.enrolled = this.waitingList.enrolled();
    }

    public void notenrolled(){
        this.notEnrolled = this.waitingList.notEnrolled();
    }
    public WaitingList getWaitingList() {
        if (waitingList == null) waitingList = new WaitingList(limit);
        return waitingList;
    }

    public HashMap<String, Boolean> status(){
        return this.waitingList.status;
    }
}
