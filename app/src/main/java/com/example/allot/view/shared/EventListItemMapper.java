package com.example.allot.view.shared;

import com.example.allot.model.event.Event;
import java.util.ArrayList;
import java.util.List;
/**
 * Turns event models into the small items used by list adapters.
 */
public class EventListItemMapper {

    /**
     * Returns the result of map events.
     *
     * @param events the events
     * @param savedEventIds the saved event ids
     * @return the result of this call
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









