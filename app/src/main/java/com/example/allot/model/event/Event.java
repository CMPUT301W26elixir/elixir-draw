package com.example.allot.model.event;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
/**
 * Holds an event and all of the saved data tied to it.
 */
public class Event {
    public static final String VISIBILITY_PUBLIC = "public";
    public static final String VISIBILITY_PRIVATE = "private";

    private String eventId;
    private String organizerId;
    private String title;
    private String description;
    private String location;
    private String category;
    private int capacity;
    private Double price;
    private Date eventDate;
    private Date drawDate;
    private Date registrationOpen;
    private Date registrationDeadline;
    private String status;
    private String posterUrl;
    private String visibility;
    private Double eventLatitude;
    private Double eventLongitude;

    private int limit = -1;
    private WaitingList waitingList;
    private ArrayList<String> chosen;
    private ArrayList<String> enrolled;
    private ArrayList<String> cancelled;
    private ArrayList<String> notEnrolled;
    private ArrayList<String> coOrganizers;
    private ArrayList<String> coOrganizerInvites;

    private ArrayList<String> galleryUrls;
    private ArrayList<String> invited;
    private ArrayList<EventComment> comments;

    private Boolean geoloc;

    /**
     * Creates an empty Event object for Firestore document deserialization.
     */
    public Event() {
        this.galleryUrls = new ArrayList<>();
        this.chosen = new ArrayList<>();
        this.enrolled = new ArrayList<>();
        this.cancelled = new ArrayList<>();
        this.notEnrolled = new ArrayList<>();
        this.invited = new ArrayList<>();
        this.visibility = VISIBILITY_PUBLIC;
        this.coOrganizers = new ArrayList<>();
        this.coOrganizerInvites = new ArrayList<>();
        this.comments = new ArrayList<>();
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
        this.chosen = new ArrayList<>();
        this.enrolled = new ArrayList<>();
        this.cancelled = new ArrayList<>();
        this.notEnrolled = new ArrayList<>();
        this.invited = new ArrayList<>();
        this.coOrganizers = new ArrayList<>();
        this.coOrganizerInvites = new ArrayList<>();
        this.comments = new ArrayList<>();
        this.limit = limit;
        this.waitingList = new WaitingList(limit);
        this.visibility = VISIBILITY_PUBLIC;
    }

    /**
     * Adds a photo URL to the event gallery.
     *
     * @param newPhotoUrl the URL of the photo to add
     */
    public void addPhotoToGallery(String newPhotoUrl) {
        if (galleryUrls == null) {
            galleryUrls = new ArrayList<>();
        }
        galleryUrls.add(newPhotoUrl);
    }

    /**
     * Runs the lottery selection for the event and stores the chosen entrants.
     */
    public void lottery() {
        getWaitingList().selectedList();
        chosen = new ArrayList<>(waitingList.chosen);
    }

    /**
     * Updates the enrolled entrant list from the waiting list status.
     */
    public void enrolled() {
        enrolled = getWaitingList().enrolled();
    }

    /**
     * Updates the not-enrolled entrant list from the waiting list status.
     */
    public void notenrolled() {
        notEnrolled = getWaitingList().notEnrolled();
    }

    /**
     * Returns the waiting list for the event, creating one if it does not exist.
     *
     * @return the waiting list for the event
     */
    public WaitingList getWaitingList() {
        if (waitingList == null) {
            waitingList = new WaitingList(limit);
        }
        return waitingList;
    }

    /**
     * Returns the enrollment status map for selected entrants.
     *
     * @return a map of entrant IDs to enrollment status values
     */
    public HashMap<String, Boolean> status() {
        return getWaitingList().status;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public Date getDrawDate() {
        return drawDate;
    }

    public void setDrawDate(Date drawDate) {
        this.drawDate = drawDate;
    }

    public Date getRegistrationOpen() {
        return registrationOpen;
    }

    public void setRegistrationOpen(Date registrationOpen) {
        this.registrationOpen = registrationOpen;
    }

    public Date getRegistrationDeadline() {
        return registrationDeadline;
    }

    public void setRegistrationDeadline(Date registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getVisibility() {
        String normalized = normalizeVisibility(visibility);
        return normalized == null ? VISIBILITY_PUBLIC : normalized;
    }

    public void setVisibility(String visibility) {
        this.visibility = normalizeVisibility(visibility);
    }

    public boolean isPublic() {
        return VISIBILITY_PUBLIC.equalsIgnoreCase(getVisibility());
    }

    public boolean isPrivate() {
        return VISIBILITY_PRIVATE.equalsIgnoreCase(getVisibility());
    }

    public Double getEventLatitude() {
        return eventLatitude;
    }

    public void setEventLatitude(Double eventLatitude) {
        this.eventLatitude = eventLatitude;
    }

    public Double getEventLongitude() {
        return eventLongitude;
    }

    public void setEventLongitude(Double eventLongitude) {
        this.eventLongitude = eventLongitude;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void setWaitingList(WaitingList waitingList) {
        this.waitingList = waitingList;
    }

    public ArrayList<String> getChosen() {
        if (chosen == null) {
            chosen = new ArrayList<>();
        }
        return chosen;
    }

    public void setChosen(ArrayList<String> chosen) {
        this.chosen = chosen;
    }

    public ArrayList<String> getEnrolled() {
        if (enrolled == null) {
            enrolled = new ArrayList<>();
        }
        return enrolled;
    }

    public void setEnrolled(ArrayList<String> enrolled) {
        this.enrolled = enrolled;
    }

    public ArrayList<String> getCancelled() {
        if (cancelled == null) {
            cancelled = new ArrayList<>();
        }
        return cancelled;
    }

    public void setCancelled(ArrayList<String> cancelled) {
        this.cancelled = cancelled;
    }

    public ArrayList<String> getNotEnrolled() {
        if (notEnrolled == null) {
            notEnrolled = new ArrayList<>();
        }
        return notEnrolled;
    }

    public void setNotEnrolled(ArrayList<String> notEnrolled) {
        this.notEnrolled = notEnrolled;
    }

    public ArrayList<String> getCoOrganizers() {
        if (coOrganizers == null) {
            coOrganizers = new ArrayList<>();
        }
        return coOrganizers;
    }

    public void setCoOrganizers(ArrayList<String> coOrganizers) {
        this.coOrganizers = coOrganizers;
    }

    public ArrayList<String> getCoOrganizerInvites() {
        if (coOrganizerInvites == null) {
            coOrganizerInvites = new ArrayList<>();
        }
        return coOrganizerInvites;
    }

    public void setCoOrganizerInvites(ArrayList<String> coOrganizerInvites) {
        this.coOrganizerInvites = coOrganizerInvites;
    }

    public ArrayList<String> getGalleryUrls() {
        if (galleryUrls == null) {
            galleryUrls = new ArrayList<>();
        }
        return galleryUrls;
    }

    public void setGalleryUrls(ArrayList<String> galleryUrls) {
        this.galleryUrls = galleryUrls;
    }

    public ArrayList<String> getInvited() {
        if (invited == null) {
            invited = new ArrayList<>();
        }
        return invited;
    }

    public void setInvited(ArrayList<String> invited) {
        this.invited = invited;
    }

    public boolean isInvited(String deviceId) {
        return deviceId != null && getInvited().contains(deviceId);
    }

    public ArrayList<EventComment> getComments() {
        if (comments == null) {
            comments = new ArrayList<>();
        }
        return comments;
    }

    public void setComments(ArrayList<EventComment> comments) {
        this.comments = comments;
    }

    public Boolean getGeoloc() {
        return geoloc;
    }

    public void setGeoloc(Boolean geoloc) {
        this.geoloc = geoloc;
    }

    private String normalizeVisibility(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim().toLowerCase();
        if (VISIBILITY_PUBLIC.equals(trimmed)) {
            return VISIBILITY_PUBLIC;
        }
        if (VISIBILITY_PRIVATE.equals(trimmed)) {
            return VISIBILITY_PRIVATE;
        }
        return VISIBILITY_PUBLIC;
    }
}









