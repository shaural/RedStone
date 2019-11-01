package com.cs407.team15.redstone.ui.freeRoam

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
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
            Toast.makeText(context, "AR", Toast.LENGTH_LONG).show()
            val nextFrag = ARFragment()
            activity!!.supportFragmentManager.beginTransaction()
                .replace(id, nextFrag, "ARFragment")
                .addToBackStack(null) //maybe should add
                .commit()
        }
        btn_map.setOnClickListener{
            Toast.makeText(context, "MAP", Toast.LENGTH_LONG).show()
            val nextFrag = TourFragment()
            activity!!.supportFragmentManager.beginTransaction()
                .replace(id, nextFrag, "TourFragment")
                .addToBackStack(null) //maybe should add
                .commit()
        }
    }
    //    fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup,
//        savedInstanceState: Bundle
//    ): View {
//        val v = inflater.inflate(R.layout.fragment_free_roam, container, false) as View
//        val list = ArrayList<Fragment>()
//
//        list.add(ARFragment())
//        list.add(TourFragment())
//
//        val pager = (R.id.viewpager) as ViewPager
//        pager.adapter = object : FragmentPagerAdapter(activity!!.supportFragmentManager) {
//            override fun getItem(i: Int): Fragment {
//                return list[i]
//            }
//
//            override fun getCount(): Int {
//                return list.size
//            }
//        }
//        return v
//    }
//    private var tabHost: FragmentTabHost? = null
//
//    fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup,
//        savedInstanceState: Bundle
//    ): FragmentTabHost? {
//        tabHost = FragmentTabHost(activity!!)
//        tabHost!!.setup(activity, childFragmentManager, R.layout.fragment_free_roam)
//
//        val arg1 = Bundle()
//        arg1.putInt("Arg for Frag1", 1)
//        tabHost!!.addTab(
//            tabHost!!.newTabSpec("Tab1").setIndicator("Frag Tab1"),
//            ARFragment::class.java!!, arg1
//        )
//
//        val arg2 = Bundle()
//        arg2.putInt("Arg for Frag2", 2)
//        tabHost!!.addTab(
//            tabHost!!.newTabSpec("Tab2").setIndicator("Frag Tab2"),
//            TourFragment::class.java!!, arg2
//        )
//
//        return tabHost
//    }
}