package com.cs407.team15.redstone.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.cs407.team15.redstone.R;
import com.cs407.team15.redstone.model.AdminPost;
import com.cs407.team15.redstone.model.Notices;
import com.cs407.team15.redstone.ui.adminpage.PostAdapter;
import com.cs407.team15.redstone.ui.adminpage.TabsPagerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 *  Home fragment
 */
public class HomeFragment extends Fragment {

//    private ArrayList<Notices> noticesArrayList = new ArrayList<>();
//    private RecyclerView recyclerView;
//    private NoticesAdapter mAdapter;
//    private FirebaseAuth mAuth;
//    private FirebaseFirestore db;
//    private CollectionReference userref;

    private String TAG = getClass().getName();
    public static final String COLLECTION_NAME_KEY = "users";

    private ArrayList<AdminPost> postArrayList = new ArrayList<>();
    private RecyclerView recyclerView;
    private static Context context;
    private PostAdapter mAdapter;
    private FirebaseDatabase db;

    private ViewPager viewPager;
    private TabsPagerAdapter tabsPagerAdapter;
    private TabLayout mTabLayout;

    private FirebaseFirestore noticeDB;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);

//        //recyclerview
//        recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
//        recyclerView.setHasFixedSize(true);
//
//
//        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
//        recyclerView.setLayoutManager(mLayoutManager);
//        recyclerView.setItemAnimator(new DefaultItemAnimator());
//
////        mAdapter = new NoticesAdapter(getActivity(), noticesArrayList, this);
//        mAdapter = new PostAdapter(getActivity(), postArrayList, "notification");
//
//        recyclerView.setAdapter(mAdapter);


        viewPager = (ViewPager) v.findViewById(R.id.home_vp);
        tabsPagerAdapter = new TabsPagerAdapter(getActivity(), getChildFragmentManager());
        viewPager.setAdapter(tabsPagerAdapter);

        mTabLayout = v.findViewById(R.id.home_vp_tab);
        mTabLayout.setupWithViewPager(viewPager);


        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       // prepareData();
    }

//    private void prepareData() {
//
//        // Get notices from DB here
//
//        noticeDB = FirebaseFirestore.getInstance();
//        // Get admin notifications
//        noticeDB.collection("admin").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//            @Override
//            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                if(!queryDocumentSnapshots.isEmpty()){
//
//                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
//                    for(DocumentSnapshot d: list){
//                        Notices n = d.toObject(Notices.class);
//                        noticesArrayList.add(new Notices(n.getWriter(), n.getTitle(), n.getContent(), n.getDate(), n.getNotice_id()));
//                    }
//
//                    mAdapter.notifyDataSetChanged();
//                }
//            }
//        });
//
//        // Get normal notifications
//        noticeDB.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getEmail())
//            .collection("notices").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//            @Override
//            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                if(!queryDocumentSnapshots.isEmpty()){
//
//                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
//                    for(DocumentSnapshot d: list){
//                        Notices n = d.toObject(Notices.class);
//                        noticesArrayList.add(new Notices(n.getWriter(), n.getTitle(), n.getContent(), n.getDate(), n.getNotice_id()));
//                    }
//
//                    mAdapter.notifyDataSetChanged();
//                }
//            }
//        });
//
//    }

    private void prepareData() {
        db = FirebaseDatabase.getInstance();
        db.getReference("AdminPost")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        postArrayList.clear();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            AdminPost post = snapshot.getValue(AdminPost.class);
                            Log.e(TAG, post.getPostid()+": "+post.getCategory());
                            if (post.getCategory().equals("Notification")) {
                                postArrayList.add(post);
                            }
                        }
                        mAdapter.notifyDataSetChanged();
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

//    @Override
//    public void onNoticeClick(int position) {
//        HomeFragment.context = getActivity().getApplicationContext();
//        CharSequence text = "Position: " + position;
//        int duration = Toast.LENGTH_LONG;
//        Toast.makeText(HomeFragment.context, text, duration).show();
//
//        Intent intent = new Intent(HomeFragment.context, NoticeActivity.class);
//        intent.putExtra("selected_notice", noticesArrayList.get(position));
//        startActivity(intent);
//    }
}
