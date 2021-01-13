package com.dungle.getlocationsample.model

import android.location.Location
import android.os.Parcelable
import androidx.room.Relation
import com.dungle.getlocationsample.data.session.local.model.LocalSession
import com.google.gson.Gson
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Session(
    @Relation(parentColumn = "locations", entityColumn = "locations")  var locations: MutableList<Location> = arrayListOf(),
    var displayAvgSpeed: Double = 0.0,
    @Relation(parentColumn = "speeds", entityColumn = "speeds")  var speeds: MutableList<Double> = arrayListOf(),
    @Relation(parentColumn = "distance", entityColumn = "distance") var distance: Double = 0.0,
    @Relation(parentColumn = "displayDuration", entityColumn = "displayDuration")  var displayDuration: String = "00:00:00",
    var duration: Long = 0L,
    var startTime: Long = 0L,
    var endTime: Long = 0L,
    var mapSnapshot: ByteArray? = null
) : Parcelable {
    fun toLocalSession(): LocalSession {
        return LocalSession(
            locations = locations,
            speeds = speeds,
            distance = distance,
            displayDuration = displayDuration,
            mapSnapshot = mapSnapshot
        )
    }
}
