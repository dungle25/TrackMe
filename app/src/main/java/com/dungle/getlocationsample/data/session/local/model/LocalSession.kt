package com.dungle.getlocationsample.data.session.local.model

import android.location.Location
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dungle.getlocationsample.model.Session

@Entity(tableName = "session_table")
data class LocalSession(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "locations") val locations: MutableList<Location> = arrayListOf(),
    @ColumnInfo(name = "currentSpeed") val displaySpeed: Double = 0.0,
    @ColumnInfo(name = "averageSpeed") val speeds: MutableList<Double> = arrayListOf(),
    @ColumnInfo(name = "distance") val distance: Double = 0.0,
    @ColumnInfo(name = "time") val totalTimeInMillis: String = ""
) {
    fun toSession(): Session {
        return Session(locations, displaySpeed, speeds, distance, totalTimeInMillis)
    }
}