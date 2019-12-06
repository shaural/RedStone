package com.cs407.team15.redstone.ui.viewtours

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.cs407.team15.redstone.R
import com.google.android.gms.maps.model.LatLng

class TourStartActivity : AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tourLocations = intent.getStringArrayListExtra("locations")
        val tourLatLang = intent.getParcelableArrayListExtra<LatLng>("latLang")
        tourLocations.forEach { loc -> Log.d("lol", loc) }
        setContentView(R.layout.activity_tour_start)
    }
}