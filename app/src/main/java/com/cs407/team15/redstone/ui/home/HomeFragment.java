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
    private String TAG = getClass().getName();

    private ViewPager viewPager;
    private TabsPagerAdapter tabsPagerAdapter;
    private TabLayout mTabLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        viewPager = (ViewPager) v.findViewById(R.id.home_vp);
        tabsPagerAdapter = new TabsPagerAdapter(getActivity(), getChildFragmentManager());
        viewPager.setAdapter(tabsPagerAdapter);

        mTabLayout = v.findViewById(R.id.home_vp_tab);
        mTabLayout.setupWithViewPager(viewPager);
        mTabLayout.getTabAt(3).setIcon(R.drawable.ic_notification);

        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


}
