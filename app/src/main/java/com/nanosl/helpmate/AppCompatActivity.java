package com.nanosl.helpmate;

import android.content.Intent;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;

/**
 * Created by Thilina on 6/3/2017.
 */

public class AppCompatActivity extends android.support.v7.app.AppCompatActivity {

    DrawerLayout drawer;

    public AppCompatActivity() {
    }

    protected void openMap() {
        startActivityForResult(new Intent(this, MyLocationDemoActivity.class), 1);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
