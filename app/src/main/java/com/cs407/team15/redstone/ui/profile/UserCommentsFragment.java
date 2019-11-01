package com.cs407.team15.redstone.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cs407.team15.redstone.R;
import com.cs407.team15.redstone.model.Comment;
import com.cs407.team15.redstone.model.Tour;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

public class UserCommentsFragment extends Fragment implements RecyclerAdapter.ItemClickListener {
    private ArrayList<String> commentList;
    private String userEmail;
    private View view;
    private RecyclerAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        commentList = new ArrayList<>();
        view = inflater.inflate(R.layout.fragment_usercomments,
                container, false);
        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child("location");
       // reference.s
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    //iterator loop for snapshot.getChildren
                    Iterator<DataSnapshot> iterator = snapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        DataSnapshot temp = iterator.next();
                        //System.out.println(temp.child("publisher"));
                        if (temp.child("publisher").getValue().equals(userEmail)) {
                            Comment comment = temp.getValue(Comment.class);
                            commentList.add(comment.toString());
                        }
                    }


                }
                if (commentList.isEmpty()) {
                    Toast toast = Toast.makeText(getContext(),"No Comments Created",Toast.LENGTH_SHORT);
                    toast.show();
                    getActivity().onBackPressed();
                }
                else {
                    fillRecycleViewer(commentList);
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast toast = Toast.makeText(getContext(),"Database Error",Toast.LENGTH_SHORT);
                                toast.show();
                                getActivity().onBackPressed();
            }
        });
        return view;
    }

    public void fillRecycleViewer(ArrayList<String> list) {
        RecyclerView recyclerView = view.findViewById(R.id.usercommentlist);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecyclerAdapter(getContext(), list);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        view.invalidate();
    }



    @Override
    public void onItemClick(View view, int position) {

    }

    @Override
    public void onEditClick(Tour tour , String tourId) {

    }
}
