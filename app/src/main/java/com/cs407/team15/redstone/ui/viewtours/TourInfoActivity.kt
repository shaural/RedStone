package com.cs407.team15.redstone.ui.viewtours

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.team15.redstone.R
import com.cs407.team15.redstone.model.Comment
import com.cs407.team15.redstone.ui.comments.CommentSectionAdapter
import com.cs407.team15.redstone.ui.comments.CommentsActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.SetOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.GeoPoint
import kotlinx.android.synthetic.main.location_display.view.*
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.Comparator

class TourInfoActivity : AppCompatActivity(), OnMapReadyCallback{

    lateinit var recyclerView: RecyclerView
    lateinit var commentList: ArrayList<Comment>
    lateinit var viewAllComments: TextView
    private lateinit var commentAdapter: CommentSectionAdapter

    private lateinit var database: DatabaseReference
    lateinit var tourName: String
    lateinit var tourId: String

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_tour_info)
        tourName = intent.getStringExtra("tourName")
        tourId = intent.getStringExtra("tourID")

        //Setting title to name of tour
        val setTourName = findViewById<TextView>(R.id.tour_name)
        setTourName.setText(tourName)

        val setVoteCount = findViewById<TextView>(R.id.total_likes)
        setVoteCount.setText(tourId)

        //Like button pressing
        var ue = FirebaseAuth.getInstance().currentUser!!.email as String
        if (tourId != null) {
            println("tourId not null" + tourId)
            val selectedTour = FirebaseFirestore.getInstance().collection("tours").document(tourId)
            selectedTour.get().addOnSuccessListener { tour ->
                var voteCount = tour["votes"] as Long
                var flag = 0
                val likeButton = findViewById<Button>(R.id.like_btn)
                var voteCountString = "Total likes: " + voteCount.toString()
                setVoteCount.setText(voteCountString)

                //disabling button if user already liked
                selectedTour.collection("users_liked").get().addOnSuccessListener { user ->
                    for (u in user.documents) {
                        if (u["email"] as String == ue) {
                            flag = 1
                            likeButton.isEnabled = false
                            likeButton.isClickable = false
                        }
                    }
                }

                //Updating vote count
                likeButton.setOnClickListener {
                    val newVoteCount = voteCount + 1
                    val updateVote = hashMapOf("votes" to newVoteCount)
                    selectedTour.set(updateVote, SetOptions.merge())
                    voteCountString = "Total likes: " + newVoteCount.toString()
                    setVoteCount.setText(voteCountString)

                    //checking if user has already liked the tour
                    val data = hashMapOf("email" to ue)
                    var flag = 0;
                    selectedTour.collection("users_liked").get().addOnSuccessListener { user ->
                        for (u in user.documents) {
                            if (u["email"] as String == ue){
                                flag = 1
                            }
                        }
                        if (flag == 0){
                            selectedTour.collection("users_liked").document().set(data)
                        }
                    }
                }
            }
        }

        //Getting the map for the tour
        val mapFragment = supportFragmentManager.findFragmentById(R.id.tourinfo_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Comments recyclerview
        viewAllComments = findViewById(R.id.tv_comments)
        recyclerView = findViewById(R.id.recycler_view_comment)
        recyclerView.setHasFixedSize(true)

        val mLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = mLayoutManager

        commentList = ArrayList<Comment>()
        commentAdapter = CommentSectionAdapter(this, commentList)
        recyclerView.adapter = commentAdapter

        database = FirebaseDatabase.getInstance().reference

        var allcomments = findViewById<TextView>(R.id.tv_comments)
        allcomments.setOnClickListener {
            val intent = Intent (this, CommentsActivity::class.java)
            intent.putExtra("path", "tour")
            intent.putExtra("postid", tourId)
            intent.putExtra("publisherid", ue)
            //intent.putExtra("context", context);
            startActivity(intent)
        }
    }

    //Where to display markers for tour
    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled=true
        mMap.setMinZoomPreference(14f)

        //setting the map to Purdue campus
        FirebaseFirestore.getInstance().collection("schools").document("Purdue").get().addOnSuccessListener {
            val schoolLoc = it["coordinates"] as GeoPoint
            mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(schoolLoc.latitude,schoolLoc.longitude)))
        }

        FirebaseFirestore.getInstance().collection("tours").document(tourId).get().addOnSuccessListener { docu ->
            if (docu != null){
                var tourLoc: ArrayList<String> = docu["locations"] as ArrayList<String>
                tourLoc?.let {
                    for (i in it){
                        FirebaseFirestore.getInstance().collection("locations").get().addOnSuccessListener { loc ->
                            for (l in loc){
                                if (l["name"] == i){
                                    val geomarker = l["coordinates"] as GeoPoint
                                    val ltlg = LatLng(geomarker.latitude, geomarker.longitude)
                                    mMap.addMarker(MarkerOptions().position(ltlg).title("Tour Spot"))
                                }
                            }
                        }
                    }
                }
            }
        }


//        val locations = FirebaseFirestore.getInstance().collection("locations").get()
//            .addOnSuccessListener { locations ->
//                for (loc in locations.documents) {
//                    if (loc["name"] as String == title as String) {
//                        location_id = loc.id
    }

    override fun onStart() {
        super.onStart()
        readComments()
    }

    /**
     * Get LocationID then Retrieve data from DB
     * after getting comments, call comment Adapter
     */
    private fun readComments() {
        val name = tourName

        // Get Location ID, then query comments
        FirebaseFirestore.getInstance().collection("tours").get()
            .addOnSuccessListener { locations ->
                for ( location in locations.documents) {
                    if (location["name"] as String == name as String) {
                        val locID = location.id
                        // Query for Comments
                        database.child("Comments")
                            .child("tour")
                            .child(locID)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    commentList.clear()
                                    // Get Comment
                                    for (snapshot in dataSnapshot.children) {
                                        val comment= snapshot.getValue(Comment::class.java)
                                        //Log.e(TAG, "Comment: " + comment!!.getComment())
                                        commentList.add(comment as Comment)
                                    }

                                    Collections.sort(commentList, cmpLikeThenTimestamp) // sorting

                                    commentAdapter.notifyDataSetChanged()
                                    viewAllComments.setText("View All "
                                            +dataSnapshot.getChildrenCount()+" Comments")
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                }
                            })

                    }

                }
            }

    }

    /**
     * Compare class
     * Dsc Likes then Asc Timestamp
     */
    internal var cmpLikeThenTimestamp: java.util.Comparator<Comment> =
        Comparator { item1, item2 ->
            val ret: Int

            if (item1.like < item2.like) {
                ret = 1
            } else if (item1.like == item2.like && item1.timestamp != null && item2.timestamp != null) {
                ret = item1.timestamp.compareTo(item2.timestamp)
            } else {
                ret = -1
            }

            ret
        }
}