package com.cs407.team15.redstone.ui.scavangerhunt

import android.os.Build
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
import kotlinx.android.synthetic.main.profile_list_item.view.*

import androidx.core.view.children
import androidx.core.view.get
import androidx.core.view.isVisible

import com.cs407.team15.redstone.ui.home.NoticesAdapter
import com.cs407.team15.redstone.ui.location.LocationPage
import com.cs407.team15.redstone.ui.location.RecyclerAdapter
import kotlinx.android.synthetic.main.add_hint_list_item.view.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlinx.android.synthetic.main.leaderboard_fragment.view.*
import kotlinx.android.synthetic.main.leaderboard_item.view.*
import kotlinx.android.synthetic.main.profile_list_item.view.*
import java.time.Duration

class LeaderBoardAdapter(private val scavangerUsername: MutableList<String>,private val scavangerLeaderScore: MutableList<Int>) :

    RecyclerView.Adapter<LeaderBoardAdapter.MyViewHolder>() {
    class MyViewHolder(val textView: LinearLayout,val username: TextView,val leaderBoardScore: TextView) : RecyclerView.ViewHolder(textView)
    // Create new views (invoked by the layout manager)
    val locationHintData: MutableList<String> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int):LeaderBoardAdapter.MyViewHolder {
        // create a new view

        val profItemLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.leaderboard_item, parent, false) as LinearLayout
        val username=profItemLayout.leaderboard_username as TextView
        val leaderboardScore=profItemLayout.leaderboard_score as TextView

        return MyViewHolder(profItemLayout,username,leaderboardScore)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var mil=Duration.ofMillis(scavangerLeaderScore.get(position).toLong())
            holder.leaderBoardScore.text=(  mil.toHours().toString()+":"+ (mil.toMinutes()%60).toString() + ":" +((mil.toMillis()/1000)%60).toString())
        } else {

           //holder.username.text=(  mil.toHours().toString()+":"+ (mil.toMinutes()%60).toString() + ":" +((mil.toMillis()/1000)%60).toString())
        }

        holder.username.text=scavangerUsername.get(position)
    }


    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = scavangerUsername.size
}


