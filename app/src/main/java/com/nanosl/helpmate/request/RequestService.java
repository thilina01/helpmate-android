package com.nanosl.helpmate.request;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Thilina on 5/30/2017.
 */

public class RequestService {

    public static final String CHILD = "requests";
    public static void save(Request request){
        FirebaseDatabase.getInstance().getReference().child(CHILD).push().setValue(request);
    }
    public static DatabaseReference getRequestReference(String id){
        return FirebaseDatabase.getInstance().getReference().child(CHILD).child(id);
    }
}
