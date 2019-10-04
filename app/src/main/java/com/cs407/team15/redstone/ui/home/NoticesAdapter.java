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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.cs407.team15.redstone.R;
import com.cs407.team15.redstone.model.Notices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class NoticesAdapter extends RecyclerView.Adapter<NoticesAdapter.ViewHolder> {

    Context context;
    private String TAG = getClass().getName();
    private ArrayList<Notices> noticeList;
    private RecyclerView recyclerView;

    private OnNoticeListener mOnNoticeListener;

    public NoticesAdapter(Context context, ArrayList<Notices> noticeList, OnNoticeListener onNoticeListener) {
        this.context = context;
        this.noticeList = noticeList;
        this.mOnNoticeListener = onNoticeListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.notice_recycle_view_item,null);

        return new ViewHolder(v, mOnNoticeListener);

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
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Notices notice = noticeList.get(position);
                noticeList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, noticeList.size());
                // Record that the user dismissed the notification
                FirebaseFirestore.getInstance().collection("users")
                        .document(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                        .collection("notices").whereEqualTo("notice_id", notice.getNotice_id())
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && !task.getResult().isEmpty()) {
                            task.getResult().getDocuments().get(0).getReference().delete();
                        }
                    }
                });
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return this.noticeList.size();
    }

    /** item layout **/
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView tv_title;
        TextView tv_date;
        TextView tv_content;
        TextView tv_writer;
        CardView cv;

        OnNoticeListener onNoticeListener;

        public ViewHolder(View v, OnNoticeListener onNoticeListener) {
            super(v);
            tv_title = (TextView) v.findViewById(R.id.tv_title);
            tv_date = (TextView) v.findViewById(R.id.tv_date);
            tv_content = (TextView) v.findViewById(R.id.tv_content);
            tv_writer = (TextView) v.findViewById(R.id.tv_writer);
            cv = (CardView) v.findViewById(R.id.cv);

            this.onNoticeListener = onNoticeListener;
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onNoticeListener.onNoticeClick(getAdapterPosition());
        }
    }

    public interface OnNoticeListener{
        void onNoticeClick(int position);
    }

}
