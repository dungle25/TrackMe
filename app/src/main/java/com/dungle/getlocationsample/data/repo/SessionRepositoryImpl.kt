package com.dungle.getlocationsample.data.repo

import com.dungle.getlocationsample.data.source.SessionDataSource
import com.dungle.getlocationsample.model.Session

class SessionRepositoryImpl(
    private val localSessionDataSource: SessionDataSource
) : SessionRepository {
    override suspend fun getSessionCount(): Int {
        return localSessionDataSource.getSessionCount()
    }

    override suspend fun getAllSession(): List<Session> {
        return localSessionDataSource.getAllSession()
    }
}