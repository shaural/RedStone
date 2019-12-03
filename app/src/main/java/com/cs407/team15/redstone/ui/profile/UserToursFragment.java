package com.cs407.team15.redstone.ui.profile;

import android.content.Intent;
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
import com.cs407.team15.redstone.ui.viewtours.TourInfoActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.cs407.team15.redstone.ui.viewtours.RecyclerAdapter;
import java.util.ArrayList;

import static androidx.core.content.ContextCompat.startActivity;

public class UserToursFragment extends Fragment implements RecyclerAdapter.ItemClickListener{
    private FirebaseFirestore db;
    private CollectionReference col;
    private FirebaseAuth mAuth;
    private RecyclerAdapter adapter;
    private View view;
    ViewGroup container2;
    ArrayList<String> tourList=new ArrayList<>();

    /**
     * create initival view for this fragment
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        view = inflater.inflate(R.layout.fragment_view_tours,
                container, false);
        container2=container;
        /*
        Get the tours associated with the user
         */
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

    /**
     * Sets up the recycler view to see all entries
     * @param list
     */
    public void fillRecycleViewer(ArrayList<String> list) {
        tourList = list;
        RecyclerView recyclerView = view.findViewById(R.id.tourList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecyclerAdapter(getContext(), list);
        adapter.setClickListener$app_debug(this);
        //adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        view.invalidate();
    }
    // need to add logic for when tour page is created

    /**
     * will be the logic to take users to a specific tour page in the future.
     * @param view
     * @param position
     */
    @Override
    public void onItemClick(View view, int position) {
        final View view2 = view;
        final String name = tourList.get(position);
        db.collection("tours").whereEqualTo("name", name).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                Intent in = new Intent(getContext(), TourInfoActivity.class);
                in.putExtra("tourName", name);
                in.putExtra("tourID", queryDocumentSnapshots.getDocuments().get(0).getId());
                Bundle bundle = new Bundle();
                startActivity(in);
                //startActivity(getContext(), in, bundle);
            }
        });

//        view.setOnClickListener{
//            val intent = Intent(view.context, TourInfoActivity::class.java)
//            intent.putExtra("tourName", allTours[position].name)
//
//            var tourID = ""
//            FirebaseFirestore.getInstance().collection("tours").get()
//                    .addOnSuccessListener { tour ->
//                for (t in tour.documents){
//                    if (t["name"] as String == allTours[position].name) {
//                        tourID = t.id
//                        intent.putExtra("tourID", tourID)
//                        //Toast.makeText(context, tourID, Toast.LENGTH_SHORT).show()
//                        view.context.startActivity(intent)
//                    }
//                }
//            }
//            //Toast.makeText(context, tourID, Toast.LENGTH_SHORT).show()
//            //intent.putExtra("tourID", tourID)
//            //view.context.startActivity(intent)
//        }
    }
}
