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
class LocationTest {
    @Test
    // Sanity test of test helper functions
    fun location_deletion() = runBlocking<Unit> {
        removeLocation("test_location")
        assert(!doesLocationExist("test_location"))
        makeLocationFlagThresholdsUnreachable()

        addLocation("test_location")
        Location.toggleHasUserFlaggedLocation("test_user", "test_location")
        removeLocation("test_location")

        assert(!doesLocationExist("test_location"))
    }

    @Test
    // Test adding and removing a flag
    fun flag_addition_and_removal() = runBlocking {
        removeLocation("test_location")
        assert(!doesLocationExist("test_location"))
        makeLocationFlagThresholdsUnreachable()

        addLocation("test_location")
        assert(!Location.hasUserFlaggedLocation("test_user", "test_location"))
        Location.toggleHasUserFlaggedLocation("test_user", "test_location")
        assert(Location.hasUserFlaggedLocation("test_user", "test_location"))
        assert(Location.getNumberOfFlagsForLocation("test_location") == 1)
        Location.toggleHasUserFlaggedLocation("test_user", "test_location")
        assert(Location.getNumberOfFlagsForLocation("test_location") == 0)
        assert(!Location.hasUserFlaggedLocation("test_user", "test_location"))

        removeLocation("test_location")
        assert(!doesLocationExist("test_location"))
    }

    @Test
    // Test that location gets removed when hitting the absolute flag threshold
    fun hit_absolute_flag_threshold() = runBlocking {
        removeLocation("test_location")
        assert(!doesLocationExist("test_location"))

        addLocation("test_location", "test_location", "daniel.j.ostrowski@gmail.com")
        setLocationFlagThresholds(5, Double.MAX_VALUE)
        Location.toggleHasUserFlaggedLocation("test_user_1", "test_location")
        Location.toggleHasUserFlaggedLocation("test_user_2", "test_location")
        Location.toggleHasUserFlaggedLocation("test_user_3", "test_location")
        Location.toggleHasUserFlaggedLocation("test_user_4", "test_location")
        assert(doesLocationExist("test_location"))
        assertEquals(4, Location.getNumberOfFlagsForLocation("test_location"))
        Location.toggleHasUserFlaggedLocation("test_user_5", "test_location")
        assert(!doesLocationExist("test_location"))
    }

    @Test
    fun hit_proportional_flag_threshold() = runBlocking {
        removeLocation("test_location")
        assert(!doesLocationExist("test_location"))

        addLocation("test_location")
        setLocationFlagThresholds(Int.MAX_VALUE, 0.5)
        val numberOfUsers = FirebaseFirestore.getInstance().collection("users").get().await().size()

        var numberOfFlagsNeededToTrigger = kotlin.math.ceil(0.5 * numberOfUsers).toInt()
        for (i in 1 until numberOfFlagsNeededToTrigger) {
            Location.toggleHasUserFlaggedLocation("test_user_$i", "test_location")
        }
        assertEquals(numberOfFlagsNeededToTrigger - 1, Location.getNumberOfFlagsForLocation("test_location"))
        Location.toggleHasUserFlaggedLocation("test_user_$numberOfFlagsNeededToTrigger", "test_location")
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
