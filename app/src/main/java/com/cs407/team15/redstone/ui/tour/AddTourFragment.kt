package com.cs407.team15.redstone.ui.tour

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.team15.redstone.R
import com.cs407.team15.redstone.model.Location
import com.cs407.team15.redstone.model.Tour
import com.cs407.team15.redstone.model.User
import com.cs407.team15.redstone.ui.location.RecyclerAdapter
import com.cs407.team15.redstone.ui.tour.helper.ItemTouchHelperAdapter
import com.cs407.team15.redstone.ui.tour.helper.SimpleItemTouchHelperCallback
import com.cs407.team15.redstone.ui.viewtours.ViewToursFragment
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.toptoche.searchablespinnerlibrary.SearchableSpinner
import kotlinx.android.synthetic.main.fragment_addtour.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AddTourFragment : Fragment(){

    // Alphabetized list of all locations that exist
    val allLocations: MutableList<Location> = mutableListOf()
    // List of all locations that exist on the tour being added
    val locationsOnTour: MutableList<Location> = mutableListOf()
    val locationsOTStr : MutableList<String> = mutableListOf()
    /// Alphabetized list of the names of all tags that exist
    val allTagNames: MutableList<String> = mutableListOf()
    // List of the names of all the tags on the tour being added
    val tagsOnTour: MutableList<String> = mutableListOf()

    var locationsAdapter: LocationsAdapter? = null
    var tagsAdapter: TagsAdapter? = null
    var containerId = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        containerId = container!!.id
        // launch location spinner for selections
        GlobalScope.launch { getLocationsAndFillLocationSpinner() }
        // launch tags spinner for selections
        GlobalScope.launch { getTagsAndFillTagSpinner() }

        // inflate view
        val view = inflater.inflate(R.layout.fragment_addtour, container, false)
        val locationsRecyclerView = view.findViewById<RecyclerView>(R.id.locationsRecyclerView)
        val tagsRecyclerView = view.findViewById<RecyclerView>(R.id.tagsRecyclerView)
        val cancelButton = view.findViewById<Button>(R.id.buttonCancelNewTour)
        val buttonCreateTour = view.findViewById<Button>(R.id.buttonCreateTour)

        var layoutManager = LinearLayoutManager(activity)
        locationsRecyclerView.layoutManager = layoutManager
        locationsRecyclerView.itemAnimator = DefaultItemAnimator()

        locationsAdapter = LocationsAdapter(activity as Activity, locationsOnTour)
        locationsRecyclerView.adapter = locationsAdapter

        //drag and drop functionality
        val callback = SimpleItemTouchHelperCallback(locationsAdapter as ItemTouchHelperAdapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(locationsRecyclerView)

        layoutManager = LinearLayoutManager(activity)
        tagsRecyclerView.layoutManager = layoutManager
        tagsRecyclerView.itemAnimator = DefaultItemAnimator()

        tagsAdapter = TagsAdapter(activity as Activity, tagsOnTour)
        tagsRecyclerView.adapter = tagsAdapter

        // X button on click listener
        cancelButton.setOnClickListener{
            cancelTour()
        }

        // add tour button on click listener
        buttonCreateTour.setOnClickListener{
            addNewTour()
        }

        return view
    }

    suspend fun getLocationsAndFillLocationSpinner() {
        // Get list of all locations in alphabetical order
        allLocations.addAll(Location.getAllTours())
        val locationNames = allLocations.map { location -> location.name }

        // put location list in selectable spinner
        activity!!.runOnUiThread {
            val locationSpinner = view!!.findViewById<Spinner>(R.id.locationSpinner)
            val spinnerAdapter = ArrayAdapter<String>(context as Context, android.R.layout.simple_spinner_item, locationNames)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            locationSpinner.adapter = spinnerAdapter
            locationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    locationsOnTour.add(allLocations[position])
                    locationsOTStr.add(allLocations[position].name)
                    locationsAdapter!!.notifyDataSetChanged()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
            (locationSpinner as SearchableSpinner).setTitle(getString(R.string.add_location_to_tour))
            locationSpinner.invalidate()
        }
    }

    suspend fun getTagsAndFillTagSpinner() {
        // Get the name of all tags in alphabetical order
        val tags = FirebaseFirestore.getInstance().collection("tags").get().await()
            .documents.map{document -> document.getString("name") as String}.sorted().toMutableList()
        allTagNames.addAll(tags)

        // put tags list in selectable spinner
        activity!!.runOnUiThread {
            val tagSpinner = view!!.findViewById<Spinner>(R.id.tagSpinner)
            val spinnerAdapter = ArrayAdapter<String>(context as Context, android.R.layout.simple_spinner_item, allTagNames)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            tagSpinner.adapter = spinnerAdapter
            tagSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val tagName = allTagNames[position]
                    if (!tagsOnTour.contains(tagName)) {
                        tagsOnTour.add(tagName)
                        tagsAdapter!!.notifyDataSetChanged()
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
            (tagSpinner as SearchableSpinner).setTitle(getString(R.string.add_tag_to_tour))
            tagSpinner.invalidate()
        }
    }

    // cancel tour creation
    fun cancelTour() {
        val builder = AlertDialog.Builder(context)

        builder.setTitle("Cancel Tour")
        builder.setMessage("Are you sure you want to cancel your new tour?")

        // if yes, go to previous page
        builder.setPositiveButton("YES") { dialog, which ->
            activity!!.onBackPressed()
        }

        // if no, exit alert dialog
        builder.setNegativeButton("NO") { dialog, which ->
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    // add new tour to the database
    fun addNewTour() {
        val db = FirebaseFirestore.getInstance()
        var user = User()

        // make current user object
        db.collection("users").document(FirebaseAuth.getInstance().currentUser?.email!!).get()
            .addOnSuccessListener(OnSuccessListener {
                user = it.toObject(User::class.java) as User
            })

        // get name of tour from edittext
        val name: String
        if(view!!.findViewById<EditText>(R.id.editTitle).text.toString().equals(null)){
            Toast.makeText(context,"Please enter something in text box", Toast.LENGTH_LONG).show()
            name = ""
            return
        }
        else{
            name = view!!.findViewById<EditText>(R.id.editTitle).text.toString()
        }

        // get "personal" or "community" type
        val type: String = if (view!!.findViewById<Switch>(R.id.switchPT).isChecked)
            "personal"
        else
            "community"

        // check if other community tours exist with the same locations
        if(type == "community" && checkRepeatTours()){
            Toast.makeText(context,"A tour with those locations already exists.", Toast.LENGTH_LONG).show()
            return
        }

        // get current user id
        val user_id: String = FirebaseAuth.getInstance().currentUser!!.uid

        // get hammer status of current user
        val hammer: Boolean = user.userType != 0

        // get list of locations
        if(locationsOTStr.isEmpty()){
            Toast.makeText(context,"Please add at least one location",Toast.LENGTH_LONG).show()
            return
        }

        // get list of tags
        if(tagsOnTour.isEmpty() && type == "community"){
            Toast.makeText(context,"Please add at least one tag", Toast.LENGTH_LONG).show()
            return
        }

        // tours start out with 0 votes
        val initialVotes = 0

        // create tour object
        val newTour = Tour(name, type, user_id, hammer, locationsOTStr, tagsOnTour, initialVotes)

        // push tour to firebase
        db.collection("tours")
            .add(newTour)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot written with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)

            }

        //go to viewTours
        val viewToursFrag = ViewToursFragment()
        val fragTransaction = fragmentManager!!.beginTransaction()
        fragTransaction.replace(containerId, viewToursFrag)
        fragTransaction.addToBackStack(null)
        fragTransaction.commit()
    }

    fun checkRepeatTours() : Boolean {
        return false
    }
}