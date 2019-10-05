package com.cs407.team15.redstone.ui.location

import android.app.ActionBar
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ImageView
import android.widget.TextView
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
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.app_bar_main.view.*
import kotlinx.android.synthetic.main.location_display.view.*
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList


class LocationPage : Fragment() {

    override fun onCreateView(inflater: LayoutInflater,
                          container: ViewGroup?,
                          savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.location_display, container, false)
        val title = arguments?.getCharSequence("title")
        root.loaction_name.text = title
        val mainRoot = inflater.inflate(R.layout.app_bar_main, container, false)

        var tagArray = ArrayList<String>()

        val locations= FirebaseFirestore.getInstance().collection("locations").get().addOnSuccessListener {locations->
            for (loc in locations.documents) {
                if (loc["name"] as String == title as String) {

                    root.location_description.text = "Tags: " + loc["description"] as String
                    //coordinates, timestamp,userid, name, description,image_src

                    //Displaying tags for the location
                    if (loc["tags"] == null) {
                        root.tagsView.text = "No tags to display"
                    } else {
                        var addingTagsArray = arrayOfNulls<String>(addingTagsArrayList.size)
                        addingTagsArrayList.toArray(addingTagsArray)


                        var builderS = StringBuilder()
                        for (i in addingTagsArray){
                            if (i == addingTagsArray.last()){
                                builderS.append(""+ i)
                            } else {
                                builderS.append("" + i + ", ")
                            }
                        }
                        var tagInput = builderS.toString()
                        println(tagInput)

                        //var splitTagArray1: List<String> = Arrays.asList(addingTagsArray)

                        var splitTagArray: Array<String> = tagInput.split(",").toTypedArray()
                        //var tagToPush: List<String> = Arrays.asList()



                        root.tagsView.text = loc["tags"] as String
                        tagArray = loc["tags"] as ArrayList<String>
                    }

                    //sets image of location if there is a image
                    var locationImage = root.location_image
                    var url = loc["image_src"] as String?
                    if (url != null) {
                        var storage = FirebaseStorage.getInstance()
                        var imageStorage = storage.getReferenceFromUrl(url)
                        Glide.with(this).load(imageStorage).into(locationImage)
                    } else {
                        locationImage.setImageResource(R.drawable.background_bell_tower)
                    }
                }
            }
        }

        //Getting all tags
        var allTagArray = ArrayList<String>()
        FirebaseFirestore.getInstance().collection("tags").get().addOnSuccessListener { tagNames ->
            for (n in tagNames.documents){
                var stringStorage = n["name"] as String
                //println(stringStorage)
                allTagArray.add(stringStorage)
            }
            //allTagArray is store inside the curly brackets. I don't know why

            //Compare tags
            var remainingTagArray = ArrayList<String>()
            var flag = 0
            if (tagArray.size > 0){
                for (a in allTagArray){
                    flag = 0
                    for (b in tagArray){
                        if (a == b){
                            flag = 1
                        }
                    }
                    if (flag == 0){
                        remainingTagArray.add(a)
                    }
                }
            } else {
                remainingTagArray = allTagArray
            }

            //TESTING
//        var bui = StringBuilder()
//        for (i in allTagArray){
//            bui.append(""+ i + " ")
//        }
            //println(bui as String)

            var checkedTags = ArrayList<Boolean>()
            for (i in 1..remainingTagArray.size){
                checkedTags.add(false)
            }

            //Adding tags functionality
            val addTagsButton = root.add_tag_button
            addTagsButton.setOnClickListener {

                val tagsArr = arrayOfNulls<String>(remainingTagArray.size)
                remainingTagArray.toArray(tagsArr)
                val checkedTags = BooleanArray(remainingTagArray.size) { i -> false }

                val builder = AlertDialog.Builder(context)
                builder.setTitle("Select tags for location:")

                //TEST:
//            var colorsArray = arrayOf("Black", "Orange", "Green", "Black", "Orange", "Green", "Black", "Orange", "Green", "Black", "Orange", "Green", "Black", "Orange", "Green", "Black", "Orange", "Green", "Black", "Orange", "Green", "Black", "Orange", "Green")
//            var checkedColorsArray = booleanArrayOf(false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false)
//            builder.setMultiChoiceItems(colorsArray, checkedColorsArray) {dialog, which, isChecked ->
//                checkedColorsArray[which] = isChecked
//            }

                builder.setMultiChoiceItems(tagsArr, checkedTags) {dialog, which, isChecked ->
                    checkedTags[which] =  isChecked
                }



                Toast.makeText(context, "Hello World", Toast.LENGTH_LONG)

                builder.setPositiveButton("Add") { dialog, which ->
                    //post to FireStore

                    var flag1 = 0
                    var addingTagsArrayList = ArrayList<String>()
                    for (i in checkedTags.indices){
                        val checked = checkedTags[i]
                        if (checked){
                            flag1 = 1;
                            addingTagsArrayList.add(remainingTagArray[i])
                        }
                    }
                    if (flag1 == 1){
                        for (i in tagArray){
                            addingTagsArrayList.add(i)
                        }
                        var addingTagsArray = arrayOfNulls<String>(addingTagsArrayList.size)
                        addingTagsArrayList.toArray(addingTagsArray)


                        var builderS = StringBuilder()
                        for (i in addingTagsArray){
                            if (i == addingTagsArray.last()){
                                builderS.append(""+ i)
                            } else {
                                builderS.append("" + i + ", ")
                            }
                        }
                        var tagInput = builderS.toString()
                        println(tagInput)

                        //var splitTagArray1: List<String> = Arrays.asList(addingTagsArray)

                        var splitTagArray: Array<String> = tagInput.split(",").toTypedArray()
                        //var tagToPush: List<String> = Arrays.asList()

                        val updates : MutableMap<String, Any> = HashMap()
                        updates.put("tags", splitTagArray)

                        val addTagToLocation =
                            FirebaseFirestore.getInstance().collection("locations").get().addOnSuccessListener { locations ->
                                for (loc in locations.documents){
                                    if (loc["name"] as String == title as String){
                                        FirebaseFirestore.getInstance().collection("locations").document(loc.id).set(updates)
                                    }
                                }
                            }
                    }
                }
                val adialog = builder.create()
                adialog.show()
            }
            /*  for (loc in locations.){

              }*/

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