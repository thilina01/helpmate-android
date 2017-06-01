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
package com.nanosl.helpmate.donation;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.appindexing.Action;
import com.google.firebase.appindexing.FirebaseAppIndex;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.appindexing.Indexable;
import com.google.firebase.appindexing.builders.PersonBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.nanosl.helpmate.AppFirebase;
import com.nanosl.helpmate.AppMenu;
import com.nanosl.helpmate.R;

import java.util.HashMap;
import java.util.Map;

import static com.google.firebase.appindexing.builders.Indexables.messageBuilder;
import static com.google.firebase.appindexing.builders.Indexables.personBuilder;

public class DonationActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "Donation";
    public static final String MESSAGES_CHILD = "donations";
    private static final int REQUEST_INVITE = 1;
    public static final String ANONYMOUS = "anonymous";
    private static final String MESSAGE_URL = "http://friendlychat.firebase.google.com/message/";

    private String mUsername;

    private FloatingActionButton addDonationButton;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private FirebaseRecyclerAdapter<Donation, DonationViewHolder> mFirebaseAdapter;
    private ProgressBar mProgressBar;
    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Helpmate: Donations");
        setContentView(R.layout.activity_donation);
        mUsername = AppFirebase.getFirebaseUser().getDisplayName();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mFirebaseAdapter = new FirebaseRecyclerAdapter<Donation, DonationViewHolder>(
                Donation.class,
                R.layout.item_message,
                DonationViewHolder.class,
                AppFirebase.getFirebaseDatabaseReference().child(MESSAGES_CHILD).orderByChild("status").equalTo("active")) {

            @Override
            protected Donation parseSnapshot(DataSnapshot snapshot) {
                Donation donation = super.parseSnapshot(snapshot);
                if (donation != null) {
                    donation.setId(snapshot.getKey());
                }
                return donation;
            }

            @Override
            protected void populateViewHolder(final DonationViewHolder viewHolder,
                                              Donation donation, int position) {
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                viewHolder.donation = donation;
                if (donation.getStatus() != null && donation.getStatus().equalsIgnoreCase("inactive")) {
                    return;
                }
                if (donation.getDescription() != null) {
                    viewHolder.messageTextView.setText(donation.getDescription().length() > 100 ? donation.getDescription().substring(0, 100) + "..." : donation.getDescription());
                    viewHolder.messageTextView.setVisibility(TextView.VISIBLE);
                }

                String username = donation.getUsername();
                String contactPerson = donation.getContactPerson();
                String messenger = contactPerson != null && !contactPerson.trim().equalsIgnoreCase(username.trim()) ? username + " / " + contactPerson : username;
                viewHolder.messengerTextView.setText(messenger);
                if (donation.getCity() != null) {
                    viewHolder.cityTextView.setText(donation.getCity());
                }
                if (donation.getAddedTime() != null) {
                    viewHolder.addedTimeTextView.setText(android.text.format.DateFormat.format("yyyy-MM-dd HH:mm", donation.getAddedTime()));
                }
                if (donation.getAddress() == null) {
                    viewHolder.messengerImageView.setImageDrawable(ContextCompat.getDrawable(DonationActivity.this,
                            R.drawable.ic_account_circle_black_36dp));
                } else {
                    Glide.with(DonationActivity.this)
                            .load(donation.getPhotoUrl())
                            .into(viewHolder.messengerImageView);
                }

                if (donation.getContactPerson() != null) {
                    // write this message to the on-device index
                    FirebaseAppIndex.getInstance().update(getMessageIndexable(donation));
                }

                // log a view action on it
                FirebaseUserActions.getInstance().end(getMessageViewAction(donation));
            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
                // to the bottom of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);

        // Define Firebase Remote Config Settings.
        FirebaseRemoteConfigSettings firebaseRemoteConfigSettings =
                new FirebaseRemoteConfigSettings.Builder()
                        .setDeveloperModeEnabled(true)
                        .build();

        // Define default config values. Defaults are used when fetched config values are not
        // available. Eg: if an error occurred fetching values from the server.
        Map<String, Object> defaultConfigMap = new HashMap<>();
        defaultConfigMap.put("friendly_msg_length", 10L);

        // Apply config settings and default values.
        AppFirebase.getFirebaseRemoteConfig().setConfigSettings(firebaseRemoteConfigSettings);
        AppFirebase.getFirebaseRemoteConfig().setDefaults(defaultConfigMap);

        // Fetch remote config.
        fetchConfig();

        addDonationButton = (FloatingActionButton) findViewById(R.id.addDonationButton);
        addDonationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addDonationIntent = new Intent(DonationActivity.this, AddDonationActivity.class);
                DonationActivity.this.startActivity(addDonationIntent);
            }
        });

    }

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(DonationActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(DonationActivity.this, permission)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(DonationActivity.this, new String[]{permission}, requestCode);

            } else {

                ActivityCompat.requestPermissions(DonationActivity.this, new String[]{permission}, requestCode);
            }
        } else {
            Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
        }
    }

    private Action getMessageViewAction(Donation donation) {
        return new Action.Builder(Action.Builder.VIEW_ACTION)
                .setObject(donation.getContactPerson(), MESSAGE_URL.concat(donation.getId()))
                .setMetadata(new Action.Metadata.Builder().setUpload(false))
                .build();
    }

    private Indexable getMessageIndexable(Donation donation) {
        PersonBuilder sender = personBuilder()
                .setIsSelf(mUsername.equals(donation.getUsername()))
                .setName(donation.getUsername())
                .setUrl(MESSAGE_URL.concat(donation.getId() + "/sender"));

        PersonBuilder recipient = personBuilder()
                .setName(mUsername)
                .setUrl(MESSAGE_URL.concat(donation.getId() + "/recipient"));

        return messageBuilder()
                .setName(donation.getContactPerson())
                .setUrl(MESSAGE_URL.concat(donation.getId()))
                .setSender(sender)
                .setRecipient(recipient)
                .build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (AppMenu.action(this, item)) {
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void sendInvitation() {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
    }

    // Fetch the config to determine the allowed length of messages.
    public void fetchConfig() {
        long cacheExpiration = 3600; // 1 hour in seconds
        // If developer mode is enabled reduce cacheExpiration to 0 so that each fetch goes to the
        // server. This should not be used in release builds.
        if (AppFirebase.getFirebaseRemoteConfig().getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }
        AppFirebase.getFirebaseRemoteConfig().fetch(cacheExpiration)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Make the fetched config available via FirebaseRemoteConfig get<type> calls.
                        AppFirebase.getFirebaseRemoteConfig().activateFetched();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // There has been an error fetching the config
                        Log.w(TAG, "Error fetching config", e);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                AppFirebase.logEvent("inv_sent");
                // Check how many invitations were sent and log.
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                Log.d(TAG, "Invitations sent: " + ids.length);
            } else {
                AppFirebase.logEvent("inv_not_sent");
                // Sending failed or it was canceled, show failure message to the user
                Log.d(TAG, "Failed to send invitation.");
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

}
