package com.dungle.getlocationsample.model

import android.location.Location
import android.os.Parcelable
import com.dungle.getlocationsample.data.session.local.model.LocalSession
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Session(
    var id: Int = 0,
    var locations: MutableList<Location> = arrayListOf(),
    var displayAvgSpeed: Double = 0.0,
    var speeds: MutableList<Double> = arrayListOf(),
    var distance: Double = 0.0,
    var displayDuration: String = "00:00:00",
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
