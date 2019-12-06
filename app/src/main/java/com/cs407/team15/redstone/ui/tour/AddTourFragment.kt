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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.team15.redstone.R
import com.cs407.team15.redstone.model.LocPair
import com.cs407.team15.redstone.model.Location
import com.cs407.team15.redstone.model.Tour
import com.cs407.team15.redstone.model.User
import com.cs407.team15.redstone.ui.tour.helper.ItemTouchHelperAdapter
import com.cs407.team15.redstone.ui.tour.helper.SimpleItemTouchHelperCallback
import com.cs407.team15.redstone.ui.viewtours.ViewToursFragment
import com.google.android.gms.common.internal.safeparcel.SafeParcelable
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QuerySnapshot
import com.toptoche.searchablespinnerlibrary.SearchableSpinner
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.lang.Math.cos
import java.lang.Math.sin
import java.math.BigDecimal
import java.math.MathContext
import java.sql.Time
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class AddTourFragment : Fragment() {

    // Alphabetized list of all locations that exist
    val allLocations: MutableList<Location> = mutableListOf()
    // List of all locations that exist on the tour being added
    val locationsOnTour: MutableList<Location> = mutableListOf()
    val locationsOTStr: MutableList<String> = mutableListOf()
    /// Alphabetized list of the names of all tags that exist
    val allTagNames: MutableList<String> = mutableListOf()
    // List of the names of all the tags on the tour being added
    val tagsOnTour: MutableList<String> = mutableListOf()
    val locPairings: ArrayList<LocPair> = arrayListOf()
    val tourList: ArrayList<LocPair> = arrayListOf()
    val finalLocations: ArrayList<Location> = arrayListOf()

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
        val autoGen = view.findViewById<Switch>(R.id.autoswitch);

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
        cancelButton.setOnClickListener {
            cancelTour()
        }

        // add tour button on click listener
        buttonCreateTour.setOnClickListener {
            addNewTour()
        }

        autoGen.setOnClickListener(View.OnClickListener {
            if (autoGen.isChecked) {
                if (tagsOnTour.size == 0) {
                    val toast = Toast.makeText(context, "Select a tag", Toast.LENGTH_SHORT);
                    toast.show()
                    autoGen.setChecked(false);
                } else {
                    GlobalScope.launch { autogenerate() }


                }
            }
            else {
                finalLocations.clear()
                tourList.clear()
                locPairings.clear()
                locationsOTStr.clear()
                locationsOnTour.clear()
                locationsAdapter!!.notifyDataSetChanged()
            }
        })

        return view
    }


    suspend fun getLocationsAndFillLocationSpinner() {
        // Get list of all locations in alphabetical order
        allLocations.addAll(Location.getAllLocations())
        val locationNames = allLocations.map { location -> location.name }

        // put location list in selectable spinner
        activity!!.runOnUiThread {
            val locationSpinner = view!!.findViewById<Spinner>(R.id.locationSpinner)
            val spinnerAdapter = ArrayAdapter<String>(
                context as Context,
                android.R.layout.simple_spinner_item,
                locationNames
            )
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            locationSpinner.adapter = spinnerAdapter
            locationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
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
            .documents.map { document -> document.getString("name") as String }.sorted()
            .toMutableList()
        allTagNames.addAll(tags)

        // put tags list in selectable spinner
        activity!!.runOnUiThread {
            val tagSpinner = view!!.findViewById<Spinner>(R.id.tagSpinner)
            val spinnerAdapter = ArrayAdapter<String>(
                context as Context,
                android.R.layout.simple_spinner_item,
                allTagNames
            )
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            tagSpinner.adapter = spinnerAdapter
            tagSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
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
        if (view!!.findViewById<EditText>(R.id.editTitle).text.toString().equals(null)) {
            Toast.makeText(context, "Please enter something in text box", Toast.LENGTH_LONG).show()
            name = ""
            return
        } else {
            name = view!!.findViewById<EditText>(R.id.editTitle).text.toString()
        }

        // get "personal" or "community" type
        val type: String = if (view!!.findViewById<Switch>(R.id.switchPT).isChecked)
            "personal"
        else
            "community"

        // check if other community tours exist with the same locations
        if (type == "community" && checkRepeatTours()) {
            Toast.makeText(
                context,
                "A tour with those locations already exists.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // get current user id
        val user_id: String = FirebaseAuth.getInstance().currentUser!!.uid

        // get hammer status of current user
        val hammer: Boolean = user.userType != 0

        // get list of locations
        if (locationsOTStr.isEmpty()) {
            Toast.makeText(context, "Please add at least one location", Toast.LENGTH_LONG).show()
            return
        }

        // get list of tags
        if (tagsOnTour.isEmpty() && type == "community") {
            Toast.makeText(context, "Please add at least one tag", Toast.LENGTH_LONG).show()
            return
        }

        // tours start out with 0 votes
        val initialVotes = 0

        //distance of tour in miles
        var distance = 0.001
        var count = 0
        var pastLoc: Location = locationsOnTour[0]
        if (locationsOTStr.size > 1) {
            for (location in locationsOnTour) {
                if (count > 0) {
                    var currentLat = location.coordinates.latitude
                    var currentLong = location.coordinates.longitude
                    distance += calcDistanceFromCoordinates(
                        currentLat,
                        currentLong,
                        pastLoc.coordinates.latitude,
                        pastLoc.coordinates.longitude
                    )
                }
                pastLoc = location
                count += 1
            }
            distance = Math.floor(distance * 100) / 100
        }
        val time = (distance*24).toInt()

        // create tour object
        val newTour =
            Tour(name, type, user_id, hammer, locationsOTStr, tagsOnTour, initialVotes, distance)

        // push tour to firebase
        db.collection("tours")
            .add(newTour)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot written with ID: ${documentReference.id}")
                db.collection("tours").document(documentReference.id).update(mapOf("time" to time))
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

    fun checkRepeatTours(): Boolean {
        return false
    }

    //calculate the distance in miles between two coordinate points
    private fun calcDistanceFromCoordinates(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0.0
        } else {
            val theta = lon1 - lon2
            var dist = (sin(Math.toRadians(lat1)) * sin(Math.toRadians(lat2))) +
                    (cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * cos(
                        Math.toRadians(
                            theta
                        )
                    ))
            dist = Math.acos(dist)
            dist = Math.toDegrees(dist)
            dist = dist * 60 * 1.1515
            return (dist)
        }
    }

    suspend fun autogenerate2() {
        System.out.println("heyhey")
        val db = FirebaseFirestore.getInstance()
        db.collection("locations").get().addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
            if (task.isSuccessful) {
                for (document in task.result!!) {
                    if (document.exists()) {
                        for (i in tourList) {
                            if (document.get("location_id").toString().equals(i.locId)) {
                                finalLocations.add(Location(document.getGeoPoint("coordinates")!!,
                                    document.get("description").toString(),
                                    document.get("name").toString(), document.get("user_id").toString()))
                            }
                        }
                    }
                }
                for (i in 0 until finalLocations.size - 1) {
                    val loc1 = android.location.Location("")
                    loc1.latitude = finalLocations.get(i).coordinates.latitude
                    loc1.longitude = finalLocations.get(i).coordinates.longitude
                    val loc2 = android.location.Location("")
                    loc2.latitude =
                        finalLocations.get(i + 1).coordinates.latitude
                    loc2.longitude =
                        finalLocations.get(i + 1).coordinates.longitude
                    var minDistance = loc1.distanceTo(loc2) as Float
                    var ind = i + 1 as Int
                    for (b in i + 2 until finalLocations.size) {
                        val loc3 = android.location.Location("")
                        loc3.latitude =
                            finalLocations.get(b).coordinates.latitude
                        loc3.longitude =
                            finalLocations.get(b).coordinates.longitude
                        if (loc1.distanceTo(loc3) < minDistance) {
                            minDistance = loc1.distanceTo(loc3)
                            ind = b
                        }
                    }
                    val temp = finalLocations.get(i + 1)
                    finalLocations.set(i + 1, finalLocations.get(ind))
                    finalLocations.set(ind, temp)
                }
                locationsOnTour.addAll(finalLocations)
                for (name in finalLocations) {
                    locationsOTStr.add(name.name)
                }
                locationsAdapter!!.notifyDataSetChanged()
            }
            else {
                Log.d("lol", "oopsie doopsie")
            }
        })

    }

    suspend fun autogenerate() {
        FirebaseDatabase.getInstance().getReference("Comments").child("location")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    for (snapshot in p0.children) {
                        val locId = snapshot.key
                        var total: Long = 0;
                        val iterator = snapshot.children.iterator()
                        while (iterator.hasNext()) {
                            val temp = iterator.next()
                            if (temp.child("tags").value != null) {
                                if (temp.child("tags").value!!.equals(tagsOnTour.get(0))) {
                                    total = total + temp.child("like").value as Long
                                }
                            }
                        }
                        System.out.println("LOCPAIR: " + locId + ", " + total)
                        locPairings.add(LocPair(locId, total))
                    }
                    val locPairings2 = locPairings.sortedWith(compareByDescending { it.total })
                    for (i in 0..3) {
                        System.out.println("chosen pair: " + locPairings2[i].locId + " " + locPairings2[i].total)
                        tourList.add(locPairings2[i]);
                    }
                    GlobalScope.launch { autogenerate2() }
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            });

    }


}