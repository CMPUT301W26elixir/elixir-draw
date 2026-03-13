package com.example.allot.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Represents the waiting list for an event, including all entrants,
 * selected entrants, and their enrollment status.
 */
public class WaitingList {
    public ArrayList<String> list;      // all entrants
    public ArrayList<String> chosen;    // selected entrants
    public HashMap<String, Boolean> status;  // enrolled status

    public int limit = -1;               // max waiting list size

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
     * Adds a user to the waiting list if there is space available
     * or if the waiting list is unlimited.
     *
     * @param user the user ID to add to the waiting list
     */
    public void joinWaitingList(String user){
        if ((this.limit > 0 && this.list.size() < this.limit) || this.limit == -1){
            this.list.add(user);
        }
    }

    /**
     * Randomly selects entrants from the waiting list up to the limit
     * and initializes their enrollment status to false.
     */
    public void selectedList(){
        ArrayList<Integer> chosenIndex = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < this.limit; i++){
            if (list.size() == 0) break;

            int index = rand.nextInt(this.list.size());
            if (!chosenIndex.contains(index)){
                chosenIndex.add(index);
                String user = this.list.get(index);
                this.chosen.add(user);
                this.status.put(user, false);
            } else {
                if (chosenIndex.size() >= list.size()) break; // can't pick more than available
                i--; // try again
            }
        }

    }

    /**
     * Replaces a selected entrant by randomly choosing a user who
     * has not already been selected.
     */
    public void replace(){
        if (chosen.size() >= list.size()) return;

        Random rand = new Random();
        boolean keepGoing = true;

        while(keepGoing){
            int index = rand.nextInt(this.list.size());
            String user = this.list.get(index);
            if(!this.chosen.contains(user)){
                this.chosen.add(user);
                this.status.put(user, false);
                keepGoing = false;
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
            if(Boolean.FALSE.equals(this.status.get(user))){
                notSigned.add(user);
            }
        }
        return notSigned;
    }
}