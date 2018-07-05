package com.epicodus.samuelgespass.remoteclassroomopentok.ui;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.epicodus.samuelgespass.remoteclassroomopentok.Constants;
import com.epicodus.samuelgespass.remoteclassroomopentok.R;
import com.epicodus.samuelgespass.remoteclassroomopentok.util.OnSessionCreated;
import com.opentok.OpenTok;
import com.opentok.Role;
import com.opentok.TokenOptions;
import com.opentok.android.Connection;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Subscriber;
import com.opentok.android.OpentokError;
import android.support.annotation.NonNull;
import android.Manifest;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
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

public class MainActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener, View.OnClickListener, AdapterView.OnItemSelectedListener, Session.SignalListener, OnSessionCreated {

    private static String API_KEY = Constants.API_KEY;
    private static String API_SECRET = Constants.API_SECRET;
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int RC_SETTINGS_SCREEN_PERM = 123;
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
    private Button mFlipScreen;
    private Button mButtonLargeFragment;
    private Button mButtonSmallFragment;
    private Button mCreateSession;
    private Button mJoinSession;
    private Button mDisconnect;
    private EditText mSessionIdText;
    private Spinner mSelectActivitySpinner;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public void connectToSession(String arg) throws OpenTokException {
        final String sessionId = arg;
        RequestQueue reqQueue = Volley.newRequestQueue(this);
        String url = "http://192.168.1.64:3000/token/" + sessionId;
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

    public void fetchSessionConnectionData() {
        RequestQueue reqQueue = Volley.newRequestQueue(this);
        reqQueue.add(new JsonObjectRequest(Request.Method.GET,
                "https://remote-classroom-open-tok.herokuapp.com" + "/session",
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    API_KEY = response.getString("apiKey");
                    sessionId = response.getString("sessionId");
                    token = response.getString("token");

                    Log.i(LOG_TAG, "API_KEY: " + API_KEY);
                    Log.i(LOG_TAG, "SESSION_ID: " + sessionId);
                    Log.i(LOG_TAG, "TOKEN: " + token);

                    mSession = new Session.Builder(MainActivity.this, API_KEY, sessionId).build();
                    mSession.setSessionListener(MainActivity.this);
                    mSession.connect(token);

                } catch (JSONException error) {
                    Log.e(LOG_TAG, "Web Service error: " + error.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG, "Web Service error: " + error.getMessage());
            }
        }));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSelectActivitySpinner = (Spinner) findViewById(R.id.activity_select_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.select_activity_array, android.R.layout.simple_spinner_dropdown_item);
        mSelectActivitySpinner.setAdapter(adapter);
        mSelectActivitySpinner.setOnItemSelectedListener(this);

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
            mCreateSession = (Button) findViewById(R.id.create_session);
            mJoinSession = (Button) findViewById(R.id.join_session);
            mFlipScreen = (Button) findViewById(R.id.button_toggle_screen);
            mButtonLargeFragment = (Button) findViewById(R.id.button_large_fragment);
            mButtonSmallFragment = (Button) findViewById(R.id.button_small_fragment);
            mDisconnect = (Button) findViewById(R.id.disconnect);
            mFragmentContainer = (FrameLayout) findViewById(R.id.fragmentContainer);
            mVideoFrame = (FrameLayout) findViewById(R.id.videoFrame);
            mSessionIdText = (EditText) findViewById(R.id.session_id_text);

            mFlipScreen.setOnClickListener(this);
            mButtonSmallFragment.setOnClickListener(this);
            mButtonLargeFragment.setOnClickListener(this);
            mCreateSession.setOnClickListener(this);
            mJoinSession.setOnClickListener(this);
            mDisconnect.setOnClickListener(this);


            Log.i(LOG_TAG, "requestPermissions success");


        } else {
            EasyPermissions.requestPermissions(this, "This app needs access to your camera and mic to make video calls", RC_VIDEO_APP_PERM, perms);
            Log.e(LOG_TAG, "requestPermissions failed");

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
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOG_TAG, "Session Disconnected");
        isConnected = false;
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
    public void onClick(View view) {
        if (view == mCreateSession) {
            fetchSessionConnectionData();
            Log.i(LOG_TAG, "Session ID: " + sessionId);
            mSession = new Session.Builder(this, API_KEY, sessionId).build();
            mSession.setSessionListener(this);
            mSession.connect(token);
        }

        if (view == mJoinSession) {
            String id = mSessionIdText.getText().toString();
            try {
                connectToSession(id);
            } catch (OpenTokException ex) {
                Log.e(LOG_TAG, ex.toString());
            }
        }

        if (view == mDisconnect) {
            if (isConnected) {
                mSession.disconnect();
            }
        }

        if (view == mFlipScreen) {
            mPublisher.swapCamera();
            Log.e("thing", "onClick: ");
        }

        if (view == mButtonLargeFragment) {
            LinearLayout.LayoutParams paramsFragment = (LinearLayout.LayoutParams) mFragmentContainer.getLayoutParams();
            LinearLayout.LayoutParams paramsMain = (LinearLayout.LayoutParams) mVideoFrame.getLayoutParams();
            if (paramsMain.weight > 1) {
                paramsFragment.weight++;
                paramsMain.weight--;
            }
            mFragmentContainer.setLayoutParams(paramsFragment);
            mVideoFrame.setLayoutParams(paramsMain);
            Log.e("thing", "onClick: ");
        }

        if (view == mButtonSmallFragment) {
            LinearLayout.LayoutParams paramsFragment = (LinearLayout.LayoutParams) mFragmentContainer.getLayoutParams();
            LinearLayout.LayoutParams paramsMain = (LinearLayout.LayoutParams) mVideoFrame.getLayoutParams();
            if (paramsFragment.weight > 1) {
                paramsFragment.weight--;
                paramsMain.weight++;
            }
            mFragmentContainer.setLayoutParams(paramsFragment);
            mVideoFrame.setLayoutParams(paramsMain);
            Log.e("thing", "onClick: ");
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
        } else {
            Toast.makeText(getApplicationContext(), "Please connect to a session", Toast.LENGTH_LONG);
        }
    }

    @Override
    public void onSignalReceived(Session session, String type, String data, Connection connection) {
        Log.e("signal recieved", "onSignalReceived: ");
        if (type != null && type.equals("Activity")) {
            if (data.equals("Memory Game")) {
                MemoryGameFragment memoryGameFragment = MemoryGameFragment.newInstance();
                Log.e("Selected", "onItemSelected: ");
                android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragmentContainer, memoryGameFragment);
                fragmentTransaction.commit();
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}