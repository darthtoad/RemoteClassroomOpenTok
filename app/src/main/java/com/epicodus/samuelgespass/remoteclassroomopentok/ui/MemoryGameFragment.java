package com.epicodus.samuelgespass.remoteclassroomopentok.ui;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.epicodus.samuelgespass.remoteclassroomopentok.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MemoryGameFragment extends Fragment {


    public MemoryGameFragment() {
        // Required empty public constructor
    }

    public static MemoryGameFragment newInstance() {
        MemoryGameFragment memoryGameFragment = new MemoryGameFragment();
        Bundle args = new Bundle();
        memoryGameFragment.setArguments(args);
        return memoryGameFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_memory_game, container, false);
    }

}
