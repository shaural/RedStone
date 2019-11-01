package com.cs407.team15.redstone.ui.comments;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cs407.team15.redstone.R;
import com.cs407.team15.redstone.model.Comment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class CommentsActivity extends AppCompatActivity {
    String TAG = getClass().toString();

    private RecyclerView recyclerView;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;
    private List<String> hammerList;
    private ProgressBar progressBar;
    private AppCompatButton sort;
    private AppCompatImageView checkbox;
    private TextView hammer_only;
    private EditText addcomment;
    private TextView post;

    ImageView image_profile;

    private String postid;
    private String publisherid;
    private String path;
    private Boolean isHammerOnly;

    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        // Setting toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Get passed data from Intent
        Intent intent = getIntent();
        postid = intent.getStringExtra("postid"); // location ID
        publisherid = intent.getStringExtra("publisherid");
        path = intent.getStringExtra("path");
        Log.e(TAG, "PATH:" + path);
        Log.e(TAG, "Author:"+ publisherid);
        Log.e(TAG, "Postid: "+ postid);

        // Recycler View init and attach adapter
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList, postid, path);
        recyclerView.setAdapter(commentAdapter);

        // initialize variables
        isHammerOnly = false;
        hammerList = new ArrayList<>();
        post = findViewById(R.id.post);
        addcomment = findViewById(R.id.add_comment);
        image_profile = findViewById(R.id.image_profile);
        progressBar = findViewById(R.id.comment_loading);
        sort = findViewById(R.id.btn_sort);
        hammer_only = findViewById(R.id.hammerCheck);
        checkbox = findViewById(R.id.hammerCheckbox);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        progressBar.setVisibility(View.VISIBLE);

        // Start
        setListener();
        readComment(0); // default: 0
    }

    private void setListener() {
        /*
            On Click, filter Hammer User comments only
         */
        hammer_only.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isHammerOnly) {
                    isHammerOnly = true;
                    checkbox.setImageResource(R.drawable.ic_check);
                } else {
                    checkbox.setImageResource(R.drawable.ic_uncheck);
                    isHammerOnly = false;
                }
                readComment(0); // default: order by Like
                Log.e(TAG, "hammer_only: " + isHammerOnly);
            }
        });

        /*
            On Click, post a comment
         */
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (addcomment.getText().toString().equals("")) {
                    Toast.makeText(CommentsActivity.this, "You can't send empty comment", Toast.LENGTH_SHORT).show();
                } else {
                    addComment();
                }
            }
        });

        /*
            On Click, sort comments
         */
        sort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(CommentsActivity.this, sort);
                //Inflating the Popup using xml file
                popup.getMenuInflater()
                        .inflate(R.menu.sort_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.sort_recent:
                                Log.e(TAG, "Sort by Timestamp");
                                readComment(1);
                                return true;
                            case R.id.sort_like:
                                Log.e(TAG, "Sort by Likes");
                                readComment(0);
                                return true;
                            default:
                                return true;
                        }
                    }
                });

                popup.show(); //showing popup menu
            }
        }); //closing the setOnClickListener method
    }

    /**
     * Add Comment
     * make a comment object then send to Firebase
     */
    private void addComment() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(path).child(postid);

        String commentid = reference.push().getKey(); // Comment ID
        Long tsLong = System.currentTimeMillis()/1000; // Timestamp
        String ts = tsLong.toString();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("comment", addcomment.getText().toString());
        hashMap.put("publisher", firebaseUser.getEmail());
        hashMap.put("publisherid", firebaseUser.getUid());
        hashMap.put("path", path);
        hashMap.put("commentid", commentid);
        hashMap.put("like", 0);
        hashMap.put("timestamp", ts);

        reference.child(commentid).setValue(hashMap);

        addcomment.setText("");
    }

    /**
     * Read Comment
     * if a user clicks Hammer User only,
     * this method gets HammerUser list, then pass to filterHammerOnly method.
     * @param mode Order by Likes:0 (Default), Otherwise 1 (Recent order)
     */
    private void readComment(final int mode){
        if (isHammerOnly) {
            FirebaseDatabase.getInstance().getReference("HammerUser")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            hammerList.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                                hammerList.add(snapshot.getKey());
                            }
                            // TO DO
                            filterHammerOnly(hammerList, mode);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        } else {
            getComments(mode);
        }

    }

    /**
     * Filter Hammer User's Comment Only
     * @param list List of Hammer Users
     * @param mode Order by Likes:0 (Default), Otherwise 1 (Recent order)
     */
    private void filterHammerOnly(final List<String> list, final int mode) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(path).child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.VISIBLE);
                commentList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Comment comment = snapshot.getValue(Comment.class);
                    if (list.contains(comment.getPublisherid())) {
                        commentList.add(0,comment); // reverse
                    }
                }

                if (mode == 0) {
                    Log.e(TAG, "HammerOnly Sort by Like");
                    Collections.sort(commentList, cmpLikeThenTimestamp); // Like -> Timestamp order
                } else if (mode == 1) {
                    Log.e(TAG, "HammerOnly Sort by Recent");
                    Collections.sort(commentList, cmpTimestamp); // Timestamp order
                }

                commentAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
            }
        });

    }

    /**
     * Get Comments
     * @param mode Order by Likes:0 (Default), Otherwise 1 (Recent order)
     */
    private void getComments(final int mode) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(path).child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.VISIBLE);
                commentList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Comment comment = snapshot.getValue(Comment.class);
                    commentList.add(comment);
                }

                if (mode == 0) {
                    Log.e(TAG, "All Sort by Like");
                    Collections.sort(commentList, cmpLikeThenTimestamp); // Like -> Timestamp order
                } else if (mode == 1) {
                    Log.e(TAG, "All Sort by Recent");
                    Collections.sort(commentList, cmpTimestamp); // Timestamp order
                }

                commentAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Compare class
     * Dsc Likes then Asc Timestamp
     */
    Comparator<Comment> cmpLikeThenTimestamp = new Comparator<Comment>() {
        @Override
        public int compare(Comment item1, Comment item2) {
            int ret ;

            if (item1.getLike() < item2.getLike()) {
                ret = 1;
            } else if (item1.getLike() == item2.getLike()) {
                ret = item1.getTimestamp().compareTo(item2.getTimestamp()) ;
            } else {
                ret = -1;
            }

            return ret ;
        }
    } ;

    /**
     * Compare class
     * Asc Timestamp
     */
    Comparator<Comment> cmpTimestamp = new Comparator<Comment>() {
        @Override
        public int compare(Comment item1, Comment item2) {
            return item1.getTimestamp().compareTo(item2.getTimestamp());
        }
    } ;
}
