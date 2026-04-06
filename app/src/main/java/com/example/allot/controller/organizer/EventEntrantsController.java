package com.example.allot.controller.organizer;

import com.example.allot.R;
import com.example.allot.common.OnCompleteListener;
import com.example.allot.common.TextHelper;
import com.example.allot.controller.event.EventOfferService;
import com.example.allot.controller.shared.UserController;
import com.example.allot.data.EventRepository;
import com.example.allot.data.NotificationRepository;
import com.example.allot.model.event.Event;
import com.example.allot.model.lottery.LotteryEntrantItem;
import com.example.allot.model.notification.NotificationItem;
import com.example.allot.model.organizer.EntrantExportRow;
import com.example.allot.model.profile.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Loads entrant data and actions for the event entrants screen.
 */
public class EventEntrantsController {
    public enum Tab {
        SELECTED,
        CANCELLED,
        NOT_ENROLLED,
        ENROLLED,
        ALL
    }

    private final EventRepository eventRepository;
    private final UserController userController;
    private final NotificationRepository notificationRepository;
    private final EventOfferService eventOfferService;
    private final Map<String, String> userNameCache = new HashMap<>();

    /**
     * Creates a new EventEntrantsController instance.
     *
     * @param context the context
     */
    public EventEntrantsController(android.content.Context context) {
        this(new EventRepository(), new UserController(context), new NotificationRepository(), new EventOfferService());
    }

    /**
     * Creates a new EventEntrantsController instance.
     *
     * @param eventRepository the event repository
     * @param userController the user controller
     * @param notificationRepository the notification repository
     * @param eventOfferService the event offer service
     */
    EventEntrantsController(EventRepository eventRepository,
                            UserController userController,
                            NotificationRepository notificationRepository,
                            EventOfferService eventOfferService) {
        this.eventRepository = eventRepository;
        this.userController = userController;
        this.notificationRepository = notificationRepository;
        this.eventOfferService = eventOfferService;
    }

    /**
     * Performs load event.
     *
     * @param eventId the event id
     * @param listener the listener
     */
    public void loadEvent(String eventId, OnCompleteListener<Event> listener) {
        if (isBlank(eventId)) {
            listener.onComplete(null, false);
            return;
        }

        eventRepository.getEventById(eventId, (event, success) -> {
            if (!success) {
                listener.onComplete(null, false);
                return;
            }

            listener.onComplete(event, event != null);
        });
    }

    /**
     * Performs load entrant items.
     *
     * @param event the event
     * @param selectedTab the selected tab
     * @param listener the listener
     */
    public void loadEntrantItems(Event event, Tab selectedTab, OnCompleteListener<List<LotteryEntrantItem>> listener) {
        if (event == null) {
            listener.onComplete(new ArrayList<>(), false);
            return;
        }

        buildTabItems(event, selectedTab, items -> listener.onComplete(items, true));
    }

    /**
     * Performs load enrolled export rows.
     *
     * @param event the event
     * @param listener the listener
     */
    public void loadEnrolledExportRows(Event event, OnCompleteListener<List<EntrantExportRow>> listener) {
        if (event == null) {
            listener.onComplete(new ArrayList<>(), false);
            return;
        }

        List<String> entrantIds = getEnrolledEntrants(event);
        if (entrantIds.isEmpty()) {
            listener.onComplete(new ArrayList<>(), true);
            return;
        }

        loadExportRows(entrantIds, 0, new ArrayList<>(), listener);
    }

    /**
     * Performs cancel selected entrant.
     *
     * @param eventId the event id
     * @param entrantId the entrant id
     * @param listener the listener
     */
    public void cancelSelectedEntrant(String eventId, String entrantId, OnCompleteListener<Boolean> listener) {
        if (isBlank(eventId) || isBlank(entrantId)) {
            listener.onComplete(false, false);
            return;
        }

        eventRepository.getEventById(eventId, (event, success) -> {
            if (!success || event == null) {
                listener.onComplete(false, false);
                return;
            }

            eventRepository.cancelSelectedEntrant(eventId, entrantId, (result, cancelSuccess) -> {
                if (cancelSuccess && result != null && result) {
                    sendCancellationNotification(entrantId, eventId, event.getTitle());
                    listener.onComplete(true, true);
                } else {
                    listener.onComplete(false, false);
                }
            });
        });
    }

    /**
     * Performs draw replacement entrant.
     *
     * @param eventId the event id
     * @param listener the listener
     */
    public void drawReplacementEntrant(String eventId, OnCompleteListener<Boolean> listener) {
        if (isBlank(eventId)) {
            listener.onComplete(false, false);
            return;
        }

        FirebaseFirestore.getInstance().runTransaction((Transaction.Function<String>) transaction -> {
            DocumentSnapshot snapshot = transaction.get(FirebaseFirestore.getInstance().collection("events").document(eventId));
            if (!snapshot.exists()) return null;

            Event event = snapshot.toObject(Event.class);
            if (event == null) return null;

            Event updatedEvent = eventOfferService.buildReplacementDrawState(event);
            if (updatedEvent == null) return null;

            transaction.update(snapshot.getReference(),
                    "chosen", updatedEvent.getChosen(),
                    "waitingList.chosen", updatedEvent.getWaitingList().chosen,
                    "waitingList.status", updatedEvent.getWaitingList().status);

            // Return the ID of the newly selected entrant to notify them
            List<String> currentChosen = updatedEvent.getChosen();
            if (currentChosen.isEmpty()) return null;
            return currentChosen.get(currentChosen.size() - 1);
        }).addOnSuccessListener(replacementId -> {
            if (replacementId != null) {
                loadEvent(eventId, (event, success) -> {
                    if (success && event != null) {
                        sendSelectionNotification(replacementId, eventId, event.getTitle());
                    }
                });
                listener.onComplete(true, true);
            } else {
                listener.onComplete(false, true);
            }
        }).addOnFailureListener(e -> listener.onComplete(false, false));
    }

    /**
     * Performs send cancellation notification.
     *
     * @param userId the user id
     * @param eventId the event id
     * @param eventTitle the event title
     */
    private void sendCancellationNotification(String userId, String eventId, String eventTitle) {
        String title = "Selection Cancelled";
        String body = "Your selection for " + eventTitle + " has been cancelled by the organizer.";
        
        // Saving to Firestore triggers the snapshot listener on the user's device
        notificationRepository.saveNotification(new NotificationItem(userId, eventId, title, body), (r, s) -> {});
    }

    /**
     * Performs send selection notification.
     *
     * @param userId the user id
     * @param eventId the event id
     * @param eventTitle the event title
     */
    private void sendSelectionNotification(String userId, String eventId, String eventTitle) {
        String title = "You've been selected!";
        String body = "Congratulations! You have been selected for " + eventTitle + ". Please accept the invitation.";
        
        // Saving to Firestore triggers the snapshot listener on the user's device
        notificationRepository.saveNotification(new NotificationItem(userId, eventId, title, body), (r, s) -> {});
    }

    /**
     * Performs build tab items.
     *
     * @param event the event
     * @param selectedTab the selected tab
     * @param consumer the consumer
     */
    private void buildTabItems(Event event, Tab selectedTab, java.util.function.Consumer<List<LotteryEntrantItem>> consumer) {
        List<String> entrantIds = getEntrantIds(event, selectedTab);
        if (entrantIds.isEmpty()) {
            consumer.accept(new ArrayList<>());
            return;
        }
        loadEntrantItems(entrantIds, 0, new ArrayList<>(), getSubtitleRes(selectedTab), consumer);
    }

    /**
     * Returns the entrant ids.
     *
     * @param event the event
     * @param selectedTab the selected tab
     * @return the entrant ids
     */
    private List<String> getEntrantIds(Event event, Tab selectedTab) {
        switch (selectedTab) {
            case CANCELLED:
                return getCancelledEntrants(event);
            case NOT_ENROLLED:
                return getNotEnrolledEntrants(event);
            case ENROLLED:
                return getEnrolledEntrants(event);
            case ALL:
                return getAllEntrants(event);
            case SELECTED:
            default:
                return getSelectedEntrants(event);
        }
    }

    /**
     * Returns the subtitle res.
     *
     * @param selectedTab the selected tab
     * @return the subtitle res
     */
    private int getSubtitleRes(Tab selectedTab) {
        switch (selectedTab) {
            case CANCELLED:
                return R.string.manage_entrants_cancelled_subtitle;
            case NOT_ENROLLED:
                return R.string.manage_entrants_not_enrolled_subtitle;
            case ENROLLED:
                return R.string.manage_entrants_enrolled_subtitle;
            case ALL:
                return R.string.manage_entrants_all_subtitle;
            case SELECTED:
            default:
                return R.string.manage_entrants_selected_subtitle;
        }
    }

    /**
     * Returns the selected entrants.
     *
     * @param event the event
     * @return the selected entrants
     */
    private List<String> getSelectedEntrants(Event event) {
        if (event == null) {
            return new ArrayList<>();
        }

        if (!event.getChosen().isEmpty()) {
            return new ArrayList<>(event.getChosen());
        }
        if (event.getWaitingList() != null && event.getWaitingList().chosen != null) {
            return new ArrayList<>(event.getWaitingList().chosen);
        }
        return new ArrayList<>();
    }

    /**
     * Returns the cancelled entrants.
     *
     * @param event the event
     * @return the cancelled entrants
     */
    private List<String> getCancelledEntrants(Event event) {
        if (event != null) {
            return new ArrayList<>(event.getCancelled());
        }
        return new ArrayList<>();
    }

    /**
     * Returns the not enrolled entrants.
     *
     * @param event the event
     * @return the not enrolled entrants
     */
    private List<String> getNotEnrolledEntrants(Event event) {
        if (event != null) {
            return new ArrayList<>(event.getNotEnrolled());
        }
        return new ArrayList<>();
    }

    /**
     * Returns the enrolled entrants.
     *
     * @param event the event
     * @return the enrolled entrants
     */
    private List<String> getEnrolledEntrants(Event event) {
        if (event == null) {
            return new ArrayList<>();
        }

        if (!event.getEnrolled().isEmpty()) {
            return new ArrayList<>(event.getEnrolled());
        }

        ArrayList<String> enrolledEntrants = new ArrayList<>();
        if (event.getWaitingList() != null && event.getWaitingList().chosen != null && event.getWaitingList().status != null) {
            for (String entrantId : event.getWaitingList().chosen) {
                if (Boolean.TRUE.equals(event.getWaitingList().status.get(entrantId))) {
                    enrolledEntrants.add(entrantId);
                }
            }
        }
        return enrolledEntrants;
    }

    /**
     * Returns the all entrants.
     *
     * @param event the event
     * @return the all entrants
     */
    private List<String> getAllEntrants(Event event) {
        if (event != null && event.getWaitingList() != null && event.getWaitingList().list != null) {
            return new ArrayList<>(event.getWaitingList().list);
        }
        return new ArrayList<>();
    }

    /**
     * Performs load entrant items.
     *
     * @param entrantIds the entrant ids
     * @param index the index
     * @param items the items
     * @param subtitleRes the subtitle res
     * @param consumer the consumer
     */
    private void loadEntrantItems(List<String> entrantIds,
                                  int index,
                                  List<LotteryEntrantItem> items,
                                  int subtitleRes,
                                  java.util.function.Consumer<List<LotteryEntrantItem>> consumer) {
        if (index >= entrantIds.size()) {
            consumer.accept(items);
            return;
        }

        String entrantId = entrantIds.get(index);
        String cachedName = userNameCache.get(entrantId);
        if (!isBlank(cachedName)) {
            items.add(new LotteryEntrantItem(entrantId, cachedName, subtitleRes));
            loadEntrantItems(entrantIds, index + 1, items, subtitleRes, consumer);
            return;
        }

        userController.getUserByDeviceId(entrantId, (User user, boolean success) -> {
            String displayName = entrantId;
            if (success && user != null && !isBlank(user.getName())) {
                displayName = user.getName();
                userNameCache.put(entrantId, displayName);
            }
            items.add(new LotteryEntrantItem(entrantId, displayName, subtitleRes));
            loadEntrantItems(entrantIds, index + 1, items, subtitleRes, consumer);
        });
    }

    /**
     * Performs load export rows.
     *
     * @param entrantIds the entrant ids
     * @param index the index
     * @param rows the rows
     * @param listener the listener
     */
    private void loadExportRows(List<String> entrantIds,
                                int index,
                                List<EntrantExportRow> rows,
                                OnCompleteListener<List<EntrantExportRow>> listener) {
        if (index >= entrantIds.size()) {
            listener.onComplete(rows, true);
            return;
        }

        String entrantId = entrantIds.get(index);
        userController.getUserByDeviceId(entrantId, (User user, boolean success) -> {
            rows.add(buildExportRow(entrantId, success ? user : null));
            loadExportRows(entrantIds, index + 1, rows, listener);
        });
    }

    /**
     * Returns the result of build export row.
     *
     * @param entrantId the entrant id
     * @param user the user
     * @return the result of this call
     */
    private EntrantExportRow buildExportRow(String entrantId, User user) {
        String fallbackName = isBlank(entrantId) ? "" : entrantId;
        String name = fallbackName;
        String email = "";
        String phone = "";

        if (user != null) {
            if (!isBlank(user.getName())) {
                name = user.getName();
            }
            if (!isBlank(user.getEmail())) {
                email = user.getEmail();
            }
            if (!isBlank(user.getPhone())) {
                phone = user.getPhone();
            }
        }

        return new EntrantExportRow(name, email, phone);
    }

    /**
     * Returns whether blank.
     *
     * @param value the value
     * @return whether blank
     */
    private boolean isBlank(String value) {
        return TextHelper.isBlank(value);
    }
}
