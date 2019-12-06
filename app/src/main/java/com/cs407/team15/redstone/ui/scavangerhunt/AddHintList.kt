package com.cs407.team15.redstone.ui.scavangerhunt

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.team15.redstone.R
import com.cs407.team15.redstone.model.Scavanger
import com.cs407.team15.redstone.ui.viewtours.ViewToursFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

public class AddHintList : Fragment() {

    var containerId=0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        containerId=container!!.id
        val root = inflater.inflate(R.layout.add_hint_list_fragment, container, false)
        val listHintData = arguments?.getStringArrayList("locationsHintList")!!.toMutableList()
        //val listHintData= arrayListOf<String>()

        val addHintListView = root.findViewById<RecyclerView>(R.id.add_hint_list_recycle)
        val createScavangerHuntButton = root.findViewById<Button>(R.id.create_scavanger_hunt_button)

        val name = arguments?.getString("name")!!
        val type = arguments?.getString("type")!!
        val uid = arguments?.getString("uid")!!
        val hammer = arguments?.getBoolean("hammer")!!
        val locations = arguments?.getStringArrayList("location")!!
        val tagsOnTour = arguments?.getStringArrayList("tags")!!
        val initialVotes = arguments?.getInt("votes")!!

        val scav =Scavanger(name,uid, type, hammer, locations,null, tagsOnTour,
            arrayListOf(), arrayListOf(), initialVotes)
       addHintListView.layoutManager = LinearLayoutManager(activity)
        addHintListView.itemAnimator = DefaultItemAnimator()
        val addHintListViewAdapter = AddHintListAdapter( listHintData)
        addHintListView.adapter=addHintListViewAdapter
        createScavangerHuntButton.setOnClickListener { createScavangerHunt(addHintListViewAdapter,listHintData, scav) }

        return root
    }
    fun createScavangerHunt(adapter: AddHintListAdapter,hintListData:MutableList<String>, scav: Scavanger) {
        val arr =adapter.getLocationHintList()
        var count=0
        for (pos in arr){
            Log.v("hello",pos+hintListData.get(count))
            count+=1
        }
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        scav.hints=arr.toList()

        db.collection("scavanger").add(scav)

        val viewToursFrag = ViewScavanger()
        val fragTransaction = fragmentManager!!.beginTransaction()
        fragTransaction.replace(containerId, viewToursFrag)
        fragTransaction.addToBackStack(null)
        fragTransaction.commit()

    }


}