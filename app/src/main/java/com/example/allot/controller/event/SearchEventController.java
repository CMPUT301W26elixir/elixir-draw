package com.example.allot.controller.event;

import com.example.allot.model.event.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class SearchEventController {

    private FirebaseFirestore db;

    /**
     * Creates a new SearchEventController instance.
     */
    public SearchEventController() {
        db = FirebaseFirestore.getInstance();
    }

    public interface SearchCallback {
        /**
         * Handles the results callback.
         *
         * @param results the results
         */
        void onResults(List<Event> results);
        /**
         * Handles the error callback.
         *
         * @param e the e
         */
        void onError(Exception e);
    }

    /**
     * Performs search events.
     *
     * @param keyword the keyword
     * @param callback the callback
     */
    public void searchEvents(String keyword, SearchCallback callback) {
        db.collection("events").get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Event> results = new ArrayList<>();
                    String lower = keyword.toLowerCase().trim();
                    for (var doc : querySnapshot) {
                        Event event = doc.toObject(Event.class);
                        /**
                         * Returns whether contains.
                         */
                        if (event.getTitle() != null &&
                                event.getTitle().toLowerCase().contains(lower)) {
                            results.add(event);
                        /**
                         * Returns whether contains.
                         */
                        } else if (event.getDescription() != null &&
                                event.getDescription().toLowerCase().contains(lower)) {
                            results.add(event);
                        }
                    }
                    callback.onResults(results);
                })
                .addOnFailureListener(callback::onError);
    }
}
