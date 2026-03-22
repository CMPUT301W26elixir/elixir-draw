package com.example.allot.model.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Tracks everyone who joined an event waiting list and the draw results for them.
 */
public class WaitingList {
    /** Stores every user who joined the waiting list. */
    public ArrayList<String> list;
    /** Stores the entrants selected during the draw. */
    public ArrayList<String> chosen;
    /** Tracks whether each selected entrant has accepted their spot. */
    public HashMap<String, Boolean> status;

    /** Limits how many entrants can be selected, or stays -1 when no limit is set yet. */
    public int limit = -1;

    /**
     * Creates an empty WaitingList for Firestore document deserialization.
     */
    public WaitingList(){
        this.list = new ArrayList<>();
        this.chosen = new ArrayList<>();
        this.status = new HashMap<>();
    }

    /**
     * Creates a WaitingList with the given maximum size.
     *
     * @param limit the maximum number of entrants allowed in the waiting list
     */
    public WaitingList(int limit){
        this.limit = limit;
        this.list = new ArrayList<>();
        this.chosen = new ArrayList<>();
        this.status = new HashMap<>();
    }

    /**
     * Randomly selects entrants from the waiting list up to the limit
     * and initializes their enrollment status to false.
     */
    public void selectedList(){
        ArrayList<Integer> chosenIndex = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < this.limit; i++){
            if (list.isEmpty()) break;

            // Pick a random entrant and skip repeats
            int index = rand.nextInt(this.list.size());
            if (!chosenIndex.contains(index)){
                chosenIndex.add(index);
                String user = this.list.get(index);
                this.chosen.add(user);
                this.status.put(user, false);
            } else {
                if (chosenIndex.size() >= list.size()) break; // Stop if every entrant was already checked
                i--; // Try this spot again
            }
        }

    }

    /**
     * Returns the list of selected entrants who have enrolled.
     *
     * @return a list of enrolled user IDs
     */
    public ArrayList<String> enrolled(){
        ArrayList<String> signed = new ArrayList<>();
        for (String user : this.chosen){
            // True means this chosen user joined
            if(Boolean.TRUE.equals(this.status.get(user))){
                signed.add(user);
            }
        }
        return signed;
    }

    /**
     * Returns the list of selected entrants who have not enrolled.
     *
     * @return a list of not-enrolled user IDs
     */
    public ArrayList<String> notEnrolled(){
        ArrayList<String> notSigned = new ArrayList<>();
        for (String user : this.chosen){
            // False means this chosen user did not join
            if(Boolean.FALSE.equals(this.status.get(user))){
                notSigned.add(user);
            }
        }
        return notSigned;
    }
}








