package com.cs407.team15.redstone.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.team15.redstone.R
import com.cs407.team15.redstone.model.Tour
import com.cs407.team15.redstone.model.User
import com.cs407.team15.redstone.ui.authentication.LoginActivity
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_profile.view.*
import com.cs407.team15.redstone.ui.tour.AddTourFragment

import kotlinx.coroutines.tasks.await



class ProfileFragment : Fragment() {

    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        profileViewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
/*get userid and search whole of tours till and add tours for now also add permisions to share*/
       val Data = ArrayList<Array<String>>()
        Data.add(arrayOf("","","","","",""))
        val privateTourData = ArrayList<Tour>()
        privateTourData.add(Tour("","","",true, listOf(),listOf(),0))
//        val viewAdapter = profileRecycleAdapter(Data,privateTourData,null,this)
//        val viewManager = LinearLayoutManager(this.context)
        val root = inflater.inflate(R.layout.fragment_profile, container, false)
      val recyclerView = root.findViewById<RecyclerView>(R.id.profile_tour_recycle_view)
        recyclerView.layoutManager =LinearLayoutManager(activity,LinearLayoutManager.VERTICAL,false)
        recyclerView.adapter=profileRecycleAdapter(Data,privateTourData,null,this)

       // val shareRecyclerView = root.findViewById<RecyclerView>(R.id.shared_recycle)
       // shareRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL,false)
            //  shareRecyclerView.adapter=shareRecycleAdapter(privateTourData,null,this)


        val auth = FirebaseAuth.getInstance()
        val current =auth.currentUser
        val emailProfile= current?.email

        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(current?.email!!).get().addOnSuccessListener(OnSuccessListener{
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
            val personalTourList =  ArrayList<Array<String>>()
            val privateTourList = ArrayList<Tour>()
            val tourIdList = ArrayList<String>()
            val shareTourList = ArrayList<Tour>()
            for (tours in it.documents){
                var tourPath = tours.reference.path.split("/")[1].toString()
                if(tours["user_id"]==current?.uid.toString() && tours["type"]=="personal"){
                    tourIdList.add(tourPath)
                    var locs= (tours["locations"] as ArrayList<String>)
                    var tags = (tours["tags"] as ArrayList<String>)

                    val userTourList= arrayOfNulls<String>(5+locs.size+tags.size+1)

                    val tour = Tour(tours["name"] as String,tours["type"] as String,tours["user_id"] as String,tours["hammer"] as Boolean, locs.toList(),tags.toList(),tours["votes"] as Number)
                    //val name: String, val type: String, val user_id: String, val hammer: Boolean, val locations: List<String>, val tags: List<String>) {


                    personalTourList.add(userTourList as Array<String>)
                    privateTourList.add(tour)
                }
                for(invite in inviteIdList){
                    if(tourPath==invite){
                        var locs= (tours["locations"] as ArrayList<String>)
                        var tags = (tours["tags"] as ArrayList<String>)
                        val tour = Tour(tours["name"] as String,tours["type"] as String,tours["user_id"] as String,tours["hammer"] as Boolean, locs.toList(),tags.toList(),tours["votes"]as Number)
                        shareTourList.add(tour)
                    }
                }


            }
            if(personalTourList.isEmpty()){
                personalTourList.add(arrayOf("","","","","",""))
                tourIdList.add("")}
            recyclerView.adapter=profileRecycleAdapter(personalTourList as ArrayList<Array<String>>,privateTourList,tourIdList,this)
                // shareRecyclerView.adapter=shareRecycleAdapter(shareTourList,inviteIdList,this)
        })
        })
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

        root.shared_tours_button.setOnClickListener(
            {
                val frag = fragmentManager!!.beginTransaction()
                val bundle = Bundle()
                val loc= UserSharedTour()
                loc.arguments=bundle
                frag.replace((view!!.parent as ViewGroup).id, loc)
                frag.addToBackStack(null)
                frag.commit()
            }
        )

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
    fun editPersonalTour(tour: Tour, tourId: String?){
        val frag = fragmentManager!!.beginTransaction()
        val bundle = Bundle()
        bundle.putString("title",tour.name)
        bundle.putString("tourId",tourId)
        bundle.putString("type",tour.type)
        bundle.putStringArrayList("tags",ArrayList(tour.tags))
        bundle.putStringArrayList("locations",ArrayList(tour.locations))
        val loc= AddTourFragment()
        loc.arguments=bundle
        frag.replace((view!!.parent as ViewGroup).id, loc)
        frag.addToBackStack(null)
        frag.commit()
    }
    fun shareTour(tour: Tour, tourId: String?){
        val frag = fragmentManager!!.beginTransaction()
        val bundle = Bundle()
        bundle.putString("title",tour.name)
        bundle.putString("tourId",tourId)
        bundle.putString("type",tour.type)
        bundle.putStringArrayList("tags",ArrayList(tour.tags))
        bundle.putStringArrayList("locations",ArrayList(tour.locations))
        val loc= ShareTour()
        loc.arguments=bundle
        frag.replace((view!!.parent as ViewGroup).id, loc)
        frag.addToBackStack(null)
        frag.commit()
    }


    fun addProfileInfo(){
     //User user =
        val db =FirebaseFirestore.getInstance()
        val auth =FirebaseAuth.getInstance()
        Log.v("aaa",LoginActivity.COLLECTION_NAME_KEY)

    }
}