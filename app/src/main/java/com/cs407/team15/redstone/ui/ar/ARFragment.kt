package com.cs407.team15.redstone.ui.ar

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.cs407.team15.redstone.R
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Pose
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment

// A great deal of this code comes from following the example in the HelloSceneForm sample AR Core
// application provided by Google:
// https://github.com/google-ar/sceneform-android-sdk/blob/master/samples/hellosceneform/app/src/main/java/com/google/ar/sceneform/samples/hellosceneform/HelloSceneformActivity.java
class ARFragment : Fragment() {
    private val TAG: String = ARFragment::class.java.simpleName
    private val MIN_OPENGL_VERSION = 3.0

    private lateinit var arFragment: ArFragment // Google's ArFragment != this class
    private lateinit var textViewTemplate: ViewRenderable

    private var currentPosition: Pose? = null // Camera's current position in space

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isThisDeviceSupported()) {
            return
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_ar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arFragment = childFragmentManager.findFragmentById(R.id.free_roam_ar_fragment) as ArFragment
        arFragment.arSceneView.scene.addOnUpdateListener { frameTime -> run {
            arFragment.onUpdate(frameTime)
            updateCameraPosition()
        }}

//        fixedRateTimer("timer", false, 0L, 1000) {
//            if (textViewTemplate != null) {
//                val anchor = hitResult.createAnchor()
//                val anchorNode = AnchorNode(anchor)
//                anchorNode.setParent(arFragment.arSceneView.scene)
//                val textViewNode = Node()
//                textViewNode.setParent(anchorNode)
//                textViewNode.localPosition = Vector3(0f, 1.0f, 0f)
//                textViewNode.renderable = textViewTemplate

//            }
//        }

        arFragment.setOnTapArPlaneListener {
                hitResult: HitResult, plane: Plane, motionEvent: MotionEvent ->
            run {
            }
        }
        ViewRenderable.builder().setView(context!!, R.layout.basic_tour_text_view).build()
            .thenAccept { renderable -> textViewTemplate = renderable }
    }

    fun updateCameraPosition() {
        val camera = arFragment.arSceneView.arFrame?.camera
        currentPosition = if (camera?.trackingState == TrackingState.TRACKING) camera?.displayOrientedPose else null
    }

    fun isThisDeviceSupported(): Boolean {
        val openGlVersion = (activity!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
            .deviceConfigurationInfo.glEsVersion.toDouble()
        if (openGlVersion < MIN_OPENGL_VERSION) {
            Toast.makeText(activity!!, getString(R.string.opengl_not_supported), Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }
}