package com.cs407.team15.redstone.ui.tour

import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

class AddLocationViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    data class Location(
        var coordinates: GeoPoint,
        var name: String,
        var description: String,
        var timestamp: Timestamp,
        var user_id: String
    )
}
