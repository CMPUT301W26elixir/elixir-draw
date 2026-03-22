package com.example.allot.controller.lottery;

import com.example.allot.R;
import com.example.allot.common.AppResult;
import com.example.allot.common.OnCompleteListener;
import com.example.allot.controller.shared.UserController;
import com.example.allot.data.EventRepository;
import com.example.allot.model.event.Event;
import com.example.allot.model.lottery.LotteryEntrantItem;
import com.example.allot.model.lottery.RunLotteryData;
import com.example.allot.model.profile.User;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
public class LotteryController {
    private final EventRepository eventRepository;
    private final UserController userController;
    private final LotteryDrawService lotteryDrawService;
    private final LotteryInputValidator lotteryInputValidator;
    private final SimpleDateFormat drawDateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());

    public LotteryController(android.content.Context context) {
        this(new EventRepository(), new UserController(context), new LotteryDrawService(), new LotteryInputValidator());
    }

    LotteryController(EventRepository eventRepository,
                      UserController userController,
                      LotteryDrawService lotteryDrawService,
                      LotteryInputValidator lotteryInputValidator) {
        this.eventRepository = eventRepository;
        this.userController = userController;
        this.lotteryDrawService = lotteryDrawService;
        this.lotteryInputValidator = lotteryInputValidator;
        drawDateFormat.setLenient(false);
    }

    /**
     * Loads the lottery screen state.
     */
    public void loadLotteryState(String eventId, OnCompleteListener<RunLotteryData> listener) {
        if (isBlank(eventId)) {
            listener.onComplete(new RunLotteryData(
                    RunLotteryData.Status.ERROR, "", "", null,
                    R.string.manage_lottery_load_failure, false, false, null), false);
            return;
        }

        eventRepository.getEventById(eventId, (event, success) -> {
            if (!success) {
                listener.onComplete(new RunLotteryData(
                        RunLotteryData.Status.ERROR, "", "", null,
                        R.string.manage_lottery_load_failure, false, false, null), false);
                return;
            }

            if (event == null) {
                listener.onComplete(new RunLotteryData(
                        RunLotteryData.Status.ERROR, "", "", null,
                        R.string.manage_lottery_not_found, false, false, null), false);
                return;
            }

            if (lotteryDrawService.hasDrawResults(event)) {
                listener.onComplete(new RunLotteryData(
                        RunLotteryData.Status.CONTENT, "", "", null,
                        0, false, true, event), true);
                return;
            }

            buildEntrantItems(event.getWaitingList() == null ? null : event.getWaitingList().list, items ->
                    listener.onComplete(buildContentState(event, items), true));
        });
    }

    /**
     * Starts the lottery draw with the current form values.
     */
    public void startLotteryDraw(String eventId,
                                 Event currentEvent,
                                 String drawDateValue,
                                 String attendeesValue,
                                 OnCompleteListener<AppResult<Event>> listener) {
        if (currentEvent == null) {
            listener.onComplete(AppResult.failure(R.string.manage_lottery_load_failure), false);
            return;
        }

        if (isBlank(drawDateValue) || isBlank(attendeesValue)) {
            listener.onComplete(AppResult.failure(R.string.manage_lottery_validation_required), false);
            return;
        }

        if (currentEvent.getWaitingList() == null) {
            currentEvent.getWaitingList();
        }
        if (currentEvent.getWaitingList() == null || currentEvent.getWaitingList().list == null || currentEvent.getWaitingList().list.isEmpty()) {
            listener.onComplete(AppResult.failure(R.string.manage_lottery_empty), false);
            return;
        }

        if (lotteryInputValidator.parseDrawDate(drawDateValue) == null) {
            listener.onComplete(AppResult.failure(R.string.manage_lottery_validation_date), false);
            return;
        }

        Integer attendees = lotteryInputValidator.parsePositiveInt(attendeesValue);
        if (attendees == null || attendees <= 0) {
            listener.onComplete(AppResult.failure(R.string.manage_lottery_validation_attendees), false);
            return;
        }

        eventRepository.getEventById(eventId, (loadedEvent, success) -> {
            if (!success || loadedEvent == null) {
                listener.onComplete(AppResult.failure(R.string.manage_lottery_draw_failure), false);
                return;
            }

            Event updatedEvent = lotteryDrawService.buildDrawResult(loadedEvent, attendees);
            if (updatedEvent == null) {
                listener.onComplete(AppResult.failure(R.string.manage_lottery_draw_failure), false);
                return;
            }

            Date drawDate = lotteryInputValidator.parseDrawDate(drawDateValue);
            updatedEvent.setDrawDate(drawDate);

            Map<String, Object> updates = new HashMap<>();
            updates.put("drawDate", drawDate);
            updates.put("capacity", updatedEvent.getCapacity());
            updates.put("limit", updatedEvent.getLimit());
            updates.put("chosen", updatedEvent.getChosen());
            updates.put("enrolled", updatedEvent.getEnrolled());
            updates.put("cancelled", updatedEvent.getCancelled());
            updates.put("notEnrolled", updatedEvent.getNotEnrolled());
            updates.put("waitingList.limit", updatedEvent.getWaitingList() == null ? null : updatedEvent.getWaitingList().limit);
            updates.put("waitingList.chosen", updatedEvent.getWaitingList() == null ? null : updatedEvent.getWaitingList().chosen);
            updates.put("waitingList.status", updatedEvent.getWaitingList() == null ? null : updatedEvent.getWaitingList().status);

            eventRepository.updateEvent(eventId, updates, (result, updateSuccess) -> {
                if (!updateSuccess || result == null || !result) {
                    listener.onComplete(AppResult.failure(R.string.manage_lottery_draw_failure), false);
                    return;
                }

                listener.onComplete(AppResult.success(updatedEvent, R.string.manage_lottery_draw_success), true);
            });
        });
    }

    private RunLotteryData buildContentState(Event event, List<LotteryEntrantItem> items) {
        Date effectiveDrawDate = event.getDrawDate() != null
                ? event.getDrawDate()
                : event.getRegistrationDeadline() != null ? event.getRegistrationDeadline() : new Date();
        int attendeesToSelect = event.getLimit() > 0 ? event.getLimit() : event.getCapacity();
        int messageRes = items.isEmpty() ? R.string.manage_lottery_empty : 0;

        return new RunLotteryData(
                RunLotteryData.Status.CONTENT,
                drawDateFormat.format(effectiveDrawDate),
                attendeesToSelect > 0 ? String.valueOf(attendeesToSelect) : "",
                items,
                messageRes,
                true,
                false,
                event
        );
    }

    private void buildEntrantItems(List<String> entrantIds, java.util.function.Consumer<List<LotteryEntrantItem>> consumer) {
        List<LotteryEntrantItem> items = new ArrayList<>();
        if (entrantIds == null || entrantIds.isEmpty()) {
            consumer.accept(items);
            return;
        }

        loadEntrantItem(entrantIds, 0, items, consumer);
    }

    private void loadEntrantItem(List<String> entrantIds,
                                 int index,
                                 List<LotteryEntrantItem> items,
                                 java.util.function.Consumer<List<LotteryEntrantItem>> consumer) {
        if (index >= entrantIds.size()) {
            consumer.accept(items);
            return;
        }

        String entrantId = entrantIds.get(index);
        if (isBlank(entrantId)) {
            items.add(new LotteryEntrantItem(entrantId, "", R.string.manage_lottery_join_time_unavailable));
            loadEntrantItem(entrantIds, index + 1, items, consumer);
            return;
        }

        userController.getUserByDeviceId(entrantId, (User user, boolean success) -> {
            String displayName = entrantId;
            if (success && user != null && !isBlank(user.getName())) {
                displayName = user.getName();
            }
            items.add(new LotteryEntrantItem(entrantId, displayName, R.string.manage_lottery_join_time_unavailable));
            loadEntrantItem(entrantIds, index + 1, items, consumer);
        });
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}









