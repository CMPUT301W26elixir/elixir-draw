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

    /**
     * Returns whether g.et Event Id
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Updates event id.
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * Returns whether g.et Organizer Id
     */
    public String getOrganizerId() {
        return organizerId;
    }

    /**
     * Updates organizer id.
     */
    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    /**
     * Returns whether g.et Title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Updates title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns whether g.et Description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Updates description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns whether g.et Location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Updates location.
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Returns whether g.et Category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Updates category.
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Returns whether g.et Capacity
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Updates capacity.
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /**
     * Returns whether g.et Price
     */
    public Double getPrice() {
        return price;
    }

    /**
     * Updates price.
     */
    public void setPrice(Double price) {
        this.price = price;
    }

    /**
     * Returns whether g.et Event Date
     */
    public Date getEventDate() {
        return eventDate;
    }

    /**
     * Updates event date.
     */
    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    /**
     * Returns whether g.et Draw Date
     */
    public Date getDrawDate() {
        return drawDate;
    }

    /**
     * Updates draw date.
     */
    public void setDrawDate(Date drawDate) {
        this.drawDate = drawDate;
    }

    /**
     * Returns whether g.et Registration Open
     */
    public Date getRegistrationOpen() {
        return registrationOpen;
    }

    /**
     * Updates registration open.
     */
    public void setRegistrationOpen(Date registrationOpen) {
        this.registrationOpen = registrationOpen;
    }

    /**
     * Returns whether g.et Registration Deadline
     */
    public Date getRegistrationDeadline() {
        return registrationDeadline;
    }

    /**
     * Updates registration deadline.
     */
    public void setRegistrationDeadline(Date registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
    }

    /**
     * Returns whether g.et Status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Updates status.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns whether g.et Poster Url
     */
    public String getPosterUrl() {
        return posterUrl;
    }

    /**
     * Updates poster url.
     */
    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    /**
     * Returns whether g.et Visibility
     */
    public String getVisibility() {
        String normalized = normalizeVisibility(visibility);
        return normalized == null ? VISIBILITY_PUBLIC : normalized;
    }

    /**
     * Updates visibility.
     */
    public void setVisibility(String visibility) {
        this.visibility = normalizeVisibility(visibility);
    }

    /**
     * Returns whether i.s Public
     */
    public boolean isPublic() {
        return VISIBILITY_PUBLIC.equalsIgnoreCase(getVisibility());
    }

    /**
     * Returns whether i.s Private
     */
    public boolean isPrivate() {
        return VISIBILITY_PRIVATE.equalsIgnoreCase(getVisibility());
    }

    /**
     * Returns whether g.et Event Latitude
     */
    public Double getEventLatitude() {
        return eventLatitude;
    }

    /**
     * Updates event latitude.
     */
    public void setEventLatitude(Double eventLatitude) {
        this.eventLatitude = eventLatitude;
    }

    /**
     * Returns whether g.et Event Longitude
     */
    public Double getEventLongitude() {
        return eventLongitude;
    }

    /**
     * Updates event longitude.
     */
    public void setEventLongitude(Double eventLongitude) {
        this.eventLongitude = eventLongitude;
    }

    /**
     * Returns whether g.et Limit
     */
    public int getLimit() {
        return limit;
    }

    /**
     * Updates limit.
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

    /**
     * Updates waiting list.
     */
    public void setWaitingList(WaitingList waitingList) {
        this.waitingList = waitingList;
    }

    /**
     * Returns whether g.et Chosen
     */
    public ArrayList<String> getChosen() {
        if (chosen == null) {
            chosen = new ArrayList<>();
        }
        return chosen;
    }

    /**
     * Updates chosen.
     */
    public void setChosen(ArrayList<String> chosen) {
        this.chosen = chosen;
    }

    /**
     * Returns whether g.et Enrolled
     */
    public ArrayList<String> getEnrolled() {
        if (enrolled == null) {
            enrolled = new ArrayList<>();
        }
        return enrolled;
    }

    /**
     * Updates enrolled.
     */
    public void setEnrolled(ArrayList<String> enrolled) {
        this.enrolled = enrolled;
    }

    /**
     * Returns whether g.et Cancelled
     */
    public ArrayList<String> getCancelled() {
        if (cancelled == null) {
            cancelled = new ArrayList<>();
        }
        return cancelled;
    }

    /**
     * Updates cancelled.
     */
    public void setCancelled(ArrayList<String> cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * Returns whether g.et Not Enrolled
     */
    public ArrayList<String> getNotEnrolled() {
        if (notEnrolled == null) {
            notEnrolled = new ArrayList<>();
        }
        return notEnrolled;
    }

    /**
     * Updates not enrolled.
     */
    public void setNotEnrolled(ArrayList<String> notEnrolled) {
        this.notEnrolled = notEnrolled;
    }

    /**
     * Returns whether g.et Co Organizers
     */
    public ArrayList<String> getCoOrganizers() {
        if (coOrganizers == null) {
            coOrganizers = new ArrayList<>();
        }
        return coOrganizers;
    }

    /**
     * Updates co organizers.
     */
    public void setCoOrganizers(ArrayList<String> coOrganizers) {
        this.coOrganizers = coOrganizers;
    }

    /**
     * Returns whether g.et Co Organizer Invites
     */
    public ArrayList<String> getCoOrganizerInvites() {
        if (coOrganizerInvites == null) {
            coOrganizerInvites = new ArrayList<>();
        }
        return coOrganizerInvites;
    }

    /**
     * Updates co organizer invites.
     */
    public void setCoOrganizerInvites(ArrayList<String> coOrganizerInvites) {
        this.coOrganizerInvites = coOrganizerInvites;
    }

    /**
     * Returns whether g.et Gallery Urls
     */
    public ArrayList<String> getGalleryUrls() {
        if (galleryUrls == null) {
            galleryUrls = new ArrayList<>();
        }
        return galleryUrls;
    }

    /**
     * Updates gallery urls.
     */
    public void setGalleryUrls(ArrayList<String> galleryUrls) {
        this.galleryUrls = galleryUrls;
    }

    /**
     * Returns whether g.et Invited
     */
    public ArrayList<String> getInvited() {
        if (invited == null) {
            invited = new ArrayList<>();
        }
        return invited;
    }

    /**
     * Updates invited.
     */
    public void setInvited(ArrayList<String> invited) {
        this.invited = invited;
    }

    /**
     * Returns whether i.s Invited
     */
    public boolean isInvited(String deviceId) {
        return deviceId != null && getInvited().contains(deviceId);
    }

    /**
     * Returns whether g.et Comments
     */
    public ArrayList<EventComment> getComments() {
        if (comments == null) {
            comments = new ArrayList<>();
        }
        return comments;
    }

    /**
     * Updates comments.
     */
    public void setComments(ArrayList<EventComment> comments) {
        this.comments = comments;
    }

    /**
     * Returns whether g.et Geoloc
     */
    public Boolean getGeoloc() {
        return geoloc;
    }

    /**
     * Updates geoloc.
     */
    public void setGeoloc(Boolean geoloc) {
        this.geoloc = geoloc;
    }

    /**
     * Handles normalize Visibility.
     */
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









