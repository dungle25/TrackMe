package com.dungle.getlocationsample.data.local.model

import android.location.Location
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dungle.getlocationsample.model.Session

@Entity(tableName = "session_table")
data class LocalSession(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "locations") val locations: List<Location> = listOf(),
    @ColumnInfo(name = "currentSpeed") val currentSpeed: Double = 0.0,
    @ColumnInfo(name = "averageSpeed") val avgSpeed: List<Double> = listOf(),
    @ColumnInfo(name = "distance") val distance: Double = 0.0,
    @ColumnInfo(name = "time") val totalTimeInMillis: Long = 0L
) {
    fun toSession() : Session {
        return Session(locations, currentSpeed, avgSpeed, distance, totalTimeInMillis)
    }
}