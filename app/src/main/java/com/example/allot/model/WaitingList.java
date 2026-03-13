package com.example.allot.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * CRC Card: WaitingList for the event
 */
public class WaitingList {
    public ArrayList<String> list;      // all entrants
    public ArrayList<String> chosen;    // selected entrants
    public HashMap<String, Boolean> status;  // enrolled status

    public int limit = -1;               // max waiting list size
    // Required for Firestore document deserialization.
    public WaitingList(){
        this.list = new ArrayList<>();
        this.chosen = new ArrayList<>();
        this.status = new HashMap<>();
    }

    // Constructor
    public WaitingList(int limit){
        this.limit = limit;
        this.list = new ArrayList<>();
        this.chosen = new ArrayList<>();
        this.status = new HashMap<>();
    }

    // Add entrant to waiting list
    public void joinWaitingList(String user){
        if ((this.limit > 0 && this.list.size() < this.limit) || this.limit == -1){
            this.list.add(user);
        }
    }

    // Randomly select entrants
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

    // Replace a random entrant not already chosen
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

    // Entrants who have enrolled (status = true)
    public ArrayList<String> enrolled(){
        ArrayList<String> signed = new ArrayList<>();
        for (String user : this.chosen){
            if(Boolean.TRUE.equals(this.status.get(user))){
                signed.add(user);
            }
        }
        return signed;
    }

    // Entrants who have not enrolled (status = false)
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
