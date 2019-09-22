package com.cs407.team15.redstone.model

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import java.io.FileReader

class Location(val coordinates: GeoPoint, val description: String, val name: String,
               val user_id: String, val timestamp: ServerTimestamp) {

    companion object {
        const val LOCATIONS = "locations"
        const val FLAGGING_USERS = "flagging_users"
        const val USER_ID = "user_id"

        // Determine if a particular user has flagged a particular location, defaulting to false
        // in case of an error
        fun hasUserFlaggedLocation(user_id: String, location_id: String): Boolean {
            var hasUserFlaggedLocation = false;
            FirebaseFirestore.getInstance().collection(LOCATIONS).document(location_id).
                collection(FLAGGING_USERS).whereEqualTo(USER_ID, user_id).get()
                .addOnSuccessListener { documents ->
                    hasUserFlaggedLocation = documents.size() > 0
                }
                .addOnFailureListener { exception ->
                    Log.w(LOCATIONS, "Error determining if ${user_id} has flagged ${location_id}. ${exception.message}")
                }
            return hasUserFlaggedLocation
        }

        // Determine if the currently logged in user has flagged a particular location, defaulting
        // to false in case of an error but failing if no user is logged in
        fun hasUserFlaggedLocation(location_id: String): Boolean {
            return Companion.hasUserFlaggedLocation(FirebaseAuth.getInstance().currentUser!!.uid)
        }

        // If a particular user has flagged a particular location, attempts to remove the flag,
        // otherwise record a flag by that user for that location
        fun toggleHasUserFlaggedLocation(user_id: String, location_id: String) {
            var hasUserFlaggedLocation = Companion.hasUserFlaggedLocation(user_id, location_id)
            if (hasUserFlaggedLocation) {
                FirebaseFirestore.getInstance().collection(LOCATIONS).document(location_id).
                    collection(FLAGGING_USERS).whereEqualTo(USER_ID, user_id).get()
                    .addOnSuccessListener { documents ->
                        // A user can only flag a location once, so there will always be 1 document
                        for (document in documents.documents) {
                            document.reference.delete()
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w(LOCATIONS, "Error when removing ${user_id}'s flag for ${location_id}. ${exception.message}")
                    }
            }
            else {
                FirebaseFirestore.getInstance().collection(LOCATIONS).document(location_id).
                    collection(FLAGGING_USERS).add(hashMapOf(USER_ID to user_id))
                    .addOnFailureListener { exception ->
                        Log.w(LOCATIONS, "Error when adding ${user_id}'s flag for ${location_id}. ${exception.message}")
                    }
            }
        }

        // If the current user has flagged a particular location, attempts to remove the flag,
        // otherwise record a flag by the current user for that location
        // Fails if there is no user logged in
        fun toggleHasUserFlaggedLocation(location_id: String) {
            Companion.toggleHasUserFlaggedLocation(FirebaseAuth.getInstance().currentUser!!.uid, location_id)
        }
    }
}