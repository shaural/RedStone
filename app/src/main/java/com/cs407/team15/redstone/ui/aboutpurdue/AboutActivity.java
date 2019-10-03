package com.cs407.team15.redstone.ui.aboutpurdue;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.cs407.team15.redstone.R;
import com.cs407.team15.redstone.ui.location.LocationListFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AboutActivity extends Fragment {
    private FirebaseFirestore db;
    private DocumentReference docRef;
    String myPurdueUrl="http://www.mypurdue.purdue.edu";
    String BlackboardUrl="https://mycourses.purdue.edu/";
    String HomeUrl="http://www.purdue.edu";
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        docRef = db.collection("schools").document("Purdue");

        //super.onCreate(savedInstanceState);
        final ViewGroup container2 = container;
        View view = inflater.inflate(R.layout.fragment_about_purdue,
                container, false);
        final TextView info = (TextView) view.findViewById(R.id.aboutparagraph);
        // connect to firebase and get the about paragraph
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        info.setText((CharSequence)document.get("about"));
                        setBlackboardUrl(document.get("blackboardUrl").toString());
                        setHomeUrl(document.get("homeUrl").toString());
                        setMyPurdueUrl(document.get("myPurdueUrl").toString());
                        //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                    } else {
                        //Log.d(TAG, "No such document");
                    }
                } else {
                    //Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
        //setContentView(R.layout.fragment_about_purdue);
        Button dismiss, home, blackboard, mypurdue, locations;
        dismiss = (Button) view.findViewById(R.id.dismiss);
        home = (Button) view.findViewById(R.id.purduehome);
        blackboard = (Button) view.findViewById(R.id.bbbutton);
        mypurdue = (Button) view.findViewById(R.id.mypurdue);
        locations = (Button) view.findViewById(R.id.locationlisting);
        mypurdue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(myPurdueUrl)));
            }
        });
        blackboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(BlackboardUrl)));
            }
        });
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(HomeUrl)));
            }
        });
        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        locations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(container.getId(), new LocationListFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        return view;
    }

    public void setMyPurdueUrl(String myPurdueUrl) {
        this.myPurdueUrl = myPurdueUrl;
    }

    public void setHomeUrl(String homeUrl) {
        HomeUrl = homeUrl;
    }

    public void setBlackboardUrl(String blackboardUrl) {
        BlackboardUrl = blackboardUrl;
    }
}
