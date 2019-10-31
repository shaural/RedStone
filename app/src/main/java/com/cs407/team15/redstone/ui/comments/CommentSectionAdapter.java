package com.cs407.team15.redstone.ui.comments;

import android.content.Context;
import android.content.DialogInterface;
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
import com.cs407.team15.redstone.model.Comment;
import com.cs407.team15.redstone.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * This is RecyclerView Adapter for Comment Section on any pages
 * It displays limited number of comments on RecyclerView
 * Can change the limit number to display comments
 */

public class CommentSectionAdapter extends RecyclerView.Adapter<CommentSectionAdapter.ImageViewHolder> {
    private String TAG = getClass().toString();
    private Context mContext;
    private List<Comment> mComment;

    private final int limit = 3;

    private FirebaseUser firebaseUser;
    private FirebaseFirestore db;
    private CollectionReference userref;
    public static final String COLLECTION_NAME_KEY = "users";



    public CommentSectionAdapter(Context context, List<Comment> comments){
        mContext = context;
        mComment = comments;
    }


    @NonNull
    @Override
    public CommentSectionAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.comment_item_section, parent, false);
        return new CommentSectionAdapter.ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CommentSectionAdapter.ImageViewHolder holder, final int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final Comment comment = mComment.get(position);

        holder.comment.setText(comment.getComment());
        holder.score.setText(comment.getLike().toString());
        getUserInfo(holder.image_profile, holder.username, comment.getPublisher());
        holder.comment.setText("aaaaaaa");

    }

    @Override
    public int getItemCount() {
        if(mComment.size() > limit){
            return limit;
        }
        else
        {
            return mComment.size();
        }
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        public ImageView image_profile;
        public TextView username, comment,score;

        public ImageViewHolder(View itemView) {
            super(itemView);
            image_profile = itemView.findViewById(R.id.image_profile);
            username = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.comment);
            score = itemView.findViewById(R.id.comment_score);
        }
    }

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

}
