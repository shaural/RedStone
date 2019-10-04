package com.cs407.team15.redstone.ui.tour

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
        database = FirebaseDatabase.getInstance().reference
        btn_add_loc.setOnClickListener {
            val name = view!!.findViewById<EditText>(R.id.et_loc_name).text.toString()
            var desc = view!!.findViewById<EditText>(R.id.et_about).text.toString()
            val currentFirebaseUser = FirebaseAuth.getInstance().currentUser

            val newKey = database.child("locations").push().key.toString()

            FirebaseFirestore.getInstance().collection("locations").document(newKey)
                .set(hashMapOf("timestamp" to com.google.firebase.Timestamp.now(), "description" to desc, "name" to name, "user_id" to currentFirebaseUser!!.uid, "coordinates" to GeoPoint(arguments!!.getDouble("latitude"), arguments!!.getDouble("longitude"))))
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
