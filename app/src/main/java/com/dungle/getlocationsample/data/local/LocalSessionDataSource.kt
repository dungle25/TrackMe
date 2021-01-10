package com.dungle.getlocationsample.data.local

import com.dungle.getlocationsample.data.source.SessionDataSource
import com.dungle.getlocationsample.model.Session

class LocalSessionDataSource(private val database: AppDatabase) : SessionDataSource {
    override suspend fun getAllSession(): List<Session>{
        return database.sessionDao().getAllSession()
    }

    override suspend fun getSessionCount(): Int {
        return database.sessionDao().getSessionCount()
    }
}