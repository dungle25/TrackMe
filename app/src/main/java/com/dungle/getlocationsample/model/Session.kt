package com.dungle.getlocationsample.model

import android.location.Location
import android.os.Parcelable
import com.dungle.getlocationsample.data.session.local.model.LocalSession
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Session(
    var locations: MutableList<Location> = arrayListOf(),
    var displayAvgSpeed: Double = 0.0,
    var speeds: MutableList<Double> = arrayListOf(),
    var distance: Double = 0.0,
    var totalTimeInMillis: String = "",
    var startTime: Long = 0L,
    var endTime: Long = 0L
) : Parcelable {
    fun toLocalSession(id: Int): LocalSession {
        return LocalSession(
            id = id,
            locations = locations,
            displaySpeed = displayAvgSpeed,
            speeds = speeds,
            distance = distance,
            totalTimeInMillis = totalTimeInMillis
        )
    }
}
