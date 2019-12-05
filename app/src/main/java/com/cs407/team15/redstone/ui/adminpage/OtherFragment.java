package com.cs407.team15.redstone.ui.adminpage;


import android.app.Notification;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;


public class OtherFragment extends Fragment {
    private final String TAG = getClass().toString();

    private ArrayList<AdminPost> postArrayList = new ArrayList<>();
    private RecyclerView recyclerView;
    private static Context context;
    private PostAdapter mAdapter;

    private FirebaseDatabase db;

    public OtherFragment() {
        // Required empty public constructor
    }


    public static OtherFragment newInstance() {
        return new OtherFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prepareData();
        //readNotifications();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_other, container, false);


        //recyclerview
        recyclerView = (RecyclerView) root.findViewById(R.id.other_rv);
        recyclerView.setHasFixedSize(true);


        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new PostAdapter(getActivity(), postArrayList, "other");

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
                            if (post.getCategory().equals("Other")) {
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
