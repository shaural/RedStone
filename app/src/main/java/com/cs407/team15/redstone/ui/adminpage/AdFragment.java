package com.cs407.team15.redstone.ui.adminpage;


import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cs407.team15.redstone.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdFragment extends Fragment {
    private final String TAG = getClass().toString();

    public AdFragment() {
        // Required empty public constructor
    }


    public static AdFragment newInstance() {
        return new AdFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_ad, container, false);


        return root;
    }

}
