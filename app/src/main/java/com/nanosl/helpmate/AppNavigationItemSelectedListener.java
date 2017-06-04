package com.nanosl.helpmate;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.view.MenuItem;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.nanosl.helpmate.donation.DonationActivity;
import com.nanosl.helpmate.donationcenter.DonationCenterActivity;
import com.nanosl.helpmate.gallery.GalleryActivity;
import com.nanosl.helpmate.request.RequestActivity;

/**
 * Created by Thilina on 6/1/2017.
 */

public class AppNavigationItemSelectedListener implements NavigationView.OnNavigationItemSelectedListener {
    private static AppNavigationItemSelectedListener instance;
    private static Activity activity;
    private static final int REQUEST_INVITE = 1;

    public static NavigationView.OnNavigationItemSelectedListener getInstance(Activity anActivity) {
        if (instance == null) {
            instance = new AppNavigationItemSelectedListener();
        }
        instance.activity = anActivity;
        return instance;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case R.id.invite_menu:
                sendInvitation();
                return true;
            case R.id.sign_out_menu:
                AppFirebase.signOut();
                AppGoogleApi.signOut();
                activity.startActivity(new Intent(activity, SignInActivity.class));
                return true;
            case R.id.request_menu:
                activity.startActivity(new Intent(activity, RequestActivity.class));
                //activity.finish();
                return true;
            case R.id.donation_center_menu:
                activity.startActivity(new Intent(activity, DonationCenterActivity.class));
                //activity.finish();
                return true;
            case R.id.donation_menu:
                activity.startActivity(new Intent(activity, DonationActivity.class));
                //activity.finish();
                return true;
            case R.id.gallery_menu:
                activity.startActivity(new Intent(activity, GalleryActivity.class));
                //activity.finish();
                return true;
            case R.id.exit_menu:
                Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                homeIntent.addCategory(Intent.CATEGORY_HOME);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                activity.startActivity(homeIntent);
                //activity.finish();
                return true;
            case R.id.fresh_config_menu:
                AppFirebase.fetchConfig();
                return true;
            default:
                return false;
        }
    }

    private static void sendInvitation() {
        Intent intent = new AppInviteInvitation.IntentBuilder(activity.getString(R.string.invitation_title))
                .setMessage(activity.getString(R.string.invitation_message))
                .setCallToActionText(activity.getString(R.string.invitation_cta))
                .build();
        activity.startActivityForResult(intent, REQUEST_INVITE);
    }
}
