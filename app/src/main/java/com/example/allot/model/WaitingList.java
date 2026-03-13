package com.example.allot.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Represents the waiting list for an Event.
 * Handles entrants, selection (lottery), and enrollment status.
 */
public class WaitingList {

    public ArrayList<String> list;      // All entrants
    public ArrayList<String> chosen;    // Entrants selected from waiting list
    public HashMap<String, Boolean> status;  // Enrollment status: true = enrolled, false = not enrolled

    public int limit = -1;               // Maximum waiting list size (-1 = no limit)

    /**
     * Default constructor required for Firestore deserialization.
     * Initializes lists and status map.
     */
    public WaitingList() {
        this.list = new ArrayList<>();
        this.chosen = new ArrayList<>();
        this.status = new HashMap<>();
    }

    /**
     * Creates a WaitingList with a specific limit.
     *
     * @param limit Maximum number of entrants allowed (-1 = unlimited)
     */
    public WaitingList(int limit) {
        this.limit = limit;
        this.list = new ArrayList<>();
        this.chosen = new ArrayList<>();
        this.status = new HashMap<>();
    }

    /**
     * Adds a user to the waiting list if space allows.
     *
     * @param user the user ID to add
     */
    public void joinWaitingList(String user) {
        if ((this.limit > 0 && this.list.size() < this.limit) || this.limit == -1) {
            this.list.add(user);
        }
    }

    /**
     * Randomly selects users from the waiting list up to the limit.
     * Adds selected users to the 'chosen' list and sets their status to false.
     *
     * AI assistance used to design fair lottery selection logic (ChatGPT, OpenAI, 2026).
     */
    public void selectedList() {
        ArrayList<Integer> chosenIndex = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < this.limit; i++) {
            if (list.size() == 0) break;

            int index = rand.nextInt(this.list.size());
            if (!chosenIndex.contains(index)) {
                chosenIndex.add(index);
                String user = this.list.get(index);
                this.chosen.add(user);
                this.status.put(user, false);
            } else {
                if (chosenIndex.size() >= list.size()) break; // Can't pick more than available
                i--; // Retry
            }
        }
    }

    /**
     * Replaces a randomly selected entrant not already chosen.
     * Ensures the chosen list grows without duplicates.
     */
    public void replace() {
        if (chosen.size() >= list.size()) return;

        Random rand = new Random();
        boolean keepGoing = true;

        while (keepGoing) {
            int index = rand.nextInt(this.list.size());
            String user = this.list.get(index);
            if (!this.chosen.contains(user)) {
                this.chosen.add(user);
                this.status.put(user, false);
                keepGoing = false;
            }
        }
    }

    /**
     * Returns a list of entrants who have enrolled (status = true).
     *
     * @return ArrayList of enrolled user IDs
     */
    public ArrayList<String> enrolled() {
        ArrayList<String> signed = new ArrayList<>();
        for (String user : this.chosen) {
            if (Boolean.TRUE.equals(this.status.get(user))) {
                signed.add(user);
            }
        }
        return signed;
    }

    /**
     * Returns a list of entrants who have not enrolled (status = false).
     *
     * @return ArrayList of not enrolled user IDs
     */
    public ArrayList<String> notEnrolled() {
        ArrayList<String> notSigned = new ArrayList<>();
        for (String user : this.chosen) {
            if (Boolean.FALSE.equals(this.status.get(user))) {
                notSigned.add(user);
            }
        }
        return notSigned;
    }
}