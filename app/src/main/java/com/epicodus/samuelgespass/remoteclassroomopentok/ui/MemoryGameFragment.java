package com.epicodus.samuelgespass.remoteclassroomopentok.ui;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.epicodus.samuelgespass.remoteclassroomopentok.R;
import com.epicodus.samuelgespass.remoteclassroomopentok.util.OnSessionCreated;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.Session;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 */
public class MemoryGameFragment extends Fragment {
//    private TextView textViewRunning;
//    private TextView textViewSleeping;
//    private TextView textViewEating;
//    private ImageButton imageButtonRunning;
//    private ImageButton imageButtonSleeping;
//    private ImageButton imageButtonEating;
//    private boolean isImageButtonRunningFlipped = false;
//    private boolean isImageButtonSleepingFlipped = false;
//    private boolean isImageButtonEatingFlipped = false;
    private boolean turnTaken = false;
    private Integer keyFlipped;
//    private boolean isRunningMatched = false;
//    private boolean isSleepingMatched = false;
//    private boolean isEatingMatched = false;
    String wordListName;
    ArrayList<Integer> foundMatches = new ArrayList<>();
    private Session mSession;
    private OnSessionCreated mOnSessionCreated;
    private TextView title;
    private LinearLayout wordListView;
    private LinearLayout imageListView;

    public void resetClickables(int arrLength) {
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
        String userId = user.getUid();
        Log.e("userId: ", userId);
        DatabaseReference databaseReferenceWords = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child(wordListName).child("wordList");
        final DatabaseReference databaseReferenceUrls = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child(wordListName).child("urlList");

        ValueEventListener wordsEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e("Start", "onDataChange1: ");
                int arrLength = (int) dataSnapshot.getChildrenCount();
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
                        final int arrLength = (int) dataSnapshot.getChildrenCount();
                        String[] urlArr = new String[arrLength];
                        Integer index = 0;
                        for (DataSnapshot urlSnapshot : dataSnapshot.getChildren()) {
                            String url = (String) urlSnapshot.getValue();
                            urlArr[index] = url;
                            index++;
                        }
                        Log.e("Success", "onDataChange2: ");


                        index = 0;
                        HashMap<Integer, String> wordMap = new HashMap<>();

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
                                        newWordTextView.setText(entry.getValue());
                                        keyFlipped = entry.getKey();
                                        for (int i = 0; i < arrLength; i++) {
                                            TextView currentTextView = getView().findViewWithTag("Text " + Integer.toString(i));
                                            currentTextView.setClickable(false);
                                        }
                                        turnTaken = true;
                                    } else {
                                        if (entry.getKey() == keyFlipped) {
                                            foundMatches.add(entry.getKey());
                                            newWordTextView.setText(entry.getValue());
                                            Toast.makeText(getContext(), "You found a match!", Toast.LENGTH_LONG).show();
                                            resetClickables(arrLength);
                                        } else {
                                            resetClickables(arrLength);
                                            Toast.makeText(getContext(), "Try again!", Toast.LENGTH_LONG).show();
                                        }
                                        turnTaken = false;
                                    }
                                }
                            });

                        }

                        HashMap<Integer, String> urlMap = new HashMap<>();
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
                                    } else {
                                        if (entry.getKey() == keyFlipped) {
                                            foundMatches.add(entry.getKey());
                                            Picasso.get()
                                                    .load(entry.getValue())
                                                    .resize(275, 183)
                                                    .centerInside()
                                                    .into(newImage);
                                            Toast.makeText(getContext(), "You found a match!", Toast.LENGTH_LONG).show();
                                            resetClickables(arrLength);
                                        } else {
                                            TextView text = getView().findViewWithTag("Text " + Integer.toString(keyFlipped));
                                            resetClickables(arrLength);
                                            Toast.makeText(getContext(), "Try again!", Toast.LENGTH_LONG).show();
                                        }
                                        turnTaken = false;
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
    }

    public MemoryGameFragment() {
        // Required empty public constructor
    }

    public static MemoryGameFragment newInstance(String wordListNameLocal) {
        MemoryGameFragment memoryGameFragment = new MemoryGameFragment();
        Bundle args = new Bundle();
        args.putString("Word List Name", wordListNameLocal);
        memoryGameFragment.setArguments(args);
        return memoryGameFragment;
    }

    public int getImage(String imageName) {

        int drawableResourceId = this.getResources().getIdentifier(imageName, "drawable", getContext().getPackageName());

        return drawableResourceId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wordListName = getArguments().getString("Word List Name");
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
//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//        int windowWidth = displayMetrics.widthPixels;
//
//        wordListView.setLayoutParams(new RelativeLayout.LayoutParams(windowWidth / 2, ViewGroup.LayoutParams.WRAP_CONTENT));
//        imageListView.setLayoutParams(new RelativeLayout.LayoutParams(windowWidth / 2, ViewGroup.LayoutParams.WRAP_CONTENT));


        getLists();



//        textViewEating = (TextView) view.findViewById(R.id.textView_eating);
//        textViewRunning = (TextView) view.findViewById(R.id.textView_running);
//        textViewSleeping = (TextView) view.findViewById(R.id.textView_sleeping);
//
//        imageButtonEating = (ImageButton) view.findViewById(R.id.imageButton_eating);
//        imageButtonRunning = (ImageButton) view.findViewById(R.id.imageButton_running);
//        imageButtonSleeping = (ImageButton) view.findViewById(R.id.imageButton_sleeping);
//
//        Glide.with(this).load(getImage("card")).into(imageButtonEating);
//        Glide.with(this).load(getImage("card")).into(imageButtonRunning);
//        Glide.with(this).load(getImage("card")).into(imageButtonSleeping);
//
//        textViewEating.setOnClickListener(this);
//        textViewSleeping.setOnClickListener(this);
//        textViewRunning.setOnClickListener(this);
//
//        imageButtonSleeping.setOnClickListener(this);
//        imageButtonRunning.setOnClickListener(this);
//        imageButtonEating.setOnClickListener(this);

        return view;
    }

    private void resetViews() {
//        if (!isRunningMatched) {
//            textViewRunning.setText("");
//            Glide.with(this).load(getImage("card")).into(imageButtonRunning);
//        }
//
//        if (!isEatingMatched) {
//            textViewEating.setText("");
//            Glide.with(this).load(getImage("card")).into(imageButtonEating);
//        }
//
//        if (!isSleepingMatched) {
//            textViewSleeping.setText("");
//            Glide.with(this).load(getImage("card")).into(imageButtonSleeping);
//        }
    }

}
