package com.cs407.team15.redstone.model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.coroutines.tasks.await

class Tour(val name: String, val type: String, val user_id: String, val hammer: Boolean, val locations: List<String>, val tags: List<String>, val votes: Number) {
    companion object {
        const val TOURS = "tours"
        const val NAME = "name"
        const val TYPE = "type"
        const val USER_ID = "user_id"
        const val HAMMER = "hammer"
        const val LOCATIONS = "locations"
        const val TAGS = "tags"
        const val POSITION = "position"
        const val TAG_ID = "tag_id"
        const val LOCATION_ID = "location_id"
        const val VOTES = "votes"

        // Get the single tour with the specified name, or null if no such tour with that name
        // exists
        suspend fun getTourByName(name: String): Tour? {
            val db = FirebaseFirestore.getInstance()
            val tourDocument = db.collection(TOURS).whereEqualTo(NAME, name).get().await().first()
            if (!tourDocument.exists()) {
                return null
            }
            val tour_id = tourDocument.id
            val type = tourDocument.getString(TYPE) as String
            val user_id = tourDocument.getString(USER_ID) as String
            val hammer = tourDocument.getBoolean(HAMMER) as Boolean
            val votes = tourDocument.getDouble(VOTES) as Number
            // Location ids for a tour are stored as pairs of location id and position.
            // Fetch the documents, sort by the position, then pull out just the location ids
            /*val locationDocuments = db.collection(TOURS).document(tour_id)
                .collection(LOCATIONS).get().await().documents
            val locationPairs = locationDocuments.map {locationDocument ->
                Pair(locationDocument.getLong(POSITION), locationDocument.getString(LOCATION_ID) as String) }
            val locations = locationPairs.sortedBy { locationPair -> locationPair.first }
                .map { locationPair -> locationPair.second }*/
            val locations = (db.collection(TOURS).document(tour_id).get().await().get(LOCATIONS) as List<String>).sorted()
            // Fetch the tag names
            val tags = (db.collection(TOURS).document(tour_id).get().await().get(TAGS) as List<String>).sorted()
            return Tour(name, type, user_id, hammer, locations, tags, votes)
        }

        // Returns whether or not a particular user is allowed to see a particular tour
        fun canUserViewTour(tour: Tour, user_id: String): Boolean {
            /* TODO: Perform meaningful filtering once categories of tours and way of storing users who can
             * see the particular tour are finalized
             */
            return true
        }

        // Assumes that a user is logged in and returns whether the current user is allowed to see
        // a particular tour
        fun canCurrentUserViewTour(tour: Tour): Boolean {
            return canUserViewTour(tour, FirebaseAuth.getInstance().currentUser!!.email as String)
        }

        // Get all tours, sorted alphabetically by name
        suspend fun getAllTours(): List<Tour> {
            // Get all tour documents
            return FirebaseFirestore.getInstance().collection(TOURS).get().await().documents
                // Build a Tour for each tour document
                .map {tourDocument -> getTourByName(tourDocument.getString(NAME) as String) as Tour}
                // Sort alphabetically by tour name, ignoring case
                .sortedBy { tour -> tour.name.toUpperCase() }
        }

        // Get all ID for tours

    }
}