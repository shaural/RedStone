package com.cs407.team15.redstone.ui.profile

import android.os.Bundle
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.cs407.team15.redstone.R
import com.cs407.team15.redstone.ui.home.NoticesAdapter
import com.cs407.team15.redstone.ui.location.LocationPage
import com.cs407.team15.redstone.ui.location.RecyclerAdapter
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlinx.android.synthetic.main.profile_list_item.view.*

class profileRecycleAdapter(private val myDataset: ArrayList<Array<String>>) :
    RecyclerView.Adapter<profileRecycleAdapter.MyViewHolder>() {
    class MyViewHolder(val textView: LinearLayout, val start:TextView, val title:TextView,val share:Button,val edit:Button, val hammer:ImageView, val privateTour:ImageView) : RecyclerView.ViewHolder(textView)
    // Create new views (invoked by the layout manager)

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): profileRecycleAdapter.MyViewHolder {
        // create a new view

        val profItemLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.profile_list_item, parent, false) as LinearLayout
        val start=profItemLayout.starting_loc as TextView
        val title =profItemLayout.name_tour as TextView
        val button=profItemLayout.share_tour as Button
        val edit=profItemLayout.edit_tour_button as Button
        val hammer=profItemLayout.hammer as ImageView
        val privateTour=profItemLayout.private_tour_image as ImageView
        return MyViewHolder(profItemLayout,start,title,share = button,edit=edit,hammer = hammer,privateTour=privateTour)
    }
    fun editTour(){

    }
    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}