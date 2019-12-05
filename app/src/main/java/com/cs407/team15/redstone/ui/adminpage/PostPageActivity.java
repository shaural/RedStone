package com.cs407.team15.redstone.ui.adminpage;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.cs407.team15.redstone.model.AdminPost;
import com.cs407.team15.redstone.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cs407.team15.redstone.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

/**
 * This activity is to show the detail of the Post
 * Only Admin can access to edit or delete activity on this activity.
 */
public class PostPageActivity extends AppCompatActivity {
    private String TAG = getClass().toString();

    private ImageView image_profile, post_image;
    private TextView username, description, createdtime, category;
    private ImageView postpage_btn_more;
    private ProgressBar progressBar;
    private String postid;
    private FirebaseDatabase db;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore fsdb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_page);


        // Setting toolbar
        Toolbar toolbar = findViewById(R.id.postpage_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        image_profile = findViewById(R.id.admin_profile);
        username = findViewById(R.id.post_username);
        description = findViewById(R.id.post_description);
        createdtime = findViewById(R.id.post_createdtime);
        post_image = findViewById(R.id.post_image);
        postpage_btn_more = findViewById(R.id.postpage_btn_more);
        category = findViewById(R.id.tv_category);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

    }

    @Override
    protected void onStart() {
        super.onStart();

        prepareData();
    }

    /**
     * parsing data transferred from last activity
     * to PostPageActivity for the detail
     */
    private void prepareData() {
        Intent intent = getIntent();

        if (intent.hasExtra("post")){
            final AdminPost post = (AdminPost)intent.getSerializableExtra("post");

            postid = post.getPostid();

            if (post.getPostimage().equals("")) {
                post_image.setVisibility(View.GONE);
            } else {
                post_image.setVisibility(View.VISIBLE);
                Glide.with(getApplicationContext()).load(post.getPostimage())
                        .apply(new RequestOptions().placeholder(R.drawable.ic_upload_photo))
                        .into(post_image);
            }

            if (post.getDescription().equals("")){
                description.setVisibility(View.GONE);
            } else {
                description.setVisibility(View.VISIBLE);
                description.setText(post.getDescription());
            }

            category.setText(post.getCategory());

            long timestamp = Long.parseLong(post.getTimestamp());
            String dateString = getDateCurrentTimeZone(timestamp);
            createdtime.setText(dateString);

            getUserInfo(username, post.getPublisherid());

            /**
             * Set Listener to Edit or Delete the post
             */
            postpage_btn_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popupMenu = new PopupMenu(PostPageActivity.this, view);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()){
                                case R.id.post_edit:
                                    editPost(post.getPostid());
                                    return true;
                                case R.id.post_delete:
                                    final String id = post.getPostid();
                                    FirebaseDatabase.getInstance().getReference("AdminPost")
                                            .child(post.getPostid()).removeValue()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        finish();
                                                    }
                                                }
                                            });
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });

                    popupMenu.inflate(R.menu.post_menu);
                    if (!post.getPublisher().equals(firebaseUser.getUid())){
//                        postpage_btn_more.setVisibility(View.GONE);
                        popupMenu.getMenu().findItem(R.id.post_edit).setVisible(false);
                        popupMenu.getMenu().findItem(R.id.post_delete).setVisible(false);
                    }
                    popupMenu.show();
                }
            });
        }

    }

    /**
     * Function to resolve timestamp
     * @param timestamp
     * @return
     */
    private String getDateCurrentTimeZone(long timestamp) {
        try{
            Calendar calendar = Calendar.getInstance();
            TimeZone tz = TimeZone.getDefault();
            calendar.setTimeInMillis(timestamp * 1000);
            calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date currenTimeZone = (Date) calendar.getTime();
            return sdf.format(currenTimeZone);
        }catch (Exception e) {
        }
        return "";
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Edit a description of the post
     * @param postid
     */
    private void editPost(final String postid){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Edit Post");

        final EditText editText = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        editText.setLayoutParams(lp);
        alertDialog.setView(editText);

        getText(postid, editText);

        alertDialog.setPositiveButton("Edit",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("description", editText.getText().toString());

                        FirebaseDatabase.getInstance().getReference("AdminPost")
                                .child(postid).updateChildren(hashMap)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            description.setText(editText.getText().toString());
                                        }
                                    }
                                });

                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        alertDialog.show();
    }

    private void getText(String postid, final EditText editText){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("AdminPost")
                .child(postid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                editText.setText(dataSnapshot.getValue(AdminPost.class).getDescription());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * To set username
     */
    private void getUserInfo(final TextView username, String publisherid){

        if (publisherid != null) {
            Log.e(TAG, "User: " + publisherid);
            fsdb = FirebaseFirestore.getInstance();
            DocumentReference docRef = fsdb.collection("users").document(publisherid);

            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            User me = document.toObject(User.class);
                            username.setText(me.getUsername());
                        } else {
                            username.setText("no username");
                            Log.e(TAG, "No User document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
        }
    }
}