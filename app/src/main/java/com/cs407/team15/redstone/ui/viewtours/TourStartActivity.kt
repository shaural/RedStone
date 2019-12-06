package com.cs407.team15.redstone.ui.viewtours

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cs407.team15.redstone.R
import com.cs407.team15.redstone.ui.ar.ARFragment
import com.cs407.team15.redstone.ui.tour.TourFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import java.util.*

class TourStartActivity : AppCompatActivity(){
    var fragmentContainerID: Int? = null
    var isAR = true
    lateinit var nextFrag : Fragment
    lateinit var arFrag : Fragment
    lateinit var mapFrag : Fragment
    lateinit var locQ : Queue<String>
    lateinit var latLangQ : Queue<LatLng>
    private lateinit var fusedLocationCleint : FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locQ = LinkedList<String>()
        latLangQ = LinkedList<LatLng>()
        setContentView(R.layout.activity_tour_start)
        val tourLocations = intent.getStringArrayListExtra("locations")
        val tourLatLang = intent.getParcelableArrayListExtra<LatLng>("latLang")
        val tourName = intent.getStringExtra("tourName")
        val tvTourName = findViewById<TextView>(R.id.tv_tour_name)
        tvTourName.text = tourName
        for (loc in tourLocations) {
            locQ.add(loc)
        }
        for (latl in tourLatLang) {
            latLangQ.add(latl)
        }
        val tvNextLoc = findViewById<TextView>(R.id.tv_next_loc_container)
        tvNextLoc.text = locQ.peek()
        tourLocations.forEach { loc -> Log.d("lol", loc) }

        // get direction using bearings
        fusedLocationCleint = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationCleint.lastLocation
            .addOnSuccessListener { location : Location? ->
                // Got last known location. In some rare situations this can be null.
                val locVar = Location("")
                locVar.latitude = latLangQ.peek().latitude
                locVar.longitude = latLangQ.peek().longitude
                val dir = location?.bearingTo(locVar)
                Log.d("lol-dir", dir.toString())
            }

        // Fragment management
        arFrag = ARFragment()
        mapFrag = TourFragment()
        nextFrag = arFrag
        fragmentContainerID = findViewById<FrameLayout>(R.id.fragmentContainerTour)!!.id
        this.supportFragmentManager.beginTransaction()
            .replace(fragmentContainerID!!, nextFrag, "ARFragment")
//            .addToBackStack(null) //maybe should add
            .commit()

        var btn_ar = findViewById<Button>(R.id.launch_ar_fragment_tour)
        var btn_map = findViewById<Button>(R.id.launch_map_fragment_tour)

        btn_ar.setOnClickListener{
            if (!isAR) {
                mapFrag.onPause()
                nextFrag = arFrag
                this.supportFragmentManager.beginTransaction()
                    .replace(fragmentContainerID!!, nextFrag, "ARFragment")
//                    .addToBackStack(null) //maybe should add
                    .commit()
                isAR = !isAR
            }
        }
        btn_map.setOnClickListener{
            if (isAR) {
                arFrag.onPause()
                nextFrag = mapFrag
                val bun : Bundle = Bundle()
                bun.putStringArrayList("locNames", tourLocations)
                bun.putParcelableArrayList("locLatLang", tourLatLang)
                nextFrag.arguments = bun
                this.supportFragmentManager.beginTransaction()
                    .replace(fragmentContainerID!!, nextFrag, "MapFragment")
//                    .addToBackStack(null) //maybe should add
                    .commit()
                isAR = !isAR
            }
        }
    }
}