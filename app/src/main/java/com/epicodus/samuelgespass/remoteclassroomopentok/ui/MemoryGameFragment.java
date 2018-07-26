package com.epicodus.samuelgespass.remoteclassroomopentok.ui;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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
import com.opentok.android.OpentokError;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.exception.OpenTokException;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.epicodus.samuelgespass.remoteclassroomopentok.Constants.API_KEY;

/**
 * A simple {@link Fragment} subclass.
 */
public class MemoryGameFragment extends Fragment implements Session.SessionListener, Session.SignalListener {
    private boolean turnTaken = false;
    private Integer keyFlipped;
    String wordListName;
    String sessionId;
    String token;
    OnSessionCreated mOnSessionCreated;
    ArrayList<Integer> foundMatches = new ArrayList<>();
    private Session mSession;
    private TextView title;
    private LinearLayout wordListView;
    private LinearLayout imageListView;
    int arrLength;
    HashMap<Integer, String> wordMap = new HashMap<>();
    HashMap<Integer, String> urlMap = new HashMap<>();
    String userId;
    String originalUserId;

    public void textFlippedTurnNotTaken(TextView newWordTextView, final Map.Entry<Integer, String> entry) {
        newWordTextView.setText(entry.getValue());
        keyFlipped = entry.getKey();
        for (int i = 0; i < arrLength; i++) {
            TextView currentTextView = getView().findViewWithTag("Text " + Integer.toString(i));
            currentTextView.setClickable(false);
        }
        turnTaken = true;
    }

    public void textFlippedNoMatch() {
        resetClickables();
        Toast.makeText(getContext(), "Try again!", Toast.LENGTH_LONG).show();
        turnTaken = false;
    }

    public void textFlippedMatch(final Map.Entry<Integer, String> entry, TextView newWordTextView) {
        foundMatches.add(entry.getKey());
        newWordTextView.setText(entry.getValue());
        Toast.makeText(getContext(), "You found a match!", Toast.LENGTH_LONG).show();
        resetClickables();
        turnTaken = false;
    }

    public void imageFlippedTurnNotTaken(final Map.Entry<Integer, String> entry, ImageButton newImage) {
        Picasso.get()
                .load(entry.getValue())
                .resize(275, 183)
                .centerInside()
                .into(newImage);
        for (int i = 0; i < arrLength; i++) {
            ImageButton currentImage = getView().findViewWithTag("Image " + Integer.toString(i));
            currentImage.setClickable(false);
        }
        keyFlipped = entry.getKey();
        turnTaken = true;
    }

    public void imageFlippedNoMatch() {
        resetClickables();
        Toast.makeText(getContext(), "Try again!", Toast.LENGTH_LONG).show();
        turnTaken = false;
    }

    public void imageFlippedMatch(final Map.Entry<Integer, String> entry, ImageButton newImage) {
        foundMatches.add(entry.getKey());
        Picasso.get()
                .load(entry.getValue())
                .resize(275, 183)
                .centerInside()
                .into(newImage);
        Toast.makeText(getContext(), "You found a match!", Toast.LENGTH_LONG).show();
        resetClickables();
        turnTaken = false;
    }

    public void connectToSession(String arg) throws OpenTokException {
        final String sessionId = arg;
        RequestQueue reqQueue = Volley.newRequestQueue(getContext());
        String url = "https://server-glcucdwubw.now.sh/token/" + sessionId;
        reqQueue.add(new JsonObjectRequest(Request.Method.GET,
                url,
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    token = response.getString("token");

                    mSession = new Session.Builder(getContext(), API_KEY, sessionId).build();
                    mSession.setSessionListener(MemoryGameFragment.this);
                    mSession.connect(token);

                } catch (JSONException error) {
                    Log.e("error", error.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error", error.getMessage());
            }
        }));
    }

    public void resetClickables() {
        for (int i = 0; i < arrLength; i++) {
            TextView currentTextView = getView().findViewWithTag("Text " + Integer.toString(i));
            ImageButton currentImage = getView().findViewWithTag("Image " + Integer.toString(i));
            if (foundMatches.contains(i)) {
                currentTextView.setClickable(false);
                currentImage.setClickable(false);
            } else {
                currentTextView.setClickable(true);
                currentImage.setClickable(true);
                currentTextView.setText("Click to See Word");
                Picasso.get()
                        .load(getImage("card"))
                        .into(currentImage);
            }
        }
    }

    public void getLists() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        try {
            if (FirebaseDatabase.getInstance().getReference().child("users").child(originalUserId).child(wordListName) != null) {
                userId = user.getUid();
            }
        } catch (NullPointerException ex) {
            Log.e("Random Word Fragment", ex.getMessage());
        }

        try {
            DatabaseReference databaseReferenceWords = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child(wordListName).child("wordList");
            final DatabaseReference databaseReferenceUrls = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child(wordListName).child("urlList");

            mSession.sendSignal("userId", userId);

            ValueEventListener wordsEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.e("Start", "onDataChange1: ");
                    arrLength = (int) dataSnapshot.getChildrenCount();
                    final String[] wordArr = new String[arrLength];
                    int index = 0;
                    for (DataSnapshot wordSnapshot : dataSnapshot.getChildren()) {
                        String word = (String) wordSnapshot.getValue();
                        wordArr[index] = word;
                        index++;
                    }

                    ValueEventListener urlEventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.e("Start", "onDataChange2: ");
//                        final int arrLength = (int) dataSnapshot.getChildrenCount();
                            String[] urlArr = new String[arrLength];
                            Integer index = 0;
                            for (DataSnapshot urlSnapshot : dataSnapshot.getChildren()) {
                                String url = (String) urlSnapshot.getValue();
                                urlArr[index] = url;
                                index++;
                            }
                            Log.e("Success", "onDataChange2: ");


                            index = 0;

                            for (String word : wordArr) {
                                wordMap.put(index, word);
                                index++;
                            }

                            //randomize hashmap and add views

                            Object[] wordKeys = wordMap.keySet().toArray();
                            Object key = wordKeys[new Random().nextInt(wordKeys.length)];

                            List<Map.Entry<Integer, String>> wordList = new ArrayList<Map.Entry<Integer, String>>(wordMap.entrySet());
                            Collections.shuffle(wordList);

                            for (final Map.Entry<Integer, String> entry : wordList) {
                                final TextView newWordTextView = new TextView(getActivity().getApplicationContext());
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                params.setMargins(10, 0, 0, 20);
                                newWordTextView.setLayoutParams(params);
                                newWordTextView.setTextSize(20);
                                newWordTextView.setBottom(5);
                                newWordTextView.setTextColor(Color.parseColor("#ff0000"));
                                newWordTextView.setBackgroundColor(Color.parseColor("#000000"));
                                newWordTextView.setText("Click To See Word");
                                wordListView.addView(newWordTextView);
                                newWordTextView.setTag("Text " + Integer.toString(entry.getKey()));
                                newWordTextView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (!turnTaken) {
                                            mSession.sendSignal(Integer.toString(entry.getKey()), "textFlippedTurnNotTaken");
                                            //TextView clicked, turn not taken
//                                        newWordTextView.setText(entry.getValue());
//                                        keyFlipped = entry.getKey();
//                                        for (int i = 0; i < arrLength; i++) {
//                                            TextView currentTextView = getView().findViewWithTag("Text " + Integer.toString(i));
//                                            currentTextView.setClickable(false);
//                                        }
//                                        turnTaken = true;
                                        } else {
                                            if (entry.getKey().equals(keyFlipped)) {
                                                //TextView clicked, turn taken, match
//                                            foundMatches.add(entry.getKey());
//                                            newWordTextView.setText(entry.getValue());
//                                            Toast.makeText(getContext(), "You found a match!", Toast.LENGTH_LONG).show();
//                                            resetClickables(arrLength);
                                                mSession.sendSignal(Integer.toString(entry.getKey()), "textFlippedMatch");
//                                            textFlippedMatch(entry, newWordTextView);
                                            } else {
                                                //TextView clicked, turn taken, no match
                                                mSession.sendSignal(Integer.toString(entry.getKey()), "textFlippedNoMatch");
//                                            resetClickables(arrLength);
//                                            Toast.makeText(getContext(), "Try again!", Toast.LENGTH_LONG).show();
                                            }
//                                        turnTaken = false;
                                        }
                                    }
                                });

                            }

                            index = 0;

                            for (String url : urlArr) {
                                urlMap.put(index, url);
                                index++;
                            }

                            Object urlKeys[] = urlMap.keySet().toArray();
                            Object urlKey = urlKeys[new Random().nextInt(urlKeys.length)];

                            List<Map.Entry<Integer, String>> urlList = new ArrayList<Map.Entry<Integer, String>>(urlMap.entrySet());
                            Collections.shuffle(urlList);

                            for (final Map.Entry<Integer, String> entry : urlList) {
                                final ImageButton newImage = new ImageButton(getContext());
                                newImage.setBottom(5);
                                imageListView.addView(newImage);
                                Picasso.get()
                                        .load(getImage("card"))
                                        .into(newImage);
                                newImage.setTag("Image " + Integer.toString(entry.getKey()));
                                newImage.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (!turnTaken) {
                                            mSession.sendSignal(Integer.toString(entry.getKey()), "imageFlippedTurnNotTaken");
//                                        imageFlippedTurnNotTaken(entry, newImage);
                                            //ImageButton clicked, turn not taken
//                                        Picasso.get()
//                                                .load(entry.getValue())
//                                                .resize(275, 183)
//                                                .centerInside()
//                                                .into(newImage);
//                                        for (int i = 0; i < arrLength; i++) {
//                                            ImageButton currentImage = getView().findViewWithTag("Image " + Integer.toString(i));
//                                            currentImage.setClickable(false);
//                                        }
//                                        keyFlipped = entry.getKey();
//                                        turnTaken = true;
                                        } else {
                                            if (entry.getKey().equals(keyFlipped)) {
                                                mSession.sendSignal(Integer.toString(entry.getKey()), "imageFlippedMatch");
                                                //imageButtonFlipped, turn taken, match
//                                            foundMatches.add(entry.getKey());
//                                            Picasso.get()
//                                                    .load(entry.getValue())
//                                                    .resize(275, 183)
//                                                    .centerInside()
//                                                    .into(newImage);
//                                            Toast.makeText(getContext(), "You found a match!", Toast.LENGTH_LONG).show();
//                                            resetClickables(arrLength);
                                            } else {
                                                mSession.sendSignal(Integer.toString(entry.getKey()), "imageFlippedNoMatch");
                                                //imageButtonFlipped, turn taken, no match
//                                            resetClickables(arrLength);
//                                            Toast.makeText(getContext(), "Try again!", Toast.LENGTH_LONG).show();
                                            }
//                                        turnTaken = false;
                                        }
                                    }
                                });
                            }
//                        newBundle.putStringArray("URL List", urlArr);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e("error", databaseError.getMessage());
                        }
                    };

                    databaseReferenceUrls.addListenerForSingleValueEvent(urlEventListener);

//                newBundle.putStringArray("Word List", wordArr);
                    Log.e("Success", "onDataChange1: ");
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("error", databaseError.getMessage());
                }
            };

            databaseReferenceWords.addListenerForSingleValueEvent(wordsEventListener);
            Log.e("GREAT SUCCESS", "getLists: ");
        } catch (NullPointerException ex) {
            Log.e("Memory Game", "Not a teacher");
        }

    }

    public MemoryGameFragment() {
        // Required empty public constructor
    }

    public static MemoryGameFragment newInstance(String wordListNameLocal, String sessionId) {
        MemoryGameFragment memoryGameFragment = new MemoryGameFragment();
        Bundle args = new Bundle();
        args.putString("Word List Name", wordListNameLocal);
        args.putString("sessionId", sessionId);
        memoryGameFragment.setArguments(args);
        return memoryGameFragment;
    }

    public int getImage(String imageName) {

        int drawableResourceId = this.getResources().getIdentifier(imageName, "drawable", getContext().getPackageName());

        return drawableResourceId;
    }

    @Override
    public void onConnected(Session session) {
        mSession = session;
        mSession.setSignalListener(this);
        getLists();
    }

    @Override
    public void onDisconnected(Session session) {
        Log.e("Memory Game", "Disconnected");
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.e("Session error", opentokError.getMessage());
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.e("Memory Game", "Stream Dropped");
    }

    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.e("Memory Game", "Stream Received");
    }

    @Override
    public void onSignalReceived(Session session, String type, String data, Connection connection) {
        //To do: change to send signals. Find TextView/ImageButton by tag. Figure out entry key (it may be the same as the iterator but I'm unsure). Then call method.
        if (type.equals("userId")) {
            userId = data;
            Log.e("userId", userId);
        }
        try {
            for (int i = 0; i < arrLength; i++) {
                final Integer I = i;
                if (type.equals(Integer.toString(I))) {
                    if (data.equals("textFlippedTurnNotTaken")) {
                        Map.Entry<Integer, String> entry = new Map.Entry<Integer, String>() {
                            @Override
                            public Integer getKey() {
                                return I;
                            }

                            @Override
                            public String getValue() {
                                return wordMap.get(I);
                            }

                            @Override
                            public String setValue(String value) {
                                return null;
                            }
                        };
                        TextView textView = getView().findViewWithTag("Text " + Integer.toString(i));
                        textFlippedTurnNotTaken(textView, entry);
                    }

                    if (data.equals("textFlippedNoMatch")) {
                        textFlippedNoMatch();
                    }

                    if (data.equals("textFlippedMatch")) {
                        Map.Entry<Integer, String> entry = new Map.Entry<Integer, String>() {
                            @Override
                            public Integer getKey() {
                                return I;
                            }

                            @Override
                            public String getValue() {
                                return wordMap.get(I);
                            }

                            @Override
                            public String setValue(String value) {
                                return null;
                            }
                        };
                        TextView textView = getView().findViewWithTag("Text " + Integer.toString(i));
                        textFlippedMatch(entry, textView);
                    }

                    if (data.equals("imageFlippedTurnNotTaken")) {
                        Map.Entry<Integer, String> entry = new Map.Entry<Integer, String>() {
                            @Override
                            public Integer getKey() {
                                return I;
                            }

                            @Override
                            public String getValue() {
                                return urlMap.get(I);
                            }

                            @Override
                            public String setValue(String value) {
                                return null;
                            }
                        };
                        ImageButton image = getView().findViewWithTag("Image " + Integer.toString(i));
                        imageFlippedTurnNotTaken(entry, image);
                    }

                    if (data.equals("imageFlippedNoMatch")) {
                        imageFlippedNoMatch();
                    }

                    if (data.equals("imageFlippedMatch")) {
                        Map.Entry<Integer, String> entry = new Map.Entry<Integer, String>() {
                            @Override
                            public Integer getKey() {
                                return I;
                            }

                            @Override
                            public String getValue() {
                                return urlMap.get(I);
                            }

                            @Override
                            public String setValue(String value) {
                                return null;
                            }
                        };
                        ImageButton image = getView().findViewWithTag("Image " + Integer.toString(i));
                        imageFlippedMatch(entry, image);
                    }
                }
            }
        } catch (NullPointerException ex) {
            getLists();
        }

    }

        @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wordListName = getArguments().getString("Word List Name");
        sessionId = getArguments().getString("sessionId");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        originalUserId = user.getUid();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mOnSessionCreated = (OnSessionCreated) context;
            Log.e("SUCCESS", "Attached");
        } catch (ClassCastException e) {
            Log.e("NOOO", "onAttach: ");
            throw new ClassCastException("NOOOO " + e.getMessage());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_memory_game, container, false);
        title = (TextView) view.findViewById(R.id.title);
        title.setText(wordListName);
        wordListView = (LinearLayout) view.findViewById(R.id.wordListView);
        imageListView = (LinearLayout) view.findViewById(R.id.imageListView);
        try {
            connectToSession(sessionId);
        } catch (OpenTokException ex) {
            Log.e("Error", ex.getMessage());
        }
//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//        int windowWidth = displayMetrics.widthPixels;
//
//        wordListView.setLayoutParams(new RelativeLayout.LayoutParams(windowWidth / 2, ViewGroup.LayoutParams.WRAP_CONTENT));
//        imageListView.setLayoutParams(new RelativeLayout.LayoutParams(windowWidth / 2, ViewGroup.LayoutParams.WRAP_CONTENT));



        return view;
    }

}
