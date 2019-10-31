package com.cs407.team15.redstone.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cs407.team15.redstone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class UserToursFragment extends Fragment implements RecyclerAdapter.ItemClickListener{
    private FirebaseFirestore db;
    private CollectionReference col;
    private FirebaseAuth mAuth;
    private RecyclerAdapter adapter;
    private View view;
    ViewGroup container2;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        view = inflater.inflate(R.layout.fragment_userspecific_tours,
                container, false);
        container2=container;
        db.collection("tours")
                .whereEqualTo("user_id", mAuth.getCurrentUser().getUid()).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<String> list = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //populate some recycler view
                                list.add(document.get("name").toString());
                                //Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                            if (list.isEmpty()) {
                                Toast toast = Toast.makeText(getContext(),"No Tours Created",Toast.LENGTH_SHORT);
                                toast.show();
                                getActivity().onBackPressed();
                            }
                            else {
                                fillRecycleViewer(list);
                            }
                        }
                        else {
                            System.out.println("ERROR GETTING DOCS");
                        }

                    }
                });
        return view;

    }
    public void fillRecycleViewer(ArrayList<String> list) {
        RecyclerView recyclerView = view.findViewById(R.id.usertourlist);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecyclerAdapter(getContext(), list);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        view.invalidate();
    }
    // need to add logic for when tour page is created
    @Override
    public void onItemClick(View view, int position) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        //LocationPage lp = new LocationPage();
        Bundle bundle = new Bundle();
        RecyclerView recycle = view.findViewById(R.id.locationlist);
        bundle.putString("title", adapter.getItem(position));
        //lp.setArguments(bundle);
        //ft.replace(container2.getId(), lp);
        ft.addToBackStack(null);
        ft.commit();
    }
}
