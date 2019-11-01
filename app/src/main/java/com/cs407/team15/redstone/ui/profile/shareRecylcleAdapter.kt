package com.cs407.team15.redstone.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cs407.team15.redstone.R
import com.cs407.team15.redstone.model.Tour
import kotlinx.android.synthetic.main.shared_tours_item.view.*

class shareRecycleAdapter( private val userPrivatTourData:ArrayList<Tour>, private val tourIdList:ArrayList<String>?, val frag:UserSharedTour) :
    RecyclerView.Adapter<shareRecycleAdapter.MyViewHolder>() {
    class MyViewHolder(val textView: LinearLayout,val tourName:TextView,val buttonGO:Button) : RecyclerView.ViewHolder(textView)
    // Create new views (invoked by the layout manager)

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): shareRecycleAdapter.MyViewHolder {
        val root = LayoutInflater.from(parent.context)
            .inflate(R.layout.shared_tours_item, parent, false) as LinearLayout
        val tourName=root.shared_tour_name
        val buttonGO =root.shared_button_go
        return shareRecycleAdapter.MyViewHolder(root,tourName,buttonGO)
    }

    override fun onBindViewHolder(holder: shareRecycleAdapter.MyViewHolder, position: Int) {
        holder.tourName.setText(userPrivatTourData.get(position).name)

    }
    override fun getItemCount() = userPrivatTourData.size

}