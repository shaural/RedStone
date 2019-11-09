package com.cs407.team15.redstone.ui.tour

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.cs407.team15.redstone.R
import com.cs407.team15.redstone.ui.location.LocationPage
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await





class TourFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener{
    val MAXIMUM_VERTICES_IN_LOCATION = 10

    var isAddLocationClicked = false
    private lateinit var mMap: GoogleMap
    private lateinit var tourViewModel: TourViewModel
    private lateinit var add_location_btn: Button
    private lateinit var finalize_location_btn: Button
    private lateinit var vertices_remaining_label: TextView
    private var locationVertices: MutableList<GeoPoint> = mutableListOf()
    private val allLocations: MutableList<DocumentSnapshot> = mutableListOf()
    private var lastLineSegment: Polyline? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_tour, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.tour_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return root
    }
    fun addLocation(location: LatLng, title:String, markerIcon: BitmapDescriptor){
       mMap.addMarker(MarkerOptions().position(location).title(title).icon(markerIcon))
    }
    suspend fun addAllKnownLocations() {
        // Only fetch all locations if we just now arrived at this page from elsewhere
        if (allLocations.size == 0) {
            allLocations.addAll(FirebaseFirestore.getInstance().collection("locations").get().await())
        }
        val markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.marker)
        // Now that the location data has been fetched, we can quickly add all the markers
        activity?.runOnUiThread {
            for (location in allLocations) {
                val title = location.get("name") as String
                val gpsPoint = location.get("coordinates") as GeoPoint
                val latLng = LatLng(gpsPoint.latitude, gpsPoint.longitude)
                addLocation(latLng, title, markerIcon)
            }
        }
    }
    override fun onMarkerClick(marker:Marker?):Boolean{
        // Only show location details if we are not currently selecting points on the map as part
        // of a new location
        if (!isAddLocationClicked) {
            val frag = fragmentManager!!.beginTransaction()
            val bundle = Bundle()
            bundle.putString("title",marker?.title)
            val loc=LocationPage()
            loc.arguments=bundle
            frag.replace((view!!.parent as ViewGroup).id, loc)
            frag.addToBackStack(null)
            frag.commit()
        }
        return true // Keep map from centering on tapped location
    }
    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled=true
        mMap.setMinZoomPreference(14f)

        FirebaseFirestore.getInstance().collection("schools").document("Purdue").get().addOnSuccessListener(
            OnSuccessListener {
                val schoolLoc=it["coordinates"] as GeoPoint
                mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(schoolLoc.latitude,schoolLoc.longitude)))
            }
        )
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(activity, R.raw.mapstyle))
        GlobalScope.launch { addAllKnownLocations() }
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMapClickListener(
            object : GoogleMap.OnMapClickListener {
                override fun onMapClick(location: LatLng?) {
                    // If we are adding a location and we can add another vertex to the polygonal
                    // representation of the building, add the clicked point as a vertex
                    if (isAddLocationClicked && locationVertices.size < MAXIMUM_VERTICES_IN_LOCATION) {
                        // Draw line between this new vertex and the previous vertex
                        if (locationVertices.size > 0) {
                            mMap.addPolyline(PolylineOptions()
                                .add(toLatLng(locationVertices.last()), location!!)
                                .width(3F).color(Color.BLACK))
                        }
                        // Draw a line between the point just added and the first point, so that the
                        // user sees a complete polygon describing the shape of the location.
                        // Every time we do this we must remove the last one of these "last mile"
                        // segments we drew
                        if (locationVertices.size > 1) {
                            if (lastLineSegment != null) {
                                lastLineSegment!!.remove()
                            }
                            lastLineSegment = mMap.addPolyline(PolylineOptions()
                                .add(location!!, toLatLng(locationVertices.first()))
                                .width(3F).color(Color.BLACK)
                            )
                        }
                        locationVertices.add(GeoPoint(location!!.latitude, location.longitude))
                        updateVerticesRemainingLabel()
                        finalize_location_btn.visibility = View.VISIBLE
                        val markerIcon = BitmapDescriptorFactory.fromResource(R.drawable.marker)
                        addLocation(location!!, "Point #${locationVertices.size}", markerIcon)

                    }
                }
            }
        )
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
        } else {
//            Toast.makeText(context,"Location not showing",Toast.LENGTH_LONG).show()
            // Show rationale and request permission.
            requestPermissions( arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), targetRequestCode)
            googleMap.isMyLocationEnabled = true
        }

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        add_location_btn = getView()!!.findViewById(R.id.add_location_button) as Button

        add_location_btn.setOnClickListener{
            if (!isAddLocationClicked) {
                Toast.makeText(context, "Click on the map where you would like to add your location.", Toast.LENGTH_LONG).show()
                isAddLocationClicked = true
                add_location_btn.setBackgroundColor(resources.getColor(R.color.GREEN))
                vertices_remaining_label.visibility = View.VISIBLE
                updateVerticesRemainingLabel()
                mMap.clear()
            }
            else {
                resetAfterPossiblyAddingLocation()
            }
        }

        finalize_location_btn = getView()!!.findViewById(R.id.finalize_location_button)
        finalize_location_btn.setOnClickListener {

            val coordinateString = locationVertices
                .map {geoPoint -> "\r\n(${String.format("%.3f", geoPoint.latitude)}, ${String.format("%.3f", geoPoint.longitude)})" }
                .reduce { acc, s -> acc + s }
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Add Location")
            builder.setMessage("Are you sure you want to add the location with the coordinates: ${coordinateString}?")
            builder.setPositiveButton("YES"){dialog, which ->
                Toast.makeText(context, "Add Location", Toast.LENGTH_SHORT).show()
                val newFragment = AddLocationFragment()
                val bundle = Bundle()
                val gp = locationVertices.first()
                bundle.putDouble("latitude", gp.latitude)
                bundle.putDouble("longitude", gp.longitude)

                // Store all the coordinates selected by the user to pass to the add location page.
                // Pass in a projection of those coordinates to the plane to show the user the
                // shape of the new location on that page.
                val latitudes = locationVertices.map { geoPoint -> geoPoint.latitude.toFloat() }
                val longitudes = locationVertices.map { geoPoint ->  geoPoint.longitude.toFloat() }
                val xPoints = locationVertices.map { geoPoint -> mMap.projection.toScreenLocation(toLatLng(geoPoint)).x }
                val yPoints = locationVertices.map { geoPoint -> mMap.projection.toScreenLocation(toLatLng(geoPoint)).y }
                bundle.putFloatArray("latitudes", latitudes.toFloatArray())
                bundle.putFloatArray("longitudes", longitudes.toFloatArray())
                bundle.putIntArray("xpoints", xPoints.toIntArray())
                bundle.putIntArray("ypoints", yPoints.toIntArray())

                resetAfterPossiblyAddingLocation()
                newFragment.arguments = bundle
                val transaction = fragmentManager!!.beginTransaction()
                transaction.replace(id, newFragment)
                transaction.addToBackStack(null)
                transaction.commit()
            }

            builder.setNegativeButton("No"){dialog,which ->
                Toast.makeText(context,"Click the Add Location Button to try again.", Toast.LENGTH_SHORT).show()
                resetAfterPossiblyAddingLocation()
            }


            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        vertices_remaining_label = getView()!!.findViewById(R.id.vertices_remaining_label)

    }

    private fun updateVerticesRemainingLabel() {
        var verticesRemaining = MAXIMUM_VERTICES_IN_LOCATION - locationVertices.size
        val s = if (verticesRemaining == 1) "" else "s"
        vertices_remaining_label.text = "Up to ${verticesRemaining} more point${s} may be added"
    }

    private fun toLatLng(geoPoint: GeoPoint): LatLng {
        return LatLng(geoPoint.latitude, geoPoint.longitude)
    }

    private fun resetAfterPossiblyAddingLocation() {
        finalize_location_btn.visibility = View.INVISIBLE
        vertices_remaining_label.visibility = View.INVISIBLE
        locationVertices = mutableListOf()

        isAddLocationClicked = false
        add_location_btn.setBackgroundColor(resources.getColor(R.color.btn_def))

        mMap.clear() // Remove vertices of new location
        GlobalScope.launch { addAllKnownLocations() } // Re-add all old locations
    }

}

