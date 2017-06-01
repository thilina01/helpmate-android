package com.nanosl.helpmate.donationcenter;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Thilina on 5/30/2017.
 */

public class DonationCenterService {

    public static final String CHILD = "donationCenters";
    public static void save(DonationCenter donationCenter){
        FirebaseDatabase.getInstance().getReference().child(CHILD).push().setValue(donationCenter);
    }
    public static DatabaseReference getReference(String id){
        return FirebaseDatabase.getInstance().getReference().child(CHILD).child(id);
    }
}
