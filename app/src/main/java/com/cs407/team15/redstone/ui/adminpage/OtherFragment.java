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


public class OtherFragment extends Fragment {
    private final String TAG = getClass().toString();

    public OtherFragment() {
        // Required empty public constructor
    }


    public static OtherFragment newInstance() {
        return new OtherFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_other, container, false);

        return root;
    }

}
