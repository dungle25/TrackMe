package com.dungle.trackme.model

import android.location.Location
import android.os.Parcelable
import com.dungle.trackme.data.session.local.model.LocalSession
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Session(
    var id: Int = 0,
    var locations: MutableList<LocationData> = arrayListOf(),
    var displayAvgSpeed: Double = 0.0, // in km/h
    var speeds: MutableList<Double> = arrayListOf(),
    var distance: Double = 0.0, // in km
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

@Parcelize
data class LocationData(
    var lat: Double = 0.0,
    var long: Double = 0.0,
    var time: Long = 0L,
    var speed: Double
) : Parcelable {
    fun toLatLng(): LatLng {
        return LatLng(lat, long)
    }

    fun distanceToInKm(target: LatLng): Double {
        val originLocation = Location("")
        originLocation.latitude = this.lat
        originLocation.longitude = this.long

        val targetLocation = Location("")
        targetLocation.latitude = target.latitude
        targetLocation.longitude = target.longitude
        return (originLocation.distanceTo(targetLocation) / 1000).toDouble()
    }
}
