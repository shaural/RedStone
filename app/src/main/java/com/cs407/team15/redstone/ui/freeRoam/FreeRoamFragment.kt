package com.cs407.team15.redstone.ui.freeRoam

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.cs407.team15.redstone.R
import com.cs407.team15.redstone.ui.ar.ARFragment
import com.cs407.team15.redstone.ui.tour.TourFragment
import com.google.android.filament.View


class FreeRoamFragment : Fragment() {
    fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle
    ): View {
        val v = inflater.inflate(R.layout.fragment_free_roam, container, false) as View
        val list = ArrayList<Fragment>()

        list.add(ARFragment())
        list.add(TourFragment())

        val pager = (R.id.viewpager) as ViewPager
        pager.adapter = object : FragmentPagerAdapter(activity!!.supportFragmentManager) {
            override fun getItem(i: Int): Fragment {
                return list[i]
            }

            override fun getCount(): Int {
                return list.size
            }
        }
        return v
    }
}