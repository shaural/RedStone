package com.cs407.team15.redstone.ui.comments;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;


import com.cs407.team15.redstone.R;
import com.cs407.team15.redstone.model.Comment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static android.content.ContentValues.TAG;

public class CommentsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener {
    String TAG = getClass().toString();

    private RecyclerView recyclerView;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;
    private ProgressBar progressBar;
    private AppCompatButton sort;

    EditText addcomment;
    ImageView image_profile;
    TextView post;

    String postid;
    String publisherid;
    String path;

    FirebaseUser firebaseUser;

    //Tag stuff
    private ArrayList<String> tagNames;
    private Context context;
    private ArrayList<String> tagsOnComment;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);
        tagsOnComment = new ArrayList<>();
        FirebaseFirestore.getInstance().collection("tags").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    tagNames = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if (document.exists()) {
                            Log.d(TAG, document.getId() + " => " + document.get("name"));

                            tagNames.add((String)document.get("name"));
                        }
                        else {
                            Log.d("michael", "no document exists");
                        }
                    }
                    spinnerSetup();
                    //fillRecycleViewer(list);
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });

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

        Intent intent = getIntent();
        postid = intent.getStringExtra("postid"); // location ID
        publisherid = intent.getStringExtra("publisherid");
        path = intent.getStringExtra("path");
        Log.e(TAG, "PATH:" + path);
        Log.e(TAG, "Author:"+ publisherid);
        Log.e(TAG, "Postid: "+ postid);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList, postid, path);
        recyclerView.setAdapter(commentAdapter);


        post = findViewById(R.id.post);
        addcomment = findViewById(R.id.add_comment);
        image_profile = findViewById(R.id.image_profile);
        progressBar = findViewById(R.id.comment_loading);
        sort = findViewById(R.id.btn_sort);

        progressBar.setVisibility(View.VISIBLE);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();


        setListener();
        sortCommentsByLikes(); // default
    }

    private void setListener() {
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
                                readComments();
                                return true;
                            case R.id.sort_like:
                                Log.e(TAG, "Sort by Likes");
                                sortCommentsByLikes();
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
        hashMap.put("path", path);
        hashMap.put("commentid", commentid);
        hashMap.put("tags", tagsOnComment);
        hashMap.put("like", 0);
        hashMap.put("timestamp", ts);

        reference.child(commentid).setValue(hashMap);

        addcomment.setText("");
    }

    /**
     * Read Comments
     * Get all data from DB and add them into List
     * then, notify comment recyclerview adapter
     * By default, ordered by timestamp
     */
    private void readComments(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(path).child(postid);
        Query sortQuery = reference.orderByChild("timestamp");

        sortQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.VISIBLE);
                commentList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Comment comment = snapshot.getValue(Comment.class);
                    commentList.add(comment);
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String tagName = tagNames.get(position);
        if (!tagsOnComment.contains(tagName)) {
            tagsOnComment.add(tagName);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void spinnerSetup() {
        Spinner tagSpinner = findViewById(R.id.tagSpinnerComment);
        ArrayAdapter spinnerAdapter = new ArrayAdapter<String>(CommentsActivity.this,android.R.layout.simple_spinner_item, tagNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tagSpinner.setAdapter(spinnerAdapter);
        tagSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    /**
     * Sort Comments
     */
    private void sortCommentsByLikes(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(path).child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.VISIBLE);
                commentList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Comment comment = snapshot.getValue(Comment.class);
                    commentList.add(0,comment); // reverse
                }

                Collections.sort(commentList, cmpLikeThenTimestamp);

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
            } else if (item1.getLike() == item2.getLike() && item1.getTimestamp() != null && item2.getTimestamp() != null) {
                ret = item1.getTimestamp().compareTo(item2.getTimestamp()) ;
            } else {
                ret = -1;
            }

            return ret ;
        }
    } ;
}
