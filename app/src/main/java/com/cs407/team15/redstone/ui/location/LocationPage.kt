package com.cs407.team15.redstone.ui.location

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.team15.redstone.MainActivity
import com.cs407.team15.redstone.R
import com.cs407.team15.redstone.ui.tour.TourFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.tasks.OnSuccessListener
import com.bumptech.glide.Glide
import com.cs407.team15.redstone.model.Comment
import com.cs407.team15.redstone.model.Location
import com.cs407.team15.redstone.ui.comments.CommentAdapter
import com.cs407.team15.redstone.ui.comments.CommentSectionAdapter
import com.cs407.team15.redstone.ui.comments.CommentsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.app_bar_main.view.*
import kotlinx.android.synthetic.main.location_display.view.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext


class LocationPage : Fragment(), CoroutineScope {
    lateinit var location_id: String
    lateinit var publisher_id: String
    lateinit var btn_flag: Button
    var isFlagged: Boolean = false
    var TAG = "LocationPage"

    lateinit var uid: String
    lateinit var recyclerView: RecyclerView
    lateinit var commentList: ArrayList<Comment>
    lateinit var viewAllComments: TextView
    private lateinit var commentAdapter: CommentSectionAdapter

    private lateinit var database: DatabaseReference// ...


    private lateinit var mJob: Job
    override val coroutineContext: CoroutineContext
        get() = mJob + Dispatchers.Main

    private suspend fun corout(setOnclick: Boolean) {
        runBlocking {
            isFlagged = Location.hasUserFlaggedLocation(location_id)
            if (isFlagged) {
                btn_flag.setBackgroundColor(resources.getColor(R.color.RED))
            } else {
                btn_flag.setBackgroundColor(resources.getColor(R.color.GREEN))
            }
        }
        if (setOnclick) {
            btn_flag.setOnClickListener {
                GlobalScope.launch(Dispatchers.IO + handler) {
                    Location.toggleHasUserFlaggedLocation(location_id)
                    corout(false)
                }
            }
        }
    }

    val handler = CoroutineExceptionHandler { _, exception ->
        Log.d("Exception", "$exception handled ! In Coroutine...")
        btn_flag.setBackgroundColor(resources.getColor(R.color.colorAccent))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        mJob = Job()
        val root = inflater.inflate(R.layout.location_display, container, false)
        btn_flag = root.btn_flag
        val user = FirebaseAuth.getInstance().currentUser

        user?.let{
            uid = user.uid
        }
        database = FirebaseDatabase.getInstance().reference

        // Comments recyclerview
        viewAllComments = root.tv_comments
        recyclerView = root.recycler_view_comment
        recyclerView.setHasFixedSize(true)

        val mLayoutManager = LinearLayoutManager(getActivity())
        recyclerView.layoutManager = mLayoutManager

        commentList = ArrayList<Comment>()
        commentAdapter = CommentSectionAdapter(getActivity(), commentList)
        recyclerView.adapter = commentAdapter

        Log.e("TITLE", arguments?.getCharSequence("title").toString() )

        launch {
            val title = arguments?.getCharSequence("title")
            root.loaction_name.text = title
            val mainRoot = inflater.inflate(R.layout.app_bar_main, container, false)

            val locations = FirebaseFirestore.getInstance().collection("locations").get()
                .addOnSuccessListener { locations ->
                    for (loc in locations.documents) {
                        if (loc["name"] as String == title as String) {
                            location_id = loc.id
                            publisher_id = loc["user_id"] as String


                            GlobalScope.launch(Dispatchers.IO + handler) {
                                corout(true)
                            }

                            root.location_description.text = loc["description"] as String
                            //coordinates, timestamp,userid, name, description,image_src

//                            //sets image of location if there is a image
                            var locationImage = root.location_image
                            var url = loc["image_src"] as String?
                            if (url != null) {
                                var storage = FirebaseStorage.getInstance()
                                var imageStorage = storage.getReferenceFromUrl(url)
                                Glide.with(view!!.context).load(imageStorage).into(locationImage)
                            } else {
                                locationImage.setImageResource(R.drawable.background_bell_tower)
                            }
//                            var locationImage = root.location_image
//                            locationImage.setImageResource(R.drawable.background_bell_tower)

                        }

                    }
                }



            val button = root.loc_back_button
            button.setOnClickListener(
                {
                    activity?.onBackPressed()
                }
            )

            /*
            *   Intent to Comments Activity
            *   put path, postid, publisherid
            *   to track comments
            */
            var allcomments = root.tv_comments
            allcomments.setOnClickListener {
                val intent = Intent (getActivity(), CommentsActivity::class.java)
                intent.putExtra("path", "location")
                intent.putExtra("postid", location_id)
                intent.putExtra("publisherid", publisher_id)
                startActivity(intent)
            }



        }

        return root
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
        val name = arguments?.getCharSequence("title")

        // Get Location ID, then query comments
        FirebaseFirestore.getInstance().collection("locations").get()
            .addOnSuccessListener { locations ->
                for ( location in locations.documents) {
                    if (location["name"] as String == name as String) {
                        val locID = location.id
                        // Query for Comments
                        database.child("Comments")
                            .child("location")
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
    internal var cmpLikeThenTimestamp: Comparator<Comment> =
        Comparator { item1, item2 ->
            val ret: Int

            if (item1.like < item2.like) {
                ret = 1
            } else if (item1.like == item2.like) {
                ret = item1.timestamp.compareTo(item2.timestamp)
            } else {
                ret = -1
            }

            ret
        }
}