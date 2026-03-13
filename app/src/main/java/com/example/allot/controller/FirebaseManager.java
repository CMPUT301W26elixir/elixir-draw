package com.example.allot.controller;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

/**
 * Manages access to Firebase services used by the application,
 * including Firestore and Firebase Storage.
 */
public class FirebaseManager {
    public FirebaseFirestore db;
    public FirebaseStorage storage;

    /**
     * Creates a FirebaseManager and initializes Firestore and Storage instances.
     */
    public FirebaseManager() {
        this.db = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
    }

    /**
     * Returns the Firestore database instance used by the application.
     *
     * @return the Firestore database instance
     */
    public FirebaseFirestore getDb() {
        return db;
    }
}