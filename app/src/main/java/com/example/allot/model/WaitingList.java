package com.example.allot.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * CRC Card: WaitingList for the event
 */
public class WaitingList {
    public ArrayList<User> list;      // all entrants
    public ArrayList<User> chosen;    // selected entrants
    public HashMap<User, Boolean> status;  // enrolled status

    public int limit = -1;               // max waiting list size

    public int choosinglimit;
    // Required for Firestore document deserialization.
    public WaitingList(){
        this.list = new ArrayList<>();
        this.chosen = new ArrayList<>();
        this.status = new HashMap<>();
    }

    // Constructor
    public WaitingList(int limit, int choosingLimit){
        this.limit = limit;
        this.list = new ArrayList<>();
        this.chosen = new ArrayList<>();
        this.status = new HashMap<>();
        this.choosinglimit = choosingLimit;
    }

    // Add entrant to waiting list
    public void joinWaitingList(User user){
        if ((this.limit > 0 && this.list.size() < this.limit) || this.limit == -1){
            this.list.add(user);
        }
    }

    // Randomly select entrants
    public ArrayList<User> selectedList(){
        ArrayList<Integer> chosenIndex = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < this.choosinglimit; i++){
            if (list.size() == 0) break;

            int index = rand.nextInt(this.list.size());
            if (!chosenIndex.contains(index)){
                chosenIndex.add(index);
                User user = this.list.get(index);
                this.chosen.add(user);
                this.status.put(user, false);
            } else {
                i--; // try again
            }
        }

        return this.chosen;
    }

    // Replace a random entrant not already chosen
    public void replace(){
        Random rand = new Random();
        boolean keepGoing = true;

        while(keepGoing){
            int index = rand.nextInt(this.list.size());
            User user = this.list.get(index);
            if(!this.chosen.contains(user)){
                this.chosen.add(user);
                this.status.put(user, false);
                keepGoing = false;
            }
        }
    }

    // Entrants who have enrolled (status = true)
    public ArrayList<User> enrolled(){
        ArrayList<User> signed = new ArrayList<>();
        for (User user : this.chosen){
            if(Boolean.TRUE.equals(this.status.get(user))){
                signed.add(user);
            }
        }
        return signed;
    }

    // Entrants who have not enrolled (status = false)
    public ArrayList<User> notEnrolled(){
        ArrayList<User> notSigned = new ArrayList<>();
        for (User user : this.chosen){
            if(Boolean.FALSE.equals(this.status.get(user))){
                notSigned.add(user);
            }
        }
        return notSigned;
    }
}

