package com.example.allot;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EntrantController entrantController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.eventsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<EventListItem> basicEvents = Arrays.asList(
                new EventListItem("Event Name 1", "Street Name 1", "Date 1", "$20", "3 Days Left"),
                new EventListItem("Event Name 2", "Street Name 2", "Date 2", "Free", "7 Days Left"),
                new EventListItem("Event Name 3", "Street Name 3", "Date 3", "$40", "10 Days Left"),
                new EventListItem("Event Name 4", "Street Name 4", "Date 4", "$15", "14 Days Left"),
                new EventListItem("Event Name 5", "Street Name 5", "Date 5", "$10", "18 Days Left"),
                new EventListItem("Event Name 6", "Street Name 6", "Date 6", "Free", "22 Days Left")
        );

        recyclerView.setAdapter(new EventListAdapter(basicEvents));

        // Keep existing entrant logic untouched.
        entrantController = new EntrantController(this);
        entrantController.getOrCreateEntrant(new EntrantController.EntrantCallback() {
            @Override
            public void onCallback(Entrant entrant) {
                Log.d("Allot_Logic", "Welcome, " + entrant.getName());
                Log.d("Allot_Logic", "Role: " + entrant.getRole());
            }
        });
    }
}
