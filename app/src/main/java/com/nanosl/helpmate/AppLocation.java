package com.nanosl.helpmate;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

import java.util.List;

/**
 * Created by Thilina on 6/2/2017.
 */

public class AppLocation {

    private static MainActivity mainActivity;

    public static void init(MainActivity theMainActivity) {
        mainActivity = theMainActivity;
    }

    public static Location getLocation(Activity activity) {
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(false);

        if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activity.requestPermissions(
                        new String[]{
                                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.INTERNET
                        }, 10);
            }
        }

        for (int i = providers.size() - 1; i >= 0; i--) {
            return locationManager.getLastKnownLocation(providers.get(i));
        }
        return null;
    }

    public static Location getLocation() {
        return getLocation(mainActivity);
    }

}
