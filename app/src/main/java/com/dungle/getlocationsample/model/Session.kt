package com.dungle.getlocationsample.model

import android.location.Location
import androidx.room.Entity
import com.dungle.getlocationsample.data.session.local.model.LocalSession

data class Session(
    var locations: MutableList<Location> = arrayListOf(),
    private val currentSpeed: Double = 0.0,
    private val avgSpeed: List<Double> = listOf(),
    private val distance: Double = 0.0,
    private val totalTimeInMillis: Long = 0L
) {
    fun toLocalSession(id: Int): LocalSession {
        return LocalSession(
            id = id,
            locations = locations,
            currentSpeed = currentSpeed,
            avgSpeed = avgSpeed,
            distance = distance,
            totalTimeInMillis = totalTimeInMillis
        )
    }
}
