package com.cs407.team15.redstone.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.cs407.team15.redstone.R

class OBFragment : Fragment() {


    private lateinit var obViewModel: OBViewModel
    private lateinit var viewPager: ViewPager
    private lateinit var adaptor: SlideAdaptor

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        obViewModel =
            ViewModelProviders.of(this).get(OBViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_onboarding, container, false)

        viewPager = view?.findViewById(R.id.viewPager_id)!!

        adaptor = SlideAdaptor(context)
        viewPager.adapter = adaptor

        return root
    }
}