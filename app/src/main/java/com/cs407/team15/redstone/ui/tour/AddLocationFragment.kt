package com.cs407.team15.redstone.ui.tour

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast

import com.cs407.team15.redstone.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint
import com.google.type.LatLng
import kotlinx.android.synthetic.main.add_location_fragment.*
import java.sql.Time
import java.sql.Timestamp
import java.lang.StringBuilder
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


class AddLocationFragment : Fragment() {
    companion object {
        fun newInstance() = AddLocationFragment()
    }

    private lateinit var viewModel: AddLocationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.add_location_fragment, container, false)
    }
    lateinit var database: DatabaseReference

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(AddLocationViewModel::class.java)

        //Retrieving the button from XML
        val btn_add_loc = getView()!!.findViewById<Button>(R.id.btn_add_location)
        val btn_newTag = getView()!!.findViewById<Button>(R.id.btn_create_tag)
        val locationShape = view!!.findViewById<ImageView>(R.id.locationShape)
        database = FirebaseDatabase.getInstance().reference

        //Initializing the key for new location to be added
        val newKey = database.child("locations").push().key.toString()
        val addTagtoLoc = FirebaseFirestore.getInstance().collection("locations").document(newKey)

        //New tag is to be added
        btn_newTag.setOnClickListener {
            val builder =  AlertDialog.Builder(context)
            val inflater = layoutInflater
            builder.setTitle("Create Tag")
            val dialogLayout = inflater.inflate(R.layout.alert_dialog_edittext, null)
            builder.setView(dialogLayout)
            builder.setPositiveButton(android.R.string.ok) {dialog, p1 ->
                val newTag = dialogLayout.findViewById<EditText>(R.id.editText).text.toString()
                var isValid = true
                if (newTag.isBlank()){
                    isValid = false
                }
                if (isValid){

                    val newTagInp = hashMapOf(
                        "name" to newTag as String
                    )
                    FirebaseFirestore.getInstance().collection("tags").document().set(newTagInp)
                    dialog.dismiss()
                }
            }
            builder.setNegativeButton(android.R.string.cancel) { dialog, p2 ->
                dialog.cancel()
            }
            builder.show()
        }

        //Adding existing tags to new location
        val addTagsButton = view!!.findViewById(R.id.btn_add_tag) as Button
        addTagsButton.setOnClickListener {
            var storeTags = ArrayList<String>()
            FirebaseFirestore.getInstance().collection("tags").get()
                .addOnSuccessListener { tagNames ->
                    for (n in tagNames.documents) {
                        var stringStorage = n["name"] as String
                        storeTags.add(stringStorage)
                    }

                    val tagsArr = arrayOfNulls<String>(storeTags.size)
                    storeTags.toArray(tagsArr)
                    val checkTags = BooleanArray(storeTags.size) { i -> false }

                    val builder = AlertDialog.Builder(context)
                    builder.setMultiChoiceItems(tagsArr, checkTags) { dialog, which, isChecked ->
                        checkTags[which] = isChecked
                    }

                    //Logic to add tags to location
                    builder.setPositiveButton("Add") { dialog, which ->
                        var addingTagsArr = ArrayList<String>()
                        for (i in checkTags.indices) {
                            val checked = checkTags[i]
                            if (checked) {

                                addingTagsArr.add(storeTags[i])
                            }
                        }
                        var addingTagsArray = arrayOfNulls<String>(addingTagsArr.size)
                        addingTagsArr.toArray(addingTagsArray)


                        var builderS = StringBuilder()
                        for (i in addingTagsArray) {
                            if (i == addingTagsArray.last()) {
                                builderS.append("" + i)
                            } else {
                                builderS.append("" + i + ", ")
                            }
                        }
                        val newKey = database.child("locations").push().key.toString()
                        val currentFirebaseUser = FirebaseAuth.getInstance().currentUser
                        val name = view!!.findViewById<EditText>(R.id.et_loc_name).text.toString()
                        var desc = view!!.findViewById<EditText>(R.id.et_about).text.toString()

                        addTagtoLoc.set(
                            hashMapOf(
                                "timestamp" to com.google.firebase.Timestamp.now(),
                                "location_id" to newKey,
                                "description" to desc,
                                "name" to name,
                                "user_id" to currentFirebaseUser!!.uid,
                                "coordinates" to GeoPoint(
                                    arguments!!.getDouble("latitude"),
                                    arguments!!.getDouble("longitude")
                                )
                            )
                        )

                        //Ensuring duplicate tags do not get added
                        var flag1 = 0
                        for (i in addingTagsArray) {

                            addTagtoLoc.collection("tags").get().addOnSuccessListener { tags ->
                                for (t in tags.documents) {
                                    if (i == t["name"]){
                                        flag1 = 1
                                    }
                                }
                                if (flag1 == 0){
                                    val tagInputs = hashMapOf(
                                        "name" to i as String
                                    )
                                    addTagtoLoc.collection("tags").add(tagInputs)
                                }
                                flag1 = 0
                            }
                        }
                    }
                    val adialog = builder.create()
                    adialog.show()
                }
        }

        //Adding location to map
        btn_add_loc.setOnClickListener {
            val name = view!!.findViewById<EditText>(R.id.et_loc_name).text.toString()
            var desc = view!!.findViewById<EditText>(R.id.et_about).text.toString()
            val currentFirebaseUser = FirebaseAuth.getInstance().currentUser

            addTagtoLoc
                .set(hashMapOf("timestamp" to com.google.firebase.Timestamp.now(), "location_id" to newKey, "description" to desc, "name" to name, "user_id" to currentFirebaseUser!!.uid, "coordinates" to GeoPoint(arguments!!.getDouble("latitude"), arguments!!.getDouble("longitude"))))
            .addOnSuccessListener {
                Toast.makeText(context, "Location Added", Toast.LENGTH_SHORT).show()
                this.activity!!.supportFragmentManager.popBackStack()
            }.addOnFailureListener{
                Toast.makeText(context, "Sorry, could not add location.", Toast.LENGTH_SHORT).show()
                this.activity!!.supportFragmentManager.popBackStack()
            }
        }
//        val btn_back = getView()!!.findViewById(R.id.btn_add_location) as Button
//        btn_back.setOnClickListener {
//            this.activity!!.supportFragmentManager.popBackStack()
//        }



        locationShape.setImageBitmap(drawLocationShapeAsBitmap(arguments!!.getIntArray("xpoints")!!,
            arguments!!.getIntArray("ypoints")!!, 200, 200, arguments!!.getFloat("mapRotation")!!))

    }

    // Rotate a set of points by the given number of degrees, then translate and scale to take up
    // as much of a canvas of width by height as possible
    private fun normalizePoints(xPoints: IntArray, yPoints: IntArray, width: Int, height: Int, degrees: Float): Pair<List<Float>, List<Float>> {
        // Rotate points so that the location is drawn with up being north.
        // See https://stackoverflow.com/a/3162657/ for an explanation
        val radians = degrees * Math.PI / 180
        val rotatedXPoints = xPoints.zip(yPoints).map { point -> point.first * Math.cos(radians) - point.second * Math.sin(radians) }
        val rotatedYPoints = xPoints.zip(yPoints).map { point -> point.first * Math.sin(radians) + point.second * Math.cos(radians) }
        // Scale and translate input coordinates to take up as much of the canvas as possible
        val minX = rotatedXPoints.min()!!
        val xWidth = rotatedXPoints.max()!! - minX
        val minY = rotatedYPoints.min()!!
        val yHeight = rotatedYPoints.max()!! - minY
        val transformedXPoints = rotatedXPoints.map { xPoint -> (1.0F * (xPoint - minX) * width / xWidth).toFloat() }
        val transformedYPoints = rotatedYPoints.map { yPoint -> (1.0F * (yPoint - minY) * height / yHeight).toFloat() }
        return Pair(transformedXPoints, transformedYPoints)
    }

    private fun drawLocationShapeAsBitmap(xPoints: IntArray, yPoints: IntArray, width: Int, height: Int, degrees :Float): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeWidth = 3F; color = 0xFFFFFFFF.toInt() }

        val transformedPoints = normalizePoints(xPoints, yPoints, width, height, degrees)
        // drawLines() needs coordinates in a certain format to represent pairs of points
        // xPoints = [1F, 2F, 3F], yPoints = [4F, 5F, 6F] => [1F, 4F, 1F, 4F, 2F, 5F, 2F, 5F, 3F, 6F, 3F, 6F]
        val pointArray = transformedPoints.first.zip(transformedPoints.second)
            .map { pair -> listOf(pair.first, pair.second, pair.first, pair.second) }.flatten()
            .map { dbl -> dbl.toFloat() }.toFloatArray()
        // Omit duplicate first two and last two coordinates
        canvas.drawLines(pointArray, 2, pointArray.size - 4, paint)
        // Draw the line between the last point and the first point
        canvas.drawLines(floatArrayOf(pointArray[pointArray.size - 2], pointArray[pointArray.size - 1], pointArray[0], pointArray[1]),
            0, 4, paint)
        return bitmap
    }


}
