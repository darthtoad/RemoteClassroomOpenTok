package com.epicodus.samuelgespass.remoteclassroomopentok.ui;


import android.content.Context;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 */
public class MemoryGameFragment extends Fragment implements View.OnClickListener {
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
    private Session mSession;
    private OnSessionCreated mOnSessionCreated;
    private TextView title;
    private ListView wordListView;
    private ListView imageListView;

    public void getLists(Bundle currentBundle) {
        final Bundle newBundle = currentBundle;
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        String userId = user.getUid();
        Log.e("userId: ", userId);
        DatabaseReference databaseReferenceWords = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child(wordListName).child("wordList");

        ValueEventListener wordsEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int arrLength = (int) dataSnapshot.getChildrenCount();
                String[] wordArr = new String[arrLength];
                int index = 0;
                for (DataSnapshot wordSnapshot : dataSnapshot.getChildren()) {
                    String word = (String) wordSnapshot.getValue();
                    wordArr[index] = word;
                    index++;
                }

                newBundle.putStringArray("Word List", wordArr);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("error", databaseError.getMessage());
            }
        };

        databaseReferenceWords.addListenerForSingleValueEvent(wordsEventListener);

        DatabaseReference databaseReferenceUrls = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child(wordListName).child("urlList");

        ValueEventListener urlEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int arrLength = (int) dataSnapshot.getChildrenCount();
                String[] urlArr = new String[arrLength];
                int index = 0;
                for (DataSnapshot urlSnapshot : dataSnapshot.getChildren()) {
                    String url = (String) urlSnapshot.getValue();
                    urlArr[index] = url;
                    index++;
                }
                newBundle.putStringArray("URL List", urlArr);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("error", databaseError.getMessage());
            }
        };

        databaseReferenceUrls.addListenerForSingleValueEvent(urlEventListener);
        setArguments(newBundle);
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
        getLists(savedInstanceState);
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
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_memory_game, container, false);

        String[] urls = savedInstanceState.getStringArray("URL List");
        String[] words = savedInstanceState.getStringArray("Word List");

        title = (TextView) view.findViewById(R.id.title);
        title.setText(wordListName);

        wordListView = (ListView) view.findViewById(R.id.wordListView);
        imageListView = (ListView) view.findViewById(R.id.imageListView);
        Integer index = 0;
        HashMap<Integer, String> wordMap = new HashMap<>();

        for (String word : words) {
            wordMap.put(index, word);
            index++;
        }

        //randomize hashmap and add views

        Object[] wordKeys = wordMap.keySet().toArray();
        Object key = wordKeys[new Random().nextInt(wordKeys.length)];

        List<Map.Entry<Integer, String>> wordList = new ArrayList<Map.Entry<Integer, String>>(wordMap.entrySet());
        Collections.shuffle(wordList);

        for (final Map.Entry<Integer, String> entry : wordList) {
            final TextView newWordTextView = new TextView(getContext());
            newWordTextView.setTextSize(12);
            newWordTextView.setBottom(5);
            newWordTextView.setTag("Text " + Integer.toString(entry.getKey()));
            newWordTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!turnTaken) {
                        newWordTextView.setText(entry.getValue());
                        keyFlipped = entry.getKey();
                        newWordTextView.setClickable(false);
                        turnTaken = true;
                    } else {
                        if (entry.getKey() == keyFlipped) {
                            newWordTextView.setText(entry.getValue());
                            Toast.makeText(getContext(), "You found a match!", Toast.LENGTH_LONG).show();
                            newWordTextView.setClickable(false);
                        } else {
                            ImageButton image = view.findViewWithTag("Image " + Integer.toString(keyFlipped));
                            Glide.with(getContext())
                                    .load(getImage("card"))
                                    .into(image);
                            image.setClickable(true);
                            Toast.makeText(getContext(), "Try again!", Toast.LENGTH_LONG).show();
                        }
                        turnTaken = false;
                    }
                }
            });

            wordListView.addView(newWordTextView);
        }

        HashMap<Integer, String> urlMap = new HashMap<>();
        index = 0;

        for (String url : urls) {
            urlMap.put(index, url);
            index++;
        }

        Object urlKeys[] = urlMap.keySet().toArray();
        Object urlKey = urlKeys[new Random().nextInt(urlKeys.length)];

        List<Map.Entry<Integer, String>> urlList = new ArrayList<Map.Entry<Integer, String>>(urlMap.entrySet());
        Collections.shuffle(urlList);

        for (final Map.Entry<Integer, String> entry : urlList) {
            final ImageButton newImage = new ImageButton(getContext());
            newImage.setTag("Image " + Integer.toString(entry.getKey()));
            newImage.setBottom(5);
            Glide.with(this)
                    .load(getImage("card"))
                    .into(newImage);
            newImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!turnTaken) {
                        Glide.with(getContext())
                                .load(entry.getValue())
                                .into(newImage);
                        newImage.setClickable(false);
                        keyFlipped = entry.getKey();
                        turnTaken = true;
                    } else {
                        if (entry.getKey() == keyFlipped) {
                            Glide.with(getContext())
                                    .load(entry.getValue())
                                    .into(newImage);
                            Toast.makeText(getContext(), "You found a match!", Toast.LENGTH_LONG).show();
                            newImage.setClickable(false);
                        } else {
                            TextView text = view.findViewWithTag("Text " + Integer.toString(keyFlipped));
                            text.setClickable(true);
                            Toast.makeText(getContext(), "Try again!", Toast.LENGTH_LONG).show();
                        }
                        turnTaken = false;
                    }
                }
            });
            imageListView.addView(newImage);
        }


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

    @Override
    public void onClick(View view) {
//
//        if (!turnTaken) {
//
//            if (view == textViewEating) {
//                turnTaken = true;
//                textViewEating.setText("eating");
//            }
//
//            if (view == textViewRunning) {
//                turnTaken = true;
//                textViewRunning.setText("running");
//            }
//
//            if (view == textViewSleeping) {
//                turnTaken = true;
//                textViewSleeping.setText("sleeping");
//            }
//
//            if (view == imageButtonEating) {
//                turnTaken = true;
//                isImageButtonEatingFlipped = true;
//                Glide.with(this).load(getImage("eating")).into(imageButtonEating);
//            }
//
//            if (view == imageButtonRunning) {
//                turnTaken = true;
//                isImageButtonRunningFlipped = true;
//                Glide.with(this).load(getImage("running")).into(imageButtonRunning);
//            }
//
//            if (view == imageButtonSleeping) {
//                turnTaken = true;
//                isImageButtonSleepingFlipped = true;
//                Glide.with(this).load(getImage("sleeping")).into(imageButtonSleeping);
//            }
//
//        }
//
//        if (turnTaken) {
//
//            if (view == textViewEating && !textViewEating.getText().equals("eating")) {
//                turnTaken = false;
//                if (isImageButtonEatingFlipped) {
//                    Toast.makeText(getContext(), "You found a match!", Toast.LENGTH_LONG).show();
//                    textViewEating.setClickable(false);
//                    imageButtonEating.setClickable(false);
//                    textViewEating.setText("eating");
//                    isEatingMatched = true;
//                } else {
//                    Toast.makeText(getContext(), "Try again", Toast.LENGTH_SHORT).show();
//                    resetViews();
//                }
//            }
//
//            if (view == textViewRunning && !textViewRunning.getText().equals("running")) {
//                turnTaken = false;
//                if (isImageButtonRunningFlipped) {
//                    Toast.makeText(getContext(), "You found a match!", Toast.LENGTH_LONG).show();
//                    textViewRunning.setClickable(false);
//                    imageButtonRunning.setClickable(false);
//                    textViewRunning.setText("running");
//                    isRunningMatched = true;
//                } else {
//                    Toast.makeText(getContext(), "Try again", Toast.LENGTH_SHORT).show();
//                    resetViews();
//                }
//            }
//
//            if (view == textViewSleeping && !textViewSleeping.getText().equals("sleeping")) {
//                turnTaken = false;
//                if (isImageButtonSleepingFlipped) {
//                    Toast.makeText(getContext(), "You found a match!", Toast.LENGTH_LONG).show();
//                    textViewSleeping.setClickable(false);
//                    imageButtonSleeping.setClickable(false);
//                    textViewSleeping.setText("sleeping");
//                    isSleepingMatched = true;
//                } else {
//                    Toast.makeText(getContext(), "Try again", Toast.LENGTH_SHORT).show();
//                    resetViews();
//                }
//            }
//
//            if (view == imageButtonEating && !isImageButtonEatingFlipped) {
//                turnTaken = false;
//                if (textViewEating.getText().equals("eating")) {
//                    Toast.makeText(getContext(), "You found a match!", Toast.LENGTH_LONG).show();
//                    textViewEating.setClickable(false);
//                    imageButtonEating.setClickable(false);
//                    Glide.with(this).load(getImage("eating")).into(imageButtonEating);
//                    isEatingMatched = true;
//                } else {
//                    Toast.makeText(getContext(), "Try again", Toast.LENGTH_SHORT).show();
//                    resetViews();
//                }
//            }
//
//            if (view == imageButtonRunning && !isImageButtonRunningFlipped) {
//                turnTaken = false;
//                if (textViewRunning.getText().equals("running")) {
//                    Toast.makeText(getContext(), "You found a match!", Toast.LENGTH_LONG).show();
//                    textViewRunning.setClickable(false);
//                    imageButtonRunning.setClickable(false);
//                    Glide.with(this).load(getImage("running")).into(imageButtonRunning);
//                    isRunningMatched = true;
//                } else {
//                    Toast.makeText(getContext(), "Try again", Toast.LENGTH_SHORT).show();
//                    resetViews();
//                }
//
//            }
//
//            if (view == imageButtonSleeping && !isImageButtonSleepingFlipped) {
//                turnTaken = false;
//                if (textViewSleeping.getText().equals("sleeping")) {
//                    Toast.makeText(getContext(), "You found a match!", Toast.LENGTH_LONG).show();
//                    textViewSleeping.setClickable(false);
//                    imageButtonSleeping.setClickable(false);
//                    Glide.with(this).load(getImage("sleeping")).into(imageButtonSleeping);
//                    isSleepingMatched = true;
//                } else {
//                    Toast.makeText(getContext(), "Try again", Toast.LENGTH_SHORT).show();
//                    resetViews();
//                }
//            }
//
//        }

    }

}
