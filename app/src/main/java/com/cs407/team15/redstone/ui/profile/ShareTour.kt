package com.cs407.team15.redstone.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.team15.redstone.R
import com.cs407.team15.redstone.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.share_tour_fragment.view.*

class ShareTour : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.share_tour_fragment, container, false)

        val tourId=arguments?.getString("tourId")


        root.share_username_text
        root.share_send_button.setOnClickListener({
            FirebaseFirestore.getInstance().collection("users").document(root.share_username_text.getText().toString())
                .get().addOnSuccessListener({
                    val tour_invites:List<String>
                    if(it["tour_invites"]!=null){
                     tour_invites=it["tour_invites"] as List<String>
                    }else{
                       tour_invites = listOf<String>()
                    }
                    for (tour in tour_invites){
                        if(tourId==tour){
                            return@addOnSuccessListener
                        }
                    }
                    var user: User? =it.toObject(User::class.java)
                    if(user?.tour_invites==null){
                        user?.setTour_invites(listOf<String>(tourId!!))
                    }else{
                    user?.tour_invites?.add(tourId)}
                    if (user != null) {
                        FirebaseFirestore.getInstance().collection("users").document(root.share_username_text.getText().toString()).set(user)
                    }

                })
            Log.v("hey", root.share_username_text.getText().toString())
        })

        return root;
    }
}