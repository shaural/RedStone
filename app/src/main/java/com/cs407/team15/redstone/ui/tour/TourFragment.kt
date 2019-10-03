package com.cs407.team15.redstone.ui.tour

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
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
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.cs407.team15.redstone.ui.location.LocationPage
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.location_display.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch





class TourFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener{


    var isAddLocationClicked = false
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
    fun addLocation(location: LatLng, title:String, markerIcon: BitmapDescriptor){
       mMap.addMarker(MarkerOptions().position(location).title(title).icon(markerIcon))
    }
    suspend fun addAllKnownLocations() {
        val locations = FirebaseFirestore.getInstance().collection("locations").get().await()
        val markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.marker)
        // Now that the location data has been fetched, we can quickly add all the markers
        activity!!.runOnUiThread {
            for (location in locations.documents) {
                val title = location.get("name") as String
                val gpsPoint = location.get("coordinates") as GeoPoint
                val latLng = LatLng(gpsPoint.latitude, gpsPoint.longitude)
                addLocation(latLng, title, markerIcon)
            }
        }
    }
    override fun onMarkerClick(marker:Marker?):Boolean{
        val frag = fragmentManager!!.beginTransaction()
        val bundle = Bundle()
        bundle.putString("title",marker?.title)
        val loc=LocationPage()
        loc.arguments=bundle
        frag.replace((view!!.parent as ViewGroup).id, loc)
       // frag.addToBackStack(null)
        frag.commit()

        return false
    }
    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled=true
        mMap.setMinZoomPreference(14f)

        FirebaseFirestore.getInstance().collection("schools").document("Purdue").get().addOnSuccessListener(
            OnSuccessListener {
                val schoolLoc=it["coordinates"] as GeoPoint
                mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(schoolLoc.latitude,schoolLoc.longitude)))
            }
        )
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(activity, R.raw.mapstyle))
        GlobalScope.launch { addAllKnownLocations() }
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMapClickListener(
            object : GoogleMap.OnMapClickListener {
                override fun onMapClick(location: LatLng?) {
                    //gets Latitude & Longitude where user Clicked
                    if (isAddLocationClicked) {
                        isAddLocationClicked = false
                        val builder = AlertDialog.Builder(context)
                        builder.setTitle("Add Location")
                        builder.setMessage("Are you sure you want to add the location at the coordinates: ${location}?")
                        builder.setPositiveButton("YES"){dialog, which ->
                            Toast.makeText(context, "Add Location", Toast.LENGTH_SHORT).show()
                            val newFragment = AddLocationFragment()
                            val bundle = Bundle()
                            val gp = GeoPoint(location!!.latitude, location.longitude)
                            bundle.putDouble("latitude", gp.latitude)
                            bundle.putDouble("longitude", gp.longitude)
                            newFragment.arguments = bundle
                            val transaction = fragmentManager!!.beginTransaction()
                            transaction.replace(id, newFragment)
                            transaction.addToBackStack(null)
                            transaction.commit()
                        }

                        builder.setNegativeButton("No"){dialog,which ->
                            Toast.makeText(context,"Click the Add Location Button to try again.", Toast.LENGTH_SHORT).show()
                        }

                        val dialog: AlertDialog = builder.create()
                        dialog.show()
                    }
                }
            }
        )
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val add_location_btn = getView()!!.findViewById(R.id.add_location_button) as Button

        add_location_btn.setOnClickListener{
            Toast.makeText(context, "Click on the map where you would like to add your location.", Toast.LENGTH_LONG).show()
            isAddLocationClicked = true
        }
    }
}

