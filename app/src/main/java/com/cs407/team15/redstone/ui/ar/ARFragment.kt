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
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Session
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

// A great deal of this code comes from following the example in the HelloAR sample AR Core
// application provided by Google
class ARFragment : Fragment(), GLSurfaceView.Renderer {

    private lateinit var aRViewModel: ARViewModel
    private var session: Session? = null
    private var firstTimeCheckingIfArCoreInstalled = true
    private lateinit var surfaceView: GLSurfaceView
    private val backgroundRenderer = BackgroundRenderer

    val CAMERA_REQUEST = 0


    override fun onResume() {
        super.onResume()

        // Ensure we have camera access
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST);
        }

        try {
            // If we don't have an AR session started, ask the user to install Google Play Services
            // for AR if they haven't or create an AR session if they have
            if (session == null) {
                val installStatus = ArCoreApk.getInstance()
                    .requestInstall(activity!!, firstTimeCheckingIfArCoreInstalled)
                // If Google Play Services for AR is installed, start a session
                if (installStatus == ArCoreApk.InstallStatus.INSTALLED) {
                    session = Session(context!!)
                }
                else {
                    // User will be prompted to install it, which pauses our app. Once the user
                    // installs it, app is resumed which causes this method to be called again
                    firstTimeCheckingIfArCoreInstalled = false
                    return
                }
            }
        }
        // Documentation is unclear. This case happens when the user has previously declined
        // installation, but I am not sure if this is reached if the user had later installed it
        // or not. I am assuming it is only reached if the user did not later install it
        catch (e: UnavailableUserDeclinedInstallationException) {
            Toast.makeText(context!!, getString(R.string.gpsfar_request), Toast.LENGTH_LONG).show()
            return
        }

        try {
            session?.resume()
        }
        catch (e: CameraNotAvailableException) {
            Toast.makeText(context!!, getString(R.string.camera_missing), Toast.LENGTH_LONG)
            session = null;
            return;
        }
        surfaceView.onResume()
    }

    override fun onPause() {
        super.onPause()
        if (session != null) {
            surfaceView.onPause()
            session?.pause()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // We can't do anything without camera permissions, so if the user selected anything other
        // than "allow", beg them to enable permission in settings
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context!!, getString(R.string.camera_request), Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        surfaceView = view!!.findViewById(R.id.surfaceview)
        // All of these values are the ones used in the HelloAR sample
        surfaceView.preserveEGLContextOnPause = true
        surfaceView.setEGLContextClientVersion(2)
        surfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        surfaceView.setRenderer(this)
        surfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        surfaceView.setWillNotDraw(false)

        firstTimeCheckingIfArCoreInstalled = true
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f) // Values from HelloAR example

        try {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_ar, container, false)
        return root
    }
}