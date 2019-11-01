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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.location_display.view.*
import java.util.ArrayList

class TourInfoActivity : AppCompatActivity() {

    lateinit var recyclerView: RecyclerView
    lateinit var commentList: ArrayList<Comment>
    lateinit var viewAllComments: TextView
    private lateinit var commentAdapter: CommentSectionAdapter

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_tour_info)
        val tourName = intent.getStringExtra("tourName")
        val tourId = intent.getStringExtra("tourID")

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


        // Comments recyclerview
        viewAllComments = findViewById(R.id.tv_comments)
        recyclerView = findViewById(R.id.recycler_view_comment)
        recyclerView.setHasFixedSize(true)

        val mLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = mLayoutManager

        commentList = ArrayList<Comment>()
        commentAdapter = CommentSectionAdapter(this, commentList)
        recyclerView.adapter = commentAdapter

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
}