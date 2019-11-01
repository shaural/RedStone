package com.cs407.team15.redstone.ui.viewtours

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.cs407.team15.redstone.R

class TourInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_tour_info)
        val tourTitle = intent.getStringExtra("tourName")

    }
}