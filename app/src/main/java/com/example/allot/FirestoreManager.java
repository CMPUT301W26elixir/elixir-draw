package com.example.allot;

import android.content.Context;
import android.provider.Settings;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirestoreManager {
    private FirebaseFirestore db;
    private Context context;

    public FirestoreManager(Context context) {
        this.db = FirebaseFirestore.getInstance();
        this.context = context;
    }

    // US 01.07.01: Identify by Device ID
    public String getDeviceId() {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public void checkOrCreateUser(UserCallback callback) {
        String id = getDeviceId();
        db.collection("users").document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        callback.onUserFound(documentSnapshot.toObject(User.class));
                    } else {
                        User newUser = new User(id, "Guest", "", "", "entrant");
                        db.collection("users").document(id).set(newUser);
                        callback.onUserFound(newUser);
                    }
                });
    }

    public interface UserCallback {
        void onUserFound(User user);
    }
}
