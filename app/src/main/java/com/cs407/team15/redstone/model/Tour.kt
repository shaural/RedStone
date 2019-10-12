package com.cs407.team15.redstone.model

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.coroutines.tasks.await

class Tour(val name: String, val type: String, val user_id: String, val locations: List<String>, val tags: List<String>) {
    companion object {
        const val TOURS = "tours"
        const val NAME = "name"
        const val TYPE = "type"
        const val USER_ID = "user_id"
        const val LOCATIONS = "locations"
        const val TAGS = "tags"
        const val POSITION = "position"
        const val TAG_ID = "tag_id"
        const val LOCATION_ID = "location_id"

        // Get the single tour with the specified name, or null if no such tour with that name
        // exists
        suspend fun getTourByName(name: String): Tour? {
            val tourDocument = FirebaseFirestore.getInstance().collection(TOURS).whereEqualTo(NAME, name).get().await().first()
            if (!tourDocument.exists()) {
                return null
            }
            val name = tourDocument.getString(NAME) as String
            val type = tourDocument.getString(TYPE) as String
            val user_id = tourDocument.getString(USER_ID) as String
            // Location ids for a tour are stored as pairs of location id and position.
            // Fetch the documents, sort by the position, then pull out just the location ids
            val locationDocuments = FirebaseFirestore.getInstance().collection(TOURS).document(name)
                .collection(LOCATIONS).get().await().documents
            val locationPairs = locationDocuments.map {locationDocument ->
                Pair(locationDocument.getLong(POSITION), locationDocument.getString(LOCATION_ID) as String) }
            val locations = locationPairs.sortedBy { locationPair -> locationPair.first }
                .map { locationPair -> locationPair.second }
            // Fetch the tag ids for the tour and sort for consistency
            val tagDocuments = FirebaseFirestore.getInstance().collection(TOURS).document(name)
                .collection(TAGS).get().await().documents
            val tags = tagDocuments.map {tagDocument -> tagDocument.getString(TAG_ID) as String}
                .sortedBy {tag_id -> tag_id.toUpperCase()}
            return Tour(name, type, user_id, locations, tags)
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
    }
}