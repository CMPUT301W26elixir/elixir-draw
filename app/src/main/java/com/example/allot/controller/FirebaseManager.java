package com.example.allot.controller;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

/**
 * FirebaseManager
 *
 * This class is responsible for providing access to Firebase services
 * used in the application. It initializes and stores references to:
 *
 * - Firebase Firestore (database)
 * - Firebase Storage (file/image storage)
 *
 * Other controllers can use this manager instead of creating their own
 * Firebase instances.
 */
public class FirebaseManager {

    // Reference to the Firestore database
    public FirebaseFirestore db;

    // Reference to Firebase Storage (used for images/files)
    public FirebaseStorage storage;

    /**
     * Constructor
     *
     * Initializes the Firebase services when the manager is created.
     */
    public FirebaseManager() {

        // Get the singleton Firestore instance
        this.db = FirebaseFirestore.getInstance();

        // Get the singleton Firebase Storage instance
        this.storage = FirebaseStorage.getInstance();
    }

    /**
     * Returns the Firestore database instance.
     *
     * This allows other classes (controllers, models, etc.)
     * to perform database operations without creating their
     * own Firestore connections.
     */
    public FirebaseFirestore getDb() {
        return db;
    }
}