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
import kotlinx.android.synthetic.main.location_display.view.*
import kotlinx.coroutines.runBlocking


class LocationPage : Fragment() {

    lateinit var location_id: String
    override fun onCreateView(inflater: LayoutInflater,
                          container: ViewGroup?,
                          savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.location_display, container, false)
       val title = arguments?.getCharSequence("title")
        root.loaction_name.text = title
        var btn_flag = root.btn_flag_location as Button
        val key: String

        Toast.makeText(context, "HELLO", Toast.LENGTH_SHORT)

        val locations= FirebaseFirestore.getInstance().collection("locations").get().addOnSuccessListener {locations->
            for (loc in locations.documents){
                if(loc["name"] as String == title as String){
                    location_id = loc.id
                    var isFlagged = false
                    Toast.makeText(context, "test", Toast.LENGTH_SHORT)
//                    runBlocking { isFlagged = Location.hasUserFlaggedLocation(loc.id) }
                    Toast.makeText(context, "here", Toast.LENGTH_SHORT)
                    if(isFlagged) {
                        btn_flag.setBackgroundColor(resources.getColor(R.color.RED))
                    } else {
                        btn_flag.setBackgroundColor(resources.getColor(R.color.GREEN))
                    }

                   root.location_description.text=loc["description"] as String
                    //coordinates, timestamp,userid, name, description,image_src

                    //sets image of location if there is a image
                    var locationImage =root.location_image
                    var url = loc["image_src"] as String?
                    if(url!=null){
                    var storage =FirebaseStorage.getInstance()
                    var imageStorage =  storage.getReferenceFromUrl(url)
                    Glide.with(this).load(imageStorage).into(locationImage)}
                    else{
                        locationImage.setImageResource(R.drawable.background_bell_tower)
                    }
                }

        } }


      /*  for (loc in locations.){

        }*/
        btn_flag.setOnClickListener {
//            runBlocking { Location.toggleHasUserFlaggedLocation(location_id) }
            var isFlagged = false
//            runBlocking { isFlagged = Location.hasUserFlaggedLocation(location_id) }
            if(isFlagged) {
                btn_flag.setBackgroundColor(resources.getColor(R.color.RED))
            } else {
                btn_flag.setBackgroundColor(resources.getColor(R.color.GREEN))
            }
        }

        val button = root.loc_back_button
        button.setOnClickListener(
            {
                val frag = fragmentManager!!.beginTransaction()
                val loc=TourFragment()
                frag.replace((view!!.parent as ViewGroup).id, loc)
                // frag.addToBackStack(null)
                frag.commit()
            }
        )
        return root
    }
/*
* service firebase.storage {
  match /b/{bucket}/o {
    match /{allPaths=**} {
      allow read, write: if request.auth != null;
    }
  }
}
* */
}