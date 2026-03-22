package com.example.allot.view.shared;

import com.example.allot.model.event.Event;
import java.util.ArrayList;
import java.util.List;
public class EventListItemMapper {

    /**
     * Converts the provided events into displayable list items and marks
     * events as saved when their IDs appear in the saved list.
     *
     * @param events the events to convert into list items
     * @param savedEventIds the IDs of events currently saved by the user
     * @return the mapped list items ready for display
     */
    public List<EventListItem> mapEvents(List<Event> events, List<String> savedEventIds) {
        List<EventListItem> listItems = new ArrayList<>();
        if (events == null) {
            return listItems;
        }

        for (Event event : events) {
            EventListItem item = EventListItem.fromEvent(event);
            item.isSaved = savedEventIds != null && savedEventIds.contains(event.getEventId());
            listItems.add(item);
        }

        return listItems;
    }
}









