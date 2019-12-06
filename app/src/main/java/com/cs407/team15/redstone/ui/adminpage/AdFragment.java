package com.cs407.team15.redstone.ui.adminpage;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cs407.team15.redstone.R;
import com.cs407.team15.redstone.model.AdminPost;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdFragment extends Fragment {
    private final String TAG = getClass().toString();

    private ArrayList<AdminPost> postArrayList = new ArrayList<>();
    private RecyclerView recyclerView;
    private static Context context;
    private PostAdapter mAdapter;

    private FirebaseDatabase db;

    public AdFragment() {
        // Required empty public constructor
    }


    public static AdFragment newInstance() {
        return new AdFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prepareData();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_ad, container, false);

        //recyclerview
        recyclerView = (RecyclerView) root.findViewById(R.id.ad_rv);
        recyclerView.setHasFixedSize(true);


        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new PostAdapter(getActivity(), postArrayList, "ads");

        recyclerView.setAdapter(mAdapter);

        return root;
    }

    private void prepareData() {
        db = FirebaseDatabase.getInstance();
        db.getReference("AdminPost")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        postArrayList.clear();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            AdminPost post = snapshot.getValue(AdminPost.class);
                            Log.e(TAG, post.getPostid()+": "+post.getCategory());
                            if (post.getCategory().equals("Ads")) {
                                postArrayList.add(post);
                            }
                        }
                        mAdapter.notifyDataSetChanged();
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

}
