package com.cs407.team15.redstone.ui.location;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cs407.team15.redstone.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class LocationListFragment extends Fragment implements RecyclerAdapter.ItemClickListener{
    private FirebaseFirestore db;
    private CollectionReference col;
    private DocumentReference docRef;
    private ArrayList<String> locList;
    private View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        //col = db.collection("locations");
        view = inflater.inflate(R.layout.fragment_location_list,
                container, false);
        Button dismiss = (Button) view.findViewById(R.id.loclistdismiss);
        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        db.collection("locations")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<String> list = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.exists()) {
                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                    list.add(document.get("name").toString());
                                }
                                else {
                                    Log.d("michael", "no document exists");
                                }
                            }
                            fillRecycleViewer(list);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
//        RecyclerView recyclerView = view.findViewById(R.id.locationlist);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        RecyclerAdapter adapter = new RecyclerAdapter(getContext(), locList);
//        adapter.setClickListener(this);
//        recyclerView.setAdapter(adapter);
        return view;
    }

    public void fillRecycleViewer(ArrayList<String> list) {
        RecyclerView recyclerView = view.findViewById(R.id.locationlist);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        RecyclerAdapter adapter = new RecyclerAdapter(getContext(), list);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        view.invalidate();
    }
    public void listTransfer(ArrayList<String> list) {
        locList = list;
    }

    @Override
    public void onItemClick(View view, int position) {
        //redirect to location page
    }
}
