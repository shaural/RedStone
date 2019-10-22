package com.cs407.team15.redstone.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.cs407.team15.redstone.R
import com.cs407.team15.redstone.model.User
import com.cs407.team15.redstone.ui.authentication.LoginActivity
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlinx.coroutines.tasks.await

class ProfileFragment : Fragment() {

    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        profileViewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_profile, container, false)

        val auth = FirebaseAuth.getInstance()
        val current =auth.currentUser
        val emailProfile= current?.email

        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(emailProfile!!).get().addOnSuccessListener(OnSuccessListener {

            val user =it.toObject(User::class.java)

            if (user != null){
            root.profile_email.text=user.email
            root.profile_username.text=user.username
            root.user_type.text=user.userType.toString()
            root.user_likes.text=user.userLikes.toString()
            root.user_dislike.text=user.userDislikes.toString()
            root.user_net_likes.text=(user.userLikes - user.userDislikes).toString()
            root.recieved_like.text=user.recievedLikes.toString()
            root.recieved_dislike.text=user.recievedDislikes.toString()
            root.recieved_net_likes.text=(user.recievedLikes-user.recievedDislikes).toString()
            root.is_hammer_user.text=user.isHammerUser.toString()
            }


        })

     /*   val database = FirebaseDatabase.getInstance().reference
        val dUser = FirebaseDatabase.getInstance().getReference("users")

        Log.v("email",emailProfile!!)
        val profileUpdate = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
               val user = snap.getValue(User::class.java)
                Log.v("Success",user?.login_attempt.toString())
            }

            override fun onCancelled(err: DatabaseError) {
                Log.v("Error","No")
            }
        }*/
        //val first =FirebaseDatabase.getInstance().reference.child("users")
          //  first.child("uid").addListenerForSingleValueEvent(profileUpdate)//addChildEventListener(profileUpdate)

        //val userInformation = database.child("users").database.getReference(emailProfile).addListenerForSingleValueEvent(profileUpdate)
       // userInformation.addListenerForSingleValueEvent(profileUpdate)
       /*val userporfile= db.collection("users").document(emailProfile!! ).get().addOnSuccessListener {

       }*/

        //Log.v("email",userProfile)
        addProfileInfo()

        return root
    }
    fun addProfileInfo(){
     //User user =
        val db =FirebaseFirestore.getInstance()
        val auth =FirebaseAuth.getInstance()
        Log.v("aaa",LoginActivity.COLLECTION_NAME_KEY)

    }
}