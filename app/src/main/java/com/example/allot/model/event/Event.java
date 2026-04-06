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
     * Creates a new Event instance.
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
     * Creates a new Event instance.
     *
     * @param eventId the event id
     * @param organizerId the organizer id
     * @param title the title
     * @param limit the limit
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
     * Performs add photo to gallery.
     *
     * @param newPhotoUrl the new photo url
     */
    public void addPhotoToGallery(String newPhotoUrl) {
        if (galleryUrls == null) {
            galleryUrls = new ArrayList<>();
        }
        galleryUrls.add(newPhotoUrl);
    }

    /**
     * Performs lottery.
     */
    public void lottery() {
        getWaitingList().selectedList();
        chosen = new ArrayList<>(waitingList.chosen);
    }

    /**
     * Performs enrolled.
     */
    public void enrolled() {
        enrolled = getWaitingList().enrolled();
    }

    /**
     * Performs notenrolled.
     */
    public void notenrolled() {
        notEnrolled = getWaitingList().notEnrolled();
    }

    /**
     * Returns the waiting list.
     *
     * @return the waiting list
     */
    public WaitingList getWaitingList() {
        if (waitingList == null) {
            waitingList = new WaitingList(limit);
        }
        return waitingList;
    }

    /**
     * Returns the result of status.
     *
     * @return the result of this call
     */
    public HashMap<String, Boolean> status() {
        return getWaitingList().status;
    }

    /**
     * Returns the event id.
     *
     * @return the event id
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Updates the event id.
     *
     * @param eventId the event id
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * Returns the organizer id.
     *
     * @return the organizer id
     */
    public String getOrganizerId() {
        return organizerId;
    }

    /**
     * Updates the organizer id.
     *
     * @param organizerId the organizer id
     */
    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    /**
     * Returns the title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Updates the title.
     *
     * @param title the title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Updates the description.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the location.
     *
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Updates the location.
     *
     * @param location the location
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Returns the category.
     *
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Updates the category.
     *
     * @param category the category
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Returns the capacity.
     *
     * @return the capacity
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Updates the capacity.
     *
     * @param capacity the capacity
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /**
     * Returns the price.
     *
     * @return the price
     */
    public Double getPrice() {
        return price;
    }

    /**
     * Updates the price.
     *
     * @param price the price
     */
    public void setPrice(Double price) {
        this.price = price;
    }

    /**
     * Returns the event date.
     *
     * @return the event date
     */
    public Date getEventDate() {
        return eventDate;
    }

    /**
     * Updates the event date.
     *
     * @param eventDate the event date
     */
    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    /**
     * Returns the draw date.
     *
     * @return the draw date
     */
    public Date getDrawDate() {
        return drawDate;
    }

    /**
     * Updates the draw date.
     *
     * @param drawDate the draw date
     */
    public void setDrawDate(Date drawDate) {
        this.drawDate = drawDate;
    }

    /**
     * Returns the registration open.
     *
     * @return the registration open
     */
    public Date getRegistrationOpen() {
        return registrationOpen;
    }

    /**
     * Updates the registration open.
     *
     * @param registrationOpen the registration open
     */
    public void setRegistrationOpen(Date registrationOpen) {
        this.registrationOpen = registrationOpen;
    }

    /**
     * Returns the registration deadline.
     *
     * @return the registration deadline
     */
    public Date getRegistrationDeadline() {
        return registrationDeadline;
    }

    /**
     * Updates the registration deadline.
     *
     * @param registrationDeadline the registration deadline
     */
    public void setRegistrationDeadline(Date registrationDeadline) {
        this.registrationDeadline = registrationDeadline;
    }

    /**
     * Returns the status.
     *
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Updates the status.
     *
     * @param status the status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the poster url.
     *
     * @return the poster url
     */
    public String getPosterUrl() {
        return posterUrl;
    }

    /**
     * Updates the poster url.
     *
     * @param posterUrl the poster url
     */
    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    /**
     * Returns the visibility.
     *
     * @return the visibility
     */
    public String getVisibility() {
        String normalized = normalizeVisibility(visibility);
        return normalized == null ? VISIBILITY_PUBLIC : normalized;
    }

    /**
     * Updates the visibility.
     *
     * @param visibility the visibility
     */
    public void setVisibility(String visibility) {
        this.visibility = normalizeVisibility(visibility);
    }

    /**
     * Returns whether public.
     *
     * @return whether public
     */
    public boolean isPublic() {
        return VISIBILITY_PUBLIC.equalsIgnoreCase(getVisibility());
    }

    /**
     * Returns whether private.
     *
     * @return whether private
     */
    public boolean isPrivate() {
        return VISIBILITY_PRIVATE.equalsIgnoreCase(getVisibility());
    }

    /**
     * Returns the event latitude.
     *
     * @return the event latitude
     */
    public Double getEventLatitude() {
        return eventLatitude;
    }

    /**
     * Updates the event latitude.
     *
     * @param eventLatitude the event latitude
     */
    public void setEventLatitude(Double eventLatitude) {
        this.eventLatitude = eventLatitude;
    }

    /**
     * Returns the event longitude.
     *
     * @return the event longitude
     */
    public Double getEventLongitude() {
        return eventLongitude;
    }

    /**
     * Updates the event longitude.
     *
     * @param eventLongitude the event longitude
     */
    public void setEventLongitude(Double eventLongitude) {
        this.eventLongitude = eventLongitude;
    }

    /**
     * Returns the limit.
     *
     * @return the limit
     */
    public int getLimit() {
        return limit;
    }

    /**
     * Updates the limit.
     *
     * @param limit the limit
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

    /**
     * Updates the waiting list.
     *
     * @param waitingList the waiting list
     */
    public void setWaitingList(WaitingList waitingList) {
        this.waitingList = waitingList;
    }

    /**
     * Returns the chosen.
     *
     * @return the chosen
     */
    public ArrayList<String> getChosen() {
        if (chosen == null) {
            chosen = new ArrayList<>();
        }
        return chosen;
    }

    /**
     * Updates the chosen.
     *
     * @param chosen the chosen
     */
    public void setChosen(ArrayList<String> chosen) {
        this.chosen = chosen;
    }

    /**
     * Returns the enrolled.
     *
     * @return the enrolled
     */
    public ArrayList<String> getEnrolled() {
        if (enrolled == null) {
            enrolled = new ArrayList<>();
        }
        return enrolled;
    }

    /**
     * Updates the enrolled.
     *
     * @param enrolled the enrolled
     */
    public void setEnrolled(ArrayList<String> enrolled) {
        this.enrolled = enrolled;
    }

    /**
     * Returns the cancelled.
     *
     * @return the cancelled
     */
    public ArrayList<String> getCancelled() {
        if (cancelled == null) {
            cancelled = new ArrayList<>();
        }
        return cancelled;
    }

    /**
     * Updates the cancelled.
     *
     * @param cancelled the cancelled
     */
    public void setCancelled(ArrayList<String> cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * Returns the not enrolled.
     *
     * @return the not enrolled
     */
    public ArrayList<String> getNotEnrolled() {
        if (notEnrolled == null) {
            notEnrolled = new ArrayList<>();
        }
        return notEnrolled;
    }

    /**
     * Updates the not enrolled.
     *
     * @param notEnrolled the not enrolled
     */
    public void setNotEnrolled(ArrayList<String> notEnrolled) {
        this.notEnrolled = notEnrolled;
    }

    /**
     * Returns the co organizers.
     *
     * @return the co organizers
     */
    public ArrayList<String> getCoOrganizers() {
        if (coOrganizers == null) {
            coOrganizers = new ArrayList<>();
        }
        return coOrganizers;
    }

    /**
     * Updates the co organizers.
     *
     * @param coOrganizers the co organizers
     */
    public void setCoOrganizers(ArrayList<String> coOrganizers) {
        this.coOrganizers = coOrganizers;
    }

    /**
     * Returns the co organizer invites.
     *
     * @return the co organizer invites
     */
    public ArrayList<String> getCoOrganizerInvites() {
        if (coOrganizerInvites == null) {
            coOrganizerInvites = new ArrayList<>();
        }
        return coOrganizerInvites;
    }

    /**
     * Updates the co organizer invites.
     *
     * @param coOrganizerInvites the co organizer invites
     */
    public void setCoOrganizerInvites(ArrayList<String> coOrganizerInvites) {
        this.coOrganizerInvites = coOrganizerInvites;
    }

    /**
     * Returns the gallery urls.
     *
     * @return the gallery urls
     */
    public ArrayList<String> getGalleryUrls() {
        if (galleryUrls == null) {
            galleryUrls = new ArrayList<>();
        }
        return galleryUrls;
    }

    /**
     * Updates the gallery urls.
     *
     * @param galleryUrls the gallery urls
     */
    public void setGalleryUrls(ArrayList<String> galleryUrls) {
        this.galleryUrls = galleryUrls;
    }

    /**
     * Returns the invited.
     *
     * @return the invited
     */
    public ArrayList<String> getInvited() {
        if (invited == null) {
            invited = new ArrayList<>();
        }
        return invited;
    }

    /**
     * Updates the invited.
     *
     * @param invited the invited
     */
    public void setInvited(ArrayList<String> invited) {
        this.invited = invited;
    }

    /**
     * Returns whether invited.
     *
     * @param deviceId the device id
     * @return whether invited
     */
    public boolean isInvited(String deviceId) {
        return deviceId != null && getInvited().contains(deviceId);
    }

    /**
     * Returns the comments.
     *
     * @return the comments
     */
    public ArrayList<EventComment> getComments() {
        if (comments == null) {
            comments = new ArrayList<>();
        }
        return comments;
    }

    /**
     * Updates the comments.
     *
     * @param comments the comments
     */
    public void setComments(ArrayList<EventComment> comments) {
        this.comments = comments;
    }

    /**
     * Returns the geoloc.
     *
     * @return the geoloc
     */
    public Boolean getGeoloc() {
        return geoloc;
    }

    /**
     * Updates the geoloc.
     *
     * @param geoloc the geoloc
     */
    public void setGeoloc(Boolean geoloc) {
        this.geoloc = geoloc;
    }

    /**
     * Returns the result of normalize visibility.
     *
     * @param value the value
     * @return the result of this call
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









