package com.cs407.team15.redstone.ui.publicboard;


import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cs407.team15.redstone.R;
import com.cs407.team15.redstone.model.Post;
import com.cs407.team15.redstone.ui.post.PostAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PostingFragment extends Fragment {
    private final String TAG = getClass().toString();

    private ArrayList<Post> postArrayList = new ArrayList<>();
    private RecyclerView recyclerView;
    private static Context context;
    private PostAdapter mAdapter;
    private FirebaseDatabase db;

    private String category, path, location;

    public PostingFragment() {
        // Required empty public constructor
    }

    public static PostingFragment newInstance() {
        return new PostingFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        category = "Post";
        path = "public";

        location = this.getArguments().getString("area");
        Log.e(TAG, location);


        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_feature, container, false);

        //recyclerview
        recyclerView = (RecyclerView) root.findViewById(R.id.feature_rv);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());


        mAdapter = new PostAdapter(getActivity(), postArrayList, category, path, location);

        recyclerView.setAdapter(mAdapter);

        //prepareData();

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        prepareData();
    }

    private void prepareData() {
        db = FirebaseDatabase.getInstance();
        db.getReference(path).child(location)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        postArrayList.clear();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Post post = snapshot.getValue(Post.class);
                            //Log.e(TAG, post.getPostid()+": "+post.getCategory());
                            if (post.getCategory().equals(category)) {
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
