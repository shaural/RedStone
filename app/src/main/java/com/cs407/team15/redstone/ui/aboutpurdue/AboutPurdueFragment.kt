package com.cs407.team15.redstone.ui.aboutpurdue

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.cs407.team15.redstone.R

class AboutPurdueFragment : Fragment() {

    private lateinit var aboutPurdueViewModel: AboutPurdueViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        aboutPurdueViewModel =
            ViewModelProviders.of(this).get(AboutPurdueViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_about_purdue, container, false)
        val textView: TextView = root.findViewById(R.id.text_about_purdue)
        aboutPurdueViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }
}