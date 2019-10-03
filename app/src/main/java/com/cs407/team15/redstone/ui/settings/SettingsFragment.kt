package com.cs407.team15.redstone.ui.settings

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.cs407.team15.redstone.R
import com.cs407.team15.redstone.ui.authentication.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : Fragment() {

    private lateinit var settingsViewModel: SettingsViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
        /*settingsViewModel = ViewModelProviders.of(this).get(SettingsViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_profile, container, false)
        val textView: TextView = root.findViewById(R.id.text_profile)

        settingsViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root*/
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val button = getView()!!.findViewById(R.id.delete_act_button) as Button

        button.setOnClickListener{
            val builder = AlertDialog.Builder(context)

            builder.setTitle("Account Deletion")
            builder.setMessage("Are you sure you want to delete your account?")

            builder.setPositiveButton("YES"){dialog, which ->
                FirebaseAuth.getInstance().signOut()
                FirebaseAuth.getInstance().currentUser?.delete()
                Toast.makeText(context,"Account deleted",Toast.LENGTH_SHORT).show()
                val intent = Intent(context, LoginActivity::class.java)
                startActivity(intent)
            }

            builder.setNegativeButton("NO"){dialog,which ->
                Toast.makeText(context,"Account not deleted",Toast.LENGTH_SHORT).show()
            }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }


        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val email = user.email.toString()
            val collectionref = FirebaseFirestore.getInstance().collection("users")
            val emailDoc = collectionref.document(email)

            var usernameRetrieved = ""
            val usernameText = getView()!!.findViewById<EditText>(R.id.username_text)
            emailDoc.get().addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot != null) {
                        usernameRetrieved = documentSnapshot!!.get("username").toString()
                        usernameText.setText(usernameRetrieved)
                    }else {
                        usernameText.setText("No current username")
                        Toast.makeText(context, "No information found", Toast.LENGTH_SHORT)
                    }
                }

            val updateButton = getView()!!.findViewById(R.id.update_button) as Button

            updateButton.setOnClickListener {
                val builder = AlertDialog.Builder(context)

                builder.setTitle("Update Profile")
                builder.setMessage("Are you sure you want to update your profile?")

                builder.setPositiveButton("YES") { dialog, which ->
                    val newUsername = usernameText.text.toString()
                    emailDoc.update("username", newUsername)
                    usernameText.setText(newUsername)
                    Toast.makeText(context, "Settings Updated", Toast.LENGTH_SHORT).show()
                }

                builder.setNegativeButton("NO") { dialog, which ->
                    Toast.makeText(context, "Settings Not Updated", Toast.LENGTH_SHORT).show()
                }

                val dialog: AlertDialog = builder.create()
                dialog.show()
            }
        }
    }
}