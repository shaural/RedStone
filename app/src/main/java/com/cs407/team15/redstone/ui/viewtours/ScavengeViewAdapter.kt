package com.cs407.team15.redstone.ui.viewtours


import android.annotation.TargetApi
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import java.lang.Math.toIntExact
import androidx.recyclerview.widget.RecyclerView

import com.cs407.team15.redstone.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.FirebaseFirestore

class ScavengeViewAdapter
// data is passed into the constructor
internal constructor(context: Context, private val mData: List<String>) :
    RecyclerView.Adapter<ScavengeViewAdapter.ViewHolder>() {
    private val mInflater: LayoutInflater
    private var mClickListener: ItemClickListener? = null

    init {
        this.mInflater = LayoutInflater.from(context)
    }

    // inflates the row layout from xml when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = mInflater.inflate(R.layout.recycler_row, parent, false)
        return ViewHolder(view)
    }

    // binds the data to the TextView in each row
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val animal = mData[position]
        addHammerIcon(position, holder);
        holder.myTextView.text = animal
    }
    @TargetApi(24)
    internal fun addHammerIcon(position: Int, holder: ViewHolder) {
        FirebaseFirestore.getInstance().collection("tours").whereEqualTo("name", mData[position])
            .get().addOnSuccessListener { documents ->
                for (document in documents) {
                    FirebaseFirestore.getInstance().collection("users").whereEqualTo("uid", document["user_id"]).get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                val one = 1

                                val two = Math.toIntExact(document["userType"] as Long)
                                if (one == two) {
                                    holder.myImageView.visibility = View.VISIBLE

                                }
                            }
                        }
                }
            }
            .addOnFailureListener {
                    exception ->
                System.out.println("EERROR")
            }
    }

    // total number of rows
    override fun getItemCount(): Int {
        return mData.size
    }


    // stores and recycles views as they are scrolled off screen
    inner class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        internal var myTextView: TextView
        internal var myImageView: ImageView

        init {
            myTextView = itemView.findViewById(R.id.tvAnimalName)
            myImageView = itemView.findViewById(R.id.hammertouricon)
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            if (mClickListener != null) mClickListener!!.onItemClick(view, adapterPosition)
        }
    }

    // convenience method for getting data at click position
    internal fun getItem(id: Int): String {
        return mData[id]
    }

    // allows clicks events to be caught
    internal fun setClickListener(itemClickListener: ItemClickListener) {
        this.mClickListener = itemClickListener
    }

    // parent activity will implement this method to respond to click events
    interface ItemClickListener {
        fun onItemClick(view: View, position: Int)
    }
}
