package com.cs407.team15.redstone.ui.profile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cs407.team15.redstone.R;
import com.cs407.team15.redstone.model.Tour;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private List<Tour> mData;
    private List<String> tourIDList;
    private LayoutInflater mInflater;
    private Context context;
    private ItemClickListener mClickListener;
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

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recycler_row, parent, false);

        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        if(userTourFrag!=null){
        String animal = mData.get(position).getName();
        holder.myTextView.setText(animal);
            holder.editTourButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    mClickListener.onEditClick(mData.get(position),tourIDList.get(position));
                }
            });

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
        Button editTourButton;

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.tvAnimalName);
            editTourButton = itemView.findViewById(R.id.edit_tour_button);
            itemView.setOnClickListener(this);
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
