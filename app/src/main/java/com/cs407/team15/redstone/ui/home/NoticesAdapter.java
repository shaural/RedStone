package com.cs407.team15.redstone.ui.home;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.cs407.team15.redstone.R;
import com.cs407.team15.redstone.model.Notices;

import java.util.ArrayList;

public class NoticesAdapter extends RecyclerView.Adapter<NoticesAdapter.MyViewHolder> {

    private String TAG = getClass().getName();
    private ArrayList<Notices> mDataset;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView description;

        //ViewHolder
        public MyViewHolder(View view) {
            super(view);
            description = (TextView) view.findViewById(R.id.notice_description);
        }
    }

    public NoticesAdapter(ArrayList<Notices> myData){
        this.mDataset = myData;
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notice_recycle_view_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoticesAdapter.MyViewHolder holder, int position) {

        holder.description.setText(mDataset.get(position).getDescription());

        //클릭이벤트
        holder.description.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, ": notice clicked");
            }
        });

    }

}
