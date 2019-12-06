package com.cs407.team15.redstone.ui.scavangerhunt

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.cs407.team15.redstone.R
import com.cs407.team15.redstone.model.Tour


import androidx.core.view.children
import androidx.core.view.get
import androidx.core.view.isVisible

import com.cs407.team15.redstone.ui.location.LocationPage
import com.cs407.team15.redstone.ui.location.RecyclerAdapter
import kotlinx.android.synthetic.main.add_hint_list_item.view.*
import kotlinx.android.synthetic.main.fragment_profile.view.*


class AddHintListAdapter(private val scavangerLocationData: MutableList<String>) :

    RecyclerView.Adapter<AddHintListAdapter.MyViewHolder>() {
    class MyViewHolder(val textView: LinearLayout, val start:TextView,val locationHint:TextView) : RecyclerView.ViewHolder(textView)
    // Create new views (invoked by the layout manager)
    val locationHintData: MutableList<String> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int):AddHintListAdapter.MyViewHolder {
        // create a new view

        val profItemLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.add_hint_list_item, parent, false) as LinearLayout
        val start=profItemLayout.hint_list_item_text as TextView
        val locationHint = profItemLayout.location_hint_list_text_id as TextView
        return MyViewHolder(profItemLayout,start,locationHint)
    }
    fun getLocationHintList(): MutableList<String> {
        return locationHintData
    }
    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        Log.v("hello","eeeeeeeeeeeee")
        holder.locationHint.text =scavangerLocationData.get(position)
        locationHintData.add("")
        holder.start.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                 //To change body of created functions use File | Settings | File Templates.

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
               //To change body of created functions use File | Settings | File Templates.
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                locationHintData.set(position,s.toString())
            }
        }

        )

    }


    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = scavangerLocationData.size
}


