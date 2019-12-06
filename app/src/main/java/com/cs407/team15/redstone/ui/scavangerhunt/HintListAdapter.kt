package com.cs407.team15.redstone.ui.scavangerhunt

import android.graphics.Paint
import android.location.Location
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.cs407.team15.redstone.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.hint_list_item.view.*
import java.lang.Math.sqrt
import java.util.*


class HintListAdapter(
    private val scavangerLocationData: MutableList<String>,
    private val locations: ArrayList<String>,
    private val hintList: HintList
) :

    RecyclerView.Adapter<HintListAdapter.MyViewHolder>() {
    class MyViewHolder(
        val textView: ConstraintLayout, val hint_text:TextView,
        val startButton: Button) : RecyclerView.ViewHolder(textView)
    // Create new views (invoked by the layout manager)
    val locationHintData: MutableList<String> = mutableListOf()
    var userLoc: Location= Location("em")
    var locationsList:ArrayList<GeoPoint> = arrayListOf()
    private var point = 0

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int):HintListAdapter.MyViewHolder {

        FirebaseFirestore.getInstance().collection("locations").get().addOnSuccessListener{ locs->
            for (loc in locs){
                for (buildinLoc in locations){
                    if(buildinLoc==loc.get("name")){
                        locationsList.add((loc.get("coordinates") as GeoPoint))
                    }
                }
            }

        }
        val profItemLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.hint_list_item, parent, false) as ConstraintLayout
        val hint_text=profItemLayout.hint_text as TextView
        val startButton = profItemLayout.hint_guess_button as Button
        return MyViewHolder(profItemLayout,hint_text,startButton)
    }
    fun getLocationHintList(): MutableList<String> {
        return locationHintData
    }
    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        //holder.locationHint.text =scavangerLocationData.get(position)
        holder.hint_text.text=scavangerLocationData.get(position)
        holder.startButton.setOnClickListener {
            var distance=checkDistance(position)
            if(distance<1 && 0 < distance){
                holder.hint_text.paintFlags=(holder.hint_text.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG)
                holder.startButton.isClickable=false
                point+=1
                if(point>=scavangerLocationData.size){
                    hintList.onWin()
                }
            }

        }

    }
    fun checkDistance(position: Int): Double {
        if(locationsList.size>position){
        var location=locationsList.get(position)
            var distance=sqrt((location.latitude-userLoc.latitude)*(location.latitude-userLoc.latitude)+(location.longitude-userLoc.longitude)*(location.longitude-userLoc.longitude))
            return distance
        }
            return -1.0
    }

    fun updateLocation(location:Location){
        userLoc=location
    }
    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = scavangerLocationData.size
}


