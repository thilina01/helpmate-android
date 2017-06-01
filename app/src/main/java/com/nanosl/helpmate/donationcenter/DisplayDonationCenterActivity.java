/**
 * Copyright Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nanosl.helpmate.donationcenter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.nanosl.helpmate.R;

public class DisplayDonationCenterActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener {


    private static final String TAG = "DisplayDonationCenter";

    private TextView nameTextView;
    private TextView descriptionTextView;
    private TextView addressTextView;
    private TextView cityTextView;
    private TextView contactPersonTextView;
    private TextView phoneTextView;
    //private TextView locationTextView;
    private Button editButton;
    private Button deleteButton;

    private DonationCenter donationCenter;
    private LinearLayoutManager mLinearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Helpmate: Donation Center Details");
        setContentView(R.layout.activity_display_donation_center);
        Intent intent = getIntent();
        donationCenter = (DonationCenter) intent.getSerializableExtra("donationCenter");

        nameTextView = (TextView) findViewById(R.id.nameTextView);
        descriptionTextView = (TextView) findViewById(R.id.descriptionTextView);
        contactPersonTextView = (TextView) findViewById(R.id.contactPersonTextView);
        addressTextView = (TextView) findViewById(R.id.addressTextView);
        cityTextView = (TextView) findViewById(R.id.cityTextView);
        phoneTextView = (TextView) findViewById(R.id.phoneTextView);
        //locationTextView = (TextView) findViewById(R.id.locationTextView);

        nameTextView.setText(donationCenter.getName());
        descriptionTextView.setText(donationCenter.getDescription());
        contactPersonTextView.setText(donationCenter.getContactPerson());
        addressTextView.setText(donationCenter.getAddress());
        cityTextView.setText(donationCenter.getCity());
        phoneTextView.setText(donationCenter.getPhone());
        //locationTextView.setText(donationCenter.getGpsLocation());

        editButton = (Button) findViewById(R.id.editButton);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addDonationCenterIntent = new Intent(view.getContext(), AddDonationCenterActivity.class);

                addDonationCenterIntent.putExtra("donationCenter", donationCenter); //Optional parameters
                view.getContext().startActivity(addDonationCenterIntent);
                finish();
                //onDeleteClick(view);
            }
        });
        deleteButton = (Button) findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DonationCenterService.getReference(donationCenter.getId()).child("status").setValue("inactive");
                finish();
                //onDeleteClick(view);
            }
        });

        // Initialize Firebase Auth
        ;
        if (!donationCenter.getUid().equalsIgnoreCase(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
            editButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);

    }

    public void onDeleteClick(View v) {

        AlertDialog.Builder alert = new AlertDialog.Builder(DisplayDonationCenterActivity.this);
        alert.setTitle("Delete");
        alert.setMessage("Are you sure to delete?");
        alert.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                finish();
            }
        });

        alert.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alert.show();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

}
