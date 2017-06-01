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
package com.nanosl.helpmate;

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
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.nanosl.helpmate.request.AddRequestActivity;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    public static final String MESSAGES_CHILD = "requests";
    public static final String ANONYMOUS = "anonymous";
    private static final String TAG = "MainActivity";
    private static final int REQUEST_INVITE = 1;
    private static final String MESSAGE_URL = "http://friendlychat.firebase.google.com/message/";

    private String mUsername;

    private FloatingActionButton mAddCollectionCenterButton;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    //private FirebaseRecyclerAdapter<Request, MessageViewHolder> mFirebaseAdapter;
    private ProgressBar mProgressBar;
    //private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    //private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Helpmate");
        setContentView(R.layout.activity_main);
        AppFirebase.init(this);
        AppGoogleApi.init(this);
        mUsername = ANONYMOUS;

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
        }

//        mGoogleApiClient = new GoogleApiClient.Builder(this)
        //              .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
        //            .addApi(Auth.GOOGLE_SIGN_IN_API)
        //          .build();

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);

        /*mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseAdapter = new FirebaseRecyclerAdapter<Request, MessageViewHolder>(
                Request.class,
                R.layout.item_message,
                MessageViewHolder.class,
                mFirebaseDatabaseReference.child(MESSAGES_CHILD).orderByChild("status").equalTo("active")) {

            @Override
            protected Request parseSnapshot(DataSnapshot snapshot) {
                Request request = super.parseSnapshot(snapshot);
                if (request != null) {
                    request.setId(snapshot.getKey());
                }
                return request;
            }

            @Override
            protected void populateViewHolder(final MessageViewHolder viewHolder,
                                              Request request, int position) {
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                viewHolder.request = request;
                if (request.getStatus() != null && request.getStatus().equalsIgnoreCase("inactive")) {
                    return;
                }
                if (request.getDescription() != null) {
                    viewHolder.messageTextView.setText(request.getDescription().length() > 100 ? request.getDescription().substring(0, 100) + "..." : request.getDescription());
                    viewHolder.messageTextView.setVisibility(TextView.VISIBLE);
                }

                String username = request.getUsername();
                String contactPerson = request.getContactPerson();
                String messenger = contactPerson != null && !contactPerson.trim().equalsIgnoreCase(username.trim()) ? username + " / " + contactPerson : username;
                viewHolder.messengerTextView.setText(messenger);
                if (request.getCity() != null) {
                    viewHolder.cityTextView.setText(request.getCity());
                }
                if (request.getAddedTime() != null) {
                    viewHolder.addedTimeTextView.setText(android.text.format.DateFormat.format("yyyy-MM-dd HH:mm", request.getAddedTime()));
                }
                if (request.getAddress() == null) {
                    viewHolder.messengerImageView.setImageDrawable(ContextCompat.getDrawable(MainActivity.this,
                            R.drawable.ic_account_circle_black_36dp));
                } else {
                    Glide.with(MainActivity.this)
                            .load(request.getPhotoUrl())
                            .into(viewHolder.messengerImageView);
                }

                if (request.getDescription() != null) {
                    // write this message to the on-device index
                    FirebaseAppIndex.getInstance().update(getMessageIndexable(request));
                }

                // log a view action on it
                FirebaseUserActions.getInstance().end(getMessageViewAction(request));
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
*/
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        // mMessageRecyclerView.setAdapter(mFirebaseAdapter);

        // Initialize Firebase Measurement.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Initialize Firebase Remote Config.
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

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
        mFirebaseRemoteConfig.setConfigSettings(firebaseRemoteConfigSettings);
        mFirebaseRemoteConfig.setDefaults(defaultConfigMap);

        // Fetch remote config.
        fetchConfig();

        mAddCollectionCenterButton = (FloatingActionButton) findViewById(R.id.addCollectionCenterButton);
        mAddCollectionCenterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addCollectionCenterIntent = new Intent(MainActivity.this, AddRequestActivity.class);
                MainActivity.this.startActivity(addCollectionCenterIntent);
            }
        });

    }

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);

            } else {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            }
        } else {
            Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
        }
    }

    /*
        private Action getMessageViewAction(Request request) {
            return new Action.Builder(Action.Builder.VIEW_ACTION)
                    .setObject(request.getContactPerson(), MESSAGE_URL.concat(request.getId()))
                    .setMetadata(new Action.Metadata.Builder().setUpload(false))
                    .build();
        }

        private Indexable getMessageIndexable(Request request) {
            PersonBuilder sender = personBuilder()
                    .setIsSelf(mUsername.equals(request.getUsername()))
                    .setName(request.getUsername())
                    .setUrl(MESSAGE_URL.concat(request.getId() + "/sender"));

            PersonBuilder recipient = personBuilder()
                    .setName(mUsername)
                    .setUrl(MESSAGE_URL.concat(request.getId() + "/recipient"));

            return messageBuilder()
                    .setName(request.getDescription())
                    .setUrl(MESSAGE_URL.concat(request.getId()))
                    .setSender(sender)
                    .setRecipient(recipient)
                    .build();
        }
    */
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
/*
        switch (item.getItemId()) {
            case R.id.invite_menu:
                sendInvitation();
            case R.id.sign_out_menu:
                AppFirebase.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mFirebaseUser = null;
                mUsername = ANONYMOUS;
                startActivity(new Intent(this, SignInActivity.class));
                return true;
            case R.id.donation_center_menu:
                startActivity(new Intent(this, DonationCenterActivity.class));
                return true;
            case R.id.donation_menu:
                startActivity(new Intent(this, DonationActivity.class));
                return true;
            case R.id.gallery_menu:
                startActivity(new Intent(this, GalleryActivity.class));
                return true;
            case R.id.fresh_config_menu:
                fetchConfig();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
*/

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
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }
        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Make the fetched config available via FirebaseRemoteConfig get<type> calls.
                        mFirebaseRemoteConfig.activateFetched();
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
                // Use Firebase Measurement to log that invitation was sent.
                Bundle payload = new Bundle();
                payload.putString(FirebaseAnalytics.Param.VALUE, "inv_sent");

                // Check how many invitations were sent and log.
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                Log.d(TAG, "Invitations sent: " + ids.length);
            } else {
                // Use Firebase Measurement to log that invitation was not sent
                Bundle payload = new Bundle();
                payload.putString(FirebaseAnalytics.Param.VALUE, "inv_not_sent");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, payload);

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
