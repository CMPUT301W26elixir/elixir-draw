package com.example.allot.controller.event;

import com.example.allot.model.event.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class SearchEventController {

    private FirebaseFirestore db;

    public SearchEventController() {
        db = FirebaseFirestore.getInstance();
    }

    public interface SearchCallback {
        void onResults(List<Event> results);
        void onError(Exception e);
    }

    public void searchEvents(String keyword, SearchCallback callback) {
        db.collection("events").get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Event> results = new ArrayList<>();
                    String lower = keyword.toLowerCase().trim();
                    for (var doc : querySnapshot) {
                        Event event = doc.toObject(Event.class);
                        if (event.getTitle() != null &&
                                event.getTitle().toLowerCase().contains(lower)) {
                            results.add(event);
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