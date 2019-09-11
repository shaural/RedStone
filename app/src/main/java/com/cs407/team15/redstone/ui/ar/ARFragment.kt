package com.cs407.team15.redstone.ui.ar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.cs407.team15.redstone.R

class ARFragment : Fragment() {

    private lateinit var aRViewModel: ARViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        aRViewModel =
            ViewModelProviders.of(this).get(ARViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_ar, container, false)
        val textView: TextView = root.findViewById(R.id.text_ar)
        aRViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }
}