package com.dungle.getlocationsample.model

import android.location.Location
import androidx.room.Entity

open class Session(
//    private val locationMap: Map<Int, List<Location>> = emptyMap(), // Mỗi lượt chạy có thể pause & resume, nên xài map -> khi pause & resume sẽ tăng key lên 1
    private val locations : List<Location> =  listOf(),
    private val currentSpeed: Double = 0.0,
    private val avgSpeed: List<Double> = listOf(),
    private val distance: Double = 0.0,
    private val totalTimeInMillis: Long = 0L
)
