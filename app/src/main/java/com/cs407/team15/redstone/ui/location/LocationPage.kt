package com.cs407.team15.redstone.ui.location

import android.app.ActionBar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ImageView
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
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.location_display.view.*


class LocationPage : Fragment() {

    override fun onCreateView(inflater: LayoutInflater,
                          container: ViewGroup?,
                          savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.location_display, container, false)
       val title = arguments?.getCharSequence("title")
        root.loaction_name.text = title



        val locations= FirebaseFirestore.getInstance().collection("locations").get().addOnSuccessListener {locations->
            for (loc in locations.documents){
                if(loc["name"] as String == title as String){
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