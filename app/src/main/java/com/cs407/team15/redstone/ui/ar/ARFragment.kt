package com.cs407.team15.redstone.ui.ar

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.cs407.team15.redstone.R
import com.cs407.team15.redstone.model.Comment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Pose
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.basic_tour_text_view.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// A great deal of this code comes from following the example in the HelloSceneForm sample AR Core
// application provided by Google:
// https://github.com/google-ar/sceneform-android-sdk/blob/master/samples/hellosceneform/app/src/main/java/com/google/ar/sceneform/samples/hellosceneform/HelloSceneformActivity.java
class ARFragment : Fragment(), SensorEventListener {
    private val TAG: String = ARFragment::class.java.simpleName
    private val MIN_OPENGL_VERSION = 3.0
    private val MAXIMUM_COMMENTS_ON_SCREEN = 30
    // We only show comments for the nearest location in front of the camera. This is the maximum number
    // of degrees that a user needs to turn to directly face a location considered in front of the
    // camera. The larger the value, the looser our definition of "in front of" becomes.
    private val MAX_POINTING_DEGREES_OFFSET = 60F

    private var arFragment: ArFragment? = null // Google's ArFragment != this class
    private var currentPosition: Pose? = null // Camera's current position in space

    private lateinit var fusedLocationClient: FusedLocationProviderClient // to get location
    private lateinit var textViewTemplate: ViewRenderable
    private var db: FirebaseFirestore? = null
    private lateinit var location_name: String
    private lateinit var location_desc: String
    private var cur_id_str: String? = null // ID of the Location whose comments, description, and tags should be shown
    private var cur_lat: Double = 0.0
    private var cur_lon: Double = 0.0
    private var locations_db: MutableMap<GeoPoint, String> = mutableMapOf<GeoPoint, String>()
    private var map_gp_id: MutableMap<GeoPoint, String> = mutableMapOf<GeoPoint, String>()
    private lateinit var displayed_text_view: View
    private var dbCompleted = false
    private lateinit var tv_to_close : HitTestResult
    private lateinit var tv_to_close_motion : MotionEvent
    private val hammerUserIDs = mutableListOf<String>()
    private val userIDToUsername = mutableMapOf<String, String>()

    private var sensorManager: SensorManager? = null
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private var accelerometerRead = false
    private var magnetometerRead = false
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private var azimuth = 0F
    private var azimuthSet = false

    // When we start watching the set of comments for a particular location, we get a callback
    // that we need to call when we want to stop watching
    private var stopWatchingCommentsCallback: (()->Unit)? = null
    // Because creating the ar text views needed to show comments is an asynchronous operation,
    // we create a finite number of them when loading the page, then we manage that group
    private val availableArCommentViewPool: MutableList<ViewRenderable> = mutableListOf()
    private val inUseArCommentViewPool: MutableList<ViewRenderable> = mutableListOf()
    // We need a reference to all the AR Nodes associated with comments so that we can remove them
    // when we want to stop showing comments for a location. Each of these nodes will have an
    // AnchorNode as a parent
    private val nodesForComments: MutableList<Node> = mutableListOf()

    private var currentLocation: android.location.Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlobalScope.launch { fetchListOfHammerUsers() }
        GlobalScope.launch { fetchMapOfUserIDsToUsernames() }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity!!)
        getLocationFromDB()
        sensorManager = activity!!.getSystemService(Activity.SENSOR_SERVICE) as SensorManager
        if (!isThisDeviceSupported()) {
            return
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ar, container, false)
        arFragment = childFragmentManager.findFragmentById(R.id.free_roam_ar_fragment) as ArFragment
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        displayed_text_view = layoutInflater.inflate(R.layout.basic_tour_text_view, null)

        var frameNumber = 0
        val sceneOnUpdate = { frameTime: FrameTime ->
            run {
                arFragment!!.onUpdate(frameTime)
                updateCameraPosition()
                frameNumber += 1
                // This is reached about once every 8 seconds on my machine. The frequency doesn't
                // really matter as long as it is between 1 - 10 seconds.
                if (frameNumber % 10 == 0) {
                    updateLocation { }
                }
            }
        }
        arFragment!!.arSceneView.scene.addOnUpdateListener(sceneOnUpdate)

        // Create the pool of ar text views to hold comments
        repeat(MAXIMUM_COMMENTS_ON_SCREEN) {
            val commentView = layoutInflater.inflate(R.layout.comment_item, null)
            ViewRenderable.builder().setView(context!!, commentView).build()
                .thenAccept { renderable ->
                    renderable.view.background = ResourcesCompat.getDrawable(resources, R.drawable.back, null)
                    renderable.view.findViewById<AppCompatImageView>(R.id.btn_like).visibility = View.INVISIBLE
                    renderable.view.findViewById<AppCompatImageView>(R.id.image_profile).visibility = View.INVISIBLE
                    availableArCommentViewPool.add(renderable) }
        }

        arFragment!!.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, motionEvent: MotionEvent ->
            updateLocation {
                if (textViewTemplate != null) {
                    val anchor = hitResult.createAnchor()
                    val anchorNode = AnchorNode(anchor)
                    anchorNode.setParent(arFragment!!.arSceneView.scene)
                    val textViewNode = Node()
                    textViewNode.setParent(anchorNode)
                    var plane_type = plane.type

                    if(plane_type == Plane.Type.VERTICAL) {
                        Log.d("lol", "vertical")
                    }else if(plane_type == Plane.Type.HORIZONTAL_UPWARD_FACING) {
                        // Ceiling, camera looking up at
                        // This is when our anchoring is correct!
                        Log.d("lol", "horizontal up")
                    } else if(plane_type == Plane.Type.HORIZONTAL_DOWNWARD_FACING) {
                        // Floor, ground, camera looking down at
                        Log.d("lol", "horizontal down")
                    }
                    textViewNode.localPosition = Vector3(0f, 1.0f, 0f)
                    textViewNode.renderable = textViewTemplate
                    textViewNode.setOnTapListener{ hitTestResult: HitTestResult, motionEvent: MotionEvent ->
                        tv_to_close = hitTestResult
                        tv_to_close_motion = motionEvent
                        // First call ArFragment's listener to handle TransformableNodes.
                        arFragment!!.onPeekTouch(tv_to_close, tv_to_close_motion)

                        //We are only interested in the ACTION_UP events - anything else just return
                        if (tv_to_close_motion.action == MotionEvent.ACTION_UP) {

                            // Check for touching a Sceneform node
                            if (tv_to_close.node != null) {
                                var hitNode =  tv_to_close.node
                                if (hitNode != null) {
                                    arFragment!!.arSceneView.scene.removeChild(hitNode);
                                    hitNode.setParent(null)
                                }
                            }
                        }
                    }
                }
            }
        }
        view!!.findViewById<Switch>(R.id.byLikesSwitch).setOnCheckedChangeListener {button: CompoundButton, state: Boolean -> sortingCriteriaChanged()}
        view!!.findViewById<Switch>(R.id.hammerOnlySwitch).setOnCheckedChangeListener {button: CompoundButton, state: Boolean -> sortingCriteriaChanged()}
        updateLocation {  }
    }

    override fun onResume() {
        super.onResume()
        // From https://developer.android.com/guide/topics/sensors/sensors_position
        // We need these to have access to the user's current azimuth
        val accelerometerSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager!!.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI)
        val magneticSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        sensorManager!!.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
        accelerometerRead = false
        magnetometerRead = false
        azimuthSet = false
        stopWatchingCommentsCallback?.invoke()
        arFragment!!.onPause()
    }

    override fun onStop() {
        super.onStop()
        stopWatchingCommentsCallback?.invoke()
        arFragment!!.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopWatchingCommentsCallback?.invoke()
        arFragment!!.onDestroy()
    }

     fun updateCameraPosition() {
        val camera = arFragment?.arSceneView?.arFrame?.camera
        currentPosition =
            if (camera?.trackingState == TrackingState.TRACKING) camera?.displayOrientedPose else null
    }

    // Update the current location asynchronously and then execute the specified callback
    fun updateLocation(then: ()->Unit) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    cur_lat = location.latitude
                    cur_lon = location.longitude
                    var bearing = location.bearing
                    currentLocation = location
                    getNearestLocation()
                }
                then()
            }
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
                            var loc_id= document.get("location_id")!!.toString()
                            var desc = document.get("description")!!.toString()
                            var gp = document.get("coordinates") as GeoPoint
//                            Toast.makeText(context, gp.latitude.toString(), Toast.LENGTH_SHORT)
                            locations_db[gp] = "$name-$desc"
                            map_gp_id[gp] = loc_id
                            // If this location is new enough to have vertices recorded for it,
                            // then we should use those GPS points to compare against our current
                            // location to determine which location is closest. Ideally we would
                            // determine nearest location in a way that also accounted for the
                            // edges of a location's polygon, let alone something that accounted for
                            // overlapping locations, but this is sufficient.
                            if (document.contains("vertices")) {
                                var vertices = document.get("vertices") as List<GeoPoint>
                                vertices.forEach {
                                    locations_db[it] = "$name-$desc"
                                    map_gp_id[it] = loc_id
                                }
                            }
                        } else {
//                            Log.d("lol", "no document exists")
                        }
                    }
                    dbCompleted = true
                    getNearestLocation()
                } else {
                    Log.d(TAG, "Error getting documents: ", task.exception)
                }
            })
        return
    }
    private fun getNearestLocation() {
        if (dbCompleted && currentLocation != null && azimuthSet) {
            val nearestLocation =
                com.cs407.team15.redstone.model.Location.getNearestLocation(

                    com.cs407.team15.redstone.model.Location.getLocationsInFrontOfCamera(
                locations_db.keys.map { run {
                    val location = Location("");
                    location.latitude = it.latitude;
                    location.longitude = it.longitude;
                    location }}, currentLocation!!, azimuth, MAX_POINTING_DEGREES_OFFSET), currentLocation!!
                )
            if (nearestLocation == null) {
                return
            }
            val nearestLocationGeoPoint = locations_db.keys.filter {
                    gp -> gp.latitude == nearestLocation.latitude && gp.longitude == nearestLocation.longitude
            }.single()
            var minVal = nearestLocationGeoPoint//dists.minBy { it.value }
            if (minVal != null && !locations_db[minVal].isNullOrEmpty()) {
                var db_val = locations_db[minVal].orEmpty()
                val new_id_str = map_gp_id[minVal].orEmpty()
                val locationChanged = cur_id_str != new_id_str
                cur_id_str = new_id_str
                if (locationChanged) {
                    locationChanged()
                }
                getTags(cur_id_str!!)
                location_name = db_val.substring(0, db_val.indexOf('-'))
                location_desc = db_val.substring(db_val.indexOf('-') + 1, db_val.length)
                displayed_text_view.tv_ar_text.text = location_name
                displayed_text_view.tv_ar_desc.text = location_desc
                ViewRenderable.builder().setView(context!!, displayed_text_view).build()
                    .thenAccept { renderable -> textViewTemplate = renderable }
            }
        }
    }
    private fun getTags(idstr: String) {
        var ar_tags = ArrayList<String>()
        db!!.collection("locations").document(idstr).collection("tags").get().addOnCompleteListener { col ->
            if (col != null) {
                col.result!!.forEach { t ->
                    ar_tags.add(t["name"].toString())
                    Log.d("lol", "DocumentSnapshot data: ${t["name"]}")
                }
                displayTags(ar_tags)
            } else {
                Log.d("lol", "No such document")
            }
        }
    }
    private fun displayTags(tagsList: ArrayList<String>) {
        var str = ""
        var max_len = 0
        tagsList.forEach { item ->
            str += "- ${item}\n"
            if (item.length > max_len) {
                max_len = item.length
            }
        }
        displayed_text_view.tv_ar_tags.text = str
        displayed_text_view.tv_ar_tags.width = 30 * max_len
        displayed_text_view.tv_ar_tags.height = tagsList.size * 75
        displayed_text_view.tv_ar_tags.visibility = View.VISIBLE
    }

    private fun stopShowingAllComments() {
        // We need to remove all the text views showing comments for the old location.
        nodesForComments.forEach { node -> run {
            arFragment!!.arSceneView.scene.removeChild(node)
            (node.parent as AnchorNode).anchor!!.detach()
            arFragment!!.arSceneView.scene.removeChild(node.parent)
        }}
        // Add all the AR text views that were in use back into the available pool
        availableArCommentViewPool.addAll(inUseArCommentViewPool)
        inUseArCommentViewPool.clear()
    }

    private fun applySortingToComments(comments: List<Comment>, byLikes: Boolean, hammerOnly: Boolean): List<Comment> {
        // If we only want hammer users remove non hammer users
        return comments.filter { comment -> !hammerOnly || hammerUserIDs.contains(comment.publisherid) }.
            sortedWith(object: Comparator<Comment> {
            override fun compare(comment1: Comment, comment2: Comment): Int {
                // Compare comments by likes descending
                if (byLikes) {
                    return -1 * comment1.like.compareTo(comment2.like)
                }
                // or by posted date descending
                else {
                    return -1 * comment1.timestamp.compareTo(comment2.timestamp)
                }
            }
        })
    }

    private fun locationChanged() {
        //you should show comment age!
        refreshCommentsDisplay()
    }

    private fun sortingCriteriaChanged() {
        refreshCommentsDisplay()
    }

    private fun refreshCommentsDisplay() {
        val sortByLikes = view!!.findViewById<Switch>(R.id.byLikesSwitch).isChecked
        val hammerOnly = view!!.findViewById<Switch>(R.id.hammerOnlySwitch).isChecked
        hideAnyExistingCommentsAndShowSortedCommentsForLocation(cur_id_str!!, sortByLikes, hammerOnly)
    }

    private fun hideAnyExistingCommentsAndShowSortedCommentsForLocation(newLocationID: String, byLikes: Boolean, hammerOnly: Boolean) {
        stopShowingAllComments()
        // Stop watching comments for the old location (if there was one)
        stopWatchingCommentsCallback?.invoke()
        // Start watching comments for the new location. When the set of comments for the location
        // is changed, all visible comments are removed, and the updated list is sorted and displayed
        stopWatchingCommentsCallback = com.cs407.team15.redstone.model.Location.watchCommentsForLocation(newLocationID)
            { comments ->
                stopShowingAllComments()
                val camera = arFragment!!.arSceneView.scene.camera
                val oneMeterForward = Vector3(camera.forward).apply { y = 0F; normalized() }
                val oneMeterLeft = Vector3(camera.left).apply { y = 0F; normalized() }
                val oneMeterUp = Vector3(camera.up).apply { x = 0F; z = 0F; normalized() }
                val nodesToBePositioned = mutableListOf<Node>()
                val anchor = arFragment!!.arSceneView.session!!.createAnchor(currentPosition)
                val anchorNode = AnchorNode(anchor)
                val currentPositionVector = Vector3(
                    currentPosition!!.tx(),
                    currentPosition!!.ty(),
                    currentPosition!!.tz()
                )
                applySortingToComments(comments, byLikes, hammerOnly).forEach { comment ->
                    val arTextView = availableArCommentViewPool.firstOrNull()
                    if (arTextView != null && arFragment!!.arSceneView.arFrame?.camera?.trackingState == TrackingState.TRACKING) {
                        availableArCommentViewPool.remove(arTextView)
                        inUseArCommentViewPool.add(arTextView)
                        anchorNode.setParent(arFragment!!.arSceneView.scene)
                        val textViewNode = Node()
                        textViewNode.setParent(anchorNode)
                        arTextView.view.findViewById<TextView>(R.id.comment).text = comment.comment
                        arTextView.view.findViewById<TextView>(R.id.username).text = userIDToUsername[comment.publisher]
                        arTextView.view.findViewById<TextView>(R.id.tv_total).text = comment.like.toString()
                        textViewNode.renderable = arTextView
                        nodesForComments.add(textViewNode)
                        nodesToBePositioned.add(textViewNode)
                    }
                }
                arrangeComments(nodesToBePositioned, currentPositionVector, oneMeterForward, oneMeterLeft, oneMeterUp)
        }
    }

    private fun arrangeComments(commentNodes: List<Node>, cameraPosition: Vector3,
                                oneMeterForward: Vector3, oneMeterLeft: Vector3, oneMeterUp: Vector3) {
        // Translate the provided node by the provided amount of meters forward, left, and up
        val translateInMeters = {node: Node, forwardLeftUp: Array<Double> -> node.worldPosition =
            Vector3.add(Vector3.add(Vector3.add(node.worldPosition, oneMeterForward.scaled(forwardLeftUp[0].toFloat())),
                oneMeterLeft.scaled(forwardLeftUp[1].toFloat())), oneMeterUp.scaled(forwardLeftUp[2].toFloat()))}

        for ((position, node) in commentNodes.withIndex()) {
            // Two meters in front of the camera but at the same height as the camera
            node.worldPosition = Vector3.add(cameraPosition, oneMeterForward.scaled(2.0F))
            // If there's enough comments for a full row, shift every comment left one meter so the
            // arrangement of comments is horizontally centered in front of the user
            if (commentNodes.size >= 3) {
                node.worldPosition = Vector3.add(node.worldPosition, oneMeterLeft)
            }
            node.setLookDirection(oneMeterForward.negated())
            // Arrange comments in 3 wide by 2 high grids, each grid 2 meters behind the previous one,
            // within a grid one meter from top to bottom row, 1.5 meters from one column to the next
            // #1 #2 #3
            // #4 #5 #6
            translateInMeters(node, arrayOf(2.0 * (position / 6).toDouble(), -1.5 * (position % 3).toDouble(), -((position % 6) / 3).toDouble()))
        }
    }

    private suspend fun fetchListOfHammerUsers() {
        hammerUserIDs.addAll(
            // From the set of users
            FirebaseFirestore.getInstance().collection("users").
            // get hammer users'
            whereEqualTo("isHammerUser", true).get().await().documents.
            // uids
            map {user -> user.getString("uid") as String})
    }

    private suspend fun fetchMapOfUserIDsToUsernames() {
        FirebaseFirestore.getInstance().collection("users").get().await().documents.
            // Map user id to username if there is one and email address otherwise
            forEach{ user -> userIDToUsername.put(user.id, user.getString("username") ?: user.getString("email")!!) }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        // This code is from https://developer.android.com/guide/topics/sensors/sensors_position
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
            accelerometerRead = true
        }
        else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
            magnetometerRead = true
        }
        // Once we have a reading from each sensor, we can update the calculate the current azimuth
        // and update it anytime we get up-to-date sensor readings
        if (accelerometerRead && magnetometerRead) {
            SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            // Azimuth is given as a value in radians from -pi to +pi. Convert to a degree value
            // from 0 to 360
            azimuth = ((Math.toDegrees(orientationAngles[0].toDouble()).toInt() + 360) % 360).toFloat()
            azimuthSet = true
            view!!.findViewById<Switch>(R.id.byLikesSwitch).text = "${azimuth}"
        }
    }


}