package com.epicodus.samuelgespass.remoteclassroomopentok.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.epicodus.samuelgespass.remoteclassroomopentok.Constants;
import com.epicodus.samuelgespass.remoteclassroomopentok.R;
import com.epicodus.samuelgespass.remoteclassroomopentok.util.OnSessionCreated;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.Connection;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Subscriber;
import com.opentok.android.OpentokError;
import android.support.annotation.NonNull;
import android.Manifest;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.opentok.exception.OpenTokException;

import org.json.JSONException;
import org.json.JSONObject;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.view.MotionEvent.INVALID_POINTER_ID;

public class MainActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener, View.OnClickListener, AdapterView.OnItemSelectedListener, Session.SignalListener, OnSessionCreated, View.OnTouchListener {

    private static String API_KEY = Constants.API_KEY;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERM = 124;
    String sessionId;
    String token;
    private boolean isConnected = false;
    private Session mSession;
    private FrameLayout mPublisherViewContainer;
    private FrameLayout mSubscriberViewContainer;
    private FrameLayout mFragmentContainer;
    private FrameLayout mVideoFrame;
    private Publisher mPublisher;
    private Subscriber mSubscriber;
    private ImageButton mFlipScreen;
    private Button mSignOut;
    private ImageButton mToggleAudio;
    private ImageButton mToggleVideo;
    private ImageButton mJoinSession;
    private ImageButton mDisconnect;
    private EditText mSessionIdText;
    private Spinner mSelectActivitySpinner;
    private Spinner mSelectWordListSpinner;
    private View mSeparator;
    private float mPosY;
    private float mLastTouchY;
    private int windowHeight;
    private int mActivePointerId = INVALID_POINTER_ID;

    public int getImage(String imageName) {

        int drawableResourceId = this.getResources().getIdentifier(imageName, "drawable", getApplicationContext().getPackageName());

        return drawableResourceId;
    }

    public void signOut() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void getWordLists() {
        Log.e(LOG_TAG, "getWordLists: " );
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        String userId = user.getUid();
        Log.e("userId: ", userId);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        Log.e(LOG_TAG, databaseReference.toString());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e(LOG_TAG, "onDataChange: ");
                int listNameArrayLength = (int) dataSnapshot.getChildrenCount();
                Log.e(LOG_TAG, "Arr length: " + Integer.toString(listNameArrayLength));
                String[] listNames = new String[listNameArrayLength];
                int listNameIndex = 0;

                for (DataSnapshot keySnapshot : dataSnapshot.getChildren()) {
                    Log.e("Snapshot: ", "snapped");
                    String listName = keySnapshot.getKey();
                    listNames[listNameIndex] = listName;
//                    int listLength = (int) keySnapshot.child("urlList").getChildrenCount();
//                    String[] urlList = new String[listLength];
//                    String[] wordList = new String[listLength];
//
//                    for (DataSnapshot snapshot: keySnapshot.getChildren()) {
//                        if (snapshot.getKey() == "urlList") {
//                            int index = 0;
//                            for (DataSnapshot currentUrlList: keySnapshot.getChildren()) {
//                                Log.e(LOG_TAG, (String) currentUrlList.getValue());
//                                urlList[index] = (String) currentUrlList.getValue();
//                                index++;
//                            }
//                        }
//                        if (snapshot.getKey() == "wordList") {
//                            int index = 0;
//                            for (DataSnapshot currentWordList : keySnapshot.getChildren()) {
//                                Log.e(LOG_TAG, (String) currentWordList.getValue());
//                                wordList[index] = (String) currentWordList.getValue();
//                                index++;
//                            }
//                        }
//                    }
                    listNameIndex++;
                    Log.e(LOG_TAG, listNames.toString());
                }
                ArrayAdapter<String> adapter =  new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, listNames);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                mSelectWordListSpinner.setAdapter(adapter);
                if (listNameArrayLength > 0) {
                    ArrayAdapter<CharSequence> selectActivityAdapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.select_activity_array, android.R.layout.simple_spinner_dropdown_item);
                    mSelectActivitySpinner.setAdapter(selectActivityAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(LOG_TAG, databaseError.getMessage());
            }
        });
    }

    public void getSessionId(final String name) {
        final String oldId = sessionId;
        DatabaseReference sessionIds = FirebaseDatabase.getInstance().getReference().child("sessionIds");
        sessionIds.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String val = (String) snapshot.child("sessionName").getValue();
                    if (val.equals(name)) {
                        sessionId = (String) snapshot.child("sessionId").getValue();
                        try {
                            connectToSession(sessionId);
                        } catch (OpenTokException ex) {
                            Log.e(LOG_TAG, ex.getMessage());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(LOG_TAG, databaseError.getMessage());
            }
        });
    }

    public void connectToSession(String arg) throws OpenTokException {
        final String sessionId = arg;
        RequestQueue reqQueue = Volley.newRequestQueue(this);
        String url = "https://server-glcucdwubw.now.sh/token/" + sessionId;
        Log.e(LOG_TAG, url);
        reqQueue.add(new JsonObjectRequest(Request.Method.GET,
                url,
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    token = response.getString("token");

                    Log.i(LOG_TAG, "TOKEN: " + token);

                    mSession = new Session.Builder(MainActivity.this, API_KEY, sessionId).build();
                    mSession.setSessionListener(MainActivity.this);
                    mSession.connect(token);
                    isConnected = true;

                } catch (JSONException error) {
                    Log.e(LOG_TAG, "Web Service error2: " + error.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG, "Web Service error1: " + error.getMessage());
            }
        }));
    }

//    public void fetchSessionConnectionData() {
//        RequestQueue reqQueue = Volley.newRequestQueue(this);
//        reqQueue.add(new JsonObjectRequest(Request.Method.GET,
//                "https://server-kzjldzvqns.now.sh" + "/session",
//                null, new Response.Listener<JSONObject>() {
//
//            @Override
//            public void onResponse(JSONObject response) {
//                try {
//                    sessionId = response.getString("sessionId");
//                    mSessionIdText.setText(sessionId);
//
//                    try {
//                        connectToSession(sessionId);
//                    } catch (OpenTokException ex) {
//                        Log.e(LOG_TAG, "OpenTokException" + ex.getMessage());
//                    }
//                } catch (JSONException error) {
//                    Log.e(LOG_TAG, "Web Service error: " + error.getMessage());
//                }
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.e(LOG_TAG, "Web Service error: " + error.getMessage());
//            }
//        }));
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        windowHeight = displayMetrics.heightPixels;


        mSignOut = (Button) findViewById(R.id.signOut);
        mSignOut.setOnClickListener(this);
        requestPermissions();
    }

    @Override
    public void onSessionCreated(Session session) {
        Log.e(LOG_TAG, "onSessionCreated listener");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions() {

        String[] perms = { Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_NETWORK_STATE };
        if (EasyPermissions.hasPermissions(this, perms)) {
            mPublisherViewContainer = (FrameLayout)findViewById(R.id.publisher_container);
            mSubscriberViewContainer = (FrameLayout)findViewById(R.id.subscriber_container);
            mJoinSession = (ImageButton) findViewById(R.id.join_session);
            mFlipScreen = (ImageButton) findViewById(R.id.button_toggle_screen);
            mDisconnect = (ImageButton) findViewById(R.id.disconnect);
            mFragmentContainer = (FrameLayout) findViewById(R.id.fragmentContainer);
            mVideoFrame = (FrameLayout) findViewById(R.id.videoFrame);
            mSessionIdText = (EditText) findViewById(R.id.session_id_text);
            mSeparator = (View) findViewById(R.id.separator);
            mToggleAudio = (ImageButton) findViewById(R.id.toggle_audio);
            mToggleVideo = (ImageButton) findViewById(R.id.toggle_video);
            mSelectWordListSpinner = (Spinner) findViewById(R.id.word_list_select_spinner);
            mSelectActivitySpinner = (Spinner) findViewById(R.id.activity_select_spinner);
            getWordLists();

            ViewGroup.LayoutParams fragmentParams = mFragmentContainer.getLayoutParams();
            ViewGroup.LayoutParams videoParams = mVideoFrame.getLayoutParams();
            fragmentParams.height = windowHeight / 2;
            videoParams.height = windowHeight / 2;

            mFragmentContainer.setLayoutParams(fragmentParams);
            mVideoFrame.setLayoutParams(videoParams);

            Glide.with(this)
                    .load(getImage("flip"))
                    .apply(new RequestOptions().override(50, 50))
                    .into(mFlipScreen);

            Glide.with(this)
                    .load(getImage("disconnect"))
                    .apply(new RequestOptions().override(50, 50))
                    .into(mDisconnect);

            Glide.with(this)
                    .load(getImage("connect"))
                    .apply(new RequestOptions().override(50, 50))
                    .into(mJoinSession);

            Glide.with(this)
                    .load(getImage("audioon"))
                    .apply(new RequestOptions().override(50, 50))
                    .into(mToggleAudio);

            Glide.with(this)
                    .load(getImage("videoon"))
                    .apply(new RequestOptions().override(50, 50))
                    .into(mToggleVideo);


            mFlipScreen.setOnClickListener(this);
            mJoinSession.setOnClickListener(this);
            mDisconnect.setOnClickListener(this);
            mSeparator.setOnTouchListener(this);
            mSelectWordListSpinner.setOnItemSelectedListener(this);
            mSelectActivitySpinner.setOnItemSelectedListener(this);

            Log.i(LOG_TAG, "requestPermissions success");


        } else {
            EasyPermissions.requestPermissions(this, "This app needs access to your camera and mic to make video calls", RC_VIDEO_APP_PERM, perms);
            Log.e(LOG_TAG, "requestPermissions failed");

        }
    }

    private void requestParentDisallowInterceptTouchEvent(boolean disallowIntercept) {
        final ViewParent parent = (ViewParent) getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    @Override
    public void onConnected(Session session) {
        Log.e(LOG_TAG, "Session Connected");
        isConnected = true;
        mSession = session;
        mSession.setSignalListener(this);

        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(this);

        mPublisherViewContainer.addView(mPublisher.getView());
        mSession.publish(mPublisher);
        mToggleVideo.setOnClickListener(this);
        mToggleAudio.setOnClickListener(this);
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOG_TAG, "Session Disconnected");
        isConnected = false;
        mPublisherViewContainer.removeAllViews();
        mToggleVideo.setOnClickListener(null);
        mToggleAudio.setOnClickListener(null);
        Glide.with(this)
                .load(getImage("audioon"))
                .apply(new RequestOptions().override(50, 50))
                .into(mToggleAudio);

        Glide.with(this)
                .load(getImage("videoon"))
                .apply(new RequestOptions().override(50, 50))
                .into(mToggleVideo);
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Received");
        if (mSubscriber == null) {
            mSubscriber = new Subscriber.Builder(this, stream).build();
            mSession.subscribe(mSubscriber);
            mSubscriberViewContainer.addView(mSubscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG, "Stream Dropped");

        if (mSubscriber != null) {
            mSubscriber = null;
            mSubscriberViewContainer.removeAllViews();
        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {
        Log.i(LOG_TAG, "Publisher onStreamCreated");
    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {
        Log.i(LOG_TAG, "Publisher onStreamDestroyed");
    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {
        Log.e(LOG_TAG, "Publisher error: " + opentokError.getMessage());
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.e(LOG_TAG, "Session error: " + opentokError.getMessage());
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (view == mSeparator) {
            Log.e(LOG_TAG, "onTouch: ");
            final int action = MotionEventCompat.getActionMasked(event);
            switch (action) {
                case MotionEvent.ACTION_DOWN: {
                    final int pointerIndex = MotionEventCompat.getActionIndex(event);
                    final float y = MotionEventCompat.getY(event, pointerIndex);

                    // Remember where we started (for dragging)
                    mLastTouchY = y;
                    // Save the ID of this pointer (for dragging)
                    mActivePointerId = MotionEventCompat.getPointerId(event, 0);
                    break;
                }

                case MotionEvent.ACTION_MOVE: {
                    Log.e(LOG_TAG, "ACTION_MOVE");

                    // Find the index of the active pointer and fetch its position
                    final int pointerIndex =
                            MotionEventCompat.findPointerIndex(event, mActivePointerId);

                    final float y = MotionEventCompat.getY(event, pointerIndex);

                    // Calculate the distance moved
                    final float dy = y - mLastTouchY;

                    mPosY += dy;

                    int yInt = Math.round(dy);

                    ViewGroup.LayoutParams fragmentParams = mFragmentContainer.getLayoutParams();
                    ViewGroup.LayoutParams videoParams = mVideoFrame.getLayoutParams();
                    fragmentParams.height -= yInt;
                    videoParams.height += yInt;

                    mFragmentContainer.setLayoutParams(fragmentParams);
                    mVideoFrame.setLayoutParams(videoParams);
//                    invalidate();

                    // Remember this touch position for the next move event
                    mLastTouchY = y;

                    break;
                }

                case MotionEvent.ACTION_UP: {
                    mActivePointerId = INVALID_POINTER_ID;
                    break;
                }

                case MotionEvent.ACTION_CANCEL: {
                    Log.e(LOG_TAG, "ACTION_CANCEL");
                    mActivePointerId = INVALID_POINTER_ID;
                    break;
                }

                case MotionEvent.ACTION_POINTER_UP: {
                    Log.e(LOG_TAG, "ACTION_POINTER_UP");

                    final int pointerIndex = MotionEventCompat.getActionIndex(event);
                    final int pointerId = MotionEventCompat.getPointerId(event, pointerIndex);

                    if (pointerId == mActivePointerId) {
                        // This was our active pointer going up. Choose a new
                        // active pointer and adjust accordingly.
                        final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                        mLastTouchY = MotionEventCompat.getY(event, newPointerIndex);
                        mActivePointerId = MotionEventCompat.getPointerId(event, newPointerIndex);
                    }
                    break;
                }
            }
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        if (view == mToggleAudio) {
            Stream stream = mPublisher.getStream();
            if (stream.hasAudio() == true) {
                mPublisher.setPublishAudio(false);
                Glide.with(this)
                        .load(getImage("audiooff"))
                        .apply(new RequestOptions().override(50, 50))
                        .into(mToggleAudio);
            } else {
                mPublisher.setPublishAudio(true);
                Glide.with(this)
                        .load(getImage("audioon"))
                        .apply(new RequestOptions().override(50, 50))
                        .into(mToggleAudio);
            }
        }

        if (view == mToggleVideo) {
            Stream stream = mPublisher.getStream();
            if (stream.hasVideo() == true) {
                mPublisher.setPublishVideo(false);
                mPublisherViewContainer.removeAllViews();
                Glide.with(this)
                        .load(getImage("videooff"))
                        .apply(new RequestOptions().override(50, 50))
                        .into(mToggleVideo);
            } else {
                mPublisher.setPublishVideo(true);
                mPublisherViewContainer.addView(mPublisher.getView());
                Glide.with(this)
                        .load(getImage("videoon"))
                        .apply(new RequestOptions().override(50, 50))
                        .into(mToggleVideo);

            }
        }

        if (view == mJoinSession) {
            mJoinSession.setOnClickListener(null);
            String name = mSessionIdText.getText().toString();
            getSessionId(name);
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        if (view == mDisconnect) {
            mJoinSession.setOnClickListener(this);
            if (isConnected) {
                mSession.disconnect();
            }
        }

        if (view == mFlipScreen) {
            mPublisher.swapCamera();
            Log.e("thing", "onClick: ");
        }

        if (view == mSignOut) {
            signOut();
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        Log.e(LOG_TAG, "Item Selected");
        if (isConnected) {
            Log.e(LOG_TAG, "Connected");
            if (parent.getItemAtPosition(pos).equals("Memory Game")) {
                Log.e(LOG_TAG, "Memory Game Selected");
                mSession.sendSignal("Activity", "Memory Game");
                Log.e(LOG_TAG, "Signal sent");
            }
            if (parent.getItemAtPosition(pos).equals("No Activity")) {
                mSession.sendSignal("Activity", "None");
            }
            if (parent.getItemAtPosition(pos).equals("Random Words")) {
                mSession.sendSignal("Activity", "Random Words");
            }
            if (parent.getItemAtPosition(pos).equals("Random Pictures")) {
                mSession.sendSignal("Activity", "Random Pictures");
            }
        }
    }

    @Override
    public void onSignalReceived(Session session, String type, String data, Connection connection) {
        Log.e("signal recieved", "onSignalReceived: ");
        if (type != null && type.equals("Activity")) {
            if (data.equals("Memory Game")) {
                String wordListName = (String) mSelectWordListSpinner.getSelectedItem();
                Log.e("List Name", wordListName);
                MemoryGameFragment memoryGameFragment = MemoryGameFragment.newInstance(wordListName, sessionId);
                Log.e("Selected", "onItemSelected: ");
                android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragmentContainer, memoryGameFragment);
                fragmentTransaction.commit();
            }

            if (data.equals("None")) {
                mFragmentContainer.removeAllViews();
            }

            if (data.equals("Random Words")) {
                String wordListName = (String) mSelectWordListSpinner.getSelectedItem();
                RandomWordFragment randomWordFragment = RandomWordFragment.newInstance(wordListName, sessionId);
                android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragmentContainer, randomWordFragment);
                fragmentTransaction.commit();
            }

            if (data.equals("Random Pictures")) {
                String wordListName = (String) mSelectWordListSpinner.getSelectedItem();
                RandomPictureFragment randomPictureFragment = RandomPictureFragment.newInstance(wordListName, sessionId);
                android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragmentContainer, randomPictureFragment);
                fragmentTransaction.commit();
            }

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}