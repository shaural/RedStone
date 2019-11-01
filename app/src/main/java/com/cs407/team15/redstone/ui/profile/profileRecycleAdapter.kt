package com.cs407.team15.redstone.ui.profile

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
import kotlinx.android.synthetic.main.profile_list_item.view.*

class profileRecycleAdapter(private val myDataset: ArrayList<Array<String>>, private val userPrivatTourData:ArrayList<Tour>,private val tourIdList:ArrayList<String>?, val frag:ProfileFragment) :
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
        val tour=userPrivatTourData.get(position)

        if(!tour.locations.isEmpty()){
            holder.start.text=tour.locations.get(0)
        }
        else{
            holder.start.text=""
        }
        //myDataset.get(position)[3]
        holder.title.text=tour.name//myDataset.get(position)[0]
        if( holder.start.text.isBlank()){
           holder.hammer.isVisible=false
            holder.privateTour.isVisible=false
            holder.share.isVisible = false
            holder.edit.isVisible=false
            return
        }
        if(tour.type=="personal"){
            holder.privateTour.setImageResource(R.drawable.ic_private_tour_24dp)
        }else if(tour.type=="draft"){
            holder.privateTour.setImageResource(R.drawable.ic_private_tour_24dp)

        }
        else{
            holder.privateTour.isVisible=false
        }
        if(tour.hammer==true){
            holder.hammer.isVisible=false
        }else{
            holder.hammer.setImageResource(R.drawable.ic_hammer)

        }

        holder.share.setOnClickListener({
          frag.editPersonalTour(tour,tourIdList?.get(position))
        })
    }
    fun populatDraftTour(){

    }
    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = userPrivatTourData.size
}
