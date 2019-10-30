package com.cs407.team15.redstone.ui.profile

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.team15.redstone.R
import com.cs407.team15.redstone.model.User
import com.cs407.team15.redstone.ui.authentication.LoginActivity
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlinx.android.synthetic.main.fragment_settings.view.*
import kotlinx.coroutines.tasks.await

class ProfileFragment : Fragment() {

    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        profileViewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)

       val Data = arrayOf<String>("hello","there","dave","there","dave","there","dave","there","dave","there","dave","there","dave")
        val viewAdapter = profileRecycleAdapter(Data)
        val viewManager = LinearLayoutManager(this.context)
        val root = inflater.inflate(R.layout.fragment_profile, container, false)

      val recyclerView = root.findViewById<RecyclerView>(R.id.profile_tour_recycle_view)
        recyclerView.layoutManager =LinearLayoutManager(activity,LinearLayoutManager.VERTICAL,false)
        recyclerView.adapter=profileRecycleAdapter(Data)


        val auth = FirebaseAuth.getInstance()
        val current =auth.currentUser
        val emailProfile= current?.email

        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(emailProfile!!).get().addOnSuccessListener(OnSuccessListener {

            val user =it.toObject(User::class.java)

            if (user != null){
                root.profile_email.text=user.email
                root.profile_username.text=user.username

                // this is pretty arbitrary, can be changed in the future
                if(user.userLikes > 20 && user.userDislikes < 5){
                    user.userType = 1
                }
                // end of arbitrary hammer user requirements
                if(user.userType != 0){
                    val d = resources.getDrawable(R.drawable.ic_hammer)
                    d.setBounds(5, 5, 5, 5)
                    root.user_type.setImageDrawable(d)
                }
                root.user_likes.text=user.userLikes.toString()
                root.user_dislike.text=user.userDislikes.toString()
                root.user_net_likes.text=(user.userLikes - user.userDislikes).toString()
                root.recieved_like.text=user.recievedLikes.toString()
                root.recieved_dislike.text=user.recievedDislikes.toString()
                root.recieved_net_likes.text=(user.recievedLikes-user.recievedDislikes).toString()


            }


        })
        /*
        Begin the fragment for user comments
         */
                root.usercommentstext.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View?) {
                        var fragment = UserCommentsFragment();
                        if (fragment != null) {
                            //var frManager = fragmentManager
                            val transaction = fragmentManager?.beginTransaction()
                            if (transaction != null) {
                                if (container != null) {
                                    transaction.replace(container.id, fragment)
                                    transaction.addToBackStack(null)
                                    transaction.commit()
                                }
                            }

                        }
                    }
        })
        /*
        begin fragment for user tours
         */
        root.usertourtext.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                var fragment = UserToursFragment();
                if (fragment != null) {
                    //var frManager = fragmentManager
                    val transaction = fragmentManager?.beginTransaction()
                    if (transaction != null) {
                        if (container != null) {
                            transaction.replace(container.id, fragment)
                            transaction.addToBackStack(null)
                            transaction.commit()
                        }
                    }

                }
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