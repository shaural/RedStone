package com.cs407.team15.redstone.ui.scavangerhunt

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.cs407.team15.redstone.R
import com.cs407.team15.redstone.model.Scavanger
import com.google.firebase.firestore.FirebaseFirestore
import java.util.ArrayList

public class ScavangerPage: Fragment() {
    var containerId=0
    var username:String=""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        containerId=container!!.id
        val root = inflater.inflate(R.layout.fragment_scavanger_page, container, false)
        val button =root.findViewById<Button>(R.id.start_hunt_button)
        val leaderBoardButton =root.findViewById<Button>(R.id.leaderboard_button)
        val name = arguments?.getString("name")!!
        val type = arguments?.getString("type")!!
        val uid = arguments?.getString("uid")!!
        val hammer = arguments?.getBoolean("hammer")!!
        val locations = arguments?.getStringArrayList("location")!!
        val hints = arguments?.getStringArrayList("hints")!!
        val tagsOnTour = arguments?.getStringArrayList("tags")!!
        val leadUser=arguments?.getStringArrayList("leaderboardUsername")
        val leadScore=arguments?.getIntegerArrayList("leaderboardTime")
        val initialVotes = arguments?.getInt("votes")!!

        val scavanger: Scavanger = Scavanger(name,uid,type,hammer,locations,hints,tagsOnTour,leadScore,leadUser,initialVotes)
        button.setOnClickListener { startScavangerHunt(scavanger) }
        val scavangerHuntNameTextView=root.findViewById<TextView>(R.id.scav_name_text)
        val scavTagTextView=root.findViewById<TextView>(R.id.scav_tags)
        val scavangerUsernameTextView=root.findViewById<TextView>(R.id.scav_username)
       // val scavangerHuntNameTextView=root.findViewById<TextView>(R.id.scav_name_text)
        scavangerHuntNameTextView.text=name
        for(tag in tagsOnTour){
        scavTagTextView.text=scavTagTextView.text.toString()+" "+tag}
        FirebaseFirestore.getInstance().collection("users").get().addOnSuccessListener {
                users->
            for(user in users.documents) {
                if (user.get("uid") == uid) {
                    scavangerUsernameTextView.text=user.get("username").toString()
                    username=user.get("username").toString()
                }
            }}

        leaderBoardButton.setOnClickListener { goToLeaderBoard(scavanger) }



        return root
    }

    private fun goToLeaderBoard(scavanger: Scavanger) {
        val addHintFrag = LeaderBoard()
        val fragTrans = fragmentManager!!.beginTransaction()
        val bundle = Bundle()

        bundle.putString("name",scavanger.name)
        bundle.putString("uid",scavanger.uid)
        bundle.putString("type",scavanger.type)
        bundle.putBoolean("hammer",scavanger.hammer)
        bundle.putStringArrayList("hints", ArrayList(scavanger.hints))
        bundle.putStringArrayList("location", ArrayList<String>(scavanger.locations))
        bundle.putStringArrayList("tags", ArrayList<String>(scavanger.tags))
        bundle.putStringArrayList("leaderboardUsername",ArrayList<String>(scavanger.leaderboardUsername))
        bundle.putIntegerArrayList("leaderboardTime",ArrayList(scavanger.leaderboardTime))
        bundle.putInt("votes",(scavanger.votes).toInt())
        addHintFrag.arguments=bundle
        fragTrans.replace(containerId, addHintFrag)
        fragTrans.addToBackStack(null)
        fragTrans.commit()
    }

    fun startScavangerHunt(scavanger: Scavanger){
        val addHintFrag = HintList()
        val fragTrans = fragmentManager!!.beginTransaction()
        val bundle = Bundle()

        bundle.putString("username",username)
        bundle.putString("name",scavanger.name)
        bundle.putString("uid",scavanger.uid)
        bundle.putString("type",scavanger.type)
        bundle.putBoolean("hammer",scavanger.hammer)
        bundle.putStringArrayList("hints", ArrayList(scavanger.hints))
        bundle.putStringArrayList("location", ArrayList<String>(scavanger.locations))
        bundle.putStringArrayList("tags", ArrayList<String>(scavanger.tags))
        bundle.putStringArrayList("leaderboardUsername",ArrayList<String>(scavanger.leaderboardUsername))
        bundle.putIntegerArrayList("leaderboardTime",ArrayList(scavanger.leaderboardTime))
        bundle.putInt("votes",(scavanger.votes).toInt())
        addHintFrag.arguments=bundle
        fragTrans.replace(containerId, addHintFrag)
        fragTrans.addToBackStack(null)
        fragTrans.commit()

    }
}