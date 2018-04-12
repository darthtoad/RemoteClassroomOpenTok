package com.epicodus.samuelgespass.remoteclassroomopentok.ui;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.epicodus.samuelgespass.remoteclassroomopentok.Constants;
import com.epicodus.samuelgespass.remoteclassroomopentok.R;
import com.opentok.android.Connection;
import com.opentok.android.OpentokError;
import com.opentok.android.Session;
import com.opentok.android.Stream;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 */
public class MemoryGameFragment extends Fragment implements View.OnClickListener, Session.SessionListener, Session.SignalListener {
    private TextView textViewRunning;
    private TextView textViewSleeping;
    private TextView textViewEating;
    private ImageButton imageButtonRunning;
    private ImageButton imageButtonSleeping;
    private ImageButton imageButtonEating;
    private boolean isImageButtonRunningFlipped = false;
    private boolean isImageButtonSleepingFlipped = false;
    private boolean isImageButtonEatingFlipped = false;
    private boolean turnTaken = false;
    private boolean isRunningMatched = false;
    private boolean isSleepingMatched = false;
    private boolean isEatingMatched = false;
    private Session mSession;
    private static String API_KEY = Constants.API_KEY;
    private static String SESSION_ID = Constants.SESSION_ID;
    private static String TOKEN = Constants.TOKEN;


    public MemoryGameFragment() {
        // Required empty public constructor
    }

    public static MemoryGameFragment newInstance() {
        MemoryGameFragment memoryGameFragment = new MemoryGameFragment();
        Bundle args = new Bundle();
        memoryGameFragment.setArguments(args);
        return memoryGameFragment;
    }

    public void fetchSessionConnectionData() {
        RequestQueue reqQueue = Volley.newRequestQueue(getContext());
        reqQueue.add(new JsonObjectRequest(Request.Method.GET,
                "https://remote-classroom-open-tok.herokuapp.com" + "/session",
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    API_KEY = response.getString("apiKey");
                    SESSION_ID = response.getString("sessionId");
                    TOKEN = response.getString("token");

                    mSession = new Session.Builder(getContext(), API_KEY, SESSION_ID).build();
                    mSession.setSessionListener(MemoryGameFragment.this);
                    mSession.setSignalListener(MemoryGameFragment.this);
                    mSession.connect(TOKEN);

                } catch (JSONException error) {
                    Log.e("ERROR", "Web Service error: " + error.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("ERROR", "Web Service error: " + error.getMessage());
            }
        }));
    }

    @Override
    public void onConnected(Session session) {

    }


    @Override
    public void onDisconnected(Session session) {

    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {

    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {

    }

    @Override
    public void onError(Session session, OpentokError opentokError) {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            fetchSessionConnectionData();
            Log.e("SUCCESS", "Attached");
        } catch (ClassCastException e) {
            Log.e("NOOO", "onAttach: ");
            throw new ClassCastException("NOOOO " + e.getMessage());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_memory_game, container, false);
        textViewEating = (TextView) view.findViewById(R.id.textView_eating);
        textViewRunning = (TextView) view.findViewById(R.id.textView_running);
        textViewSleeping = (TextView) view.findViewById(R.id.textView_sleeping);

        imageButtonEating = (ImageButton) view.findViewById(R.id.imageButton_eating);
        imageButtonRunning = (ImageButton) view.findViewById(R.id.imageButton_running);
        imageButtonSleeping = (ImageButton) view.findViewById(R.id.imageButton_sleeping);

        textViewEating.setOnClickListener(this);
        textViewSleeping.setOnClickListener(this);
        textViewRunning.setOnClickListener(this);

        imageButtonSleeping.setOnClickListener(this);
        imageButtonRunning.setOnClickListener(this);
        imageButtonEating.setOnClickListener(this);

        return view;
    }

    private void resetViews() {
        if (!isRunningMatched) {
            textViewRunning.setText("");
            imageButtonRunning.setImageResource(R.drawable.card);
        }

        if (!isEatingMatched) {
            textViewEating.setText("");
            imageButtonEating.setImageResource(R.drawable.card);
        }

        if (!isSleepingMatched) {
            textViewSleeping.setText("");
            imageButtonSleeping.setImageResource(R.drawable.card);
        }
    }

    @Override
    public void onSignalReceived(Session session, String type, String data, Connection connection) {

        if (!turnTaken) {

            if (type.equals("word")) {

                if (data.equals("eating")) {
                    turnTaken = true;
                    textViewEating.setText("eating");
                }

                if (data.equals("running")) {
                    turnTaken = true;
                    textViewRunning.setText("running");
                }

                if (data.equals("sleeping")) {
                    turnTaken = true;
                    textViewSleeping.setText("sleeping");
                }
            }

            if (type.equals("image")) {

                if (data.equals("eating")) {
                    turnTaken = true;
                    isImageButtonEatingFlipped = true;
                    imageButtonEating.setImageResource(R.drawable.eating);
                }

                if (data.equals("running")) {
                    turnTaken = true;
                    isImageButtonRunningFlipped = true;
                    imageButtonRunning.setImageResource(R.drawable.running);
                }

                if (data.equals("sleeping")) {
                    turnTaken = true;
                    isImageButtonSleepingFlipped = true;
                    imageButtonSleeping.setImageResource(R.drawable.sleeping);
                }

            }
        }

        if (turnTaken) {
            if (type.equals("word")) {

                if (data.equals("eating")) {
                    if (!textViewEating.getText().equals("eating")) {
                        turnTaken = false;
                        if (isImageButtonEatingFlipped) {
                            Toast.makeText(getContext(), "You found a match!", Toast.LENGTH_LONG).show();
                            textViewEating.setClickable(false);
                            imageButtonEating.setClickable(false);
                            textViewEating.setText("eating");
                            isEatingMatched = true;
                        } else {
                            Toast.makeText(getContext(), "Try again", Toast.LENGTH_SHORT).show();
                            resetViews();
                        }
                    }
                }

                if (data.equals("running")) {
                    if (!textViewRunning.getText().equals("running")) {
                        turnTaken = false;
                        if (isImageButtonRunningFlipped) {
                            Toast.makeText(getContext(), "You found a match!", Toast.LENGTH_LONG).show();
                            textViewRunning.setClickable(false);
                            imageButtonRunning.setClickable(false);
                            textViewRunning.setText("running");
                            isRunningMatched = true;
                        } else {
                            Toast.makeText(getContext(), "Try again", Toast.LENGTH_SHORT).show();
                            resetViews();
                        }
                    }
                }

                if (data.equals("sleeping")) {
                    if (!textViewSleeping.getText().equals("sleeping")) {
                        turnTaken = false;
                        if (isImageButtonSleepingFlipped) {
                            Toast.makeText(getContext(), "You found a match!", Toast.LENGTH_LONG).show();
                            textViewSleeping.setClickable(false);
                            imageButtonSleeping.setClickable(false);
                            textViewSleeping.setText("sleeping");
                            isSleepingMatched = true;
                        } else {
                            Toast.makeText(getContext(), "Try again", Toast.LENGTH_SHORT).show();
                            resetViews();
                        }
                    }
                }

            }

            if (type.equals("image")) {

                if (data.equals("eating")) {
                    if (!isImageButtonEatingFlipped) {
                        turnTaken = false;
                        if (textViewEating.getText().equals("eating")) {
                            Toast.makeText(getContext(), "You found a match!", Toast.LENGTH_LONG).show();
                            textViewEating.setClickable(false);
                            imageButtonEating.setClickable(false);
                            imageButtonEating.setImageResource(R.drawable.eating);
                            isEatingMatched = true;
                        } else {
                            Toast.makeText(getContext(), "Try again", Toast.LENGTH_SHORT).show();
                            resetViews();
                        }
                    }
                }

                if (data.equals("running")) {
                    if (!isImageButtonRunningFlipped) {
                        turnTaken = false;
                        if (textViewRunning.getText().equals("running")) {
                            Toast.makeText(getContext(), "You found a match!", Toast.LENGTH_LONG).show();
                            textViewRunning.setClickable(false);
                            imageButtonRunning.setClickable(false);
                            imageButtonRunning.setImageResource(R.drawable.running);
                            isRunningMatched = true;
                        } else {
                            Toast.makeText(getContext(), "Try again", Toast.LENGTH_SHORT).show();
                            resetViews();
                        }

                    }
                }

                if (data.equals("sleeping")) {
                    if (!isImageButtonSleepingFlipped) {
                        turnTaken = false;
                        if (textViewSleeping.getText().equals("sleeping")) {
                            Toast.makeText(getContext(), "You found a match!", Toast.LENGTH_LONG).show();
                            textViewSleeping.setClickable(false);
                            imageButtonSleeping.setClickable(false);
                            imageButtonSleeping.setImageResource(R.drawable.sleeping);
                            isSleepingMatched = true;
                        } else {
                            Toast.makeText(getContext(), "Try again", Toast.LENGTH_SHORT).show();
                            resetViews();
                        }
                    }
                }

            }
        }
    }

    @Override
    public void onClick(View view) {

        if (view == textViewEating) {
            mSession.sendSignal("word", "eating");
        }

        if (view == textViewRunning) {
            mSession.sendSignal("word", "running");
        }

        if (view == textViewSleeping) {
            mSession.sendSignal("word", "sleeping");
        }

        if (view == imageButtonEating) {
            mSession.sendSignal("image", "eating");
        }

        if (view == imageButtonRunning) {
            mSession.sendSignal("image", "running");
        }

        if (view == imageButtonSleeping) {
            mSession.sendSignal("image", "sleeping");
        }

    }

}
