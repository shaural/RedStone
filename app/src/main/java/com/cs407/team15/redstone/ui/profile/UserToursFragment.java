package com.cs407.team15.redstone.ui.profile;

import android.os.Bundle;
import android.util.Log;
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
import com.cs407.team15.redstone.model.Tour;
import com.cs407.team15.redstone.ui.tour.AddTourFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

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
                            ArrayList<Tour> list = new ArrayList<>();
                            ArrayList<String> tourIDList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //populate some recycler view
                                tourIDList.add((String) document.getReference().getPath().split("/")[0]);
                                Tour tour = new Tour((String)document.get("name"),(String)document.get("type"),(String)document.get("user_id"),(Boolean)document.get("hammer"),(List<String>) document.get("locations"),(List<String>)document.get("tags"));
                                list.add(tour);
                                //Log.d(TAG, document.getId() + " => " + document.getData());
                            }
                            if (list.isEmpty()) {
                                Toast toast = Toast.makeText(getContext(),"No Tours Created",Toast.LENGTH_SHORT);
                                toast.show();
                                getActivity().onBackPressed();
                            }
                            else {
                                fillRecycleViewer(list,tourIDList);
                            }
                        }
                        else {
                            System.out.println("ERROR GETTING DOCS");
                        }

                    }
                });
        return view;

    }
    public void fillRecycleViewer(ArrayList<Tour> list, ArrayList<String> tourIDList) {
        RecyclerView recyclerView = view.findViewById(R.id.usertourlist);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecyclerAdapter(getContext(), list,tourIDList,this);
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


    @Override
    public void onEditClick(Tour tour , String tourId) {
        FragmentManager  frag = getFragmentManager();
        FragmentTransaction ft=frag.beginTransaction();
        Bundle bundle = new  Bundle();
        bundle.putString("title",tour.getName());
        bundle.putString("tourId",tourId);
        bundle.putString("type",tour.getType());
        bundle.putStringArrayList("tags",new ArrayList(tour.getTags()));
        bundle.putStringArrayList("locations",new ArrayList(tour.getLocations()));
        AddTourFragment loc= new AddTourFragment();
        loc.setArguments(bundle);
        ft.replace(container2.getId(),loc);
        //frag.replace((view!!.parent as ViewGroup).id, loc)
        ft.addToBackStack(null);
        ft.commit();
    }
}
