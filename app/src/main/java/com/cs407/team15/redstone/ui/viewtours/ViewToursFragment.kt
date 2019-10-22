package com.cs407.team15.redstone.ui.viewtours

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.team15.redstone.R
import com.cs407.team15.redstone.model.Tour
import com.google.firebase.firestore.FirebaseFirestore
import com.toptoche.searchablespinnerlibrary.SearchableSpinner
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
    val ANY = "any"

    var selectedFilterText = ""
    var selectedTag = ANY

    override fun onItemClick(view: View, position: Int) {
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        GlobalScope.launch { getAndDisplayTourData() }
        GlobalScope.launch { getAndDisplayTagData() }
        return inflater.inflate(R.layout.fragment_view_tours, container, false)
    }

    override fun onStart() {
        super.onStart()
        val searchField = view!!.findViewById<EditText>(R.id.searchField)
        searchField.addTextChangedListener(this)
    }

    // Only to be used first time that tourNames of tours to be displayed is set, in order to set up
    // the view. For subsequent updates, use setVisibleTourNames()
    fun setupRecyclerView(tourNames: List<String>) {
        val recyclerView = view!!.findViewById<RecyclerView>(R.id.tourList)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = RecyclerAdapter(context as Context, tourNames)
        adapter.setClickListener(this)
        recyclerView.adapter = adapter
        getView()!!.invalidate()
    }

    fun setVisibleTourNames(tourNames: List<String>) {
        val recyclerView = view!!.findViewById<RecyclerView>(R.id.tourList)
        val adapter = RecyclerAdapter(context as Context, tourNames)
        adapter.setClickListener(this)
        recyclerView.adapter = adapter
        getView()!!.invalidate()
    }

    suspend fun getAndDisplayTourData() {
        // Filter out tours that the user is not allowed to see here, so that nowhere else on the
        // page will need to handle this filtering
        allTours.addAll(0, Tour.getAllTours().filter {tour -> Tour.canCurrentUserViewTour(tour)} )
        activity!!.runOnUiThread { setupRecyclerView(allTours.map { tour -> tour.name }) }
    }

    fun reapplyFiltering() {
        val tourNamesFilteredByNameAndTag = allTours
            .filter { tour -> tour.name.contains(selectedFilterText, ignoreCase = true)}
            .filter { tour -> selectedTag == ANY || tour.tags.contains(selectedTag) }
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

}
