package com.cs407.team15.redstone.ui.viewtours

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.team15.redstone.R
import com.cs407.team15.redstone.model.Comment
import com.cs407.team15.redstone.ui.comments.CommentSectionAdapter
import com.cs407.team15.redstone.ui.comments.CommentsActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.SetOptions
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class TourInfoActivity : AppCompatActivity(), OnMapReadyCallback{

    lateinit var recyclerView: RecyclerView
    lateinit var commentList: ArrayList<Comment>
    lateinit var viewAllComments: TextView
    private lateinit var commentAdapter: CommentSectionAdapter

    private lateinit var database: DatabaseReference
    lateinit var tourName: String
    lateinit var tourId: String
    lateinit var tourLocations : ArrayList<String>
    lateinit var tourLatLng : ArrayList<LatLng>

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_tour_info)
        tourName = intent.getStringExtra("tourName")
        Log.d("lol", tourName)
        tourId = intent.getStringExtra("tourID")

        //Setting title to name of tour
        val setTourName = findViewById<TextView>(R.id.tour_name)
        setTourName.setText(tourName)

        val setVoteCount = findViewById<TextView>(R.id.total_likes)
        setVoteCount.setText(tourId)

        val setTags = findViewById<TextView>(R.id.tag)

        val setDistance = findViewById<TextView>(R.id.text_distance)

        val setTime = findViewById<TextView>(R.id.text_time)

        //Start tour btn clicked
        tourLocations = ArrayList<String>()
        tourLatLng = ArrayList<LatLng>()
        var btnStartTour = findViewById<Button>(R.id.btn_start_tour)
        btnStartTour.setOnClickListener {
            val intent = Intent (this, TourStartActivity::class.java)
            intent.putExtra("locations", tourLocations)
            intent.putExtra("latLang", tourLatLng)
            intent.putExtra("tourName", tourName)
            startActivity(intent)
        }

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

        //Tags of tour
        var tourTags: ArrayList<String>
        FirebaseFirestore.getInstance().collection("tours").document(tourId).get().addOnSuccessListener { doc ->
            if (doc != null){
                tourTags = doc["tags"] as ArrayList<String>
                val tagL = "Tags: " + tourTags.joinToString(separator = ", ")
                setTags.setText(tagL)
            }
        }

        //Displaying tour distance and time
        FirebaseFirestore.getInstance().collection("tours").document(tourId).get().addOnSuccessListener { doc ->
            if (doc != null){
                val tourDist = "Distance: " + doc["distance"] + " mi"
                setDistance.setText(tourDist)
                val tourTime = ((doc["distance"] as Double)*24).toInt()
                var tourTimeStr: String
                if (tourTime >= 60){
                    //tourTimeStr = (((doc["distance"] as Double).toInt())/60).toString() + " hr " + ((doc["distance"] as Int)%60).toString() + "min"
                    tourTimeStr = (tourTime/60).toString() + " hr " + (tourTime%60).toString() + " min"
                } else if (tourTime < 60 && tourTime >= 0){
                    //tourTimeStr = ((doc["distance"] as Double)).toString() + "min"
                    tourTimeStr = tourTime.toString() + " min"
                } else {
                    tourTimeStr = "Time Unavailable"
                }
                setTime.setText("Time: " + tourTimeStr)
            }
        }

        //Adding functions to add tag and delete tag buttons
        val addTagBtn = findViewById<Button>(R.id.btn_add_tag)
        val deleteTagBtn = findViewById<Button>(R.id.btn_delete_tag)
        addTagBtn.setOnClickListener {

            FirebaseFirestore.getInstance().collection("tours").document(tourId).get().addOnSuccessListener { doc ->
                if (doc != null) {
                    tourTags = doc["tags"] as ArrayList<String>

                    var storeTags = ArrayList<String>()
                    FirebaseFirestore.getInstance().collection("tags").get().addOnSuccessListener { tagNames ->
                        for (n in tagNames.documents){

                            //Storing tag names to storeTags
                            var flag = 0
                            var stringStorage = n["name"] as String
                            for (tt in tourTags) {

                                //make sure that duplicate tags aren't shown
                                if (tt == n["name"]){
                                    flag = 1
                                }
                            }
                            if (flag == 0){
                                storeTags.add(stringStorage)
                            } else {
                                flag = 0
                            }

                        }

                        val tagsArr = arrayOfNulls<String>(storeTags.size)
                        storeTags.toArray(tagsArr)
                        val checkTags = BooleanArray(storeTags.size) {i -> false}

                        //Storing which tags are checked to add
                        val builder = AlertDialog.Builder(this)
                        builder.setMultiChoiceItems(tagsArr, checkTags) {dialog, which, isChecked ->
                            checkTags[which] = isChecked
                        }

                        builder.setPositiveButton("Add") {dialog, which ->
                            var addingTagsArr = ArrayList<String>()
                            for (i in checkTags.indices) {
                                val checked = checkTags[i]
                                if (checked) {
                                    addingTagsArr.add(storeTags[i])
                                }
                            }

                            //addingTagsArray stores all tags to be added
                            var addingTagsArray = arrayOfNulls<String>(addingTagsArr.size)
                            addingTagsArr.toArray(addingTagsArray)

                            for (addingTag in addingTagsArray) {
                                FirebaseFirestore.getInstance().collection("tours").document(tourId).update("tags", FieldValue.arrayUnion(addingTag as String))
                            }

                            //update the UI
                            FirebaseFirestore.getInstance().collection("tours").document(tourId).get().addOnSuccessListener { doc ->
                                if (doc != null){
                                    tourTags = doc["tags"] as ArrayList<String>
                                    val tagL = "Tags: " + tourTags.joinToString(separator = ", ")
                                    setTags.setText(tagL)
                                }
                            }
                        }
                        val adialog = builder.create()
                        adialog.show()
                    }
                }
            }
        }

        //Delete tag(s) button
        deleteTagBtn.setOnClickListener {
            FirebaseFirestore.getInstance().collection("tours").document(tourId).get().addOnSuccessListener { doc ->
                if (doc != null) {
                    tourTags = doc["tags"] as ArrayList<String>

                    val tagsArr = arrayOfNulls<String>(tourTags.size)
                    tourTags.toArray(tagsArr)
                    val checkTags = BooleanArray(tourTags.size) { i -> false }

                    val builder = AlertDialog.Builder(this)
                    builder.setMultiChoiceItems(tagsArr, checkTags) { dialog, which, isChecked ->
                        checkTags[which] = isChecked
                    }

                    builder.setPositiveButton("Delete") { dialog, which ->
                        var addingTagsArr = ArrayList<String>()
                        for (i in checkTags.indices) {
                            val checked = checkTags[i]
                            if (checked) {
                                addingTagsArr.add(tourTags[i])
                            }
                        }

                        //addingTagsArray stores all tags to be added
                        var addingTagsArray = arrayOfNulls<String>(addingTagsArr.size)
                        addingTagsArr.toArray(addingTagsArray)

                        for (deletingTag in addingTagsArray) {
                            FirebaseFirestore.getInstance().collection("tours").document(tourId).update("tags", FieldValue.arrayRemove(deletingTag as String))
                        }

                        FirebaseFirestore.getInstance().collection("tours").document(tourId).get().addOnSuccessListener { doc ->
                            if (doc != null){
                                tourTags = doc["tags"] as ArrayList<String>
                                val tagL = "Tags: " + tourTags.joinToString(separator = ", ")
                                setTags.setText(tagL)
                            }
                        }
                    }
                    val adialog = builder.create()
                    adialog.show()
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
        //lower the number, higher the zoom
        mMap.setMinZoomPreference(13f)
        mMap.isMyLocationEnabled = true
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
                                    val markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.marker)
                                    mMap.addMarker(MarkerOptions().position(ltlg).title(l["name"] as String).icon(markerIcon))
                                    tourLocations.add(l["name"] as String)
                                    tourLatLng.add(ltlg)
                                }
                            }
                        }
                    }
                }
            }
        }
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