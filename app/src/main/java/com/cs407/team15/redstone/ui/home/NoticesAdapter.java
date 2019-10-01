package com.cs407.team15.redstone.ui.home;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.cs407.team15.redstone.R;
import com.cs407.team15.redstone.model.Notices;

import java.util.ArrayList;

public class NoticesAdapter extends RecyclerView.Adapter<NoticesAdapter.ViewHolder> {

    Context context;
    private String TAG = getClass().getName();
    private ArrayList<Notices> noticeList;

    public NoticesAdapter(Context context, ArrayList<Notices> noticeList) {
        this.context = context;
        this.noticeList = noticeList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.notice_recycle_view_item,null);
        return new ViewHolder(v);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    /** Do Event Handling **/

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Notices noticeItem = noticeList.get(position);
        holder.tv_writer.setText(noticeItem.getWriter()); // Author
        Log.e("[writer]", noticeItem.getWriter());
        holder.tv_title.setText(noticeItem.getTitle()); // Title
        holder.tv_content.setText(noticeItem.getContent()); // Content
        holder.tv_date.setText(noticeItem.getDate()); // Date
    }

    @Override
    public int getItemCount() {
        return this.noticeList.size();
    }

    /** item layout **/
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_title;
        TextView tv_date;
        TextView tv_content;
        TextView tv_writer;
        CardView cv;

        public ViewHolder(View v) {
            super(v);
            tv_title = (TextView) v.findViewById(R.id.tv_title);
            tv_date = (TextView) v.findViewById(R.id.tv_date);
            tv_content = (TextView) v.findViewById(R.id.tv_content);
            tv_writer = (TextView) v.findViewById(R.id.tv_writer);
            cv = (CardView) v.findViewById(R.id.cv);
        }
    }


}
