package com.cs407.team15.redstone.ui.ar

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.cs407.team15.redstone.R
import android.Manifest;
import android.app.ActivityManager
import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Session
import com.google.ar.core.examples.java.common.helpers.DisplayRotationHelper
import com.google.ar.core.examples.java.common.rendering.BackgroundRenderer
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import java.io.IOException
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

// A great deal of this code comes from following the example in the HelloSceneForm sample AR Core
// application provided by Google:
// https://github.com/google-ar/sceneform-android-sdk/blob/master/samples/hellosceneform/app/src/main/java/com/google/ar/sceneform/samples/hellosceneform/HelloSceneformActivity.java
class ARFragment : Fragment() {
    private val TAG: String = ARFragment::class.java.simpleName
    private val MIN_OPENGL_VERSION = 3.0

    private lateinit var arFragment: ArFragment // Google's ArFragment != this class

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