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
    /** Stores each entrant's join location details keyed by device ID. */
    private HashMap<String, WaitlistJoinLocation> joinLocations;

    /** Limits how many entrants can be selected, or stays -1 when no limit is set yet. */
    public int limit = -1;

    /**
     * Creates a new WaitingList instance.
     */
    public WaitingList(){
        this.list = new ArrayList<>();
        this.chosen = new ArrayList<>();
        this.status = new HashMap<>();
        this.joinLocations = new HashMap<>();
    }

    /**
     * Creates a new WaitingList instance.
     *
     * @param limit the limit
     */
    public WaitingList(int limit){
        this.limit = limit;
        this.list = new ArrayList<>();
        this.chosen = new ArrayList<>();
        this.status = new HashMap<>();
        this.joinLocations = new HashMap<>();
    }

    /**
     * Performs selected list.
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
     * Returns the result of enrolled.
     *
     * @return the result of this call
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
     * Returns the result of not enrolled.
     *
     * @return the result of this call
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

    /**
     * Returns the join locations.
     *
     * @return the join locations
     */
    public HashMap<String, WaitlistJoinLocation> getJoinLocations() {
        if (joinLocations == null) {
            joinLocations = new HashMap<>();
        }
        return joinLocations;
    }

    /**
     * Updates the join locations.
     *
     * @param joinLocations the join locations
     */
    public void setJoinLocations(HashMap<String, WaitlistJoinLocation> joinLocations) {
        this.joinLocations = joinLocations;
    }
}








