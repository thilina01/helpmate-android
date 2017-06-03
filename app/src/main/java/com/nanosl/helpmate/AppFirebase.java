package com.nanosl.helpmate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

/**
 * Created by Thilina on 5/31/2017.
 */

public class AppFirebase {

    private static FirebaseAuth firebaseAuth;
    private static FirebaseUser firebaseUser;

    private static FirebaseAnalytics firebaseAnalytics;
    private static FirebaseRemoteConfig firebaseRemoteConfig;

    private static DatabaseReference firebaseDatabaseReference;

    public static void init(Activity activity) {

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth == null || firebaseAuth.getCurrentUser() == null) {
            // Not signed in, launch the Sign In activity
            activity.startActivity(new Intent(activity, SignInActivity.class));
            activity.finish();
        }
        firebaseUser = firebaseAuth.getCurrentUser();

        // Initialize Firebase Measurement.
        firebaseAnalytics = FirebaseAnalytics.getInstance(activity);

        // Initialize Firebase Remote Config.
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        firebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public static FirebaseUser getFirebaseUser() {
        return firebaseUser;
    }

    public static void signOut() {
        firebaseAuth.signOut();
        firebaseUser = null;
    }

    public static FirebaseRemoteConfig getFirebaseRemoteConfig() {
        return firebaseRemoteConfig;
    }

    public static void logEvent(String value) {

        // Use Firebase Measurement to log that invitation was not sent
        Bundle payload = new Bundle();
        payload.putString(FirebaseAnalytics.Param.VALUE, value);
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, payload);
    }

    public static DatabaseReference getFirebaseDatabaseReference() {
        return firebaseDatabaseReference;
    }

    // Fetch the config to determine the allowed length of messages.
    public static void fetchConfig() {
        long cacheExpiration = 3600; // 1 hour in seconds
        // If developer mode is enabled reduce cacheExpiration to 0 so that each fetch goes to the
        // server. This should not be used in release builds.
        if (firebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }
        firebaseRemoteConfig.fetch(cacheExpiration)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Make the fetched config available via FirebaseRemoteConfig get<type> calls.
                        firebaseRemoteConfig.activateFetched();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // There has been an error fetching the config
                        Log.w("FETCH_CONFIG", "Error fetching config", e);
                    }
                });
    }

}
