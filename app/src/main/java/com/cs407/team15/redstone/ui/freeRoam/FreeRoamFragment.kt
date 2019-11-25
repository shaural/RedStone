package com.cs407.team15.redstone.ui.freeRoam

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.cs407.team15.redstone.R
import com.cs407.team15.redstone.ui.ar.ARFragment
import com.cs407.team15.redstone.ui.tour.TourFragment


class FreeRoamFragment : Fragment() {
    var isAR = true
    lateinit var nextFrag : Fragment
    lateinit var arFrag : Fragment
    lateinit var mapFrag : Fragment
    var fragmentContainerID: Int? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_free_roam, container, false)
        arFrag = ARFragment()
        mapFrag = TourFragment()
        nextFrag = arFrag
        return root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentContainerID = view!!.findViewById<FrameLayout>(R.id.fragmentContainer)!!.id
        activity!!.supportFragmentManager.beginTransaction()
            .replace(fragmentContainerID!!, nextFrag, "ARFragment")
//            .addToBackStack(null) //maybe should add
            .commit()

        var btn_ar = getView()!!.findViewById(R.id.launch_ar_fragment) as Button
        var btn_map = getView()!!.findViewById(R.id.launch_map_fragment) as Button

        btn_ar.setOnClickListener{
            if (!isAR) {
                nextFrag = arFrag
                activity!!.supportFragmentManager.beginTransaction()
                    .replace(fragmentContainerID!!, nextFrag, "ARFragment")
//                    .addToBackStack(null) //maybe should add
                    .commit()
                isAR = !isAR
            }
        }
        btn_map.setOnClickListener{
            if (isAR) {
                nextFrag = mapFrag
                activity!!.supportFragmentManager.beginTransaction()
                    .replace(fragmentContainerID!!, nextFrag, "MapFragment")
//                    .addToBackStack(null) //maybe should add
                    .commit()
                isAR = !isAR
            }
        }
    }

    override fun onDestroy() {
        var rem_ar = activity!!.supportFragmentManager.findFragmentByTag("ARFragment")
        if (rem_ar != null) {
            activity!!.supportFragmentManager.beginTransaction().remove(rem_ar).commit()
        }
        var rem_map = activity!!.supportFragmentManager.findFragmentByTag("MapFragment")
        if (rem_map != null) {
            activity!!.supportFragmentManager.beginTransaction().remove(rem_map).commit()
        }
        super.onDestroy()
    }
}