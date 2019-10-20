package com.cs407.team15.redstone.ui.tour

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.team15.redstone.R
import com.cs407.team15.redstone.model.Location
import com.toptoche.searchablespinnerlibrary.SearchableSpinner
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AddTourFragment : Fragment(){

    // Alphabetized list of all locations that exist
    val allLocations: MutableList<Location> = mutableListOf()
    // List of all locations that exist on the tour being added
    val locationsOnTour: MutableList<Location> = mutableListOf()

    var locationsAdapter: LocationsAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        GlobalScope.launch { getLocationsAndFillLocationSpinner() }
        val view = inflater.inflate(R.layout.fragment_addtour, container, false)
        val locationsRecyclerView = view.findViewById<RecyclerView>(R.id.locationsRecyclerView)

        val layoutManager = LinearLayoutManager(activity)
        locationsRecyclerView.layoutManager = layoutManager
        locationsRecyclerView.itemAnimator = DefaultItemAnimator()

        locationsAdapter = LocationsAdapter(activity as Activity, locationsOnTour)
        locationsRecyclerView.adapter = locationsAdapter
        return view
    }

    suspend fun getLocationsAndFillLocationSpinner() {
        // Get list of all locations in alphabetical order
        allLocations.addAll(Location.getAllTours())
        val locationNames = allLocations.map { location -> location.name }
        activity!!.runOnUiThread {
            val locationSpinner = view!!.findViewById<Spinner>(R.id.locationSpinner)
            val spinnerAdapter = ArrayAdapter<String>(context as Context, android.R.layout.simple_spinner_item, locationNames)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            locationSpinner.adapter = spinnerAdapter
            locationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
            (locationSpinner as SearchableSpinner).setTitle(getString(R.string.add_location_to_tour))
            locationSpinner.invalidate()
            // Temporarya and for testing only!!
            locationsOnTour.add(allLocations[0])
            locationsAdapter!!.notifyDataSetChanged()
        }
    }
}