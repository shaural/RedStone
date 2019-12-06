package com.cs407.team15.redstone.ui.post;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import androidx.recyclerview.widget.RecyclerView;

import com.cs407.team15.redstone.R;
import com.cs407.team15.redstone.model.Post;
import com.cs407.team15.redstone.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ImageViewHolder>  {
    private String TAG = getClass().toString();
    private Context mContext;
    private List<Post> mPost;

    private String category, path, location;
    private String email;

    private FirebaseUser firebaseUser;
    private FirebaseFirestore db;
    private DatabaseReference reference;

//    public PostAdapter (Context context, List<Post> posts, String category, String path) {
//        mContext = context;
//        mPost = posts;
//        this.category = category;
//        this.path = path;
//    }

    public PostAdapter (Context context, List<Post> posts, String category, String path, String location) {
        mContext = context;
        mPost = posts;
        this.category = category;
        this.path = path;
        this.location = location;
    }

    @NonNull
    @Override
    public PostAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false);
        return new PostAdapter.ImageViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mPost.size();
    }

    @Override
    public void onBindViewHolder(@NonNull final PostAdapter.ImageViewHolder holder, final int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        email = firebaseUser.getEmail();

        Log.e(TAG, "!@#" + path + " " + location);
        if (path.equals("admin")) {
            reference = FirebaseDatabase.getInstance().getReference(path);

        } else if (path.equals("public")) {
            reference = FirebaseDatabase.getInstance().getReference(path).child(location);
        }

        final Post post = mPost.get(position);

        holder.description.setText(post.getDescription());
        getUserInfo(holder.image_profile, holder.username, post.getPublisherid());

        long timestamp = Long.parseLong(post.getTimestamp());
        String dateString = getDateCurrentTimeZone(timestamp);
        holder.createdtime.setText(dateString);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, PostPageActivity.class);
                intent.putExtra("post", post);
                intent.putExtra("area", location);
                intent.putExtra("path", path);
                mContext.startActivity(intent);
            }
        });

        /**
         * On long Click Delete comment dialog pop up
         */
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (post.getPublisherid().equals(email)) {
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
                                    reference.child(post.getPostid())
                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(mContext, "Deleted!", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                                    if (post.getPostimage() != null) {

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

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        public ImageView image_profile, post_image;
        public TextView username, description, createdtime;
        public ProgressBar progressBar;

        public ImageViewHolder(View itemView) {
            super(itemView);
            // progressBar = itemView.findViewById((R.id.comment_loading));
            image_profile = itemView.findViewById(R.id.admin_profile);
            username = itemView.findViewById(R.id.post_username);
            description = itemView.findViewById(R.id.post_description);
            createdtime = itemView.findViewById(R.id.post_createdtime);
            post_image = itemView.findViewById(R.id.post_image);
        }

    }


    /**
     * To set username
     */
    private void getUserInfo(final ImageView imageView, final TextView username, String publisherid){

        if (publisherid != null) {
            Log.e(TAG, "User: " + publisherid);
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
}
