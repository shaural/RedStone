package com.cs407.team15.redstone

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.cs407.team15.redstone.ui.authentication.LoginActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import android.widget.TextView
import com.cs407.team15.redstone.model.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.google.firebase.auth.FirebaseUser




class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var nav_user: TextView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var nav_userEmail: TextView
    private var TAG:String = "CCC"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)


        val hView = navView.getHeaderView(0)

        nav_user = hView.findViewById(R.id.nav_profile_username)
        nav_userEmail = hView.findViewById(R.id.nav_profile_useremail)
        getUserInfo()

        val navController = findNavController(R.id.nav_host_fragment)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        // Add menu ID here if you need
        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.nav_home, R.id.nav_aboutpurdue, R.id.nav_locations, R.id.nav_tour,
            R.id.nav_free_roam, R.id.nav_profile), drawerLayout)

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.action_signout -> {
                signOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * To set username
     */
    private fun getUserInfo() {
        var publisherid: String = FirebaseAuth.getInstance().currentUser!!.email.toString()

        if (publisherid != null) {
            Log.e(TAG, "User: $publisherid")
            val fsdb = FirebaseFirestore.getInstance()
            val docRef = fsdb.collection("users").document(publisherid)

            docRef.get().addOnCompleteListener(OnCompleteListener<DocumentSnapshot> { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    if (document!!.exists()) {
                        val me = document.toObject(User::class.java)
                        nav_user.text = me!!.getUsername()
                        nav_userEmail.text = me!!.getEmail()
                    } else {
                        nav_user.text = "no username"
                        nav_userEmail.text = "no Email"
                        Log.e(TAG, "No User document")
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.exception)
                }
            })
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (this.supportFragmentManager.fragments.size > 2) {
            val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                super.onBackPressed()
            }
        }
    }



    // Sign out Action
    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        Log.e(TAG, "Main Activity - User Signed out")
        intent = Intent(applicationContext, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}