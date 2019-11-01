package com.cs407.team15.redstone.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.team15.redstone.R
import com.cs407.team15.redstone.model.Tour
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserSharedTour : Fragment(){

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val auth = FirebaseAuth.getInstance()
        val current =auth.currentUser
        val emailProfile= current?.email

        val root = inflater.inflate(R.layout.share_user_tour_fragment, container, false)

        val privateTourData = ArrayList<Tour>()

        val shareRecyclerView = root.findViewById<RecyclerView>(R.id.shared_recycle)
        shareRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL,false)
        shareRecyclerView.adapter=shareRecycleAdapter(privateTourData,null,this)

        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(current?.email!!).get().addOnSuccessListener({
            var tourInviteList = ArrayList<Tour>()
            val inviteIdList = ArrayList<String>()
            if(it["tour_invites"]!=null){
                val tourList=it["tour_invites"] as List<String>
                for( tour in tourList){
                    inviteIdList.add(tour)
                }
            }
            db.collection("tours").get().addOnSuccessListener({
                val tourList = it
                val shareTourList = ArrayList<Tour>()
                for (tours in it.documents){
                    var tourPath = tours.reference.path.split("/")[1].toString()

                    for(invite in inviteIdList){
                        if(tourPath==invite){
                            var locs= (tours["locations"] as ArrayList<String>)
                            var tags = (tours["tags"] as ArrayList<String>)
                            val tour = Tour(tours["name"] as String,tours["type"] as String,tours["user_id"] as String,tours["hammer"] as Boolean, locs.toList(),tags.toList(),tours["votes"] as Number)
                            shareTourList.add(tour)
                        }
                    }
                }
                shareRecyclerView.adapter=shareRecycleAdapter(shareTourList,inviteIdList,this)
            })


        })

        return root
    }
}