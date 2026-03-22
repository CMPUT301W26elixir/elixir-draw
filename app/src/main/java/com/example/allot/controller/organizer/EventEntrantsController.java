package com.example.allot.controller.organizer;

import com.example.allot.R;
import com.example.allot.common.OnCompleteListener;
import com.example.allot.common.TextHelper;
import com.example.allot.controller.shared.UserController;
import com.example.allot.data.EventRepository;
import com.example.allot.model.event.Event;
import com.example.allot.model.lottery.LotteryEntrantItem;
import com.example.allot.model.organizer.EntrantsExportResult;
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
     * Returns true when the event currently has enrolled entrants.
     */
    public boolean hasEnrolledEntrants(Event event) {
        return !getEnrolledEntrants(event).isEmpty();
    }

    /**
     * Prepares the export file name and CSV content for enrolled entrants.
     */
    public void buildExportData(Event event, OnCompleteListener<EntrantsExportResult> listener) {
        if (event == null) {
            listener.onComplete(new EntrantsExportResult(false, R.string.manage_entrants_export_failure, null, null), false);
            return;
        }

        List<String> enrolledEntrantIds = getEnrolledEntrants(event);
        if (enrolledEntrantIds.isEmpty()) {
            listener.onComplete(new EntrantsExportResult(false, R.string.manage_entrants_export_empty, null, null), false);
            return;
        }

        loadExportNames(enrolledEntrantIds, 0, new ArrayList<>(), event, listener);
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

    private void loadExportNames(List<String> entrantIds,
                                 int index,
                                 List<String> exportedNames,
                                 Event event,
                                 OnCompleteListener<EntrantsExportResult> listener) {
        if (index >= entrantIds.size()) {
            listener.onComplete(
                    new EntrantsExportResult(true, R.string.manage_entrants_export_success, buildExportFileName(event), buildCsvContent(exportedNames)),
                    true
            );
            return;
        }

        String entrantId = entrantIds.get(index);
        userController.getUserByDeviceId(entrantId, (User user, boolean success) -> {
            exportedNames.add(buildExportName(entrantId, success ? user : null));
            loadExportNames(entrantIds, index + 1, exportedNames, event, listener);
        });
    }

    private String buildExportName(String entrantId, User user) {
        if (user == null) {
            return safeCsvValue(entrantId, entrantId);
        }
        return safeCsvValue(user.getName(), entrantId);
    }

    private String buildCsvContent(List<String> exportedNames) {
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("Name\n");
        for (String exportedName : exportedNames) {
            csvBuilder.append(escapeCsv(exportedName)).append('\n');
        }
        return csvBuilder.toString();
    }

    private String buildExportFileName(Event event) {
        String eventTitle = event == null || isBlank(event.getTitle()) ? "event" : event.getTitle();
        String safeTitle = eventTitle.replaceAll("[^a-zA-Z0-9_-]+", "_").replaceAll("_+", "_");
        if (safeTitle.startsWith("_")) {
            safeTitle = safeTitle.substring(1);
        }
        if (safeTitle.endsWith("_")) {
            safeTitle = safeTitle.substring(0, safeTitle.length() - 1);
        }
        if (isBlank(safeTitle)) {
            safeTitle = "event";
        }
        return safeTitle + "_enrolled_entrants.csv";
    }

    private String escapeCsv(String value) {
        String safeValue = value == null ? "" : value;
        String escapedValue = safeValue.replace("\"", "\"\"");
        return "\"" + escapedValue + "\"";
    }

    private String safeCsvValue(String value, String fallback) {
        if (!isBlank(value)) {
            return value.trim();
        }
        return fallback == null ? "" : fallback;
    }

    private boolean isBlank(String value) {
        return TextHelper.isBlank(value);
    }
}









