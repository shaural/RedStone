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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class TourFragment : Fragment(), OnMapReadyCallback{

    private lateinit var tourViewModel: TourViewModel
    private  lateinit var mMap: GoogleMap
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        tourViewModel =
            ViewModelProviders.of(this).get(TourViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_tour, container, false)
        //val textView: TextView = root.findViewById(R.id.text_tour)
        //tourViewModel.text.observe(this, Observer {
          //  textView.text = it
            //val mapFragment = childFragmentManager.findFragmentById(R.id.tour_map) as SupportMapFragment
            //mapFragment.getMapAsync(this)
        //})

        val mapFragment = childFragmentManager.findFragmentById(R.id.tour_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return root
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setMaxZoomPreference(6.0f)
        mMap.uiSettings.isZoomControlsEnabled=true
        val purdue = LatLng(40.4237,-86.9212)
        mMap.addMarker(MarkerOptions().position(purdue).title("PURDUEEE"))
        mMap.animateCamera(CameraUpdateFactory.zoomIn())
        mMap.moveCamera(CameraUpdateFactory.newLatLng(purdue))
    }
}