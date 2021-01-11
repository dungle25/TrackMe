package com.dungle.getlocationsample.data.source

import com.dungle.getlocationsample.model.Session

interface SessionDataSource {
    suspend fun getAllSession(): List<Session>
    suspend fun getSessionCount(): Int
}