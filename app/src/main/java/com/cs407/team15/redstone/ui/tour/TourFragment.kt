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
import android.util.Log


class TourFragment : Fragment(), OnMapReadyCallback{

    private lateinit var mMap: GoogleMap
    private lateinit var tourViewModel: TourViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_tour, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.tour_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return root
    }
    fun addLocation(location: LatLng, title:String){
       mMap.addMarker(MarkerOptions().position(location).title(title))
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled=true
        mMap.setMinZoomPreference(6f)
        val purdue = LatLng(40.4237,-86.9212)
        mMap.addMarker(MarkerOptions().position(purdue).title("PURDUE"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(purdue))

        mMap.setOnMapClickListener(
            object : GoogleMap.OnMapClickListener {
            override fun onMapClick(location: LatLng?) {
                //gets Latitude & Longitude where user Clicked
            }
        }
        )
    }
}