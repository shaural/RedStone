package com.cs407.team15.redstone.model

import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.FileReader
import kotlin.reflect.KClass


data class Location(val coordinates: GeoPoint = GeoPoint(0.0,0.0),
                    val description: String = "", val name: String = "", val user_id: String = "") {

    companion object {
        const val LOCATIONS = "locations"
        const val NAME = "name"
        const val USERS = "users"
        const val NOTICES = "notices"
        const val MESSAGE = "message"
        const val IS_DISMISSED = "is_dismissed"
        const val FLAGGING_USERS = "flagging_users"
        const val USER_ID = "user_id"
        const val LOCATION_ID = "location_id"
        // A location gets automatically deleted if it gets flagged and the proportion of the
        // user base is at least PROPORTIONAL_FLAG_THRESHOLD *or* the total number of flags is
        // at least ABSOLUTE_FLAG_THRESHOLD
        const val PROPORTIONAL_FLAG_THRESHOLD = "configurationAndMetaData/proportionalLocationFlagThreshold"
        const val ABSOLUTE_FLAG_THRESHOLD = "configurationAndMetaData/absoluteLocationFlagThreshold"

        // Determine if a particular user has flagged a particular location, defaulting to false
        // in case of an error
        suspend fun hasUserFlaggedLocation(user_id: String, location_id: String): Boolean {
            return try {
                FirebaseFirestore.getInstance().collection(LOCATIONS).document(location_id).
                    collection(FLAGGING_USERS).whereEqualTo(USER_ID, user_id).get().await().
                    documents.size > 0
            }
            catch (e: FirebaseException) {
                false
            }
        }

        // Determine if the currently logged in user has flagged a particular location, defaulting
        // to false in case of an error but failing if no user is logged in
        suspend fun hasUserFlaggedLocation(location_id: String): Boolean {
            return hasUserFlaggedLocation(FirebaseAuth.getInstance().currentUser!!.email as String, location_id)
        }

        // If a particular user has flagged a particular location, attempts to remove the flag,
        // otherwise record a flag by that user for that location. If a flag is added and that
        // flag causes the location to meet the threshold for removal due to flags, delete the
        // location and notify the location's creator that the location has been removed.
        // Returns true if no error occurred
        suspend fun toggleHasUserFlaggedLocation(user_id: String, location_id: String): Boolean {
            return try {
                val db = FirebaseFirestore.getInstance()
                val location = db.collection(LOCATIONS).document(location_id).get().await()
                // Remove flag if the user has flagged the location
                if (hasUserFlaggedLocation(user_id, location_id)) {
                    val userFlags = location.reference.collection(FLAGGING_USERS)
                        .whereEqualTo(USER_ID, user_id).get().await().documents
                    userFlags.first().reference.delete() // There should always be exactly one flag here
                }
                // Otherwise add a flag by the user
                else {
                    location.reference.collection(FLAGGING_USERS).add(hashMapOf(USER_ID to user_id)).await()
                    // Delete the location and notify location creator if it has been flagged too much
                    if (hasLocationMetFlagThreshold(location_id)) {
                        // Notify the user who created the location that the location has been
                        // removed due to flags
                        val locationCreator = location.get(USER_ID) as String
                        val locationName = location.get(NAME) as String
                        val message = "$locationName has been removed due to receiving too many flags."
                      
                        val notice = hashMapOf(MESSAGE to message, IS_DISMISSED to false)
                        Notices.submitNotice("System", "Location Removed", message, locationCreator)

                        // Delete all of the location's flags and then the location itself
                        val locationFlags = location.reference.collection(FLAGGING_USERS).get().await().documents
                        for (locationFlag in locationFlags) {
                            locationFlag.reference.delete().await()
                        }
                        db.collection(LOCATIONS).document(location_id).delete().await()
                    }
                }
                true
            }
            catch (e: FirebaseException) {
                false
            }
        }

        // Convenience wrapper that toggles whether the currently-logged-in user has flagged the
        // specified location
        suspend fun toggleHasUserFlaggedLocation(location_id: String): Boolean {
            return toggleHasUserFlaggedLocation(FirebaseAuth.getInstance().currentUser!!.email as String, location_id)
        }

        // Get the number of flags for a location, defaulting to -1 in case of an error
        suspend fun getNumberOfFlagsForLocation(location_id: String): Int {
            return try {
                FirebaseFirestore.getInstance().collection(LOCATIONS).document(location_id)
                    .collection(FLAGGING_USERS).get().await().documents.size
            }
            catch (e: FirebaseException) {
                -1
            }
        }

        // Determine if a location should be removed due to the flags it has, defaulting to false
        // in case of an error
        suspend fun hasLocationMetFlagThreshold(location_id: String): Boolean {
            return try {
                val db = FirebaseFirestore.getInstance()
                val userCount = db.collection(USERS).get().await().size()
                val numberOfFlags = getNumberOfFlagsForLocation(location_id)
                val proportionalFlagThreshold = db.document(PROPORTIONAL_FLAG_THRESHOLD).get()
                    .await().get("value") as Double
                val absoluteFlagThreshold = db.document(ABSOLUTE_FLAG_THRESHOLD).get()
                    .await().get("value") as Long
                // Ensure fetching number of flags succeeded
                if (numberOfFlags > 0) {
                    (1.0 * numberOfFlags / userCount >= proportionalFlagThreshold ||
                            numberOfFlags >= absoluteFlagThreshold)
                }
                else {
                    false
                }
            }
            catch (e: FirebaseException) {
                false
            }
        }

        // Get all tours, sorted alphabetically by name
        suspend fun getAllLocations(): List<Location> {
            // Get all tour documents
            return FirebaseFirestore.getInstance().collection(LOCATIONS).get().await().documents
                // Build a Location from each location document
                .map { locationDocument -> locationDocument.toObject(Location::class.java) as Location }
                // Sort alphabetically by tour name, ignoring case
                .sortedBy { location -> location.name.toUpperCase() }
        }
    }
}