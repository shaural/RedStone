package com.cs407.team15.redstone.ui.location

import android.app.ActionBar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.cs407.team15.redstone.MainActivity
import com.cs407.team15.redstone.R
import com.cs407.team15.redstone.ui.tour.TourFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.android.gms.tasks.OnSuccessListener
import com.bumptech.glide.Glide
import com.cs407.team15.redstone.model.Location
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.app_bar_main.view.*
import kotlinx.android.synthetic.main.location_display.view.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class LocationPage : Fragment(), CoroutineScope {
    lateinit var location_id: String
    lateinit var btn_flag: Button
    var isFlagged: Boolean = false

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
        launch {
            val title = arguments?.getCharSequence("title")
            root.loaction_name.text = title
            val mainRoot = inflater.inflate(R.layout.app_bar_main, container, false)

            val locations = FirebaseFirestore.getInstance().collection("locations").get()
                .addOnSuccessListener { locations ->
                    for (loc in locations.documents) {
                        if (loc["name"] as String == title as String) {
                            location_id = loc.id
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
        }
        return root
    }
}