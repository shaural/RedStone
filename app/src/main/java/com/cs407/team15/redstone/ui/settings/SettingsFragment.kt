package com.cs407.team15.redstone.ui.settings

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.cs407.team15.redstone.R
import com.cs407.team15.redstone.ui.authentication.LoginActivity
import com.google.firebase.auth.FirebaseAuth

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
                // Question
                // Isn't this fragment still alive after the intent?
                // ex) onBackPressed it goes back to Main Activity after delete and sign out?
            }

            builder.setNegativeButton("No"){dialog,which ->
                Toast.makeText(context,"Account not deleted",Toast.LENGTH_SHORT).show()
            }

            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }

}