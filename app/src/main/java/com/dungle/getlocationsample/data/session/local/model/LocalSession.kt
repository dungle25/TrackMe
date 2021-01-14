package com.dungle.getlocationsample.data.session.local.model

import android.location.Location
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dungle.getlocationsample.model.LocationData
import com.dungle.getlocationsample.model.Session

@Entity(tableName = "session_table")
data class LocalSession(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "locations") val locations: MutableList<LocationData> = arrayListOf(),
    @ColumnInfo(name = "averageSpeed") val speeds: MutableList<Double> = arrayListOf(),
    @ColumnInfo(name = "distance") val distance: Double = 0.0,
    @ColumnInfo(name = "time") val displayDuration: String = "00:00:00",
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) var mapSnapshot: ByteArray? = null

) {
    fun toSession(): Session {
        return Session(
            locations = locations,
            speeds = speeds,
            distance = distance,
            displayDuration = displayDuration,
            mapSnapshot = mapSnapshot
        )
    }
}