package com.example.allot;
import java.util.ArrayList;
public class Event {
    public String eventId;
    public String organizerId;
    public String title;
    public String description;
    public int capacity;
    public String status;
    public String posterUrl;  // This is the main image for the event

    public ArrayList<String> galleryUrls; // This is for the Gallery

    public Event() {
        this.status = "open";
        this.galleryUrls = new ArrayList<>(); // Initialize the list
    }

    // Testing events
    public Event(String eventId, String title) {
        this.eventId = eventId;
        this.title = title;
        this.status = "open";
        this.galleryUrls = new ArrayList<>();
    }

    // Organizer Creation
    public Event(String eventId, String organizerId, String title) {
        this.eventId = eventId;
        this.organizerId = organizerId;
        this.title = title;
        this.status = "open";
        this.galleryUrls = new ArrayList<>();
    }


    //Helper to add a photo to the gallery
    public void addPhotoToGallery(String newPhotoUrl) {
        if (this.galleryUrls == null) {
            this.galleryUrls = new ArrayList<>();
        }
        this.galleryUrls.add(newPhotoUrl);
    }
}