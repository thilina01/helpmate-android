package com.nanosl.helpmate.gallery;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Thilina on 5/30/2017.
 */

public class GalleryService {

    public static final String CHILD = "gallerys";
    public static void save(Gallery gallery){
        FirebaseDatabase.getInstance().getReference().child(CHILD).push().setValue(gallery);
    }
    public static DatabaseReference getReference(String id){
        return FirebaseDatabase.getInstance().getReference().child(CHILD).child(id);
    }
}
