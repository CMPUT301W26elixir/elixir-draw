package com.example.allot.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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
    public Date registrationDeadline;
    public String status;
    public String posterUrl;  // This is the main image for the event

    public int limit = -1;

    public int choosingLimit = 0;
    public WaitingList waitingList;
    public ArrayList<String> galleryUrls; // This is for the Gallery

    public Boolean geoloc;

    // Organizer Creation
    public Event(String eventId, String organizerId, String title, int limit, int choosingLimit) {
        this.eventId = eventId;
        this.organizerId = organizerId;
        this.title = title;
        this.status = "open";
        this.galleryUrls = new ArrayList<>();
        this.limit = limit;
        this.choosingLimit = choosingLimit;
        this.waitingList = new WaitingList(limit, choosingLimit);
    }


    //Helper to add a photo to the gallery
    public void addPhotoToGallery(String newPhotoUrl) {
        if (this.galleryUrls == null) {
            this.galleryUrls = new ArrayList<>();
        }
        this.galleryUrls.add(newPhotoUrl);
    }

    public String getBrowseTitleText() {
        return isBlank(title) ? "Untitled Event" : title;
    }

    public String getBrowseLocationText() {
        return isBlank(location) ? "Location TBA" : location;
    }

    public String getBrowseDateText() {
        if (eventDate == null) {
            return "Date TBA";
        }

        return new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(eventDate);
    }

    public String getBrowsePriceText() {
        if (price == null || price <= 0) {
            return "Free";
        }

        if (Math.rint(price) == price) {
            return String.format(Locale.getDefault(), "$%.0f", price);
        }

        return String.format(Locale.getDefault(), "$%.2f", price);
    }

    public String getBrowseDeadlineText() {
        if (registrationDeadline == null) {
            return "Deadline TBA";
        }

        long millisRemaining = registrationDeadline.getTime() - System.currentTimeMillis();
        if (millisRemaining <= 0) {
            return "Closed";
        }

        long daysLeft = TimeUnit.MILLISECONDS.toDays(millisRemaining);
        if (daysLeft == 0) {
            return "Ends Today";
        }

        if (daysLeft == 1) {
            return "1 Day Left";
        }

        return daysLeft + " Days Left";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
    public ArrayList<Entrant> lottery(){
        return this.waitingList.selectedList();
    }
    public WaitingList getWaitingList(){
        return this.waitingList;
    }
}
