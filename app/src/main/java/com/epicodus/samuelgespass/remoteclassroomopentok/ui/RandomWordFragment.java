package com.epicodus.samuelgespass.remoteclassroomopentok.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

import org.json.JSONException;
import org.json.JSONObject;

import static com.epicodus.samuelgespass.remoteclassroomopentok.Constants.API_KEY;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link RandomWordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RandomWordFragment extends Fragment implements Session.SessionListener, Session.SignalListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    // TODO: Rename and change types of parameters
    private String mWordListName;
    private String mSessionId;
    private String token;
    private Session mSession;
    private OnSessionCreated mOnSessionCreated;
    TextView mWordTextView;
    int arrLength;
    String[] globalWordArr;

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

                    mSession = new Session.Builder(getActivity().getApplicationContext(), API_KEY, sessionId).build();
                    mSession.setSessionListener(RandomWordFragment.this);
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

    public void getList() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        String userId = user.getUid();
        Log.e("userId: ", userId);
        DatabaseReference databaseReferenceWords = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child(mWordListName).child("wordList");
        ValueEventListener wordEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                arrLength = (int) dataSnapshot.getChildrenCount();
                final String[] wordArr = new String[arrLength];
                int index = 0;
                for (DataSnapshot wordSnapshot : dataSnapshot.getChildren()) {
                    String word = (String) wordSnapshot.getValue();
                    wordArr[index] = word;
                    index++;
                }
                globalWordArr = wordArr;
                mWordTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSession.sendSignal("", "Random Word");
                    }
                });
                mSession.sendSignal("", "Random Word");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        databaseReferenceWords.addValueEventListener(wordEventListener);

    }

    public RandomWordFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RandomWordFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RandomWordFragment newInstance(String param1, String param2) {
        RandomWordFragment fragment = new RandomWordFragment();
        Bundle args = new Bundle();
        args.putString("wordListName", param1);
        args.putString("sessionId", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mWordListName = getArguments().getString("wordListName");
            mSessionId = getArguments().getString("sessionId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_random_word, container, false);
        mWordTextView = (TextView) view.findViewById(R.id.wordTextView);
        try {
            connectToSession(mSessionId);
        } catch (OpenTokException ex) {
            Log.e("RandomWordFragment", ex.getMessage());
        }
        return view;
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
    public void onConnected(Session session) {
        mSession = session;
        mSession.setSignalListener(this);
        getList();
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
        if (data.equals("Random Word")) {
            int random = (int) Math.round(Math.random() * (arrLength - 1));
            mWordTextView.setText(globalWordArr[random]);
        }
    }
}
