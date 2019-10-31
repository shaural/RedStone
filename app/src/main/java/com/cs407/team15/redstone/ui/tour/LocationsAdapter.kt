package com.cs407.team15.redstone.ui.tour

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.cs407.team15.redstone.model.Location
import androidx.recyclerview.widget.ItemTouchHelper.Callback.makeMovementFlags
import com.cs407.team15.redstone.ui.tour.helper.ItemTouchHelperAdapter
import java.util.*





class LocationsAdapter(val context: Context, val tourLocationNames: MutableList<Location>) :
    RecyclerView.Adapter<LocationsAdapter.ViewHolder>(), ItemTouchHelperAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(com.cs407.team15.redstone.R.layout.basic_recycle_view_item, parent, false)

        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.locationNameLabel.text = tourLocationNames[position].name
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        val swipeCallback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                tourLocationNames.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, tourLocationNames.size)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun getItemCount(): Int {
        return tourLocationNames.size
    }

    fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    fun isLongPressDragEnabled(): Boolean {
        return true
    }

    fun isItemViewSwipeEnabled(): Boolean {
        return true
    }

    override fun onItemDismiss(position: Int) {
        tourLocationNames.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        val prev = tourLocationNames.removeAt(fromPosition)
        tourLocationNames.add(if (toPosition > fromPosition) toPosition - 1 else toPosition, prev)
        notifyItemMoved(fromPosition, toPosition)
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val locationNameLabel = view.findViewById<TextView>(com.cs407.team15.redstone.R.id.basicLabel)
    }
}