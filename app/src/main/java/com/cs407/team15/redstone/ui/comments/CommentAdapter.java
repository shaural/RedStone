package com.cs407.team15.redstone.ui.comments;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.EventListener;
import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ImageViewHolder> {
    private String TAG = getClass().toString();
    private Context mContext;
    private List<Comment> mComment;
    private String postid;
    private String email;
    private String path;
    private String location;

    private FirebaseUser firebaseUser;
    private FirebaseFirestore db;
    private CollectionReference userref;
    public static final String COLLECTION_NAME_KEY = "users";



    public CommentAdapter(Context context, List<Comment> comments, String postid, String path){
        mContext = context;
        mComment = comments;
        this.postid = postid;
        this.path = path;
        this.location = location;
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

        holder.comment.setText(comment.getComment());
        holder.score.setText(comment.getLike().toString());
        getUserInfo(holder.image_profile, holder.username, comment.getPublisher());

        //holder.score.setText(comment.getCommentScore());
        holder.up_vote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                commentVote(comment,true);
            }
        });
        holder.down_vote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                commentVote(comment,false);
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
                                    FirebaseDatabase.getInstance().getReference("Comments").child(path)
                                            .child(postid).child(comment.getCommentid())
                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                Toast.makeText(mContext, "Deleted!", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
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

        public ImageView image_profile;
        public TextView username, comment,score;
        public ProgressBar progressBar;
        public Button up_vote,down_vote;

        public ImageViewHolder(View itemView) {
            super(itemView);
            progressBar = itemView.findViewById((R.id.comment_loading));
            image_profile = itemView.findViewById(R.id.image_profile);
            username = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.comment);
            up_vote = itemView.findViewById(R.id.up_vote_comment);
            down_vote = itemView.findViewById(R.id.down_vote_comment);
            score = itemView.findViewById(R.id.comment_score);

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
    private void commentVote(Comment comment, final Boolean up){

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        if(up){
        comment.setLike(comment.getLike()+1);}
        else{
            comment.setLike(comment.getLike()-1);
        }
        database.child("Comments").child("location").child(postid).child(comment.getCommentid()).setValue(comment);
        comment.getPublisher();

         db.collection("users").get().addOnSuccessListener(
                 new OnSuccessListener<QuerySnapshot>() {
                     @Override
                     public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                         for(QueryDocumentSnapshot queryDocumentSnapshot: queryDocumentSnapshots){
                             User user=queryDocumentSnapshot.toObject(User.class);
                             if(up){
                             user.recievedLikes=user.recievedLikes+1;}
                             else{
                                 user.recievedLikes=user.recievedLikes-1;
                             }

                             db.collection("users").document(user.email).set(user);
                         }
                     }
                 }
         );
        String useremail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

      db.collection("users").document(useremail).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
          @Override
          public void onSuccess(DocumentSnapshot documentSnapshot) {
              User user = documentSnapshot.toObject(User.class);
              if(up){
              user.userLikes=1+user.userLikes;}
              else{
                  user.userLikes=user.userLikes-1;
              }
              db.collection("users").document(user.email).set(user);
          }
      });

    }

}
