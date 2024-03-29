package com.cs407.team15.redstone.ui.comments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.cs407.team15.redstone.R;
import com.cs407.team15.redstone.model.Comment;
import com.cs407.team15.redstone.model.User;
import com.cs407.team15.redstone.ui.location.LocationPage;
import com.cs407.team15.redstone.ui.viewtours.TourInfoActivity;
import com.cs407.team15.redstone.utility.NotificationTool;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static androidx.core.content.ContextCompat.startActivity;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ImageViewHolder> {
    private String TAG = getClass().toString();
    private Context mContext;
    private List<Comment> mComment;

    private ArrayList<String> postid; // Location ID
    private String email;
    private ArrayList<String> path;

    private FirebaseUser firebaseUser;
    private FirebaseFirestore db;
    private boolean onePathAndPost;

    private int vgId;

    /**
     * Comment Adapter
     * @param context
     * @param comments
     * @param postid Post ID of the post attaching CommentAdapter
     * @param path Database path to store the comment data
     */
    public CommentAdapter(Context context, List<Comment> comments, ArrayList<String> postid, ArrayList<String> path){
        mContext = context;
        mComment = comments;
        this.postid = postid;
        this.path = path;
        if (path.size() > 1) {
            onePathAndPost = false;
        }
        else {
            onePathAndPost = true;
        }
    }


    @NonNull
    @Override
    public CommentAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.comment_item, parent, false);
        return new CommentAdapter.ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CommentAdapter.ImageViewHolder holder, final int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        email = firebaseUser.getEmail();
        final Comment comment = mComment.get(position);

        NotificationTool notificationTool = new NotificationTool(firebaseUser, comment.getLocationId());

        holder.comment.setText(comment.getComment());
        getUserInfo(holder.image_profile, holder.username, comment.getPublisher());
        isLiked(comment.getCommentid(), holder.like,position);
        getLikesCount(comment.getCommentid(), holder.like_count, position);

        /**
         * will take user to location or tour page
         */
        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View v2 = v;
                if (mComment.get(position).getPath().equals("tour")) {
                    //need tour name and tour id
                    final String locId = mComment.get(position).getLocationId();//assuming the implementation is correct, this should be the tour id
                    db.collection("tours").document(locId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            String title = (String)documentSnapshot.get("name");
                            //fragment stuff
                            Bundle bundle = new Bundle();
                            Intent in = new Intent(mContext, TourInfoActivity.class);
                            in.putExtra("tourName", title);
                            in.putExtra("tourID", locId);
                            startActivity(mContext,in,bundle);
                        }
                    });
                }
                else if (mComment.get(position).getPath().equals("location")) {
                    //title
                    String locId = mComment.get(position).getLocationId(); // use this in a query to get the location and start the location page fragment
                    //String title;
                    db.collection("locations").document(locId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @SuppressLint("ResourceType")
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            String title = (String)documentSnapshot.get("name");
                            FragmentManager fm = ((FragmentActivity)mContext).getSupportFragmentManager();

                            FragmentTransaction ft = fm.beginTransaction();
                            LocationPage lp = new LocationPage();
                            Bundle bundle = new Bundle();
                            bundle.putString("title", title);
                            lp.setArguments(bundle);
                            ft.replace(R.id.nav_host_fragment, lp);
                            ft.addToBackStack(null);
                            ft.commit();
                        }
                    });
                    //pass in title
                }
                else {

                }
            }
        });

        /**
         * On Click like the comment
         */
        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!onePathAndPost) {
                    if (holder.like.getTag().equals("like")) {
                        FirebaseDatabase.getInstance().getReference("Likes").child(path.get(position)).child(comment.getCommentid())
                                .child(firebaseUser.getUid()).setValue(true);
                        addNotification(comment.getPublisherid(),comment.getCommentid(), "liked your comment", comment.getLocationId());
                    } else {
                        FirebaseDatabase.getInstance().getReference("Likes").child(path.get(position)).child(comment.getCommentid())
                                .child(firebaseUser.getUid()).removeValue();
                    }
                }
                else {
                    if (holder.like.getTag().equals("like")) {
                        FirebaseDatabase.getInstance().getReference("Likes").child(path.get(0)).child(comment.getCommentid())
                                .child(firebaseUser.getUid()).setValue(true);
                        //addNotification(comment.getPublisherid(),comment.getCommentid(), "liked your comment", comment.getLocationId());
                        notificationTool.addNotification(comment.getPublisherid(), "liked your comment", true);
                    } else {
                        FirebaseDatabase.getInstance().getReference("Likes").child(path.get(0)).child(comment.getCommentid())
                                .child(firebaseUser.getUid()).removeValue();
                    }
                }
            }
        });


        /**
         * On long Click Delete comment dialog pop up
         */
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (comment.getPublisher().equals(firebaseUser.getEmail())) {

                    AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                    alertDialog.setTitle("Do you want to delete?");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.e(TAG, "Remove attempt: " + "Comments/"+path+"/"+postid+"/"+comment.getCommentid());
                                    if (!onePathAndPost) {
                                        FirebaseDatabase.getInstance().getReference("Comments").child(path.get(position))
                                                .child(postid.get(position)).child(comment.getCommentid())
                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(mContext, "Deleted!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                    else {
                                        FirebaseDatabase.getInstance().getReference("Comments").child(path.get(0))
                                                .child(postid.get(0)).child(comment.getCommentid())
                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    deleteNotifications(comment.getCommentid(), firebaseUser.getUid());
                                                    Toast.makeText(mContext, "Deleted!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mComment.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        public ImageView image_profile, like;
        public TextView username, comment, like_count;
        public ProgressBar progressBar;

        public ImageViewHolder(View itemView) {
            super(itemView);
            progressBar = itemView.findViewById((R.id.comment_loading));
            image_profile = itemView.findViewById(R.id.image_profile);
            username = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.comment);
            like_count = itemView.findViewById(R.id.tv_total);
            like = itemView.findViewById(R.id.btn_like);
        }
    }

    /**
     * To set username
     */
    private void getUserInfo(final ImageView imageView, final TextView username, String publisherid){

        if (publisherid != null) {
            db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("users").document(publisherid);

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

    /**
     * Like
     */
    private void isLiked(final String commentid, final ImageView imageView, int position){

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference;
        if (!onePathAndPost) {
            reference = FirebaseDatabase.getInstance().getReference("Likes").child(path.get(position)).child(commentid);
        }
        else {
            reference = FirebaseDatabase.getInstance().getReference("Likes").child(path.get(0)).child(commentid);
        }

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(firebaseUser.getUid()).exists()){
                    imageView.setImageResource(R.drawable.ic_liked);
                    imageView.setTag("liked");
                } else{
                    imageView.setImageResource(R.drawable.ic_like);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Count and update the number of likes
     */
    private void getLikesCount(String commentid, final TextView likes, final int position){
        DatabaseReference reference;
        if (!onePathAndPost) {
            reference = FirebaseDatabase.getInstance().getReference("Likes").child(path.get(position)).child(commentid);
        }
        else {
            reference = FirebaseDatabase.getInstance().getReference("Likes").child(path.get(0)).child(commentid);

        }
        final String cid = commentid; // comment id

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final long cnt = dataSnapshot.getChildrenCount();
                likes.setText(dataSnapshot.getChildrenCount()+" ");

                // Update likes count
                DatabaseReference ref;
                if (!onePathAndPost) {
                    ref = FirebaseDatabase.getInstance().getReference("Comments")
                            .child(path.get(position)).child(postid.get(position));
                }
                else {
                    ref = FirebaseDatabase.getInstance().getReference("Comments")
                            .child(path.get(0)).child(postid.get(0));
                }
                Query commentQuery = ref.orderByChild("commentid").equalTo(cid);
                commentQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                            Comment comment = singleSnapshot.getValue(Comment.class);
                            comment.setLike(cnt);
                            if (!onePathAndPost) {
                                FirebaseDatabase.getInstance().getReference("Comments")
                                        .child(path.get(position)).child(postid.get(position)).child(cid).setValue(comment);
                            }
                            else {
                                FirebaseDatabase.getInstance().getReference("Comments")
                                        .child(path.get(0)).child(postid.get(0)).child(cid).setValue(comment);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "onCancelled", databaseError.toException());
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void addNotification(String userid, String commentid, String text, String postID){
        Log.e(TAG, "notification sent to " + userid + "from " + postid);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userid", firebaseUser.getUid());
        hashMap.put("userEmail", firebaseUser.getEmail());
        hashMap.put("text", text);
        hashMap.put("commentid", commentid); // will be the key in Table
        hashMap.put("postid", postID);
        hashMap.put("ispost", true);

        reference.push().setValue(hashMap);
    }

    private void deleteNotifications(final String postid, String userid){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    if (snapshot.child("commentid").getValue().equals(postid)){
                        snapshot.getRef().removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}
