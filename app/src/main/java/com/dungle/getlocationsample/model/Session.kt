package com.dungle.getlocationsample.model

import android.location.Location
import androidx.room.Entity

open class Session(
    private val locationMap: Map<Int, List<Location>> = emptyMap(),
    private val currentSpeed: Double = 0.0,
    private val avgSpeed: List<Double> = emptyList(),
    private val distance: Double = 0.0,
    private val totalTimeInMillis: Long = 0L
)
