package com.example.allot.common.core.firebase;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

/**
 * CRC Card: FirebaseManager
 * Responsibility: Synchronize data and handle Storage.
 */
public class FirebaseManager {
    public FirebaseFirestore db;
    public FirebaseStorage storage;

    public FirebaseManager() {
        this.db = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
    }

    // This manager provides the "tools" for the other classes
    public FirebaseFirestore getDb() {
        return db;
    }
}
