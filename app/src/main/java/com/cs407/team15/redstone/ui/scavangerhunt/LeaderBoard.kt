package com.cs407.team15.redstone.ui.scavangerhunt

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.team15.redstone.R

public class LeaderBoard : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.leaderboard_fragment, container, false)


        val name=arguments?.get("name") as String
        val leadUser=arguments?.get("leaderboardUsername") as ArrayList<String>
        val leadScore=arguments?.get("leaderboardTime") as ArrayList<Int>



        val leaderBoardRecyclerView = root.findViewById<RecyclerView>(R.id.leaderboard_recycler)
        leaderBoardRecyclerView.layoutManager = LinearLayoutManager(activity)
        leaderBoardRecyclerView.itemAnimator = DefaultItemAnimator() as RecyclerView.ItemAnimator?
        val leaderBoardAdapter = LeaderBoardAdapter( leadUser!!,leadScore!!)
        leaderBoardRecyclerView.adapter=leaderBoardAdapter

        return root
    }
}