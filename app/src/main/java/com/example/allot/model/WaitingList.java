package com.example.allot.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * CRC Card: WaitingList for the event
 */
public class WaitingList {
    public ArrayList<Entrant> list;      // all entrants
    public ArrayList<Entrant> chosen;    // selected entrants
    public HashMap<Entrant, Boolean> status;  // enrolled status

    public int limit = -1;               // max waiting list size

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
    }

    // Add entrant to waiting list
    public void joinWaitingList(Entrant entrant){
        if ((this.limit > 0 && this.list.size() < this.limit) || this.limit == -1){
            this.list.add(entrant);
        }
    }

    // Randomly select entrants
    public ArrayList<Entrant> selectedList(){
        ArrayList<Integer> chosenIndex = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < this.list.size(); i++){
            if (list.size() == 0) break;

            int index = rand.nextInt(this.list.size());
            if (!chosenIndex.contains(index)){
                chosenIndex.add(index);
                Entrant entrant = this.list.get(index);
                this.chosen.add(entrant);
                this.status.put(entrant, false);
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
            Entrant entrant = this.list.get(index);
            if(!this.chosen.contains(entrant)){
                this.chosen.add(entrant);
                this.status.put(entrant, false);
                keepGoing = false;
            }
        }
    }

    // Entrants who have enrolled (status = true)
    public ArrayList<Entrant> enrolled(){
        ArrayList<Entrant> signed = new ArrayList<>();
        for (Entrant entrant : this.chosen){
            if(Boolean.TRUE.equals(this.status.get(entrant))){
                signed.add(entrant);
            }
        }
        return signed;
    }

    // Entrants who have not enrolled (status = false)
    public ArrayList<Entrant> notEnrolled(){
        ArrayList<Entrant> notSigned = new ArrayList<>();
        for (Entrant entrant : this.chosen){
            if(Boolean.FALSE.equals(this.status.get(entrant))){
                notSigned.add(entrant);
            }
        }
        return notSigned;
    }
}

