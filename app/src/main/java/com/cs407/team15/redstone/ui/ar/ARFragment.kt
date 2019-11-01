package com.cs407.team15.redstone.ui.ar

import android.app.ActivityManager
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.cs407.team15.redstone.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Pose
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.basic_tour_text_view.view.*

// A great deal of this code comes from following the example in the HelloSceneForm sample AR Core
// application provided by Google:
// https://github.com/google-ar/sceneform-android-sdk/blob/master/samples/hellosceneform/app/src/main/java/com/google/ar/sceneform/samples/hellosceneform/HelloSceneformActivity.java
class ARFragment : Fragment() {
    private val TAG: String = ARFragment::class.java.simpleName
    private val MIN_OPENGL_VERSION = 3.0

    private lateinit var arFragment: ArFragment // Google's ArFragment != this class
    private var currentPosition: Pose? = null // Camera's current position in space


    private lateinit var fusedLocationClient: FusedLocationProviderClient // to get location
    private lateinit var textViewTemplate: ViewRenderable
    private var db: FirebaseFirestore? = null
    private lateinit var location_name: String
    private lateinit var location_desc: String
    private var cur_lat: Double = 0.0
    private var cur_lon: Double = 0.0
    private var locations_db: MutableMap<GeoPoint, String> = mutableMapOf<GeoPoint, String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
        getLocationFromDB()
        if (!isThisDeviceSupported()) {
            return
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        Log.d("lol", "Reached")

        return inflater.inflate(R.layout.fragment_ar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arFragment = childFragmentManager.findFragmentById(R.id.free_roam_ar_fragment) as ArFragment
        arFragment.arSceneView.scene.addOnUpdateListener { frameTime ->
            run {
                arFragment.onUpdate(frameTime)
                updateCameraPosition()
            }
        }

        arFragment.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, motionEvent: MotionEvent ->

            run {
                if (textViewTemplate != null) {
                    val anchor = hitResult.createAnchor()
                    val anchorNode = AnchorNode(anchor)
                    anchorNode.setParent(arFragment.arSceneView.scene)
                    val textViewNode = Node()
                    textViewNode.setParent(anchorNode)
                    textViewNode.localPosition = Vector3(0f, 1.0f, 0f)
                    textViewNode.renderable = textViewTemplate

                }
            }
        }
    }

    fun updateCameraPosition() {
        val camera = arFragment.arSceneView.arFrame?.camera
        currentPosition =
            if (camera?.trackingState == TrackingState.TRACKING) camera?.displayOrientedPose else null
    }

    fun isThisDeviceSupported(): Boolean {
        val openGlVersion =
            (activity!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
                .deviceConfigurationInfo.glEsVersion.toDouble()
        if (openGlVersion < MIN_OPENGL_VERSION) {
            Toast.makeText(activity!!, getString(R.string.opengl_not_supported), Toast.LENGTH_LONG)
                .show()
            return false
        }
        return true
    }

    private fun getLocationFromDB() {
        db = FirebaseFirestore.getInstance()
        db!!.collection("locations")
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        if (document.exists()) {
                            Log.d(TAG, document.id + " => " + document.data)
                            var name = document.get("name")!!.toString()
                            var desc = document.get("description")!!.toString()
                            var gp = document.get("coordinates") as GeoPoint
//                            Toast.makeText(context, gp.latitude.toString(), Toast.LENGTH_SHORT)
                            locations_db[gp] = "$name-$desc"
                        } else {
                            Log.d("lol", "no document exists")
                        }
                    }
                    getNearestLocation()
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                }
            })
        return
    }
    private fun getNearestLocation() {
        var locationSource = Location("source")
        locationSource.setLatitude(cur_lat);
        locationSource.setLongitude(cur_lon);
        val points = locations_db.keys
        val dists : MutableMap<GeoPoint, Float> = mutableMapOf<GeoPoint, Float>()
        points.forEach{ p ->
            run {
                var locationCompare = Location("compareLocation")
                locationCompare.setLatitude(p.latitude)
                locationCompare.setLongitude(p.longitude)

                var distance = locationSource.distanceTo(locationCompare)
//                Log.d("lol-dist", distance.toString())
                dists.put(p, distance)
            }
        }
        var minVal = dists.minBy { it.value }
        if (minVal != null && !locations_db[minVal.key].isNullOrEmpty()) {
            var db_val = locations_db[minVal.key].orEmpty()
            location_name = db_val.substring(0, db_val.indexOf('-'))
            location_desc = db_val.substring(db_val.indexOf('-')+1, db_val.length)
            var v1 = getLayoutInflater().inflate(R.layout.basic_tour_text_view, null)
            v1.tv_ar_text.text = location_name
            v1.tv_ar_desc.text = location_desc
            ViewRenderable.builder().setView(context!!, v1).build()
                .thenAccept { renderable -> textViewTemplate = renderable }
        }
    }
}