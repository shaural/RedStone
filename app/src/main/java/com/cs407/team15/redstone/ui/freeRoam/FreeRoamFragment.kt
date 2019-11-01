package com.cs407.team15.redstone.ui.freeRoam

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.cs407.team15.redstone.R
import com.cs407.team15.redstone.ui.ar.ARFragment
import com.cs407.team15.redstone.ui.tour.TourFragment


class FreeRoamFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_free_roam, container, false)

        return root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var btn_ar = getView()!!.findViewById(R.id.launch_ar_fragment) as Button
        var btn_map = getView()!!.findViewById(R.id.launch_map_fragment) as Button

        btn_ar.setOnClickListener{
            val nextFrag = ARFragment()
            activity!!.supportFragmentManager.beginTransaction()
                .replace(id, nextFrag, "ARFragment")
                .addToBackStack(null) //maybe should add
                .commit()
        }
        btn_map.setOnClickListener{
            val nextFrag = TourFragment()
            activity!!.supportFragmentManager.beginTransaction()
                .replace(id, nextFrag, "TourFragment")
                .addToBackStack(null) //maybe should add
                .commit()
        }
    }
}