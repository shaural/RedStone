package com.cs407.team15.redstone

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cs407.team15.redstone.model.Location
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    // Sanity test of test helper functions
    fun location_deletion() = runBlocking<Unit> {
        removeLocation("test_location")
        assert(!doesLocationExist("test_location"))
        makeLocationFlagThresholdsUnreachable()

        addLocation("test_location")
        Location.toggleHasUserFlaggedLocation("test_location", "test_user")
        removeLocation("test_location")

        assert(!doesLocationExist("test_location"))
    }

    suspend fun setLocationFlagThresholds(absoluteLocationFlagThreshold: Int, proportionalLocationFlagThreshold: Double) {
        val db = FirebaseFirestore.getInstance()
        db.document("configurationAndMetaData/absoluteLocationFlagThreshold")
            .set(hashMapOf("value" to absoluteLocationFlagThreshold)).await()
        db.document("configurationAndMetaData/proportionalLocationFlagThreshold")
            .set(hashMapOf("value" to proportionalLocationFlagThreshold)).await()
    }

    suspend fun addLocation(location_id: String, name: String = "test_location",
                            user_id: String = "test_user_id") {
        FirebaseFirestore.getInstance().collection("locations").document(location_id)
            .set(hashMapOf("name" to name, "user_id" to user_id)).await()
    }

    suspend fun removeLocation(location_id: String) {
        val location = FirebaseFirestore.getInstance().collection("locations")
            .document(location_id).get().await()
        val flags = location.reference.collection("flagging_users").get().await()
        for (flag in flags) {
            flag.reference.delete().await()
        }
        location.reference.delete().await()
    }

    suspend fun doesLocationExist(location_id: String): Boolean {
        return FirebaseFirestore.getInstance().collection("locations").document(location_id)
            .get().await().exists()
    }

    // Useful so we don't have to worry about unintentionally hitting the flag threshold when testing
    suspend fun makeLocationFlagThresholdsUnreachable() {
        setLocationFlagThresholds(Int.MAX_VALUE, Double.MAX_VALUE)
    }
}
