package com.cs407.team15.redstone.ui.viewtours

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.team15.redstone.MainActivity
import com.cs407.team15.redstone.R
import com.cs407.team15.redstone.model.Tour
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.toptoche.searchablespinnerlibrary.SearchableSpinner
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.Boolean.FALSE
import java.lang.Boolean.TRUE

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ViewToursFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ViewToursFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ViewToursFragment : Fragment(), RecyclerAdapter.ItemClickListener, TextWatcher {

    // Contains all the tours which the user is allowed to see
    var allTours: MutableList<Tour> = mutableListOf()
    // Contains the names of all tags, plus the special "any" option
    var allTags: MutableList<String> = mutableListOf()
    // Contains the names of all locations, plus the special "any" option
    var allLocations: MutableList<String> = mutableListOf()
    val ANY = "any"

    var selectedFilterText = ""
    var selectedTag = ANY
    var selectedLoc = ANY
    var selectedHammer = FALSE

    //navigate to tour information
    override fun onItemClick(view: View, position: Int) {
        view.setOnClickListener{
            val intent = Intent(view.context, TourInfoActivity::class.java)
            intent.putExtra("tourName", allTours[position].name)

            var tourID = ""
            FirebaseFirestore.getInstance().collection("tours").get()
                .addOnSuccessListener { tour ->
                    for (t in tour.documents){
                        if (t["name"] as String == allTours[position].name) {
                            tourID = t.id
                            intent.putExtra("tourID", tourID)
                            //Toast.makeText(context, tourID, Toast.LENGTH_SHORT).show()
                            view.context.startActivity(intent)
                        }
                    }
                }
            //Toast.makeText(context, tourID, Toast.LENGTH_SHORT).show()
            //intent.putExtra("tourID", tourID)
            //view.context.startActivity(intent)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        GlobalScope.launch { getAndDisplayTourData() }
        GlobalScope.launch { getAndDisplayTagData() }
        GlobalScope.launch { getAndDisplayLocData() }
        return inflater.inflate(R.layout.fragment_view_tours, container, false)
    }

    override fun onStart() {
        super.onStart()
        val searchField = view!!.findViewById<EditText>(R.id.searchField)
        searchField.addTextChangedListener(this)

        val sw = view!!.findViewById<Switch>(R.id.hammerSwitch)
        sw?.setOnCheckedChangeListener { _, isChecked ->
            getHammerUsers(isChecked)
        }
    }

    // Only to be used first time that tourNames of tours to be displayed is set, in order to set up
    // the view. For subsequent updates, use setVisibleTourNames()
    fun setupRecyclerView(tourNames: List<String>) {
        val recyclerView = view!!.findViewById<RecyclerView>(R.id.tourList)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = RecyclerAdapter(context as Context, tourNames)
        adapter.setClickListener(this)
        recyclerView.adapter = adapter
        view!!.invalidate()
        reapplyFiltering()
    }

    fun setVisibleTourNames(tourNames: List<String>) {
        val recyclerView = view!!.findViewById<RecyclerView>(R.id.tourList)
        val adapter = RecyclerAdapter(context as Context, tourNames)
        adapter.setClickListener(this)
        recyclerView.adapter = adapter
        view!!.invalidate()
    }

    suspend fun getAndDisplayTourData() {
        // Filter out tours that the user is not allowed to see here, so that nowhere else on the
        // page will need to handle this filtering
        allTours.addAll(0, Tour.getAllTours().filter {tour -> Tour.canCurrentUserViewTour(tour)} )
        activity!!.runOnUiThread {
            reapplyFiltering()
            setupRecyclerView(allTours.map { tour -> tour.name })
        }
    }

    // filter all specifications: name, tags, hammer user, and personal tours
    fun reapplyFiltering() {
        val tourNamesFilteredByNameAndTag = allTours
            .filter { tour -> tour.name.contains(selectedFilterText, ignoreCase = true)}
            .filter { tour -> selectedTag == ANY || tour.tags.contains(selectedTag) }
            .filter { tour -> tour.hammer || tour.hammer == selectedHammer}
            .filter { tour -> (tour.type == "community" || ((tour.type == "personal") && tour.user_id == FirebaseAuth.getInstance().currentUser!!.uid))}
            .filter { tour -> selectedLoc == ANY || tour.locations.contains(selectedLoc) } // sort by locations
            .map {tour -> tour.name}
        setVisibleTourNames(tourNamesFilteredByNameAndTag)
    }

    // When the search field gets edited, re-
    override fun afterTextChanged(s: Editable?) {
        selectedFilterText = s.toString()
        reapplyFiltering()
    }
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    suspend fun getAndDisplayTagData() {
        // Get the name of all tags in alphabetical order
        val tags = FirebaseFirestore.getInstance().collection("tags").get().await()
            .documents.map{document -> document.getString("name") as String}.sorted().toMutableList()
        tags.add(0, ANY) // Prepend the special "any tag" option
        allTags.addAll(tags)
        activity!!.runOnUiThread {
            val tagSpinner = view!!.findViewById<Spinner>(R.id.tagSpinner)
            val spinnerAdapter = ArrayAdapter<String>(context as Context, android.R.layout.simple_spinner_item, tags)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            tagSpinner.adapter = spinnerAdapter
            tagSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedTag = allTags[position]
                    reapplyFiltering()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
            (tagSpinner as SearchableSpinner).setTitle("Search tours by tag")
            tagSpinner.invalidate()
        }
    }

    suspend fun getAndDisplayLocData() {
        // Get the name of all tags in alphabetical order
        val locations = FirebaseFirestore.getInstance().collection("locations").get().await()
            .documents.map{document -> document.getString("name") as String}.sorted().toMutableList()
        locations.add(0, ANY) // Prepend the special "any tag" option
        allLocations.addAll(locations)
        activity!!.runOnUiThread {
            val locSpinner = view!!.findViewById<Spinner>(R.id.locSpinner)
            val spinnerAdapter = ArrayAdapter<String>(context as Context, android.R.layout.simple_spinner_item, locations)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            locSpinner.adapter = spinnerAdapter
            locSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedLoc = allLocations[position]
                    reapplyFiltering()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
            (locSpinner as SearchableSpinner).setTitle("Search tours by location")
            locSpinner.invalidate()
        }
    }

    fun getHammerUsers(isChecked : Boolean) {
        if(isChecked){
            selectedHammer = TRUE
            reapplyFiltering()
        }
        else{
            selectedHammer = FALSE
            reapplyFiltering()
        }
    }
}
