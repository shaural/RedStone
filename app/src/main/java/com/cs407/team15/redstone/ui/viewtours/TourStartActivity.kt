package com.cs407.team15.redstone.ui.viewtours

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cs407.team15.redstone.MainActivity
import com.cs407.team15.redstone.R
import com.cs407.team15.redstone.ui.ar.ARFragment
import com.cs407.team15.redstone.ui.tour.TourFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlin.properties.Delegates


class TourStartActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    var fragmentContainerID: Int? = null
    var isAR = true
    lateinit var nextFrag : Fragment
    lateinit var arFrag : Fragment
    lateinit var mapFrag : Fragment
    lateinit var locQ : ArrayList<String>
    lateinit var latLangQ : ArrayList<LatLng>
    var indexLoc = 0
    private lateinit var fusedLocationCleint : FusedLocationProviderClient
    var bear : Float = 0f
    var rot : Float = 0f
    lateinit var ivArr : ImageView
    lateinit var tvDist : TextView
    lateinit var curLocation: Location
    private lateinit var tvNextLocName : TextView
    lateinit var tourName : String
    var arrow_angle : Float by Delegates.observable(0f) { _, oldValue, newValue ->
//        Log.d("lol-old", oldValue.toString())
//        Log.d("lol-new", newValue.toString())
        ivArr.rotation = 180 + newValue
    }
    var distMiles : Float by Delegates.observable(0f) {_, oldValue, newValue ->
//        Log.d("lol-old", oldValue.toString())
//        Log.d("lol-new", newValue.toString())
        tvDist.text = newValue.toString()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        ivArr.visibility =
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager // to get orientation of device
        locQ = ArrayList<String>()
        latLangQ = ArrayList<LatLng>()
        setContentView(R.layout.activity_tour_start)
        ivArr = findViewById<ImageView>(R.id.iv_arrow)
        tvDist = findViewById<TextView>(R.id.tv_dist_to_loc_container)
        val tourLocations = intent.getStringArrayListExtra("locations")
        val tourLatLang = intent.getParcelableArrayListExtra<LatLng>("latLang")
        tourName = intent.getStringExtra("tourName")
        val tvTourName = findViewById<TextView>(R.id.tv_tour_name)
        tvNextLocName = findViewById(R.id.tv_next_loc_container)
        tvTourName.text = tourName
//        for (loc in tourLocations) {
//            locQ.add(loc)
//        }
//        for (latl in tourLatLang) {
//            latLangQ.add(latl)
//        }
        locQ = tourLocations
        latLangQ = tourLatLang
        val tvNextLoc = findViewById<TextView>(R.id.tv_next_loc_container)
        tvNextLoc.text = locQ[indexLoc]
//        tourLocations.forEach { loc -> Log.d("lol", loc) }

        // get direction using bearings
//        fusedLocationCleint = LocationServices.getFusedLocationProviderClient(this)
//        fusedLocationCleint.lastLocation
//            .addOnSuccessListener { location : Location? ->
//                // Got last known location. In some rare situations this can be null.
//                val locVar = Location("")
//                locVar.latitude = latLangQ[indexLoc].latitude
//                locVar.longitude = latLangQ[indexLoc].longitude
//                var dir = location!!.bearingTo(locVar)
//                if (dir < 0) {
//                    dir += 360
//                }
//                bear = dir
//                distMiles = location.distanceTo(locVar)
//                curLocation = locVar
//                checkArrived()
////                Log.d("lol-dir", dir.toString())
//            }

        // Fragment management
        arFrag = ARFragment()
        mapFrag = TourFragment()
        nextFrag = arFrag
        fragmentContainerID = findViewById<FrameLayout>(R.id.fragmentContainerTour)!!.id
        this.supportFragmentManager.beginTransaction()
            .replace(fragmentContainerID!!, nextFrag, "ARFragment")
//            .addToBackStack(null) //maybe should add
            .commit()

        var btn_ar = findViewById<Button>(R.id.launch_ar_fragment_tour)
        var btn_map = findViewById<Button>(R.id.launch_map_fragment_tour)

        btn_ar.setOnClickListener{
            if (!isAR) {
                mapFrag.onPause()
                nextFrag = arFrag
                this.supportFragmentManager.beginTransaction()
                    .replace(fragmentContainerID!!, nextFrag, "ARFragment")
//                    .addToBackStack(null) //maybe should add
                    .commit()
                isAR = !isAR
            }
        }
        btn_map.setOnClickListener{
            if (isAR) {
                arFrag.onPause()
                nextFrag = mapFrag
                val bun : Bundle = Bundle()
                bun.putStringArrayList("locNames", tourLocations)
                bun.putParcelableArrayList("locLatLang", tourLatLang)
                nextFrag.arguments = bun
                this.supportFragmentManager.beginTransaction()
                    .replace(fragmentContainerID!!, nextFrag, "MapFragment")
//                    .addToBackStack(null) //maybe should add
                    .commit()
                isAR = !isAR
            }
        }
        var btnNextLoc = findViewById<Button>(R.id.btn_next_location)
        btnNextLoc.setOnClickListener{
            // get next from queue
            setNextDestination()
        }
        var btnPrevLoc = findViewById<Button>(R.id.btn_prev_location)
        btnPrevLoc.setOnClickListener{
            // get next from queue
            setPrevDestination()
        }
    }

    fun setNextDestination() {
        indexLoc++
        if (locQ.size == indexLoc) {
            // tour done
            // TODO decide what to do when completed
            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setMessage("Congratulations! You have completed the " + tourName + " tour.")
                // if the dialog is cancelable
                .setCancelable(false)
                // positive button text and action
                .setPositiveButton("Proceed", DialogInterface.OnClickListener {
                        dialog, id -> run{
                    this.finish()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }})
//                // negative button text and action
                .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                        dialog, id -> run{
                    indexLoc--
                    dialog.cancel()
                }
                })

            // create dialog box
            val alert = dialogBuilder.create()
            // set title for alert dialog box
            alert.setTitle("Tour Completed")
            // show alert dialog
            alert.show()
        }
        if (indexLoc < latLangQ.size) {
            val locVar = Location("")
            locVar.latitude = latLangQ[indexLoc].latitude
            locVar.longitude = latLangQ[indexLoc].longitude
            var dir = curLocation.bearingTo(locVar)
            if (dir < 0) {
                dir += 360
            }
            bear = dir
            distMiles = curLocation.distanceTo(locVar)
            tvNextLocName.text = locQ[indexLoc]
        }
    }

    fun setPrevDestination() {
        if (indexLoc != 0) {
            indexLoc--
            val locVar = Location("")
            locVar.latitude = latLangQ[indexLoc].latitude
            locVar.longitude = latLangQ[indexLoc].longitude
            var dir = curLocation.bearingTo(locVar)
            if (dir < 0) {
                dir += 360
            }
            bear = dir
            distMiles = curLocation.distanceTo(locVar)
            tvNextLocName.text = locQ[indexLoc]
        }
    }

    // Code for orientation sensors
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.
        Log.d("lol-change", "accuracy changed")
    }

    override fun onResume() {
        super.onResume()

        // Get updates from the accelerometer and magnetometer at a constant rate.
        // To make batch operations more efficient and reduce power consumption,
        // provide support for delaying updates to the application.
        //
        // In this example, the sensor reporting delay is small enough such that
        // the application receives an update before the system checks the sensor
        // readings again.
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticField ->
            sensorManager.registerListener(
                this,
                magneticField,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    override fun onPause() {
        super.onPause()

        // Don't receive any more updates from either sensor.
        sensorManager.unregisterListener(this)

        //TODO stop anchoring as app crashes when change to map
    }

    var iter = 0
    var rate = 20
    // Get readings from accelerometer and magnetometer. To simplify calculations,
    // consider storing these readings as unit vectors.
    override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(
                    event.values,
                    0,
                    accelerometerReading,
                    0,
                    accelerometerReading.size
                )
            } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
            }
            updateOrientationAngles()
            var rotation = Math.toDegrees(orientationAngles[0].toDouble())
            if (rotation < 0) {
                rotation += 360
            }
            rot = rotation.toFloat()
            var angleBetween = bear - rot
            if (angleBetween < 0) {
                angleBetween += 360
            }
            arrow_angle = angleBetween
        if (iter == rate) {

            // get direction using bearings
            fusedLocationCleint = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationCleint.lastLocation
                .addOnSuccessListener { location: Location? ->
                    // Got last known location. In some rare situations this can be null.
                    if (indexLoc < latLangQ.size) {
                        val locVar = Location("")
                        locVar.latitude = latLangQ[indexLoc].latitude
                        locVar.longitude = latLangQ[indexLoc].longitude
                        var dir = location!!.bearingTo(locVar)
                        if (dir < 0) {
                            dir += 360
                        }
                        bear = dir
                        distMiles = location.distanceTo(locVar)
                        curLocation = locVar
                        checkArrived()
                    }
//                Log.d("lol-dir", dir.toString())
                }
            iter = 0
        } else {
            iter++
        }

//        Log.d("lol-diff", angleBetween.toString())
    }

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    fun updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )

        // "mRotationMatrix" now has up-to-date information.

        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        // "mOrientationAngles" now has up-to-date information.
    }

    var alertShowing = false
    fun checkArrived() {
        if (!alertShowing) {
            if (distMiles < 20) {
                alertShowing = true
                // threshold of arrival met
                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder.setMessage("You have reached: " + locQ[indexLoc])
                    // if the dialog is cancelable
                    .setCancelable(false)
                    // positive button text and action
                    .setPositiveButton("Proceed", DialogInterface.OnClickListener { dialog, id ->
                        setNextDestination()
                        alertShowing = false
                    })
//                // negative button text and action
//                .setNegativeButton("Cancel", DialogInterface.OnClickListener {
//                        dialog, id -> dialog.cancel()
//                })

                // create dialog box
                val alert = dialogBuilder.create()
                // set title for alert dialog box
                alert.setTitle("Location Reached")
                // show alert dialog
                alert.show()
            }
        } else {

        }
    }
}