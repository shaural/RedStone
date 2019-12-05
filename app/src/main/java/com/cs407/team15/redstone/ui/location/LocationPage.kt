package com.cs407.team15.redstone.ui.location

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
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
import kotlinx.android.synthetic.main.nav_header_main.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList
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

        //Initialize all tag related items
        var tagText = root.location_tag
        var addTagBtn = root.btn_add_tag
        var deleteTagBtn = root.btn_delete_tag

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

            val locations = FirebaseFirestore.getInstance().collection("locations")
            locations.get().addOnSuccessListener { locations ->
                for (loc in locations.documents) {
                        if (loc["name"] as String == title as String) {
                            location_id = loc.id
                            publisher_id = loc["user_id"] as String

                            //Display the location's tags
                            var locTags = arrayListOf<String>()
                            FirebaseFirestore.getInstance().collection("locations").document(location_id).collection("tags").get().addOnSuccessListener { t ->
                                for (tNames in t){
                                    locTags.add(tNames["name"].toString())
                                }
                                val tagL = "Tags: " + locTags.joinToString(separator = ", ")
                                tagText.setText(tagL)
                            }

                            // If the location is new enough to have vertices representing its
                            // polygonal form, then draw the polygon representing the location's
                            // boundaries and draw the location's center, otherwise hide the
                            // imageview that would have shown this information
                            val locationShape = view!!.findViewById<ImageView>(R.id.locationShape)
                            if (loc.contains("polygonImageXCoordinates")) {
                                val polygonImageXCoordinates = loc["polygonImageXCoordinates"] as List<Double>
                                val polygonImageYCoordinates = loc["polygonImageYCoordinates"] as List<Double>

                                locationShape.setImageBitmap(drawLocationShapeAsBitmap(polygonImageXCoordinates,
                                    polygonImageYCoordinates, true, 150, 150))
                            }
                            else {
                                locationShape.visibility = View.GONE
                            }

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
                //intent.putExtra("context", context);
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

    // This is based on the function of the same name in AddLocationFragment.kt
    private fun drawLocationShapeAsBitmap(xPoints: List<Double>, yPoints: List<Double>,
                                          shouldDrawCentroid: Boolean,
                                          width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeWidth = 3F; color = 0xFFFFFFFF.toInt() }

        // drawLines() needs coordinates in a certain format to represent pairs of points
        // xPoints = [1F, 2F, 3F], yPoints = [4F, 5F, 6F] => [1F, 4F, 1F, 4F, 2F, 5F, 2F, 5F, 3F, 6F, 3F, 6F]
        val pointArray = xPoints.zip(yPoints)
            .map { pair -> listOf(pair.first.toFloat(), pair.second.toFloat(), pair.first.toFloat(), pair.second.toFloat()) }
            .flatten().toFloatArray()
        // Omit duplicate first two and last two coordinates
        canvas.drawLines(pointArray, 2, pointArray.size - 2, paint)
        // Draw the line between the last point and the first point
        canvas.drawLines(floatArrayOf(pointArray[pointArray.size - 2], pointArray[pointArray.size - 1], pointArray[0], pointArray[1]),
            0, 4, paint)

        if (shouldDrawCentroid) {
            // Compute the central point of the location's vertices by averaging them.
            // See the function with this same name in AddLocationFragment for further explanation
            val centroidX = xPoints.reduce {acc, point -> acc + point} / xPoints.size
            val centroidY = yPoints.reduce {acc, point -> acc + point} / yPoints.size
            canvas.drawCircle(centroidX.toFloat(), centroidY.toFloat(), 5F, paint)
        }

        return bitmap
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
            } else if (item1.like == item2.like && item1.timestamp != null && item2.timestamp != null) {
                ret = item1.timestamp.compareTo(item2.timestamp)
            } else {
                ret = -1
            }

            ret
        }
}