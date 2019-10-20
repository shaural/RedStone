package com.cs407.team15.redstone.ui.tour

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cs407.team15.redstone.R
import com.cs407.team15.redstone.model.Location

class LocationsAdapter(val context: Context, val tourLocationNames: MutableList<Location>) :
    RecyclerView.Adapter<LocationsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.location_recycle_view_item, null)

        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.locationNameLabel.text = tourLocationNames[position].name
    }

    override fun getItemCount(): Int {
        return tourLocationNames.size
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val locationNameLabel = view.findViewById<TextView>(R.id.locationNameLabel)
    }
}