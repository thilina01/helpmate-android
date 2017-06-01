package com.nanosl.helpmate.donation;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Thilina on 5/30/2017.
 */

public class DonationService {

    public static final String CHILD = "donations";
    public static void save(Donation donation){
        FirebaseDatabase.getInstance().getReference().child(CHILD).push().setValue(donation);
    }
    public static DatabaseReference getReference(String id){
        return FirebaseDatabase.getInstance().getReference().child(CHILD).child(id);
    }
}
