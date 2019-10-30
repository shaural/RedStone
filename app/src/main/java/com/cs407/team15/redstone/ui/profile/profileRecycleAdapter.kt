package com.cs407.team15.redstone.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import com.cs407.team15.redstone.R
import com.cs407.team15.redstone.ui.home.NoticesAdapter
import com.cs407.team15.redstone.ui.location.RecyclerAdapter
import kotlinx.android.synthetic.main.fragment_profile.view.*

class profileRecycleAdapter(private val myDataset: Array<String>) :
    RecyclerView.Adapter<profileRecycleAdapter.MyViewHolder>() {
    class MyViewHolder(val textView: LinearLayout) : RecyclerView.ViewHolder(textView)


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): profileRecycleAdapter.MyViewHolder {
        // create a new view
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.profile_list_item, parent, false) as LinearLayout
        // set the view's size, margins, paddings and layout parameters
        return MyViewHolder(textView)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}