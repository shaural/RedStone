package com.cs407.team15.redstone.ui.profile;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.cs407.team15.redstone.MainActivity;
import com.cs407.team15.redstone.R;
import com.cs407.team15.redstone.model.Comment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import com.cs407.team15.redstone.model.Tour;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private List<Tour> mData;
    private List<String> tourIDList;
    private LayoutInflater mInflater;
    private Context context;
    private ItemClickListener mClickListener;
    private ArrayList<Comment> comments;
    private  UserToursFragment userTourFrag;

    // data is passed into the constructor
    RecyclerAdapter(Context context, List<Tour> data,List<String> tourIDList, UserToursFragment userToursFrag) {
        this.mInflater = LayoutInflater.from(context);
        this.context=context;
        this.mData = data;
        this.userTourFrag = userToursFrag;
        this.tourIDList=tourIDList;
    }
    RecyclerAdapter(Context context, List<String> data) {
        this.mInflater = LayoutInflater.from(context);
        this.context=context;
        this.tourIDList=data;
    }

    RecyclerAdapter(Context context, List<String> data, ArrayList<Comment> comments) {
        this.mInflater = LayoutInflater.from(context);
        //this.mData = data;

        this.tourIDList=data;
        this.comments = comments;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycler_row_uc, parent, false);
        //for user tour list page, change text of button
        Button btn = view.findViewById(R.id.delete_tag_button);
        if (comments == null) {
            btn = view.findViewById(R.id.delete_tag_button);
            btn.setText("DELETE TOUR");
        }
        else{
            btn.setText("Delete Tag");
        }
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        if(userTourFrag!=null){
        String animal = mData.get(position).getName();
        holder.myTextView.setText(animal);
            /*holder.editTourButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    mClickListener.onEditClick(mData.get(position),tourIDList.get(position));
                }
            });*/

        }else{
            String animal = tourIDList.get(position);
            holder.myTextView.setText(animal);
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return tourIDList.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView myTextView;
        Button delButton;
        ViewHolder(final View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.tvAnimalName);
            delButton = itemView.findViewById(R.id.delete_tag_button);
            itemView.setOnClickListener(this);
            delButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (delButton.getText().equals("DELETE TOUR")) {
                        final FirebaseFirestore db = FirebaseFirestore.getInstance(); //watch this
                        final View v2 = v;
                        db.collection("tours")
                                .whereEqualTo("user_id", FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .whereEqualTo("name", mData.get(getAdapterPosition())).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    //ArrayList<String> list = new ArrayList<>();
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        db.collection("tours").document(document.getId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast toast = Toast.makeText(v2.getContext(), "Tour deleted", Toast.LENGTH_SHORT);
                                                toast.show();
                                                mData.remove(getAdapterPosition());
                                                notifyItemChanged(getAdapterPosition());
                                                //RecyclerAdapter.this.notifyItemChanged(getAdapterPosition());
                                            }
                                        });
                                    }
                                }
                            }
                        });
                    }
                    else{
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(comments.get(getAdapterPosition()).getPath()).child(comments.get(getAdapterPosition()).getLocationId())
                            .child(comments.get(getAdapterPosition()).getCommentid()).child("tags");

                    reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast toast = Toast.makeText(itemView.getContext(), "Removed Tag", Toast.LENGTH_SHORT);
                            toast.show();
                            //notifyItemChanged(getAdapterPosition());
                        }
                    });
                }
                }
            });
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return tourIDList.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
        void onEditClick(Tour tour , String tourId);
    }
}
