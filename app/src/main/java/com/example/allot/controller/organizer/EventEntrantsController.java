package com.example.allot.controller.organizer;

import com.example.allot.R;
import com.example.allot.common.OnCompleteListener;
import com.example.allot.common.TextHelper;
import com.example.allot.controller.shared.UserController;
import com.example.allot.data.EventRepository;
import com.example.allot.model.event.Event;
import com.example.allot.model.lottery.LotteryEntrantItem;
import com.example.allot.model.organizer.EntrantExportRow;
import com.example.allot.model.profile.User;
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
    private final Map<String, String> userNameCache = new HashMap<>();

    public EventEntrantsController(android.content.Context context) {
        this(new EventRepository(), new UserController(context));
    }

    EventEntrantsController(EventRepository eventRepository,
                            UserController userController) {
        this.eventRepository = eventRepository;
        this.userController = userController;
    }

    /**
     * Loads the event used by the entrants screen.
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
     * Loads the entrant items shown for one tab.
     */
    public void loadEntrantItems(Event event, Tab selectedTab, OnCompleteListener<List<LotteryEntrantItem>> listener) {
        if (event == null) {
            listener.onComplete(new ArrayList<>(), false);
            return;
        }

        buildTabItems(event, selectedTab, items -> listener.onComplete(items, true));
    }

    /**
     * Loads export-ready rows for enrolled entrants only.
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
     * Cancels a selected entrant and moves them to the not selected list.
     */
    public void cancelSelectedEntrant(String eventId, String entrantId, OnCompleteListener<Boolean> listener) {
        if (isBlank(eventId) || isBlank(entrantId)) {
            listener.onComplete(false, false);
            return;
        }

        eventRepository.cancelSelectedEntrant(eventId, entrantId, listener);
    }

    private void buildTabItems(Event event, Tab selectedTab, java.util.function.Consumer<List<LotteryEntrantItem>> consumer) {
        List<String> entrantIds = getEntrantIds(event, selectedTab);
        if (entrantIds.isEmpty()) {
            consumer.accept(new ArrayList<>());
            return;
        }
        loadEntrantItems(entrantIds, 0, new ArrayList<>(), getSubtitleRes(selectedTab), consumer);
    }

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

    private List<String> getCancelledEntrants(Event event) {
        if (event != null) {
            return new ArrayList<>(event.getCancelled());
        }
        return new ArrayList<>();
    }

    private List<String> getNotEnrolledEntrants(Event event) {
        if (event != null) {
            return new ArrayList<>(event.getNotEnrolled());
        }
        return new ArrayList<>();
    }

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

    private List<String> getAllEntrants(Event event) {
        if (event != null && event.getWaitingList() != null && event.getWaitingList().list != null) {
            return new ArrayList<>(event.getWaitingList().list);
        }
        return new ArrayList<>();
    }

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

    private boolean isBlank(String value) {
        return TextHelper.isBlank(value);
    }
}









