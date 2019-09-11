package com.cs407.team15.redstone.ui.tour

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.cs407.team15.redstone.R

class TourFragment : Fragment() {

    private lateinit var tourViewModel: TourViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        tourViewModel =
            ViewModelProviders.of(this).get(TourViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_tour, container, false)
        val textView: TextView = root.findViewById(R.id.text_tour)
        tourViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }
}