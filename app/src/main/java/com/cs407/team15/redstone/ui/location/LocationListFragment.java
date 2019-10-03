package com.cs407.team15.redstone.ui.location;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        col = db.collection("locations");
        View view = inflater.inflate(R.layout.fragment_location_list,
                container, false);
        col.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<String> list = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        list.add((String)document.get("name"));
                    }
                    listTransfer(list);
                    Log.d(TAG, list.toString());
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
        RecyclerView recyclerView = view.findViewById(R.id.locationlist);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        RecyclerAdapter adapter = new RecyclerAdapter(getContext(), locList);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        return view;
    }
    public void listTransfer(List<String> list) {
        locList = (ArrayList)list;
    }

    @Override
    public void onItemClick(View view, int position) {
        //redirect to location page
    }
}
