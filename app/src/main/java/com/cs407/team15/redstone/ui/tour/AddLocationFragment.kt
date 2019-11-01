package com.cs407.team15.redstone.ui.tour

import android.app.AlertDialog
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import com.cs407.team15.redstone.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint
import com.google.type.LatLng
import kotlinx.android.synthetic.main.add_location_fragment.*
import java.sql.Time
import java.sql.Timestamp
import java.lang.StringBuilder
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


class AddLocationFragment : Fragment() {
    companion object {
        fun newInstance() = AddLocationFragment()
    }

    private lateinit var viewModel: AddLocationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.add_location_fragment, container, false)
    }
    lateinit var database: DatabaseReference

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(AddLocationViewModel::class.java)
        val btn_add_loc = getView()!!.findViewById(R.id.btn_add_location) as Button
        val btn_newTag = getView()!!.findViewById(R.id.btn_create_tag) as Button
        database = FirebaseDatabase.getInstance().reference


        val newKey = database.child("locations").push().key.toString()
        val addTagtoLoc = FirebaseFirestore.getInstance().collection("locations").document(newKey)

        btn_newTag.setOnClickListener {
            val builder =  AlertDialog.Builder(context)
            val inflater = layoutInflater
            builder.setTitle("Create Tag")
            val dialogLayout = inflater.inflate(R.layout.alert_dialog_edittext, null)
            builder.setView(dialogLayout)
            builder.setPositiveButton(android.R.string.ok) {dialog, p1 ->
                val newTag = dialogLayout.findViewById<EditText>(R.id.editText).text.toString()
                //Toast.makeText(context, newTag, Toast.LENGTH_SHORT).show()
                var isValid = true
                if (newTag.isBlank()){
                    isValid = false
                }
                if (isValid){

                    val newTagInp = hashMapOf(
                        "name" to newTag as String
                    )
                    FirebaseFirestore.getInstance().collection("tags").document().set(newTagInp)
                    dialog.dismiss()
                }
            }
            builder.setNegativeButton(android.R.string.cancel) { dialog, p2 ->
                dialog.cancel()
            }
            builder.show()
        }

        val addTagsButton = view!!.findViewById(R.id.btn_add_tag) as Button
        addTagsButton.setOnClickListener {
            var storeTags = ArrayList<String>()
            FirebaseFirestore.getInstance().collection("tags").get()
                .addOnSuccessListener { tagNames ->
                    for (n in tagNames.documents) {
                        var stringStorage = n["name"] as String
                        storeTags.add(stringStorage)
                    }

//            var checkTags = ArrayList<Boolean>()
//            for (i in 1..storeTags.size){
//                checkTags.add(false)
//            }

                    //addTagsButton.setOnClickListener {
                    val tagsArr = arrayOfNulls<String>(storeTags.size)
                    storeTags.toArray(tagsArr)
                    val checkTags = BooleanArray(storeTags.size) { i -> false }

                    val builder = AlertDialog.Builder(context)
                    builder.setMultiChoiceItems(tagsArr, checkTags) { dialog, which, isChecked ->
                        checkTags[which] = isChecked
                    }
                    builder.setPositiveButton("Add") { dialog, which ->
                        var flag0 = 0
                        var addingTagsArr = ArrayList<String>()
                        for (i in checkTags.indices) {
                            val checked = checkTags[i]
                            if (checked) {
                                flag0 = 1
                                addingTagsArr.add(storeTags[i])
                            }
                        }
                        var addingTagsArray = arrayOfNulls<String>(addingTagsArr.size)
                        addingTagsArr.toArray(addingTagsArray)


                        var builderS = StringBuilder()
                        for (i in addingTagsArray) {
                            if (i == addingTagsArray.last()) {
                                builderS.append("" + i)
                            } else {
                                builderS.append("" + i + ", ")
                            }
                        }
                        val newKey = database.child("locations").push().key.toString()
                        val currentFirebaseUser = FirebaseAuth.getInstance().currentUser
                        val name = view!!.findViewById<EditText>(R.id.et_loc_name).text.toString()
                        var desc = view!!.findViewById<EditText>(R.id.et_about).text.toString()

                        addTagtoLoc.set(
                            hashMapOf(
                                "timestamp" to com.google.firebase.Timestamp.now(),
                                "location_id" to newKey,
                                "description" to desc,
                                "name" to name,
                                "user_id" to currentFirebaseUser!!.uid,
                                "coordinates" to GeoPoint(
                                    arguments!!.getDouble("latitude"),
                                    arguments!!.getDouble("longitude")
                                )
                            )
                        )
                        for (i in addingTagsArray) {
                            val tagInputs = hashMapOf(
                                "name" to i as String
                            )
                            addTagtoLoc.collection("tags").add(tagInputs)
                        }
                    }
                    val adialog = builder.create()
                    adialog.show()
                    //}
                }
        }

        btn_add_loc.setOnClickListener {
            val name = view!!.findViewById<EditText>(R.id.et_loc_name).text.toString()
            var desc = view!!.findViewById<EditText>(R.id.et_about).text.toString()
            val currentFirebaseUser = FirebaseAuth.getInstance().currentUser

            //val newKey = database.child("locations").push().key.toString()

            addTagtoLoc
                .set(hashMapOf("timestamp" to com.google.firebase.Timestamp.now(), "location_id" to newKey, "description" to desc, "name" to name, "user_id" to currentFirebaseUser!!.uid, "coordinates" to GeoPoint(arguments!!.getDouble("latitude"), arguments!!.getDouble("longitude"))))
            .addOnSuccessListener {
                Toast.makeText(context, "Location Added", Toast.LENGTH_SHORT).show()
                this.activity!!.supportFragmentManager.popBackStack()
            }.addOnFailureListener{
                Toast.makeText(context, "Sorry, could not add location.", Toast.LENGTH_SHORT).show()
                this.activity!!.supportFragmentManager.popBackStack()
            }
        }
//        val btn_back = getView()!!.findViewById(R.id.btn_add_location) as Button
//        btn_back.setOnClickListener {
//            this.activity!!.supportFragmentManager.popBackStack()
//        }

    }


}
