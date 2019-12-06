package com.cs407.team15.redstone.ui.scavangerhunt

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.team15.redstone.R
import com.cs407.team15.redstone.model.Scavanger
import com.cs407.team15.redstone.ui.viewtours.RecyclerAdapter
import com.google.firebase.firestore.FirebaseFirestore
import java.util.ArrayList

public class ViewScavanger: Fragment() {
    var allScavangers:MutableList<Scavanger> = mutableListOf()
    var containerId=0
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        containerId=container!!.id
        val root = inflater.inflate(R.layout.fragment_scav_list, container, false)
        getAndDisplayScavangerData(root)
        return root
    }

    fun getAndDisplayScavangerData(root: View) {
        val scavangers= FirebaseFirestore.getInstance().collection("scavanger").get().addOnSuccessListener {
            var scavangers=it.documents.map { scavanger ->
            Log.v("hello", scavanger.toString())

            //String name,String uid,String type,Boolean hammer,List<String> locations,List<String> hints,List<String> tags,Integer votes
            Scavanger(
                scavanger.getString("name"),
                scavanger.getString("uid"),
                scavanger.getString("type"),
                scavanger.getBoolean("hammer"),
                scavanger.get("locations") as List<String>,
                scavanger.get("hints") as List<String>,
                scavanger.get("tags") as List<String>,
                scavanger.get("leaderboardTime") as ArrayList<Int>,
                scavanger.get("leaderboardUsername") as ArrayList<String>,
                (scavanger.get("votes") as Long).toInt()
            )
        }.sortedBy { scavanger -> scavanger.name.toUpperCase() }
            allScavangers.addAll(scavangers)


            val recyclerView = root.findViewById<RecyclerView>(R.id.scavanger_list_recycle)
            recyclerView.layoutManager = LinearLayoutManager(context)
            val adapter = RecyclerAdapter(
                context as Context,
                allScavangers.map { scavanger -> scavanger.name })
            val ad = object : RecyclerAdapter.ItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    val newScav = allScavangers.get(position)
                    val addHintFrag = ScavangerPage()
                    val fragTrans = fragmentManager!!.beginTransaction()
                    val bundle = Bundle()

                    bundle.putString("name", newScav.name)
                    bundle.putString("uid", newScav.uid)
                    bundle.putString("type", newScav.type)
                    bundle.putBoolean("hammer", newScav.hammer)
                    bundle.putStringArrayList("hints", ArrayList(newScav.hints))
                    bundle.putStringArrayList("location", ArrayList<String>(newScav.locations))
                    bundle.putStringArrayList("tags", ArrayList<String>(newScav.tags))
                    bundle.putStringArrayList(
                        "leaderboardUsername",
                        ArrayList<String>(newScav.leaderboardUsername)
                    )
                    bundle.putIntegerArrayList(
                        "leaderboardTime",
                        ArrayList(newScav.leaderboardTime)
                    )
                    bundle.putInt("votes", (newScav.votes).toInt())
                    addHintFrag.arguments = bundle

                    fragTrans.replace(containerId, addHintFrag)
                    fragTrans.addToBackStack(null)
                    fragTrans.commit()
                }
            }
            adapter.setClickListener(ad)
            recyclerView.adapter = adapter
            root!!.invalidate()
        }

    }
}