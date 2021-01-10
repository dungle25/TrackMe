package com.dungle.getlocationsample.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dungle.getlocationsample.model.Session

@Entity(tableName = "session_table")
data class LocalSession(
    @PrimaryKey
    @ColumnInfo(name = "id") private val id: Int = 0
) : Session()