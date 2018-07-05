package com.epicodus.samuelgespass.remoteclassroomopentok.ui;


import android.content.Context;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.epicodus.samuelgespass.remoteclassroomopentok.R;
import com.epicodus.samuelgespass.remoteclassroomopentok.util.OnSessionCreated;
import com.opentok.android.Session;

/**
 * A simple {@link Fragment} subclass.
 */
public class MemoryGameFragment extends Fragment implements View.OnClickListener {
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
    private OnSessionCreated mOnSessionCreated;


    public MemoryGameFragment() {
        // Required empty public constructor
    }

    public static MemoryGameFragment newInstance() {
        MemoryGameFragment memoryGameFragment = new MemoryGameFragment();
        Bundle args = new Bundle();
        memoryGameFragment.setArguments(args);
        return memoryGameFragment;
    }

    public int getImage(String imageName) {

        int drawableResourceId = this.getResources().getIdentifier(imageName, "drawable", getContext().getPackageName());

        return drawableResourceId;
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
        View view = inflater.inflate(R.layout.fragment_memory_game, container, false);
        textViewEating = (TextView) view.findViewById(R.id.textView_eating);
        textViewRunning = (TextView) view.findViewById(R.id.textView_running);
        textViewSleeping = (TextView) view.findViewById(R.id.textView_sleeping);

        imageButtonEating = (ImageButton) view.findViewById(R.id.imageButton_eating);
        imageButtonRunning = (ImageButton) view.findViewById(R.id.imageButton_running);
        imageButtonSleeping = (ImageButton) view.findViewById(R.id.imageButton_sleeping);

        Glide.with(this).load(getImage("card")).into(imageButtonEating);
        Glide.with(this).load(getImage("card")).into(imageButtonRunning);
        Glide.with(this).load(getImage("card")).into(imageButtonSleeping);

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
            Glide.with(this).load(getImage("card")).into(imageButtonRunning);
        }

        if (!isEatingMatched) {
            textViewEating.setText("");
            Glide.with(this).load(getImage("card")).into(imageButtonEating);
        }

        if (!isSleepingMatched) {
            textViewSleeping.setText("");
            Glide.with(this).load(getImage("card")).into(imageButtonSleeping);
        }
    }

    @Override
    public void onClick(View view) {

        if (!turnTaken) {

            if (view == textViewEating) {
                turnTaken = true;
                textViewEating.setText("eating");
            }

            if (view == textViewRunning) {
                turnTaken = true;
                textViewRunning.setText("running");
            }

            if (view == textViewSleeping) {
                turnTaken = true;
                textViewSleeping.setText("sleeping");
            }

            if (view == imageButtonEating) {
                turnTaken = true;
                isImageButtonEatingFlipped = true;
                Glide.with(this).load(getImage("eating")).into(imageButtonEating);
            }

            if (view == imageButtonRunning) {
                turnTaken = true;
                isImageButtonRunningFlipped = true;
                Glide.with(this).load(getImage("running")).into(imageButtonRunning);
            }

            if (view == imageButtonSleeping) {
                turnTaken = true;
                isImageButtonSleepingFlipped = true;
                Glide.with(this).load(getImage("sleeping")).into(imageButtonSleeping);
            }

        }

        if (turnTaken) {

            if (view == textViewEating && !textViewEating.getText().equals("eating")) {
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

            if (view == textViewRunning && !textViewRunning.getText().equals("running")) {
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

            if (view == textViewSleeping && !textViewSleeping.getText().equals("sleeping")) {
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

            if (view == imageButtonEating && !isImageButtonEatingFlipped) {
                turnTaken = false;
                if (textViewEating.getText().equals("eating")) {
                    Toast.makeText(getContext(), "You found a match!", Toast.LENGTH_LONG).show();
                    textViewEating.setClickable(false);
                    imageButtonEating.setClickable(false);
                    Glide.with(this).load(getImage("eating")).into(imageButtonEating);
                    isEatingMatched = true;
                } else {
                    Toast.makeText(getContext(), "Try again", Toast.LENGTH_SHORT).show();
                    resetViews();
                }
            }

            if (view == imageButtonRunning && !isImageButtonRunningFlipped) {
                turnTaken = false;
                if (textViewRunning.getText().equals("running")) {
                    Toast.makeText(getContext(), "You found a match!", Toast.LENGTH_LONG).show();
                    textViewRunning.setClickable(false);
                    imageButtonRunning.setClickable(false);
                    Glide.with(this).load(getImage("running")).into(imageButtonRunning);
                    isRunningMatched = true;
                } else {
                    Toast.makeText(getContext(), "Try again", Toast.LENGTH_SHORT).show();
                    resetViews();
                }

            }

            if (view == imageButtonSleeping && !isImageButtonSleepingFlipped) {
                turnTaken = false;
                if (textViewSleeping.getText().equals("sleeping")) {
                    Toast.makeText(getContext(), "You found a match!", Toast.LENGTH_LONG).show();
                    textViewSleeping.setClickable(false);
                    imageButtonSleeping.setClickable(false);
                    Glide.with(this).load(getImage("sleeping")).into(imageButtonSleeping);
                    isSleepingMatched = true;
                } else {
                    Toast.makeText(getContext(), "Try again", Toast.LENGTH_SHORT).show();
                    resetViews();
                }
            }

        }

    }

}
