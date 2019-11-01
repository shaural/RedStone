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
import com.cs407.team15.redstone.model.Location
import com.cs407.team15.redstone.model.Tour
import com.cs407.team15.redstone.model.User
import com.cs407.team15.redstone.ui.tour.helper.ItemTouchHelperAdapter
import com.cs407.team15.redstone.ui.tour.helper.SimpleItemTouchHelperCallback
import com.cs407.team15.redstone.ui.viewtours.ViewToursFragment
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.toptoche.searchablespinnerlibrary.SearchableSpinner
import kotlinx.android.synthetic.main.fragment_addtour.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.ArrayList

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
        val locations = arguments?.getStringArrayList("locations")
        val tourId = arguments?.getString("tourId")
        GlobalScope.launch { getLocationsAndFillLocationSpinner(locations) }
        GlobalScope.launch { getTagsAndFillTagSpinner() }
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

        cancelButton.setOnClickListener{
            cancelTour()
        }
// add edit/ draft switch

        val title = arguments?.getString("title")
        val type = arguments?.getString("type")
        val tags = arguments?.getStringArrayList("tags")


        if(title!=null){
            view.editTitle.setText(title)
        }
        if(type!=null){
            if(type=="personal") {
                view.switchPT.isChecked = true
            }else{
                view.switchPT.isChecked= false
            }
        }

        if(tags!=null){
            for( tag in tags){
                tagsOnTour.add(tag)
            }
        }
        //Location.getAllTours()
        if(title!=null||type!=null||tags!=null||locations!=null) {
            buttonCreateTour.setOnClickListener {
                addNewTour(true,tourId!!)
            }
        }else{
            buttonCreateTour.setOnClickListener {
                addNewTour(false,null)
            }
        }
        return view
    }

    suspend fun getLocationsAndFillLocationSpinner(locations: ArrayList<String>?) {
        // Get list of all locations in alphabetical order
        allLocations.addAll(Location.getAllTours())


        if(locations!=null && locations.isNotEmpty()){
            val locName= arrayOfNulls<String>(locations?.size)
            val locPlace= arrayOfNulls<Location>(locations?.size)
            for(allLocation in allLocations){
                for(i in 0..locations.size-1){

                if(locations.get(i)==allLocation.name){
                    locPlace[i]=(allLocation)
                    locName[i]=(allLocation.name)
                }
            }
            }
            if(locName.size>0){
            for(i in 0..locName.size-1){
                if(locName[i]!=null){
                locationsOnTour.add(locPlace[i]!!)
                locationsOTStr.add(locName[i]!!)
//                locationsAdapter!!.notifyDataSetChanged()
                }
            }}
        }


        val locationNames = allLocations.map { location -> location.name }
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

    fun cancelTour() {
        val builder = AlertDialog.Builder(context)

        builder.setTitle("Cancel Tour")
        builder.setMessage("Are you sure you want to cancel your new tour?")

        builder.setPositiveButton("YES") { dialog, which ->
            activity!!.onBackPressed()
        }

        builder.setNegativeButton("NO") { dialog, which ->
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    fun addNewTour(isEdit:Boolean,tourId:String?) {
        val db = FirebaseFirestore.getInstance()
        var user = User()

        db.collection("users").document(FirebaseAuth.getInstance().currentUser?.email!!).get()
            .addOnSuccessListener(OnSuccessListener {
                user = it.toObject(User::class.java) as User
            })

        val name: String
        if(view!!.findViewById<EditText>(R.id.editTitle).text.toString().equals(null)){
            Toast.makeText(context,"Please enter something in text box", Toast.LENGTH_LONG).show()
            name = ""
            return
        }
        else{
            name = view!!.findViewById<EditText>(R.id.editTitle).text.toString()
        }

        val type: String = if (view!!.findViewById<Switch>(R.id.switchPT).isChecked)
            "personal"
        else
            "community"

        if(type == "community" && checkRepeatTours()&&!isEdit){
            Toast.makeText(context,"A tour with those locations already exists.", Toast.LENGTH_LONG).show()
            return
        }

        val user_id: String = FirebaseAuth.getInstance().currentUser!!.uid
        val hammer: Boolean = user.userType != 0

        if(locationsOTStr.isEmpty()){
            Toast.makeText(context,"Please add at least one location",Toast.LENGTH_LONG).show()
            return
        }

        if(tagsOnTour.isEmpty() && type == "community"){
            Toast.makeText(context,"Please add at least one tag", Toast.LENGTH_LONG).show()
            return
        }

        val newTour = Tour(name, type, user_id, hammer, locationsOTStr, tagsOnTour)
if(isEdit){
    db.collection("tours").document(tourId!!).set(newTour)
}else {
    db.collection("tours")
        .add(newTour)
        .addOnSuccessListener { documentReference ->
            Log.d(TAG, "DocumentSnapshot written with ID: ${documentReference.id}")
        }
        .addOnFailureListener { e ->
            Log.w(TAG, "Error adding document", e)

        }
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