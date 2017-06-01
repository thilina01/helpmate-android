package com.nanosl.helpmate;

import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by Thilina on 5/31/2017.
 */

public class AppGoogleApi {
    private static GoogleApiClient mGoogleApiClient;

    public static void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
    }

    public static void init(FragmentActivity activity) {
        GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {

            @Override
            public void onConnectionFailed(ConnectionResult connectionResult) {
                Log.d("CONNECTION_FAILED", "onConnectionFailed:" + connectionResult);
            }
        };
        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .enableAutoManage(activity /* FragmentActivity */, onConnectionFailedListener /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

    }
}
